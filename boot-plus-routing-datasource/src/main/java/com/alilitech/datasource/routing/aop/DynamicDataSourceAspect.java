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
package com.alilitech.datasource.routing.aop;

import com.alilitech.datasource.routing.DataSourceContextHolder;
import com.alilitech.datasource.routing.annotation.DynamicSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Aspect
public class DynamicDataSourceAspect implements Ordered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Matches methods annotated with {@link DynamicSource}.
     */
    @Pointcut("@annotation(com.alilitech.datasource.routing.annotation.DynamicSource)")
    public void annotatedMethod() {
    }

    /**
     * Matches methods declared inside a class annotated with {@link DynamicSource}.
     */
    @Pointcut("@within(com.alilitech.datasource.routing.annotation.DynamicSource)")
    public void annotatedClass() {
    }

    /**
     * The annotation is bound directly as an advice argument, so the aspect does not have to
     * look it up through reflection on every invocation.
     */
    @Around("annotatedMethod() && @annotation(dynamicSource)")
    public Object aroundAnnotatedMethod(ProceedingJoinPoint point, DynamicSource dynamicSource) throws Throwable {
        return switchDataSource(point, dynamicSource);
    }

    /**
     * A method level annotation takes precedence over a class level one, so the class level
     * advice excludes methods that carry their own annotation, otherwise such a method would
     * be advised twice.
     */
    @Around("annotatedClass() && !annotatedMethod() && @within(dynamicSource)")
    public Object aroundAnnotatedClass(ProceedingJoinPoint point, DynamicSource dynamicSource) throws Throwable {
        return switchDataSource(point, dynamicSource);
    }

    private Object switchDataSource(ProceedingJoinPoint point, DynamicSource annotation) throws Throwable {

        // if the data source name is resolved at runtime, it is owned by the caller: leave it untouched
        if (annotation.runtime()) {
            return point.proceed();
        }

        // take the data source name declared on the annotation
        String dataSourceName = annotation.value();

        // switch data source
        DataSourceContextHolder.setDataSource(dataSourceName);

        if(logger.isDebugEnabled()) {
            logger.debug("current transaction use datasource：{}", dataSourceName);
        }

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSource();
        }
    }

    // aop cuts in before the transaction
    @Override
    public int getOrder() {
        return -1;
    }
}
