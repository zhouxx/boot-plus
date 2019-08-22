/*
 * Copyright 2008-2019 the original author or authors.
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
package com.alili.integration.jpa.statement.parser;

import com.alili.integration.jpa.definition.MethodDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single part of a method name that has to be transformed into a query part. The actual transformation is defined by
 * a {@link Type} that is determined from inspecting the given part. The query part can then be looked up via
 * {@link #getProperty()}.
 * 
 * @author Oliver Gierke
 * @author Martin Baumgartner
 * @author zhouxiaoxiang
 */
public class Part {

	private static final Pattern IGNORE_CASE = Pattern.compile("Ignor(ing|e)Case");

	private final PropertyPath propertyPath;
	private final Part.Type type;

	private TestCondition testCondition;

	private boolean oneParameter;

	private LikeType likeType;

	private int index;
	/**
	 * Creates a new {@link Part} from the given method name part, the {@link Class} the part originates from and the
	 * start parameter index.
	 *
	 * @param source must not be {@literal null}.
	 * @param clazz
	 */
	public Part(String source, Class<?> clazz, MethodDefinition methodDesc) {

		Assert.hasText(source, "Part source must not be null or emtpy!");
		//Assert.notNull(clazz, "Type must not be null!");

		String partToUse = detectAndSetIgnoreCase(source);
		this.type = Type.fromProperty(partToUse);
		this.propertyPath = PropertyPath.from(type.extractProperty(partToUse), clazz, methodDesc);
		this.likeType = type.likeType;
	}

	public boolean getParameterRequired() {

		return getNumberOfArguments() > 0;
	}

	private String detectAndSetIgnoreCase(String part) {

		Matcher matcher = IGNORE_CASE.matcher(part);
		String result = part;

		if (matcher.find()) {
			result = part.substring(0, matcher.start()) + part.substring(matcher.end(), part.length());
		}

		return result;
	}

	/**
	 * Returns how many method parameters are bound by this part.
	 *
	 * @return
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

	public TestCondition getTestCondition() {
		return testCondition;
	}

	public void setTestCondition(TestCondition testCondition) {
		this.testCondition = testCondition;
	}

	public boolean isOneParameter() {
		return oneParameter;
	}

	public void setOneParameter(boolean oneParameter) {
		this.oneParameter = oneParameter;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		//替换占位符为arg0，arg1...
		String typeValue = type.toString();
		if(type.getNumberOfArguments() > 0) {
			for(int i=0; i<type.getNumberOfArguments(); i++) {
				if(this.isOneParameter()) {
					typeValue = typeValue.replaceAll("#\\{" + i +  "\\}", "#{_parameter}");
					typeValue = typeValue.replaceAll("@\\{" + i +  "\\}", "list");
				} else {
					typeValue = typeValue.replaceAll("#\\{" + i +  "\\}", "#{arg" + (index+i) + "}");
					typeValue = typeValue.replaceAll("@\\{" + i +  "\\}", "arg" + (index+i) + "");
				}
			}
		}

		if(testCondition == null) {
			return String.format("And %s %s", propertyPath.getColumnName(), typeValue);
		}

		List<String> conditions = new ArrayList<>();


		if(testCondition.isNotEmpty()) {
			conditions.add("!= null");
			conditions.add("!= \"\"");
		} else if(testCondition.isNotNull() && !testCondition.isNotEmpty()) {
			conditions.add("!= null");
		}
		conditions.addAll(Arrays.asList(testCondition.getConditions()));

		//拼装最终条件
		List<String> conditionWithArgs = new ArrayList<>();

		String paraName = "arg" + this.getIndex();

		//如果只有一个参数
		if(this.isOneParameter()) {
			paraName = "_parameter";
		}

		for(String str : conditions) {
			conditionWithArgs.add(paraName + " " + str);
		}

		if(conditionWithArgs.size() > 0) {
			return String.format("<if test='" + StringUtils.collectionToDelimitedString(conditionWithArgs," and ") + "'>AND %s %s</if>", propertyPath.getColumnName(), typeValue);
		} else {
			return String.format("And %s %s", propertyPath.getColumnName(), typeValue);
		}

	}

	/**
	 * The type of a method name part. Used to create query parts in various ways.
	 *
	 * @author Oliver Gierke
	 * @author Thomas Darimont
	 */
	public static enum Type {

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
		EXISTS(0, new Expression("exists"), "Exists"),
		TRUE(0, new Expression("is true"), "IsTrue", "True"),
		FALSE(0, new Expression("is false"), "IsFalse","False"),
		NEGATING_SIMPLE_PROPERTY(new Expression("<![CDATA[ <> #{0} ]]>"), "IsNot", "Not"),
		SIMPLE_PROPERTY(new Expression("= #{0}"), "Is", "Equals");

