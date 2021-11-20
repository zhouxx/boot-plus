/*
 *    Copyright 2017-2021 the original author or authors.
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
package com.alilitech.mybatis.jpa.statement;

import com.alilitech.mybatis.jpa.anotation.Trigger;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.meta.ColumnMetaData;
import com.alilitech.mybatis.jpa.parameter.TriggerValueType;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.util.StringUtils;


/**
 * Statement assistant
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class StatementAssistant {

	private StatementAssistant() {
	}

	/**
	 * resolve the {@link MethodType} by {@link MethodDefinition}
	 */
	public static MethodType resolveMethodType(MethodDefinition definition) {
		String methodName = definition.getMethodName();
		if (methodName.equals(MethodType.INSERT_BATCH.getType())) {
			return MethodType.INSERT_BATCH;
		}

		if (methodName.equals(MethodType.INSERT_SELECTIVE.getType())) {
			return MethodType.INSERT_SELECTIVE;
		}

		if (methodName.equals(MethodType.INSERT.getType())) {
			return MethodType.INSERT;
		}

		if (methodName.equals(MethodType.UPDATE_BATCH.getType())) {
			return MethodType.UPDATE_BATCH;
		}

		if (methodName.equals(MethodType.UPDATE_SELECTIVE.getType())) {
			return MethodType.UPDATE_SELECTIVE;
		}

		if (methodName.equals(MethodType.UPDATE.getType())) {
			return MethodType.UPDATE;
		}

		// findAllPage, findAllPageSort, findAll
		if (methodName.startsWith(MethodType.FIND_ALL_PAGE.getType())
				|| methodName.equals(MethodType.FIND_ALL.getType())) {
			return MethodType.FIND_ALL;
		}

		if (methodName.equals(MethodType.FIND_ALL_BY_ID.getType())) {
			return MethodType.FIND_ALL_BY_ID;
		}

		if (methodName.equals(MethodType.DELETE_BATCH.getType())) {
			return MethodType.DELETE_BATCH;
		}

		if (methodName.equals(MethodType.DELETE_BY_ID.getType())) {
			return MethodType.DELETE_BY_ID;
		}

		if (methodName.equals(MethodType.FIND_BY_ID.getType())) {
			return MethodType.FIND_BY_ID;
		}

		if (methodName.startsWith(MethodType.FIND_JOIN.getType())) {
			return MethodType.FIND_JOIN;
		}

		if (methodName.equals(MethodType.EXISTS_BY_ID.getType())) {
			return MethodType.EXISTS_BY_ID;
		}

		if (definition.isSpecification()) {
			return MethodType.FIND_SPECIFICATION;
		}

		// 其它
		return MethodType.OTHER;
	}

	/**
	 * 装配sql中动态参数的占位符
	 */
	public static String resolveSqlParameterBySysFunction(ColumnMetaData columnMeta, SqlCommandType sqlCommandType, String alias) {
		if(columnMeta.getTriggers() != null) {
			for(Trigger trigger : columnMeta.getTriggers()) {
				if(trigger.triggerType() == sqlCommandType && trigger.valueType() == TriggerValueType.DATABASE_FUNCTION) {
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

}
