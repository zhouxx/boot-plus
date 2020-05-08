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
package com.alilitech.mybatis.jpa.pagination;

import com.alilitech.mybatis.dialect.SqlDialectFactory;
import com.alilitech.mybatis.jpa.DatabaseType;
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
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;


/**
 * 分页插件
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PaginationInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(PaginationInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // 先判断是不是SELECT操作
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }

        RowBounds rowBounds = (RowBounds) metaObject.getValue("delegate.rowBounds");

        if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        // 拿到configuration，实例化一个SqlDialectFactory
        Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
        SqlDialectFactory sqlDialectFactory = new SqlDialectFactory(configuration.getDatabaseId());

        // 针对定义了rowBounds，做为mapper接口方法的参数
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String originalSql = boundSql.getSql();
        Connection connection = (Connection) invocation.getArgs()[0];
        if (rowBounds instanceof Pagination) {
            Pagination page = (Pagination) rowBounds;
            String sqlCount = getOriginalCountSql(originalSql, sqlDialectFactory.getDatabaseType());
            this.queryTotal(sqlCount, mappedStatement, boundSql, page, connection);

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
     * 根据原生SQL,包装count SQL
     * @param originalSql original sql
     * @return count sql
     */
    private String getOriginalCountSql(String originalSql, DatabaseType databaseType) {
        if(databaseType == DatabaseType.MS_SQL_SERVER) {
            String loweredString = originalSql.toLowerCase();
            int orderByIndex = loweredString.indexOf("order by");
            if (orderByIndex != -1) {
                originalSql = originalSql.substring(0, orderByIndex);
            }
        }
        return String.format("SELECT COUNT(1) FROM ( %s ) TOTAL", originalSql);
    }

    /**
     * 查询总记录条数
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