		// Need to list them again explicitly as the order is important
		// (esp. for IS_NULL, IS_NOT_NULL)
		private static final List<Type> ALL = Arrays.asList(IS_NOT_NULL, IS_NULL, BETWEEN, LESS_THAN, LESS_THAN_EQUAL,
				GREATER_THAN, GREATER_THAN_EQUAL, BEFORE, AFTER, NOT_LIKE, LIKE, STARTING_WITH, ENDING_WITH, NOT_CONTAINING,
				CONTAINING, NOT_IN, IN, NEAR, WITHIN, REGEX, EXISTS, TRUE, FALSE, NEGATING_SIMPLE_PROPERTY, SIMPLE_PROPERTY);

		public static final Collection<String> ALL_KEYWORDS;

		static {
			List<String> allKeywords = new ArrayList<String>();
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
		 * Creates a new {@link Type} using the given keyword, number of arguments to be bound and operator. Keyword and
		 * operator can be {@literal null}.
		 *  @param numberOfArguments
		 * @param expression
		 * @param keywords
		 */
		private Type(int numberOfArguments, LikeType likeType, Expression expression, String... keywords) {

			this.numberOfArguments = numberOfArguments;
			this.expression = expression;
			this.keywords = Arrays.asList(keywords);
			this.likeType = likeType;
		}

		private Type(int numberOfArguments, Expression expression, String... keywords) {
			this(numberOfArguments, null, expression, keywords);
		}

		private Type(LikeType likeType, Expression expression, String... keywords) {
			this(1, likeType, expression, keywords);
		}

		private Type(Expression expression, String... keywords) {
			this(1, expression, keywords);
		}

		/**
		 * Returns the {@link Type} of the {@link Part} for the given raw propertyPath. This will try to detect e.g.
		 * keywords contained in the raw propertyPath that trigger special query creation. Returns {@link #SIMPLE_PROPERTY}
		 * by default.
		 *
		 * @param rawProperty
		 * @return
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
		 * 
		 * @return
		 */
		public Collection<String> getKeywords() {
			return Collections.unmodifiableList(keywords);
		}

		/**
		 * Returns whether the the type supports the given raw property. Default implementation checks whether the property
		 * ends with the registered keyword. Does not support the keyword if the property is a valid field as is.
		 * 
		 * @param property
		 * @return
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
		 * 
		 * @return
		 */
		public int getNumberOfArguments() {
			return numberOfArguments;
		}



		/**
		 * Callback method to extract the actual propertyPath to be bound from the given part. Strips the keyword from the
		 * part's end if available.
		 * 
		 * @param part
		 * @return
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

		private boolean isOneParameter;

		private boolean notNull;

		private boolean notEmpty;

		private String[] conditions;

		private int index;

		public TestCondition() {}

		public TestCondition(boolean notNull, boolean notEmpty, String[] conditions) {
			this.notNull = notNull;
			this.notEmpty = notEmpty;
			this.conditions = conditions;
		}

		public boolean isOneParameter() {
			return isOneParameter;
		}

		public void setOneParameter(boolean oneParameter) {
			isOneParameter = oneParameter;
		}

		public boolean isNotNull() {
			return notNull;
		}

		public void setNotNull(boolean notNull) {
			this.notNull = notNull;
		}

		public boolean isNotEmpty() {
			return notEmpty;
		}

		public void setNotEmpty(boolean notEmpty) {
			this.notEmpty = notEmpty;
		}

		public String[] getConditions() {
			return conditions;
		}

		public void setConditions(String[] conditions) {
			this.conditions = conditions;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}
	}
}
