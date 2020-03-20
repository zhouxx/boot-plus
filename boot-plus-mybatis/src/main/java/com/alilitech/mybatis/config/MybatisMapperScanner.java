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
package com.alilitech.mybatis.config;

import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notNull;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Component
public class MybatisMapperScanner implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, InitializingBean, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(MybatisMapperScanner.class);

    private ResourceLoader resourceLoader;

    private MapperScanProperties mapperScanProperties;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

        // this check is needed in Spring 3.1
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        Class<? extends Annotation> annotationClass = mapperScanProperties.getAnnotationClass();
        if (!Annotation.class.equals(annotationClass)) {
            scanner.setAnnotationClass(annotationClass);
        }

        Class<?> markerInterface = mapperScanProperties.getMarkerInterface();
        if (!Class.class.equals(markerInterface)) {
            scanner.setMarkerInterface(markerInterface);
        }

        Class<? extends BeanNameGenerator> generatorClass = mapperScanProperties.getNameGenerator();
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            scanner.setBeanNameGenerator(BeanUtils.instantiateClass(generatorClass));
        }

        /*Class<? extends MapperFactoryBean> mapperFactoryBeanClass = mapperScanProperties.get
        if (!MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
            scanner.setMapperFactoryBean(BeanUtils.instantiateClass(mapperFactoryBeanClass));
        }*/

        scanner.setSqlSessionTemplateBeanName(mapperScanProperties.getSqlSessionTemplateRef());
        scanner.setSqlSessionFactoryBeanName(mapperScanProperties.getSqlSessionFactoryRef());

        List<String> basePackages = new ArrayList<>();
        if(mapperScanProperties.getBasePackages() != null && mapperScanProperties.getBasePackages().length > 0) {
            for (String pkg : mapperScanProperties.getBasePackages()) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }
        }
        for (Class<?> clazz : mapperScanProperties.getBasePackageClasses()) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        scanner.registerFilters();
        //scanner.addExcludeFilter(new AnnotationTypeFilter(NoRepositoryBean.class));

        if(!CollectionUtils.isEmpty(basePackages)) {
            scanner.doScan(StringUtils.toStringArray(basePackages));
        }

        if(CollectionUtils.isEmpty(basePackages)) {
            logger.warn("Mybatis mapper scanner init, scan packages count: {}", 0);
        } else {
            logger.info("Mybatis mapper scanner init, scan packages count: {}", basePackages.size());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * init base packages
     * @param environment
     */
    private void init(Environment environment) {
        BindResult<String> bindResult = Binder.get(environment)
                .bind("mybatis.mapper-scan.base-packages", String.class);

        if(!bindResult.isBound()) {
            logger.warn("mybatis.mapper-scan.base-packages not found!");
            return;
        }
        mapperScanProperties.setBasePackages(StringUtils.tokenizeToStringArray(bindResult.get(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.mapperScanProperties, "Property 'mapperScanProperties' is required");
    }

    @Override
    public void setEnvironment(Environment environment) {
        mapperScanProperties = new MapperScanProperties();
        init(environment);
    }
}
