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
package com.alilitech.mybatis.jpa.pagination;

import com.alilitech.mybatis.jpa.AutoGenerateStatementRegistry;
import com.alilitech.mybatis.jpa.parameter.TriggerValue4NoKeyGenerator;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pre paging
 * Handle the case where null is passed in paging, which means no paging
 * If you need to query by page, the total number of queries.
 * If the total number is equal to 0, the query ends and an empty List is returned directly
 * @author Zhou Xiaoxiang
 * @since 1.3.7
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PrePaginationInterceptor implements Interceptor {

    private static Pattern fromPattern = Pattern.compile("\\sfrom\\s");

    private static Pattern orderPattern = Pattern.compile("\\sorder\\s+by\\s");

    public static final String STATEMENT_ID_POSTFIX = "_count";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object rowBounds = invocation.getArgs()[2];
        // If the incoming paging parameter is null, it is set to RowBounds.DEFAULT
        if(rowBounds == null) {
            invocation.getArgs()[2] = RowBounds.DEFAULT;
        }

        // If the paging parameter is null or RowBounds.DEFAULT means no paging is required
        if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];

        if (rowBounds instanceof Pagination) {
            Pagination<?> page = (Pagination<?>) rowBounds;

            if(page.isSelectCount()) {
                Executor executor = (Executor) invocation.getTarget();
                Object parameter = invocation.getArgs()[1];

                BoundSql originalBoundSql = ms.getBoundSql(parameter);
                String originalSql = originalBoundSql.getSql();

                MetaObject metaObject = SystemMetaObject.forObject(executor);
                Configuration configuration;
                if(executor instanceof CachingExecutor) {
                    configuration = (Configuration) metaObject.getValue("delegate.configuration");
                } else {
                    configuration = (Configuration) metaObject.getValue("configuration");
                }

                String countSql = buildCountSql(originalSql, ms.getId());
                long totalCount = this.queryTotal(configuration, executor, ms, originalBoundSql, countSql);
                page.setTotal(totalCount);

                // if total count equal zero, do not execute page query
                if(totalCount == 0) {
                    return new ArrayList<>();
                }

            } else {
                page.setTotal(Long.MAX_VALUE);
            }
        }
        return invocation.proceed();
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
                return "SELECT COUNT(*)" + originalSql.substring(fromMatcher.start());
            } else {
                return String.format("SELECT COUNT(*) FROM ( %s ) TOTAL", originalSql);
            }
        }

        // custom sql
        return String.format("SELECT COUNT(*) FROM ( %s ) TOTAL", originalSql);
    }

    /**
     * query total count
     */
    private Long queryTotal(Configuration configuration, Executor executor, MappedStatement ms, BoundSql originalBoundSql, String countSql) throws SQLException {
        // construct 'count BoundSql'
        BoundSql countBoundSql = new BoundSql(configuration, countSql, originalBoundSql.getParameterMappings(), originalBoundSql.getParameterObject());

        MetaObject metaObject = configuration.newMetaObject(originalBoundSql);
        Map<String, Object> additionalParameters = (Map) metaObject.getValue("additionalParameters");
        for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            countBoundSql.setAdditionalParameter(key, value);
        }

        // construct 'count MappedStatement'
        MappedStatement.Builder builder =
                new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + STATEMENT_ID_POSTFIX, new BoundSqlSqlSource(countBoundSql), ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(new TriggerValue4NoKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, new ArrayList<>(0)).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        MappedStatement countMs = builder.build();

        // use Executor to doQuery
        List<Object> retList = executor.query(countMs, originalBoundSql.getParameterObject(), RowBounds.DEFAULT, null);

        if(retList.size() > 0) {
            return (Long) retList.get(0);
        } else {
            // count search must do not effect page query
            return Long.MAX_VALUE;
        }
    }

    static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
