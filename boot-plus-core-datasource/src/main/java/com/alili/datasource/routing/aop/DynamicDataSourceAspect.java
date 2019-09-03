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
package com.alili.datasource.routing.aop;

import com.alili.datasource.routing.DefaultDynamicDataSource;
import com.alili.datasource.routing.DataSourceContextHolder;
import com.alili.datasource.routing.annotation.DynamicSource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Aspect
@Component
public class DynamicDataSourceAspect implements Ordered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataSource primaryDataSource;

    @Before("@annotation(com.alili.datasource.routing.annotation.DynamicSource) || @within(com.alili.datasource.routing.annotation.DynamicSource)")
    public void beforeSwitchDataSource(JoinPoint point) throws NoSuchMethodException {

        //获得当前访问的class
        Class<?> clazz = point.getTarget().getClass();

        //获得访问的方法名
        String methodName = point.getSignature().getName();
        //得到方法的参数的类型
        Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
        String dataSourceName = null;
        // 得到访问的方法对象
        Method method = clazz.getMethod(methodName, argClass);

        // 判断是否存在@DynamicSource注解
        if (method.isAnnotationPresent(DynamicSource.class) || clazz.isAnnotationPresent(DynamicSource.class)) {
            DynamicSource annotation = method.getAnnotation(DynamicSource.class);

            if(annotation == null) {
                annotation = clazz.getAnnotation(DynamicSource.class);
            }

            //若是运行期间指定，则拿指定的数据源名称
            if(annotation.runtime()) {
                dataSourceName = DataSourceContextHolder.getDataSource();
            } else {
                // 取出注解中的数据源名
                dataSourceName = annotation.value();
            }

            DefaultDynamicDataSource defaultDynamicDataSource = (DefaultDynamicDataSource)primaryDataSource;

            /*if(!dynamicDataSource.contains(dataSourceName)) {
                logger.debug("Create DataSource [{}]", dataSourceName);
                DataSource dataSource = dynamicDataSource.createDataSource("com.mysql.jdbc.Driver", "jdbc:mysql://10.88.41.200:3306/test", "root", "123456");
                dynamicDataSource.addResolvedDataSource(dataSourceName, dataSource);
            }*/
        }

        // 切换数据源
        DataSourceContextHolder.setDataSource(dataSourceName);

        if(dataSourceName != null) {
            logger.debug("current tansaction use datasource：{}", dataSourceName);
        }
    }

    @After("@annotation(com.alili.datasource.routing.annotation.DynamicSource) || @within(com.alili.datasource.routing.annotation.DynamicSource)")
    public void afterSwitchDataSource(JoinPoint point){
        DataSourceContextHolder.clearDataSource();
    }

    //AOP在事务前面切入
    @Override
    public int getOrder() {
        return -1;
    }
}
