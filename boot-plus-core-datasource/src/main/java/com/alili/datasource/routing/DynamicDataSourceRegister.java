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
package com.alili.datasource.routing;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.*;


/**
 * 动态注册DefaultDynamicDataSource数据源作为主数据源，并把原数据源置为defaultDataSource
 *
 * @author ZhouXiaoxiang
 * @since 1.0
 */
public class DynamicDataSourceRegister implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ApplicationContextAware {

    private static final String DATASOURCE_BEAN_NAME = "dataSource";

    private Environment environment;

    private ApplicationContext context;

    private final ConfigurationPropertyName SPRING_DATASOURCE = ConfigurationPropertyName
            .of("spring.datasource");

    private static final Bindable<Map<String, String>> STRING_STRING_MAP = Bindable
            .mapOf(String.class, String.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        //没有数据源返回
        if(!registry.containsBeanDefinition(DATASOURCE_BEAN_NAME)) {
            return;
        }

        //取得已经注册的数据源
        BeanDefinition beanDefinition = registry.getBeanDefinition(DATASOURCE_BEAN_NAME);


        if(beanDefinition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abstractBeanDefinition = (AbstractBeanDefinition) beanDefinition;
            //移除并添加为defaultDataSource的数据源
            registry.removeBeanDefinition(DATASOURCE_BEAN_NAME);

            registry.registerBeanDefinition("defaultDataSource", abstractBeanDefinition);

            //新建动态数据源，并注册为系统数据源
            GenericBeanDefinition dynamicBeanDefinition = new GenericBeanDefinition();
            dynamicBeanDefinition.setBeanClass(DefaultDynamicDataSource.class);
            dynamicBeanDefinition.setSynthetic(true);
            dynamicBeanDefinition.setPrimary(true);

            //初始化的路由列表
            Map<Object, Object> targetDataSources = new HashMap<>();
            //初始化配置的数据源列表，在spring.datasource下并以ds开头的就是数据源
            addConfigDataSource(targetDataSources);

            MutablePropertyValues mpv = dynamicBeanDefinition.getPropertyValues();
            //mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
            mpv.addPropertyValue("targetDataSources", targetDataSources);
            registry.registerBeanDefinition(DATASOURCE_BEAN_NAME, dynamicBeanDefinition);
        }

    }

    private void addConfigDataSource(Map<Object, Object> targetDataSources) {

        Binder binder = Binder.get(environment);
        Map<String, String> datasourceMap = binder.bind(SPRING_DATASOURCE, STRING_STRING_MAP)
                .orElseGet(Collections::emptyMap);

       /* List<String> list = Binder.get(environment).bind("spring.datasource", String[].class)
                .map(Arrays::asList).orElse(Collections.emptyList());

        String[] excludes = environment.getProperty("spring.datasource", String[].class);*/

        List<String> dataSourceNames = new ArrayList<>();

        datasourceMap.forEach((k, v) -> {
            if(k.startsWith("ds")) {
                String dataSourceName = k.split("\\.")[0];

                if(!dataSourceNames.contains(dataSourceName)) {
                    dataSourceNames.add(dataSourceName);
                }
            }
        });

        dataSourceNames.forEach(dataSourceName -> {
            targetDataSources.put(dataSourceName, buildDataSource(dataSourceName));
        });

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

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
        BindResult<DataSourceProperties> bindResult = Binder.get(environment).bind("spring.datasource." + dataSourceName, DataSourceProperties.class);
        DataSource dataSource = bindResult.get().initializeDataSourceBuilder().build();

        //if datasource type is HikariDataSource, bind properties to hikari
        if(hasClass("com.zaxxer.hikari.HikariDataSource")) {
            if(dataSource instanceof HikariDataSource) {
                BindHandler bindHandler = new IgnoreTopLevelConverterNotFoundBindHandler();
                Bindable<?> target = Bindable.of(HikariDataSource.class).withExistingValue((HikariDataSource) dataSource);
                Binder.get(environment).bind("spring.datasource." + dataSourceName + ".hikari", target, bindHandler);
            }
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
        catch (Throwable ex) {
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
