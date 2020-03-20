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

import org.springframework.beans.factory.support.BeanNameGenerator;

import java.lang.annotation.Annotation;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MapperScanProperties {

    private String[] basePackages;

    private Class<?>[] basePackageClasses = new Class[]{};

    private Class<?> markerInterface = Class.class;

    private String sqlSessionTemplateRef = "";

    private String sqlSessionFactoryRef = "";

    private Class<? extends Annotation> annotationClass = Annotation.class;

    private Class<? extends BeanNameGenerator> nameGenerator = BeanNameGenerator.class;

    public String[] getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

    public Class<?>[] getBasePackageClasses() {
        return basePackageClasses;
    }

    public void setBasePackageClasses(Class<?>[] basePackageClasses) {
        this.basePackageClasses = basePackageClasses;
    }

    public Class<?> getMarkerInterface() {
        return markerInterface;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public String getSqlSessionTemplateRef() {
        return sqlSessionTemplateRef;
    }

    public void setSqlSessionTemplateRef(String sqlSessionTemplateRef) {
        this.sqlSessionTemplateRef = sqlSessionTemplateRef;
    }

    public String getSqlSessionFactoryRef() {
        return sqlSessionFactoryRef;
    }

    public void setSqlSessionFactoryRef(String sqlSessionFactoryRef) {
        this.sqlSessionFactoryRef = sqlSessionFactoryRef;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public Class<? extends BeanNameGenerator> getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(Class<? extends BeanNameGenerator> nameGenerator) {
        this.nameGenerator = nameGenerator;
    }
}
