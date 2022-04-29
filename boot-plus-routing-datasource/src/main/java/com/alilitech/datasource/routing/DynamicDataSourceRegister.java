/*
 *    Copyright 2017-2022 the original author or authors.
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
package com.alilitech.datasource.routing;

import com.alilitech.datasource.routing.encrypt.resolver.EncryptPropertyResolver;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.*;


/**
 * Dynamically register the Default Dynamic Data Source data source as the main data source,
 * And set the original data source as the default Data Source
 *
 * @author ZhouXiaoxiang
 * @since 1.0
 */
public class DynamicDataSourceRegister implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ApplicationContextAware {

    private static final String DATASOURCE_BEAN_NAME = "dataSource";

    private Environment environment;

    private ApplicationContext context;

    private static final String SPRING_DATASOURCE_PREFIX = "spring.datasource";

    private static final ConfigurationPropertyName SPRING_DATASOURCE = ConfigurationPropertyName
            .of(SPRING_DATASOURCE_PREFIX);

    private static final Bindable<Map<String, String>> STRING_STRING_MAP = Bindable
            .mapOf(String.class, String.class);

    private EncryptPropertyResolver encryptPropertyResolver;

    /**
     * to resolve the result
     */
    private BindHandler bindHandler = new BindHandler() {
        @Override
        public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            if(encryptPropertyResolver != null && target.getType().getType().equals(String.class)
                && encryptPropertyResolver.supportResolve(name.toString(), result.toString())) {
                result = encryptPropertyResolver.resolve(result.toString());
                return result;
            }
            return result;
        }
    };


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        // do nothing without dataSource
        if(!registry.containsBeanDefinition(DATASOURCE_BEAN_NAME)) {
            return;
        }

        // get the registered data source
        BeanDefinition beanDefinition = registry.getBeanDefinition(DATASOURCE_BEAN_NAME);

        if(beanDefinition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abstractBeanDefinition = (AbstractBeanDefinition) beanDefinition;
            // Remove and add the data source as the default Data Source
            registry.removeBeanDefinition(DATASOURCE_BEAN_NAME);

            registry.registerBeanDefinition("defaultDataSource", abstractBeanDefinition);

            // Create a new dynamic data source and register it as a system data source
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultDynamicDataSource.class);
            beanDefinitionBuilder.addPropertyReference("defaultTargetDataSource", "defaultDataSource");
            beanDefinitionBuilder.setPrimary(true);

            // initialized routing list
            Map<Object, Object> targetDataSources = new HashMap<>();
            // Initialize the configured dataSource list, under 'spring.datasource' and start with 'ds' is the dataSource
            addConfigDataSource(targetDataSources);

            beanDefinitionBuilder.addPropertyValue("targetDataSources", targetDataSources);

            registry.registerBeanDefinition(DATASOURCE_BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
        }

    }

    private void addConfigDataSource(Map<Object, Object> targetDataSources) {

        Binder binder = Binder.get(environment);
        Map<String, String> datasourceMap = binder.bind(SPRING_DATASOURCE, STRING_STRING_MAP)
                .orElseGet(Collections::emptyMap);

        List<String> dataSourceNames = new ArrayList<>();

        datasourceMap.forEach((k, v) -> {
            if(k.startsWith("ds")) {
                String dataSourceName = k.split("\\.")[0];

                if(!dataSourceNames.contains(dataSourceName)) {
                    dataSourceNames.add(dataSourceName);
                }
            }
        });

        /**
         * try to init bean if exist {@link EncryptPropertyResolver} bean
         * bean will be instantiated in advance
         */
        if(!dataSourceNames.isEmpty()) {
            try {
                encryptPropertyResolver = context.getBean(EncryptPropertyResolver.class);
            } catch (BeansException e) {
                // do nothing
            }
        }

        dataSourceNames.forEach(dataSourceName -> targetDataSources.put(dataSourceName, buildDataSource(dataSourceName)));

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    private DataSource buildDataSource(String dataSourceName) {
        // according to the need for encryption and resolver, construct different results
        BindResult<DataSourceProperties> bindResult;
        if(encryptPropertyResolver == null) {
            bindResult = Binder.get(environment).bind(SPRING_DATASOURCE_PREFIX + "." + dataSourceName, DataSourceProperties.class);
        } else {
            bindResult = Binder.get(environment).bind(SPRING_DATASOURCE_PREFIX + "." + dataSourceName, Bindable.of(DataSourceProperties.class), bindHandler);
        }

        DataSource dataSource = bindResult.get().initializeDataSourceBuilder().build();

        // if datasource type is HikariDataSource, bind properties to hikari
        if(hasClass("com.zaxxer.hikari.HikariDataSource") && dataSource instanceof HikariDataSource) {
            BindHandler bindHandlerTmp = new IgnoreTopLevelConverterNotFoundBindHandler();
            Bindable<?> target = Bindable.of(HikariDataSource.class).withExistingValue((HikariDataSource) dataSource);
            Binder.get(environment).bind(SPRING_DATASOURCE_PREFIX + "." + dataSourceName + ".hikari", target, bindHandlerTmp);
        }
        return dataSource;
    }

    private boolean hasClass(String className) {
        ClassLoader classLoader = context.getClassLoader();
        boolean hasClassHikariDataSource;
        try {
            forName(className, classLoader);
            hasClassHikariDataSource = true;
        }
        catch (Exception ex) {
            hasClassHikariDataSource = false;
        }

        return hasClassHikariDataSource;
    }

    private static Class<?> forName(String className, ClassLoader classLoader)
            throws ClassNotFoundException {
        if (classLoader != null) {
            return classLoader.loadClass(className);
        }
        return Class.forName(className);
    }

}
