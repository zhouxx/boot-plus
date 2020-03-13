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
import com.alilitech.integration.jpa.definition.MethodDefinition;
import com.alilitech.integration.jpa.meta.ColumnMetaData;
import com.alilitech.integration.jpa.parameter.TriggerValueType;
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
	 * @param definition
	 * @return
	 */
	public static MethodType resolveMethodType(MethodDefinition definition) {
		String methodName = definition.getMethodName();
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

		// findAllPage, findAllPageSort, findAll
		if (methodName.startsWith(MethodType.findAllPage.getType())
				|| methodName.equals(MethodType.findAll.getType())) {
			return MethodType.findAll;
		}

		if (methodName.equals(MethodType.findAllById.getType())) {
			return MethodType.findAllById;
		}

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

		if (definition.isSpecification()) {
			return MethodType.findSpecification;
		}

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

}
