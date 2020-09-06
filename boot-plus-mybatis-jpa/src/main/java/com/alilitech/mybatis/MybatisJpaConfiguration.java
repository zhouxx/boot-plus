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
import com.alilitech.mybatis.jpa.DatabaseIdProviderImpl;
import com.alilitech.mybatis.jpa.DatabaseTypeRegistry;
import com.alilitech.mybatis.jpa.pagination.PaginationInterceptor;
import com.alilitech.mybatis.jpa.parameter.MybatisJpaConfigurationCustomizer;
import com.alilitech.mybatis.jpa.primary.key.GeneratorRegistry;
import com.alilitech.mybatis.spring.DatabaseRegistry;
import com.alilitech.mybatis.spring.MybatisJpaConfigurer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.2.4
 */
@Configuration
public class MybatisJpaConfiguration {

    @Bean
    public MybatisJpaStart mybatisJpaStart(SqlSessionFactory sqlSessionFactory, DatabaseRegistry databaseRegistry, DatabaseTypeRegistry databaseTypeRegistry, KeySqlDialectRegistry keySqlDialectRegistry, PaginationDialectRegistry paginationDialectRegistry,@Nullable List<MybatisJpaConfigurer> mybatisJpaConfigurers) {
        return new MybatisJpaStart(sqlSessionFactory, databaseRegistry, databaseTypeRegistry, keySqlDialectRegistry, paginationDialectRegistry, mybatisJpaConfigurers);
    }

    @Bean
    public PaginationDialectRegistry paginationDialectRegistry() {
        return PaginationDialectRegistry.getInstance();
    }

    @Bean
    public KeySqlDialectRegistry keySqlDialectRegistry() {
        return KeySqlDialectRegistry.getInstance();
    }

    @Bean
    public DatabaseTypeRegistry databaseTypeRegistry() {
        return DatabaseTypeRegistry.getInstance();
    }

    @Bean
    public DatabaseRegistry databaseRegistry() {
        return new DatabaseRegistry();
    }

    @Bean
    public GeneratorRegistry generatorRegistry() {
        return GeneratorRegistry.getInstance();
    }

    @Bean
    public DatabaseIdProviderImpl databaseIdProvider() {
        return new DatabaseIdProviderImpl();
    }

    @Bean
    public MybatisJpaConfigurationCustomizer mybatisJpaConfigurationCustomizer() {
        return new MybatisJpaConfigurationCustomizer();
    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}
