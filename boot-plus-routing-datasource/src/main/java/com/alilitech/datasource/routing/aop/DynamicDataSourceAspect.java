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
package com.alilitech.datasource.routing.aop;

import com.alilitech.datasource.routing.DataSourceContextHolder;
import com.alilitech.datasource.routing.annotation.DynamicSource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Aspect
public class DynamicDataSourceAspect implements Ordered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Before("@annotation(com.alilitech.datasource.routing.annotation.DynamicSource) || @within(com.alilitech.datasource.routing.annotation.DynamicSource)")
    public void beforeSwitchDataSource(JoinPoint point) throws NoSuchMethodException {

        Class<?> clazz = point.getTarget().getClass();
        String methodName = point.getSignature().getName();
        Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
        // access method object
        Method method = clazz.getMethod(methodName, argClass);

        String dataSourceName = null;
        /**
         * Determine whether there is {@link DynamicSource} annotation
          */
        if (method.isAnnotationPresent(DynamicSource.class) || clazz.isAnnotationPresent(DynamicSource.class)) {
            DynamicSource annotation = method.getAnnotation(DynamicSource.class);

            if(annotation == null) {
                annotation = clazz.getAnnotation(DynamicSource.class);
            }

            // 若是运行期间指定，则拿指定的数据源名称
            if(annotation.runtime()) {
                dataSourceName = DataSourceContextHolder.getDataSource();
            } else {
                // 取出注解中的数据源名
                dataSourceName = annotation.value();
            }

        }

        // switch data source
        DataSourceContextHolder.setDataSource(dataSourceName);

        if(dataSourceName != null) {
            logger.debug("current transaction use datasource：{}", dataSourceName);
        }
    }

    @After("@annotation(com.alilitech.datasource.routing.annotation.DynamicSource) || @within(com.alilitech.datasource.routing.annotation.DynamicSource)")
    public void afterSwitchDataSource(){
        DataSourceContextHolder.clearDataSource();
    }

    // aop cuts in before the transaction
    @Override
    public int getOrder() {
        return -1;
    }
}
