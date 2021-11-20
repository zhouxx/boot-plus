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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class FieldDefinition {

    private String scope = "private";

    private String type;

    private String name;

    //初始化值
    private String initializationString;

    //多个注解
    private List<String> annotationList;

    //多行注释
    private List<String> comments;

    private Set<String> importList;

    public FieldDefinition(String name) {
        this.name = name;
    }

    public FieldDefinition(Class<?> type, String name) {
        this(type.getSimpleName(), name);
        this.addImport(type);
    }

    public FieldDefinition(String type, String name) {
        this(type, name, null);
    }

    public FieldDefinition(String type, String name, String initializationString) {
        this.name = name;
        this.type = type;
        this.initializationString = initializationString;
    }

    public String getScope() {
        return scope;
    }

    public FieldDefinition setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getType() {
        return type;
    }

    public FieldDefinition setTypeClass(Class<?> typeClass) {
        this.type = typeClass.getSimpleName();
        this.addImport(typeClass);
        return this;
    }

    public String getName() {
        return name;
    }

    public FieldDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getInitializationString() {
        return initializationString;
    }

    public FieldDefinition setInitializationString(String initializationString) {
        this.initializationString = initializationString;
        return this;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }

    public List<String> getComments() {
        return comments;
    }

    public Set<String> getImportList() {
        return importList;
    }

    public FieldDefinition addImport(String importString) {
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importString);
        return this;
    }

    public FieldDefinition addImport(Class<?> importClass) {
        if(importClass.getTypeName().startsWith("java.lang")) {
            return this;
        }
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importClass.getName());
        return this;
    }

    public FieldDefinition addAnnotation(String annotationName) {
        if(annotationList == null) {
            this.annotationList = new ArrayList<>();
        }
        annotationList.add(annotationName);

        return this;
    }

    public FieldDefinition addAnnotation(AnnotationDefinition annotationDefinition) {
        this.addAnnotation(annotationDefinition.toString());
        annotationDefinition.getImportList().forEach(str -> {
            this.addImport(str);
        });
        return this;
    }

    public FieldDefinition addAnnotation(Class<?> clazz) {
        return this.addAnnotation(clazz.getSimpleName()).addImport(clazz);
    }

    public FieldDefinition addComment(String comment) {
        if(comments == null) {
            this.comments = new ArrayList<>();
        }
        comments.add(comment);
        return this;
    }
}
