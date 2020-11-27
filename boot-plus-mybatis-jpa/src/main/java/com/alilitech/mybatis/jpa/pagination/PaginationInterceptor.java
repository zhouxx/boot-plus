/*
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
package com.alilitech.mybatis.jpa.pagination;

import com.alilitech.mybatis.MybatisJpaProperties;
import com.alilitech.mybatis.dialect.SqlDialectFactory;
import com.alilitech.mybatis.jpa.AutoGenerateStatementRegistry;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Pagination Plugin
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PaginationInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(PaginationInterceptor.class);

    private static Pattern fromPattern = Pattern.compile("\\sfrom\\s");

    private static Pattern orderPattern = Pattern.compile("\\sorder\\s+by\\s");

    private MybatisJpaProperties mybatisJpaProperties;

    public PaginationInterceptor(MybatisJpaProperties mybatisJpaProperties) {
        this.mybatisJpaProperties = mybatisJpaProperties;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // determine whether it is a SELECT operation
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }

        // if it is not the RowBounds parameter then skip it
        RowBounds rowBounds = (RowBounds) metaObject.getValue("delegate.rowBounds");
        if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String originalSql = boundSql.getSql();

        if (rowBounds instanceof Pagination) {
            Pagination page = (Pagination) rowBounds;

            // add since v1.2.5
            Connection connection = (Connection) invocation.getArgs()[0];
            if(page.isSelectCount()) {
                String sqlCount = buildCountSql(originalSql, mappedStatement.getId());
                this.queryTotal(sqlCount, mappedStatement, boundSql, page, connection);
            }

            // determine whether the paging dialect is actively set or auto set by config
            // add since v1.2.7
            SqlDialectFactory sqlDialectFactory = null;
            if(page.getDatabaseType() == null) {
                // add since v1.2.8 use autoDialect
                if(this.mybatisJpaProperties.getPage().isAutoDialect()) {
                    String databaseProductName = connection.getMetaData().getDatabaseProductName();
                    sqlDialectFactory = new SqlDialectFactory(databaseProductName);

                    if(sqlDialectFactory.getDatabaseType() == null) {
                        logger.warn("The databaseId of current connection used auto dialect do not has databaseType in com.alilitech.mybatis.jpa.DatabaseTypeRegistry, it will use mybatis configuration's databaseId!");
                        // get the configuration and instantiate a SqlDialectFactory
                        Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
                        sqlDialectFactory = new SqlDialectFactory(configuration.getDatabaseId());
                    }

                } else {
                    // get the configuration and instantiate a SqlDialectFactory
                    Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
                    sqlDialectFactory = new SqlDialectFactory(configuration.getDatabaseId());
                }
            } else {
                sqlDialectFactory = new SqlDialectFactory(page.getDatabaseType());
            }
            originalSql = sqlDialectFactory.buildPaginationSql(page, originalSql);
        }

        metaObject.setValue("delegate.boundSql.sql", originalSql);
        metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
        metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        properties.list(System.out);
    }

    /**
     * according to the original sql to build `count sql`
     * @param originalSql original sql
     * @return count sql
     */
    private String buildCountSql(String originalSql, String statement) {
        // only auto generate statement optimize select count sql
        if(AutoGenerateStatementRegistry.getInstance().contains(statement)) {
            String loweredString = originalSql.toLowerCase();

            // order matcher to optimize `order by`
            Matcher orderMatcher = orderPattern.matcher(loweredString);
            if(orderMatcher.find()) {
                originalSql = originalSql.substring(0, orderMatcher.start());
                loweredString = loweredString.substring(0, orderMatcher.start());
            }

            Matcher fromMatcher = fromPattern.matcher(loweredString);
            if (fromMatcher.find()) {
                return "SELECT COUNT(1)" + originalSql.substring(fromMatcher.start());
            } else {
                return String.format("SELECT COUNT(1) FROM ( %s ) TOTAL", originalSql);
            }
        }

        // custom sql
        return String.format("SELECT COUNT(1) FROM ( %s ) TOTAL", originalSql);
    }

    /**
     * query the total number of records
     * @param sql sql
     * @param mappedStatement mappedStatement
     * @param boundSql boundSql
     * @param page page
     */
    protected void queryTotal(String sql, MappedStatement mappedStatement, BoundSql boundSql, Pagination page, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
            parameterHandler.setParameters(statement);
            long total = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    total = resultSet.getLong(1);
                }
            }
            page.setTotal(total);
        } catch (Exception e) {
            logger.error("select count occur error: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
