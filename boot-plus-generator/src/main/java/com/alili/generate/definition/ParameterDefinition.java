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
package com.alili.generate.definition;

import java.util.*;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ParameterDefinition {

    private String type;

    private String name;

    private Set<String> importList;

    private List<String> annotationList;

    public ParameterDefinition(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public ParameterDefinition setType(String type) {
        this.type = type;
        return this;
    }

    public ParameterDefinition setTypeClass(Class typeClass) {
        this.type = typeClass.getSimpleName();
        this.addImport(typeClass);
        return this;
    }

    public ParameterDefinition addImport(String importString) {
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importString);
        return this;
    }

    public ParameterDefinition addImport(Class importClass) {
        if(importClass.getTypeName().startsWith("java.lang")) {
            return this;
        }
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importClass.getName());
        return this;
    }

    public ParameterDefinition addAnnotation(String annotationName) {
        if(annotationList == null) {
            this.annotationList = new ArrayList<>();
        }
        annotationList.add(annotationName);

        return this;
    }

    public ParameterDefinition addAnnotation(Class<?> clazz) {
        return this.addAnnotation(clazz.getSimpleName()).addImport(clazz);
    }

    public String getName() {
        return name;
    }

    public ParameterDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public Set<String> getImportList() {
        return importList;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }
}
