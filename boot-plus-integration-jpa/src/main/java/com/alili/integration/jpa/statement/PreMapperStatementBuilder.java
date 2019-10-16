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

import com.alili.integration.dialect.SqlDialectFactory;
import com.alili.integration.jpa.EntityMetaDataRegistry;
import com.alili.integration.jpa.definition.GenericType;
import com.alili.integration.jpa.definition.JoinStatementDefinition;
import com.alili.integration.jpa.definition.MethodDefinition;
import com.alili.integration.jpa.meta.ColumnMetaData;
import com.alili.integration.jpa.meta.EntityMetaData;
import com.alili.integration.jpa.parameter.GenerationType;
import com.alili.integration.jpa.parameter.TriggerValue4Jdbc3KeyGenerator;
import com.alili.integration.jpa.parameter.TriggerValue4SelectKeyGenerator;
import com.alili.integration.jpa.util.ResultMapIdUtils;
import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public abstract class PreMapperStatementBuilder extends BaseBuilder {

    protected MapperBuilderAssistant builderAssistant;

    protected MethodDefinition methodDesc;

    protected EntityMetaData entityMetaData;

    protected SqlDialectFactory sqlDialectFactory;

    public PreMapperStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration);
        this.builderAssistant = builderAssistant;

        this.sqlDialectFactory = new SqlDialectFactory(configuration.getDatabaseId());
    }

    public PreMapperStatement buildPreMapperStatement(MethodDefinition methodDesc, GenericType genericType) {
        this.methodDesc = methodDesc;
        this.entityMetaData = EntityMetaDataRegistry.getInstance().get(genericType.getDomainType());

        PreMapperStatement preMapperStatement = new PreMapperStatement();

        String id = builderAssistant.getCurrentNamespace() + "." + methodDesc.getMethodName();
        LanguageDriver langDriver = getLanguageDriver(null);
        SqlSource sqlSource = this.buildSqlSource(buildSQL(), langDriver);

        preMapperStatement.setId(id);
        preMapperStatement.setLang(langDriver);
        preMapperStatement.setSqlSource(sqlSource);
        preMapperStatement.setParameterType(getParameterTypeClass());

        // dynamic & has parameters
        preMapperStatement.setStatementType(StatementType.PREPARED);
        // unknow
        preMapperStatement.setResultSetType(ResultSetType.FORWARD_ONLY);

        buildPreMapperStatementExtend(preMapperStatement, genericType);

        boolean isSelect = preMapperStatement.getSqlCommandType() == SqlCommandType.SELECT;
        boolean flushCache = !isSelect;
        boolean useCache = isSelect;

        preMapperStatement.setFlushCache(flushCache);
        preMapperStatement.setUseCache(useCache);

        return preMapperStatement;
    }

    protected abstract void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType);

    protected abstract String buildSQL() ;

    protected abstract Class<?> getParameterTypeClass() ;

    /** 创建mybatis SqlSource */
    private SqlSource buildSqlSource(String sqlScript, LanguageDriver languageDriver) {
        return languageDriver.createSqlSource(configuration, sqlScript, getParameterTypeClass());
    }

    protected LanguageDriver getLanguageDriver(String lang) {
        Class<? extends LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = resolveClass(lang);
        }
        return builderAssistant.getLanguageDriver(langClass);
    }

    protected String buildWhere() {
        String condition = StatementAssistant.buildCondition(methodDesc, entityMetaData);
        return " " + condition;
    }

    protected String buildSort() {
        String orderBy = StatementAssistant.buildSort(methodDesc);
        return " " + orderBy;
    }

    protected String buildWhere(String alias) {
        String condition = StatementAssistant.buildCondition(methodDesc, entityMetaData, alias);
        return " " + condition;
    }

    /**
     * 设置无key生成器
     * @param preMapperStatement
     */
    protected void setNoKeyGenerator(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(new NoKeyGenerator());
        preMapperStatement.setKeyColumn(null);
        preMapperStatement.setKeyProperty(null);
    }

    /**
     * 设置JDBC类型key生成器，
     * @param preMapperStatement
     */
    protected void setTriggerValue4Jdbc3KeyGenerator(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(new TriggerValue4Jdbc3KeyGenerator());
        //jdbc
        preMapperStatement.setKeyColumn(entityMetaData.getPrimaryColumnMetaData().getColumnName());
        preMapperStatement.setKeyProperty(entityMetaData.getPrimaryColumnMetaData().getProperty());
    }

    /**
     * 设置JDBC类型key生成器，
     * @param preMapperStatement
     */
    protected void setTriggerValue4Jdbc3KeyGeneratorButNotCallBack(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(new TriggerValue4Jdbc3KeyGenerator());
        //jdbc
        preMapperStatement.setKeyColumn(null);
        preMapperStatement.setKeyProperty(null);
    }

    /**
     * 设置Select类型key生成器，需要执行一次sql，得到key
     * @param preMapperStatement
     */
    protected void setTriggerValue4SelectKeyGenerator(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(buildSelectKeyGenerator(entityMetaData.getPrimaryColumnMetaData(), true));
        preMapperStatement.setKeyColumn(entityMetaData.getPrimaryColumnMetaData().getColumnName());
        preMapperStatement.setKeyProperty(entityMetaData.getPrimaryColumnMetaData().getProperty());
    }

    /**
     * 只有insert或update的时候才有key和触发值
     * @param preMapperStatement
     */
    protected void setKeyGeneratorAndTriggerValue(PreMapperStatement preMapperStatement) {
        //未指定主键
        if(entityMetaData.getPrimaryColumnMetaData() == null) {
            setNoKeyGenerator(preMapperStatement);
        } else if(entityMetaData.getPrimaryColumnMetaData().getIdGenerationType() == GenerationType.AUTO){
            setNoKeyGenerator(preMapperStatement);
        }
        //自增，指定回调
        else if(entityMetaData.getPrimaryColumnMetaData().getIdGenerationType() == GenerationType.IDENTITY) {
            setTriggerValue4Jdbc3KeyGenerator(preMapperStatement);
        }
        //主键不是序列，并且不是自增，没有回调，否则一些数据库会出此置空ID的情况
        else if(entityMetaData.getPrimaryColumnMetaData().getIdGenerationType() != GenerationType.SEQUENCE) {
            setTriggerValue4Jdbc3KeyGeneratorButNotCallBack(preMapperStatement);
        }
        //主键是序列
        else {
            setTriggerValue4SelectKeyGenerator(preMapperStatement);
        }
    }

    /**
     * find语句提供ResultType或id
     * @param preMapperStatement
     * @param genericType
     */
    protected void setFindResultIdOrType(PreMapperStatement preMapperStatement, GenericType genericType) {
        if(methodDesc.isResultMap() && methodDesc.isBase()) {
            preMapperStatement.setResultMap(ResultMapIdUtils.buildBaseResultMapId(builderAssistant));
        } else if(methodDesc.isResultMap()) {
            String resultMapId = buildResultMap();
            preMapperStatement.setResultMap(resultMapId);
        } else {
            preMapperStatement.setResultType((Class)genericType.getDomainType());
        }
    }

    private String buildResultMap() {

        String resultMapId = builderAssistant.getCurrentNamespace() + "." + methodDesc.getMethodName() + "ResultMap";
        if(configuration.hasResultMap(resultMapId)) {
            return resultMapId;
        }

        List<ResultMapping> mappings = new ArrayList<>();

        for (JoinStatementDefinition joinStatementDesc : methodDesc.getJoinStatementDefinitions()) {

            ResultMapping resultMapping = builderAssistant.buildResultMapping(
                    joinStatementDesc.getResultType(),
                    joinStatementDesc.getProperty(),
                    joinStatementDesc.getColumn(),
                    joinStatementDesc.getJavaType(),
                    null,
                    joinStatementDesc.getNestedSelect(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

            mappings.add(resultMapping);
        }

        ResultMap resultMap = builderAssistant.addResultMap(
                resultMapId,
                entityMetaData.getEntityType(),
                ResultMapIdUtils.buildBaseResultMapId(builderAssistant),
                null,
                mappings,
                true
        );

        return resultMap.getId();
    }

    private KeyGenerator buildSelectKeyGenerator(ColumnMetaData columnMetaData, boolean executeBefore) {

        String id = "BaseKey" + SelectKeyGenerator.SELECT_KEY_SUFFIX + executeBefore;

        if(configuration.hasStatement(builderAssistant.applyCurrentNamespace(id, false))) {
            return configuration.getKeyGenerator(builderAssistant.applyCurrentNamespace(id, false));
        }

        Class<?> resultTypeClass = columnMetaData.getType();
        StatementType statementType = StatementType.PREPARED;
        String keyProperty = columnMetaData.getProperty();
        String keyColumn = columnMetaData.getColumnName();

        // defaults
        boolean useCache = false;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;

        SqlSource sqlSource = getLanguageDriver(null).createSqlSource(builderAssistant.getConfiguration(),
                sqlDialectFactory.buildKeyGeneratorScript(columnMetaData.getSequenceName()), null);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, null, resultMap, resultTypeClass, resultSetTypeEnum,
                flushCache, useCache, false,
                keyGenerator, keyProperty, keyColumn, null, getLanguageDriver(null), null);

        id = builderAssistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        SelectKeyGenerator answer = new TriggerValue4SelectKeyGenerator(keyStatement, executeBefore);
        configuration.addKeyGenerator(id, answer);

        return answer;
    }

}
