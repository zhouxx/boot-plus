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
package com.alilitech.generate.definition;

import com.alilitech.mybatis.jpa.parameter.GenerationType;

import java.util.*;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class AnnotationDefinition {

    private Set<String> importList;

    private Class clazz;

    private Map<String, String> properties;

    public AnnotationDefinition() {
    }

    public AnnotationDefinition(Class clazz) {
        this.clazz = clazz;
        this.addImport(clazz.getName());
    }

    public AnnotationDefinition setClazz(Class clazz) {
        this.clazz = clazz;
        this.addImport(clazz.getName());
        return this;
    }

    public AnnotationDefinition addProperty(String propertyName, Object value) {
        if(properties == null) {
            properties = new LinkedHashMap<>();
        }
        if(propertyName == null || propertyName.trim().equals("")) {
            propertyName = "value";
        }
        if(value instanceof String) {
            properties.put(propertyName, "\"" + value.toString() + "\"");
        } else if(value instanceof Class) {
            Class temp = ((Class)value);
            properties.put(propertyName, temp.getSimpleName() + ".class");
            this.addImport(temp.getName());
        } else if(value.getClass().isEnum()) {
            Enum anEnum = (Enum) value;
            properties.put(propertyName, value.getClass().getSimpleName() + "." + anEnum.name());
            this.addImport(value.getClass().getName());
        } else if(value.getClass().isArray()) {

        } else if(value.getClass().isAnnotation()) {

        }

        return this;
    }

    private AnnotationDefinition addImport(String importString) {
        if(importList == null) {
            importList = new TreeSet<>();
        }
        importList.add(importString);
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Set<String> getImportList() {
        return importList;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(clazz.getSimpleName() + "(");
        List<String> propertyStr = new ArrayList<>();
        properties.forEach((key, value) -> {
            propertyStr.add(key + " = " + value);
        });

        String join = String.join(",", propertyStr);
        buffer.append(join);
        buffer.append(")");

        return buffer.toString();
    }

    public static void main(String[] args) {
        GenerationType auto = GenerationType.AUTO;
        System.out.println(auto.getClass());
    }
}
