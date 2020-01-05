/**
 *    Copyright 2017-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.alilitech.integration.jpa.statement;

import com.alilitech.integration.jpa.anotation.Trigger;
import com.alilitech.integration.jpa.meta.ColumnMetaData;
import com.alilitech.integration.jpa.meta.EntityMetaData;
import com.alilitech.integration.jpa.parameter.TriggerValueType;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.util.StringUtils;



/**
 * Statement助手
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class StatementAssistant {

	private StatementAssistant() {
	}

	/*public static String buildSort(MethodDefinition methodDefinition) {
		if(methodDefinition.getSortIndex() > -1) {
			StringBuffer orderString = new StringBuffer();

			String paramName = methodDefinition.isOneParameter() ? "_parameter" : ("arg" + methodDefinition.getSortIndex());
			orderString.append("<if test=\"" + paramName + "!= null\">");
			orderString.append("<foreach item=\"item\" index=\"index\" open=\"order by\" separator=\",\" close=\"\" collection=\"" + paramName + ".orders\">");
			orderString.append("${item.property} ${item.direction} ");
			orderString.append("</foreach>");
			orderString.append("</if>");
			return orderString.toString();
		} else {
			return "";
		}
	}*/

	/*public static String buildCondition(MethodDefinition methodDefinition, EntityMetaData entityMetaData) {
		return buildCondition(methodDefinition, entityMetaData, "");
	}*/

	/**
	 *  解析method的where条件,如果没有where语句,返回空字符串""
	 */
	/*public static String buildCondition(MethodDefinition methodDefinition, EntityMetaData entityMetaData, String alias ) {
		MethodType methodType = resolveMethodType(methodDefinition.getMethodName());
		String expression = null;

		//这些都是byPrimaryKey的
		if(methodDefinition.getMethodName().equals("findById")
				|| methodDefinition.getMethodName().equals("update")
				|| methodDefinition.getMethodName().equals("updateSelective")
				|| methodDefinition.getMethodName().equals("updateBatch")
				|| methodDefinition.getMethodName().equals("deleteById")
				|| methodDefinition.getMethodName().equals("existsById"))
		{
			expression = "WHERE "
					+ entityMetaData.getPrimaryColumnMetaData().getColumnName()
					+ " = #{" + (StringUtils.isEmpty(alias) ? "" : (alias + ".")) + entityMetaData.getPrimaryColumnMetaData().getProperty() + "}" ;
			return expression;
		} else if(!methodDefinition.getMethodName().contains("By") && !methodDefinition.getMethodName().contains("With")) {
			return "";
		} else {
			if(methodDefinition.getMethodName().contains("By")) {
				expression = methodDefinition.getMethodName().substring(methodType.getType().length() + 2);
			} else {
				expression = methodDefinition.getMethodName().substring(methodType.getType().length() + 4);
			}

		}
		Predicate predicate = null;
		if(methodType.equals(MethodType.findJoin)) {   //多对多关联查询，没有类型
			predicate = new Predicate(expression, null, methodDefinition);
		} else {   //自定义查询
			predicate = new Predicate(expression, entityMetaData.getEntityType(), methodDefinition);
		}

		return predicate.toString();
	}*/

	/**
	 * 判断走哪个解析器
	 * @param methodName
	 * @return
	 */
	public static MethodType resolveMethodType(String methodName) {
		// 注意顺序 insert insertSelective,insert应放在后面判断
		if (methodName.equals(MethodType.insertBatch.getType())) {
			return MethodType.insertBatch;
		}

		if (methodName.equals(MethodType.insertSelective.getType())) {
			return MethodType.insertSelective;
		}

		if (methodName.equals(MethodType.insert.getType())) {
			return MethodType.insert;
		}

		if (methodName.equals(MethodType.updateBatch.getType())) {
			return MethodType.updateBatch;
		}

		if (methodName.equals(MethodType.updateSelective.getType())) {
			return MethodType.updateSelective;
		}

		if (methodName.equals(MethodType.update.getType())) {
			return MethodType.update;
		}

		if (methodName.startsWith(MethodType.findAllPage.getType())) {
			return MethodType.findAllPage;
		}

		if (methodName.equals(MethodType.findAllById.getType())) {
			return MethodType.findAllById;
		}

		if (methodName.equals(MethodType.findAll.getType())) {
			return MethodType.findAll;
		}

		/*if (methodName.startsWith(MethodType.findPage.getType())) {
			return MethodType.findPage;
		}*/

		if (methodName.equals(MethodType.deleteBatch.getType())) {
			return MethodType.deleteBatch;
		}

		if (methodName.equals(MethodType.deleteById.getType())) {
			return MethodType.deleteById;
		}

		if (methodName.equals(MethodType.findById.getType())) {
			return MethodType.findById;
		}

		if (methodName.startsWith(MethodType.findJoin.getType())) {
			return MethodType.findJoin;
		}

		if (methodName.equals(MethodType.existsById.getType())) {
			return MethodType.existsById;
		}

		if (methodName.equals(MethodType.findSpecification.getType()) || methodName.equals(MethodType.findPageSpecification.getType())) {
			return MethodType.findSpecification;
		}

		/*if (methodName.startsWith(MethodType.find.getType())) {
			return MethodType.find;
		}*/

		// 其它
		return MethodType.other;
	}

	/**
	 * 装配sql中动态参数的占位符
	 */
	public static String resolveSqlParameterBySysFunction(ColumnMetaData columnMeta, SqlCommandType sqlCommandType, String alias) {
		if(columnMeta.getTriggers() != null) {
			for(Trigger trigger : columnMeta.getTriggers()) {
				if(trigger.triggerType() == sqlCommandType && trigger.valueType() == TriggerValueType.DatabaseFunction) {
					return trigger.value();
				}
			}
		}
		return resolveSqlParameter(columnMeta, alias);
	}

	/**
	 * 装配sql中动态参数的占位符
	 */
	public static String resolveSqlParameterBySysFunction(ColumnMetaData columnMetaData, SqlCommandType sqlCommandType) {
		return resolveSqlParameterBySysFunction(columnMetaData, sqlCommandType, "");
	}

	/**
	 * 装配sql中动态参数的占位符
	 */
	public static String resolveSqlParameter(ColumnMetaData columnMetaData) {
		return resolveSqlParameter(columnMetaData, "");
	}

	/**
	 * 装配sql中动态参数的占位符 #{alias.propertyName,jdbcType=,}
	 */
	public static String resolveSqlParameter(ColumnMetaData columnMetaData, String alias) {

		StringBuilder stringBuilder = new StringBuilder()
				.append("#{")
				.append(StringUtils.isEmpty(alias) ? "" : alias + ".")
				.append(columnMetaData.getProperty())
				.append(columnMetaData.getJdbcTypeAlias() != null ? ", jdbcType=" + columnMetaData.getJdbcTypeAlias() : "")
				.append("}");

		return stringBuilder.toString();
	}

	public static String buildPrimaryKeyCondition(EntityMetaData entityMetaData) {
		return entityMetaData.getPrimaryColumnMetaData().getColumnName() + " = " + resolveSqlParameter(entityMetaData.getPrimaryColumnMetaData());
	}

	public static String buildPrimaryKeyCondition(EntityMetaData entityMetaData, String alias) {
		return entityMetaData.getPrimaryColumnMetaData().getColumnName() + " = " + resolveSqlParameter(entityMetaData.getPrimaryColumnMetaData(), alias);
	}

}
