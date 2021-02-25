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

import com.alilitech.mybatis.dialect.SqlDialectFactory;
import com.alilitech.mybatis.jpa.EntityMetaDataRegistry;
import com.alilitech.mybatis.jpa.definition.GenericType;
import com.alilitech.mybatis.jpa.definition.JoinStatementDefinition;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.meta.ColumnMetaData;
import com.alilitech.mybatis.jpa.meta.EntityMetaData;
import com.alilitech.mybatis.jpa.parameter.GenerationType;
import com.alilitech.mybatis.jpa.parameter.TriggerValue4Jdbc3KeyGenerator;
import com.alilitech.mybatis.jpa.parameter.TriggerValue4NoKeyGenerator;
import com.alilitech.mybatis.jpa.parameter.TriggerValue4SelectKeyGenerator;
import com.alilitech.mybatis.jpa.primary.key.KeyGenerator4Auto;
import com.alilitech.mybatis.jpa.statement.parser.PartTree;
import com.alilitech.mybatis.jpa.statement.parser.SimplePart;
import com.alilitech.mybatis.jpa.util.ResultMapIdUtils;
import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public abstract class PreMapperStatementBuilder extends BaseBuilder {

    protected Logger logger = LoggerFactory.getLogger(PreMapperStatementBuilder.class);

    protected MapperBuilderAssistant builderAssistant;

    protected MethodDefinition methodDefinition;

    protected EntityMetaData entityMetaData;

    protected SqlDialectFactory sqlDialectFactory;

    protected MethodType methodType;

    //设置返回值，自定义查询的时候部分需要确定返回值
    protected Class<?> resultType;

    public PreMapperStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration);
        this.methodType = methodType;
        this.builderAssistant = builderAssistant;
        this.sqlDialectFactory = new SqlDialectFactory(configuration.getDatabaseId());
    }

    public PreMapperStatement buildPreMapperStatement(MethodDefinition methodDefinition, GenericType genericType) {
        this.methodDefinition = methodDefinition;
        this.entityMetaData = EntityMetaDataRegistry.getInstance().get(genericType.getDomainType());

        PreMapperStatement preMapperStatement = new PreMapperStatement();

        String id = methodDefinition.getNameSpace() + "." + methodDefinition.getMethodName();
        LanguageDriver langDriver = getLanguageDriver(null);

        String scriptString = buildSQL();
        logger.trace("script==>" + scriptString);
        SqlSource sqlSource = this.buildSqlSource(scriptString, langDriver);

        preMapperStatement.setId(id);
        preMapperStatement.setLang(langDriver);
        preMapperStatement.setSqlSource(sqlSource);
        preMapperStatement.setParameterType(getParameterTypeClass());

        // dynamic & has parameters
        preMapperStatement.setStatementType(StatementType.PREPARED);
        // unknown
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

    protected abstract Class<?> getParameterTypeClass();

    /**
     * create mybatis SqlSource
     */
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

    /**
     * build part tree
     * @return
     */
    protected PartTree buildPartTree() {
        String expression = methodDefinition.getMethodName();

        //将ById 转换成 ByPrimaryKey
        if(ParameterType.ID == methodType.getParameterType()) {
            String paramExpression = entityMetaData.getPrimaryColumnMetaData().getProperty();
            paramExpression = Character.toUpperCase(paramExpression.charAt(0)) + paramExpression.substring(1);
            expression = expression.replaceAll("ById", "By" + paramExpression);
        }

        return new PartTree(expression, entityMetaData.getEntityType(), methodDefinition);
    }

    /**
     * build simple predicate part
     * @param property
     * @return
     */
    protected SimplePart buildSimplePart(String property) {
        return new SimplePart(property, entityMetaData.getEntityType(), methodDefinition);
    }

    /**
     * build sort sql script
     */
    protected String buildSort() {
        if(methodDefinition.getSortIndex() > -1) {
            String paramName = methodDefinition.isOneParameter() ? "_parameter" : ("arg" + methodDefinition.getSortIndex());
            StringBuilder orderString = new StringBuilder()
                    .append("<if test=\"" + paramName + "!= null\">")
                    .append("<foreach item=\"item\" index=\"index\" open=\"ORDER BY\" separator=\", \" close=\"\" collection=\"" + paramName + ".orders\">")
                    .append("${item.property} ${item.direction}")
                    .append("</foreach>")
                    .append("</if>");
            return orderString.toString();
        } else {
            return "";
        }
    }

    /**
     * build mybatis script
     */
    protected String buildScript(List<String> sqlParts) {
        StringBuilder script = new StringBuilder()
                .append("<script>")
                .append(StringUtils.collectionToDelimitedString(sqlParts, " "))
                .append("</script>");
        return script.toString();
    }

    /**
     * 设置无key生成器
     * @param preMapperStatement
     */
    protected void setNoKeyGenerator(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(new TriggerValue4NoKeyGenerator());
        preMapperStatement.setKeyColumn(null);
        preMapperStatement.setKeyProperty(null);
    }

    /**
     * set jdbc key generator
     * @param preMapperStatement
     */
    protected void setTriggerValue4Jdbc3KeyGenerator(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(new TriggerValue4Jdbc3KeyGenerator());
        preMapperStatement.setKeyColumn(entityMetaData.getPrimaryColumnMetaData().getColumnName());
        preMapperStatement.setKeyProperty(entityMetaData.getPrimaryColumnMetaData().getProperty());
    }

    /**
     * set jdbc key generator, but not callback
     * @param preMapperStatement
     */
    protected void setTriggerValue4Jdbc3KeyGeneratorButNotCallBack(PreMapperStatement preMapperStatement) {
        preMapperStatement.setKeyGenerator(new TriggerValue4Jdbc3KeyGenerator());
        preMapperStatement.setKeyColumn(null);
        preMapperStatement.setKeyProperty(null);
    }

    /**
     * set select(sequence) key generator, need execute the sql
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
        }
        //指定了id class优先用自定义id class去生成
        else if(entityMetaData.getPrimaryColumnMetaData().getIdGeneratorClass() != KeyGenerator4Auto.class) {
            setTriggerValue4Jdbc3KeyGenerator(preMapperStatement);
        }
        //下面都是没有自定义id class
        else if(entityMetaData.getPrimaryColumnMetaData().getIdGenerationType() == GenerationType.AUTO){
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
     */
    protected void setFindResultIdOrType(PreMapperStatement preMapperStatement, GenericType genericType) {
        if(resultType != null) {
            preMapperStatement.setResultType(resultType);
        } else if(methodDefinition.isBaseResultMap()) {
            preMapperStatement.setResultMap(ResultMapIdUtils.buildBaseResultMapId(builderAssistant));
        } else if(methodDefinition.isCompositeResultMap()) {
            String resultMapId = buildResultMap();
            preMapperStatement.setResultMap(resultMapId);
        } else {
            preMapperStatement.setResultType((Class)genericType.getDomainType());
        }
    }

    /**
     * 生成复合查询的ResultMap
     */
    private String buildResultMap() {

        String resultMapId = builderAssistant.getCurrentNamespace() + "." + methodDefinition.getMethodName() + "ResultMap";
        if(configuration.hasResultMap(resultMapId)) {
            return resultMapId;
        }

        List<ResultMapping> mappings = new ArrayList<>();

        for (JoinStatementDefinition joinStatementDefinition : methodDefinition.getJoinStatementDefinitions()) {

            ResultMapping resultMapping = builderAssistant.buildResultMapping(
                    joinStatementDefinition.getResultType(),
                    joinStatementDefinition.getProperty(),
                    joinStatementDefinition.getColumn(),
                    joinStatementDefinition.getJavaType(),
                    null,
                    joinStatementDefinition.getNestedSelect(),
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
