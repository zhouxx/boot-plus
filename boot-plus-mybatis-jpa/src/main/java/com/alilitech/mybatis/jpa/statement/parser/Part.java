/*
 * Copyright 2008-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alilitech.mybatis.jpa.statement.parser;

import com.alilitech.mybatis.jpa.LikeContainer;
import com.alilitech.mybatis.jpa.anotation.IfTest;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.pagination.PrePaginationInterceptor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A single part of a method name that has to be transformed into a query part. The actual transformation is defined by
 * a {@link Type} that is determined from inspecting the given part. The query part can then be looked up via
 * {@link #getProperty()}.
 *
 * @author Oliver Gierke
 * @author Martin Baumgartner
 * @author ZhouXiaoxiang
 */
public class Part implements Render {

	private static final Pattern IGNORE_CASE = Pattern.compile("Ignor(ing|e)Case");

	private final PropertyPath propertyPath;
	private final Part.Type type;

	private TestCondition testCondition;

	private boolean oneParameter;

	private LikeType likeType;

	private int argumentIndex;

	private Class<?> argumentType;

	/**
	 * Creates a new {@link Part} from the given method name part, the {@link Class} the part originates from and the
	 * start parameter argumentIndex.
	 * @param source must not be {@literal null}.
	 * @param clazz domain Class
	 * @param argumentIndex
	 */
	public Part(String source, Optional<Class> clazz, MethodDefinition methodDefinition, AtomicInteger argumentIndex) {

		Assert.hasText(source, "Part source must not be null or empty!");

		String partToUse = detectAndSetIgnoreCase(source);
		this.type = Type.fromProperty(partToUse);
		this.propertyPath = PropertyPath.from(type.extractProperty(partToUse), clazz, methodDefinition);
		this.likeType = type.likeType;
		this.oneParameter = methodDefinition.isOneParameter();

		//没有参数直接忽略
		if(this.getNumberOfArguments() <= 0) {
			return;
		}

		//跳过Page 和 Sort
		if(methodDefinition.getParameterDefinitions().get(argumentIndex.get()).isPage()) {
			argumentIndex.incrementAndGet();
		}
		if(methodDefinition.getParameterDefinitions().get(argumentIndex.get()).isSort()) {
			argumentIndex.incrementAndGet();
		}

		//设置当前参数索引
		this.argumentIndex = argumentIndex.get();
		//设置参数类型
		argumentType = methodDefinition.getParameterDefinitions().get(argumentIndex.get()).getParameterClass();

		//拿到testCondition
		this.testCondition = getCondition(this.argumentIndex, methodDefinition);

		//将参数索引加上参数数量
		argumentIndex.addAndGet(this.getNumberOfArguments());

		// 将like的信息放入缓存中，后面需要改变其参数
		if(this.getLikeType() != null) {
			String key = methodDefinition.getStatementId();
			String countKey = key + PrePaginationInterceptor.STATEMENT_ID_POSTFIX;

			if(this.oneParameter) {
				countKey += "._parameter";
				key += "._parameter";
			} else {
				countKey += (".arg" + this.getArgumentIndex());
				key += (".arg" + this.getArgumentIndex());
			}
			LikeContainer.getInstance().put(key, this.getLikeType());

			// 如果有分页的话需要存储分页相关的key
			if(methodDefinition.hasPage()) {
				LikeContainer.getInstance().put(countKey, this.getLikeType());
			}

		}
	}

	//根据参数索引获得IfCondition
	private TestCondition getCondition(int index, MethodDefinition methodDefinition) {
		//先读取方法的IfTest
		boolean methodIfTest = methodDefinition.isMethodIfTest();
		List<Annotation> annotationList = methodDefinition.getParameterDefinitions().get(index).getAnnotations();
		if(annotationList.size() <= 0 && !methodIfTest) {
			return null;
		}

		TestCondition testCondition = null;

		for(Annotation annotation : annotationList) {
			//若参数有注解，则以参数注解为准
			if(annotation instanceof IfTest) {
				IfTest ifTest = (IfTest) annotation;
				testCondition = new TestCondition(ifTest.notNull(), ifTest.notEmpty(), ifTest.conditions());
				return testCondition;
			}
		}
		//否则默认方法的注解
		if(methodIfTest) {
			IfTest ifTest = methodDefinition.getIfTest();
			testCondition = new TestCondition(ifTest.notNull(), ifTest.notEmpty(), ifTest.conditions());
		}

		return testCondition;
	}

