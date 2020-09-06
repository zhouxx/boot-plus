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
package com.alilitech.mybatis;

import com.alilitech.mybatis.dialect.KeySqlDialectRegistry;
import com.alilitech.mybatis.dialect.PaginationDialectRegistry;
import com.alilitech.mybatis.jpa.DatabaseTypeRegistry;
import com.alilitech.mybatis.jpa.JpaInitializer;
import com.alilitech.mybatis.spring.DatabaseRegistration;
import com.alilitech.mybatis.spring.DatabaseRegistry;
import com.alilitech.mybatis.spring.MybatisJpaConfigurer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.List;

/**
 * initialize Mybatis Jpa
 * init component highest - 10
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class MybatisJpaStart implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationContext applicationContext;

    private final SqlSessionFactory sqlSessionFactory;
    private final DatabaseRegistry databaseRegistry;
    private final DatabaseTypeRegistry databaseTypeRegistry;
    private final KeySqlDialectRegistry keySqlDialectRegistry;
    private final PaginationDialectRegistry paginationDialectRegistry;
    private final List<MybatisJpaConfigurer> mybatisJpaConfigurers;

    public MybatisJpaStart(SqlSessionFactory sqlSessionFactory, DatabaseRegistry databaseRegistry, DatabaseTypeRegistry databaseTypeRegistry, KeySqlDialectRegistry keySqlDialectRegistry, PaginationDialectRegistry paginationDialectRegistry, List<MybatisJpaConfigurer> mybatisJpaConfigurers) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.databaseRegistry = databaseRegistry;
        this.databaseTypeRegistry = databaseTypeRegistry;
        this.keySqlDialectRegistry = keySqlDialectRegistry;
        this.paginationDialectRegistry = paginationDialectRegistry;
        this.mybatisJpaConfigurers = mybatisJpaConfigurers;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // mybatis configuration
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();

        //start mybatis jpa
        StopWatch watch = new StopWatch();
        watch.start();

        JpaInitializer jpaInitializer = new JpaInitializer(configuration);
        jpaInitializer.buildJoinMetaDataAndRelationMethodDefinition().invokeJpaMapperStatementBuilder();

        // add custom database
        if(!CollectionUtils.isEmpty(mybatisJpaConfigurers)) {
            mybatisJpaConfigurers.forEach(mybatisJpaConfigurer -> mybatisJpaConfigurer.addDatabase(databaseRegistry));
        }

        //really start register
        List<DatabaseRegistration> databaseRegistrations = databaseRegistry.getDatabaseRegistrations();

        databaseRegistrations.forEach(databaseRegistration -> {
            databaseTypeRegistry.register(databaseRegistration.getDatabaseType());
            if(databaseRegistration.getKeySqlGenerator() != null) {
                keySqlDialectRegistry.register(databaseRegistration.getDatabaseType(), databaseRegistration.getKeySqlGenerator());
            }
            if(databaseRegistration.getPaginationDialect() == null) {
                logger.warn("DatabaseType: {} does not provider pagination dialect, see class com.alilitech.integration.dialect.pagination.PaginationDialect", databaseRegistration.getDatabaseType().getDatabaseId());
            } else {
                paginationDialectRegistry.register(databaseRegistration.getDatabaseType(), databaseRegistration.getPaginationDialect());
            }
        });

        watch.stop();
        logger.debug("Started Mybatis Jpa in {} ms", watch.getTotalTimeMillis());
        applicationContext.publishEvent(new MybatisJpaStartedEvent(applicationContext));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
