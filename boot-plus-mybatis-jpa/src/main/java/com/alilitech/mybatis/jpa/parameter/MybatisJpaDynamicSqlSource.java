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
package com.alilitech.mybatis.jpa.parameter;

import com.alilitech.mybatis.jpa.EntityMetaDataRegistry;
import com.alilitech.mybatis.jpa.domain.Order;
import com.alilitech.mybatis.jpa.domain.Sort;
import com.alilitech.mybatis.jpa.meta.EntityMetaData;
import com.alilitech.mybatis.jpa.criteria.CriteriaBuilder;
import com.alilitech.mybatis.jpa.criteria.CriteriaQuery;
import com.alilitech.mybatis.jpa.criteria.Specification;
import com.alilitech.mybatis.jpa.criteria.expression.PredicateExpression;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * 扩展DynamicSqlSource，主要是扩展一些参数信息和转换
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MybatisJpaDynamicSqlSource extends DynamicSqlSource {

    private Class<?> domainType;

    public MybatisJpaDynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        super(configuration, rootSqlNode);
    }

    public MybatisJpaDynamicSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> domainType) {
        super(configuration, rootSqlNode);
        this.domainType = domainType;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 以下操作是在${ }转换前需要操作的
        // 转换排序参数
        if(parameterObject instanceof Sort && domainType != null) {
            Sort sort = (Sort) parameterObject;
            for(Order order : sort.getOrders()) {
                EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(domainType);
                String columnName = entityMetaData.getColumnMetaDataMap().get(order.getProperty()).getColumnName();
                order.setProperty(columnName);
            }
        } else if(parameterObject instanceof MapperMethod.ParamMap && domainType != null) {
            MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap<?>) parameterObject;
            paramMap.forEach((key, value) -> {
                if(value instanceof Sort) {
                    Sort sort = (Sort) value;
                    for(Order order : sort.getOrders()) {
                        EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(domainType);
                        if(entityMetaData.getColumnMetaDataMap().containsKey(order.getProperty())) {
                            String columnName = entityMetaData.getColumnMetaDataMap().get(order.getProperty()).getColumnName();
                            order.setProperty(columnName);
                        }
                    }
                }
            });
        }

        // 转换规格查询参数
        if(parameterObject instanceof Specification && domainType != null) {
            Specification<?> specification = (Specification<?>) parameterObject;

            CriteriaBuilder<?> cb = new CriteriaBuilder<>(domainType);

            CriteriaQuery<?> query = new CriteriaQuery<>(domainType);

            PredicateExpression<?> predicate = specification.toPredicate(cb, query);

            if(predicate != null) {
                query.where(predicate);
            }
            parameterObject = query;
        }

        return super.getBoundSql(parameterObject);
    }
}
