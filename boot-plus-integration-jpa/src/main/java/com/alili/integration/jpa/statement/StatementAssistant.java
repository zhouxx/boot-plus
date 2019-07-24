/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.integration.jpa.statement;

import com.alili.integration.jpa.anotation.Trigger;
import com.alili.integration.jpa.definition.MethodDefinition;
import com.alili.integration.jpa.meta.ColumnMetaData;
import com.alili.integration.jpa.meta.EntityMetaData;
import com.alili.integration.jpa.parameter.TriggerValueType;
import com.alili.integration.jpa.statement.parser.Predicate;
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

	public static String buildSort(MethodDefinition methodDesc) {
		if(methodDesc.getSortIndex() > -1) {
			StringBuffer orderString = new StringBuffer();

			String paramName = methodDesc.isOneParameter() ? "_parameter" : ("arg" + methodDesc.getSortIndex());
			orderString.append("<if test=\"" + paramName + "!= null\">");
			orderString.append("<foreach item=\"item\" index=\"index\" open=\"order by\" separator=\",\" close=\"\" collection=\"" + paramName + ".orders\">");
			orderString.append("${item.property} ${item.direction} ");
			orderString.append("</foreach>");
			orderString.append("</if>");
			return orderString.toString();
		} else {
			return "";
		}
	}

	public static String buildCondition(MethodDefinition methodDesc, EntityMetaData entityMetaData) {
		return buildCondition(methodDesc, entityMetaData, "");
	}

	/**
	 *  解析method的where条件,仅支持单一字段条件,如果没有where语句,返回空字符串""
	 */
	public static String buildCondition(MethodDefinition methodDesc, EntityMetaData entityMetaData, String alias ) {
		MethodType methodType = resolveMethodType(methodDesc.getMethodName());
		String expression = null;

		//这些都是byPrimaryKey的
		if(methodDesc.getMethodName().equals("findOne")
				|| methodDesc.getMethodName().equals("update")
				|| methodDesc.getMethodName().equals("updateSelective")
				|| methodDesc.getMethodName().equals("updateBatch")
				|| methodDesc.getMethodName().equals("delete")
				|| methodDesc.getMethodName().equals("exists"))
		{
			expression = "WHERE "
					+ entityMetaData.getPrimaryColumnMetaData().getColumnName()
					+ " = #{" + (StringUtils.isEmpty(alias) ? "" : (alias + ".")) + entityMetaData.getPrimaryColumnMetaData().getProperty() + "}" ;
			return expression;
		} else if(!methodDesc.getMethodName().contains("By") && !methodDesc.getMethodName().contains("With")) {
			return "";
		} else {
			if(methodDesc.getMethodName().contains("By")) {
				expression = methodDesc.getMethodName().substring(methodType.getType().length() + 2);
			} else {
				expression = methodDesc.getMethodName().substring(methodType.getType().length() + 4);
			}

		}
		Predicate predicate = null;
		if(methodType.equals(MethodType.findJoin)) {   //多对多关联查询，没有类型
			predicate = new Predicate(expression, null, methodDesc);
		} else {   //自定义查询
			predicate = new Predicate(expression, entityMetaData.getEntityType(), methodDesc);
		}

		return predicate.toString();
	}


	public static MethodType resolveMethodType(String methodName) {
		// 注意顺序 insert insertSelective,insert应放在后面判断
		if (methodName.startsWith(MethodType.insertBatch.getType())) {
			return MethodType.insertBatch;
		}

		if (methodName.startsWith(MethodType.insertSelective.getType())) {
			return MethodType.insertSelective;
		}

		if (methodName.startsWith(MethodType.insert.getType())) {
			return MethodType.insert;
		}

		if (methodName.startsWith(MethodType.updateBatch.getType())) {
			return MethodType.updateBatch;
		}

		if (methodName.startsWith(MethodType.updateSelective.getType())) {
			return MethodType.updateSelective;
		}

		if (methodName.startsWith(MethodType.update.getType())) {
			return MethodType.update;
		}

		if (methodName.startsWith(MethodType.findAllPage.getType())) {
			return MethodType.findAllPage;
		}

		if (methodName.startsWith(MethodType.findAllIds.getType())) {
			return MethodType.findAllIds;
		}

		if (methodName.startsWith(MethodType.findAll.getType())) {
			return MethodType.findAll;
		}

		if (methodName.startsWith(MethodType.findPage.getType())) {
			return MethodType.findPage;
		}

		if (methodName.equals(MethodType.findOne.getType())) {
			return MethodType.findOne;
		}

		if (methodName.startsWith(MethodType.findJoin.getType())) {
			return MethodType.findJoin;
		}

		if (methodName.startsWith(MethodType.find.getType())) {
			return MethodType.find;
		}

		if (methodName.startsWith(MethodType.deleteBatch.getType())) {
			return MethodType.deleteBatch;
		}

		if (methodName.startsWith(MethodType.delete.getType())) {
			return MethodType.delete;
		}

		if (methodName.equals(MethodType.exists.getType())) {
			return MethodType.exists;
		}

		// 自定义
		return MethodType.custom;
	}

	/**
	 * 装配sql中动态参数的占位符 #{paramterName,jdbcType=,typeHandler=}
	 */
	public final static String resolveSqlParameterBySysFunction(ColumnMetaData columnMeta, SqlCommandType sqlCommandType, String alias) {
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
	 * 装配sql中动态参数的占位符 #{paramterName,jdbcType=,typeHandler=}
	 */
	public final static String resolveSqlParameterBySysFunction(ColumnMetaData columnMeta, SqlCommandType sqlCommandType) {
		return resolveSqlParameterBySysFunction(columnMeta, sqlCommandType, "");
	}

	/**
	 * 装配sql中动态参数的占位符 #{paramterName,jdbcType=,typeHandler=}
	 */
	public final static String resolveSqlParameter(ColumnMetaData columnMeta) {
		return resolveSqlParameter(columnMeta, "");
	}

	/**
	 * 装配sql中动态参数的占位符 #{alias.paramterName,jdbcType=,typeHandler=}
	 */
	public final static String resolveSqlParameter(ColumnMetaData columnMeta, String alias) {
		String sqlParameter = "#{";
		if (alias != null && !"".equals(alias)) {
			sqlParameter += alias + ".";
		}
		sqlParameter += columnMeta.getProperty();

		// jdbcType
		if (columnMeta.getJdbcTypeAlias() != null) {
			sqlParameter += ", jdbcType=" + columnMeta.getJdbcTypeAlias();
		}
		// typeHandler
		/*if (columnMeta.getTypeHandlerClass() != null) {
			sqlParameter += ", typeHandler=" + columnMeta.getTypeHandlerClass().getName();
		}*/
		sqlParameter += "} ";

		return sqlParameter;
	}

}