	private String detectAndSetIgnoreCase(String part) {

		Matcher matcher = IGNORE_CASE.matcher(part);
		String result = part;

		if (matcher.find()) {
			result = part.substring(0, matcher.start()) + part.substring(matcher.end());
		}

		return result;
	}

	/**
	 * Returns how many method parameters are bound by this part.
	 * @return arguments count
	 */
	public int getNumberOfArguments() {
		return type.getNumberOfArguments();
	}

	/**
	 * @return the propertyPath
	 */
	public PropertyPath getProperty() {
		return propertyPath;
	}

	/**
	 * @return the type
	 */
	public Part.Type getType() {
		return type;
	}

	public boolean isOneParameter() {
		return oneParameter;
	}


	public int getArgumentIndex() {
		return argumentIndex;
	}

	public LikeType getLikeType() {
		return likeType;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}

		Part that = (Part) obj;
		return this.propertyPath.equals(that.propertyPath) && this.type.equals(that.type);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 37;
		result += 17 * propertyPath.hashCode();
		result += 17 * type.hashCode();
		return result;
	}

	@Override
	public void render(RenderContext context) {

		String typeValue = type.toString();

		//替换占位符为arg0，arg1...
		if(type.getNumberOfArguments() > 0) {

			if(this.isOneParameter()) {
				typeValue = typeValue.replaceAll("#\\{0\\}", "#{_parameter}");
				if(argumentType.isArray()) {
					typeValue = typeValue.replaceAll("@\\{0\\}", "array");
				} else {
					typeValue = typeValue.replaceAll("@\\{0\\}", "collection");
				}
			} else {
				for(int i=0; i<this.getNumberOfArguments(); i++) {
					if(StringUtils.isEmpty(context.getArgAlias())) {
						typeValue = typeValue.replaceAll("#\\{" + i +  "\\}", "#{arg" + (argumentIndex +i) + "}");
						typeValue = typeValue.replaceAll("@\\{" + i +  "\\}", "arg" + (argumentIndex +i));
					} else {
						typeValue = typeValue.replaceAll("#\\{" + i +  "\\}", "#{" + context.getArgAlias() + "." + propertyPath.getName() + "}");
					}
				}
			}
		}

		if(testCondition == null) {
			context.renderString(" AND ");
			context.renderString(StringUtils.isEmpty(context.getVariableAlias()) ? propertyPath.getColumnName() : context.getVariableAlias() + "." + propertyPath.getColumnName());
			context.renderBlank();
			context.renderString(typeValue);
			return;
		}

		// 一个参数设置为_parameter
		// 如果是集合，强制设置成arg_,集合没有_parameter参数
		String paraName = this.isOneParameter() ? (type == Type.IN ? "arg" + this.getArgumentIndex() : "_parameter") : "arg" + this.getArgumentIndex();

		List<ConditionWithArg> conditions = new ArrayList<>();

		// 常规字符串
		if(testCondition.isNotEmpty() && type != Type.IN) {
			conditions.add(new ConditionWithArg(paraName, "!= null"));
			conditions.add(new ConditionWithArg(paraName, "!= \"\""));
		}
		// 集合和数组
		else if(testCondition.isNotEmpty() && type == Type.IN) {
			conditions.add(new ConditionWithArg(paraName, "!= null"));
			if(argumentType.isArray()) {
				conditions.add(new ConditionWithArg(paraName + ".length", "> 0"));  // 如果是集合表明他是需要判断size()
			} else {
				conditions.add(new ConditionWithArg(paraName + ".size()", "> 0"));  // 如果是集合表明他是需要判断size()
			}
		}
		// 一般对象
		else if(testCondition.isNotNull() && !testCondition.isNotEmpty()) {
			conditions.add(new ConditionWithArg(paraName, "!= null"));
		}

		conditions.addAll(Arrays.stream(testCondition.getConditions()).map(s -> new ConditionWithArg(paraName, s)).collect(Collectors.toList()));

		List<String> conditionWithArgs = conditions.stream().map(ConditionWithArg::toString).collect(Collectors.toList());

		//表示有条件
		if(conditionWithArgs.size() > 0) {
			context.renderString("<if test='");
			context.renderString(StringUtils.collectionToDelimitedString(conditionWithArgs," and "));
			context.renderString("'>");
			context.renderString(" AND ");
			context.renderString(StringUtils.isEmpty(context.getVariableAlias()) ? propertyPath.getColumnName() : context.getVariableAlias() + "." + propertyPath.getColumnName());
			context.renderBlank();
			context.renderString(typeValue);
			context.renderString("</if>");
			//String.format("<if test='" + StringUtils.collectionToDelimitedString(conditionWithArgs," and ") + "'>AND %s %s</if>", propertyPath.getColumnName(), typeValue);
		} else {
			context.renderString(" AND ");
			context.renderString(StringUtils.isEmpty(context.getVariableAlias()) ? propertyPath.getColumnName() : context.getVariableAlias() + "." + propertyPath.getColumnName());
			context.renderBlank();
			context.renderString(typeValue);
			//String.format("AND %s %s", propertyPath.getColumnName(), typeValue);
		}

	}

	/**
	 * The type of a method name part. Used to create query parts in various ways.
	 *
	 * @author Oliver Gierke
	 * @author Thomas Darimont
	 */
	enum Type {

		NOT_BETWEEN(2, new Expression("not between #{0} and #{1}"), "IsNotBetween", "NotBetween"),
		BETWEEN(2, new Expression("between #{0} and #{1}"), "IsBetween", "Between"),
		IS_NOT_NULL(0, new Expression("is not null"), "IsNotNull", "NotNull"),
		IS_NULL(0, new Expression("is null"), "IsNull", "Null"),
		LESS_THAN(new Expression("<![CDATA[ < #{0} ]]>"), "IsLessThan", "LessThan"),
		LESS_THAN_EQUAL(new Expression("<![CDATA[ <= #{0} ]]>"), "IsLessThanEqual", "LessThanEqual"),
		GREATER_THAN(new Expression("<![CDATA[ > #{0} ]]>"), "IsGreaterThan","GreaterThan"),
		GREATER_THAN_EQUAL(new Expression("<![CDATA[ >= #{0} ]]>"), "IsGreaterThanEqual", "GreaterThanEqual"),
		BEFORE(new Expression("<![CDATA[ < #{0} ]]>"), "IsBefore", "Before"),
		AFTER(new Expression("<![CDATA[ > #{0} ]]>"), "IsAfter", "After"),
		NOT_LIKE(LikeType.CONTAIN, new Expression("not like #{0}"), "IsNotLike", "NotLike"),
		LIKE(LikeType.CONTAIN, new Expression("like #{0}"), "IsLike", "Like"),
		STARTING_WITH(LikeType.AFTER, new Expression("like #{0}"), "IsStartingWith","StartingWith", "StartsWith"),
		ENDING_WITH(LikeType.BEFORE, new Expression("like #{0}"), "IsEndingWith", "EndingWith", "EndsWith"),
		NOT_CONTAINING(LikeType.CONTAIN, new Expression("not like #{0}"), "IsNotContaining", "NotContaining", "NotContains"),
		CONTAINING(LikeType.CONTAIN, new Expression("like #{0}"), "IsContaining", "Containing", "Contains"),
		NOT_IN(new Expression("not in <foreach collection=\"@{0}\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>"), "IsNotIn", "NotIn"),
		IN(new Expression("in <foreach collection=\"@{0}\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>"), "IsIn", "In"),
		NEAR(new Expression("in #{0}"), "IsNear", "Near"),
		WITHIN(new Expression("in #{0}"), "IsWithin", "Within"),
		REGEX(new Expression("in #{0}"), "MatchesRegex", "Matches", "Regex"),
		EXISTS(0, new Expression("existsById"), "Exists"),
		TRUE(0, new Expression("is true"), "IsTrue", "True"),
		FALSE(0, new Expression("is false"), "IsFalse","False"),
		NEGATING_SIMPLE_PROPERTY(new Expression("<![CDATA[ <> #{0} ]]>"), "IsNot", "Not"),
		SIMPLE_PROPERTY(new Expression("= #{0}"), "Is", "Equals");

		// Need to list them again explicitly as the order is important
		// (esp. for IS_NULL, IS_NOT_NULL)
		private static final List<Type> ALL = Arrays.asList(IS_NOT_NULL, IS_NULL, NOT_BETWEEN, BETWEEN, LESS_THAN, LESS_THAN_EQUAL,
				GREATER_THAN, GREATER_THAN_EQUAL, BEFORE, AFTER, NOT_LIKE, LIKE, STARTING_WITH, ENDING_WITH, NOT_CONTAINING,
				CONTAINING, NOT_IN, IN, NEAR, WITHIN, REGEX, EXISTS, TRUE, FALSE, NEGATING_SIMPLE_PROPERTY, SIMPLE_PROPERTY);

		public static final Collection<String> ALL_KEYWORDS;

		static {
			List<String> allKeywords = new ArrayList<>();
			for (Type type : ALL) {
				allKeywords.addAll(type.keywords);
			}
			ALL_KEYWORDS = Collections.unmodifiableList(allKeywords);
		}

		private final List<String> keywords;   //关键字，可能会有多种表达方式
		private final int numberOfArguments;   //几个参数
		private final Expression expression;   //sql表达式
		private final LikeType likeType; 		//like类型

		/**
		 * Creates a new {@link Type} using the given keyword, number of arguments to be bound and comparison. Keyword and
		 * comparison can be {@literal null}.
		 */
		Type(int numberOfArguments, LikeType likeType, Expression expression, String... keywords) {

			this.numberOfArguments = numberOfArguments;
			this.expression = expression;
			this.keywords = Arrays.asList(keywords);
			this.likeType = likeType;
		}

		Type(int numberOfArguments, Expression expression, String... keywords) {
			this(numberOfArguments, null, expression, keywords);
		}

		Type(LikeType likeType, Expression expression, String... keywords) {
			this(1, likeType, expression, keywords);
		}

		Type(Expression expression, String... keywords) {
			this(1, expression, keywords);
		}

		/**
		 * Returns the {@link Type} of the {@link Part} for the given raw propertyPath. This will try to detect e.g.
		 * keywords contained in the raw propertyPath that trigger special query creation. Returns {@link #SIMPLE_PROPERTY}
		 * by default.
		 */
		public static Part.Type fromProperty(String rawProperty) {

			for (Part.Type type : ALL) {
				if (type.supports(rawProperty)) {
					return type;
				}
			}

			return SIMPLE_PROPERTY;
		}

		/**
		 * Returns all keywords supported by the current {@link Type}.
		 */
		public Collection<String> getKeywords() {
			return Collections.unmodifiableList(keywords);
		}

		/**
		 * Returns whether the the type supports the given raw property. Default implementation checks whether the property
		 * ends with the registered keyword. Does not support the keyword if the property is a valid field as is.
		 */
		protected boolean supports(String property) {

			if (keywords == null) {
				return true;
			}

			for (String keyword : keywords) {
				if (property.endsWith(keyword)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Returns the number of arguments the propertyPath binds. By default this exactly one argument.
		 */
		public int getNumberOfArguments() {
			return numberOfArguments;
		}

		/**
		 * Callback method to extract the actual propertyPath to be bound from the given part. Strips the keyword from the
		 * part's end if available.
		 */
		public String extractProperty(String part) {

			String candidate = StringUtils.uncapitalize(part);

			for (String keyword : keywords) {
				if (candidate.endsWith(keyword)) {
					return candidate.substring(0, candidate.length() - keyword.length());
				}
			}

			return candidate;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return String.format("%s ", this.expression.getExpression());
			//return String.format("%s %s", name(), getNumberOfArguments(), getKeywords());
		}

		public static class Expression {
			private String expression;

			public Expression(String expression) {
				this.expression = expression;
			}

			public String getExpression() {
				return expression;
			}

			public void setExpression(String expression) {
				this.expression = expression;
			}
		}
	}

	static class TestCondition {

		private boolean notNull;

		private boolean notEmpty;

		private String[] conditions;

		public TestCondition(boolean notNull, boolean notEmpty, String[] conditions) {
			this.notNull = notNull;
			this.notEmpty = notEmpty;
			this.conditions = conditions;
		}

		public boolean isNotNull() {
			return notNull;
		}

		public boolean isNotEmpty() {
			return notEmpty;
		}

		public String[] getConditions() {
			return conditions;
		}

	}

	static class ConditionWithArg {

		private String argName;

		private String condition;

		public ConditionWithArg(String argName, String condition) {
			this.argName = argName;
			this.condition = condition;
		}

		public String getArgName() {
			return argName;
		}

		public String getCondition() {
			return condition;
		}

		@Override
		public String toString() {
			return argName + " " + condition;
		}
	}
}
