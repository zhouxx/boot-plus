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
public class MethodDefinition {

    private String scope = "public";

    private boolean hasBody = true;

    private String returnValue = "void";

    private String methodName;

    private List<ParameterDefinition> parameters = new ArrayList<>();

    private List<String> bodyLines = new ArrayList<>();

    private List<String> annotationList;

    private Set<String> importList;

    public MethodDefinition(String methodName) {
        this.methodName = methodName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isHasBody() {
        return hasBody;
    }

    public MethodDefinition setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
        return this;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodDefinition setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public MethodDefinition setReturnValueClass(Class returnValueClass) {
        this.setReturnValue(returnValueClass.getSimpleName());
        this.addImport(returnValueClass);
        return this;
    }

    public List<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDefinition> parameters) {
        this.parameters = parameters;
    }

    public MethodDefinition addImport(String importString) {
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importString);
        return this;
    }

    public MethodDefinition addImport(Class importClass) {
        if(importClass.getTypeName().startsWith("java.lang")) {
            return this;
        }
        if(importList == null) {
            this.importList = new TreeSet<>();
        }
        importList.add(importClass.getName());
        return this;
    }

    public MethodDefinition addAnnotation(String annotationName) {
        if(annotationList == null) {
            this.annotationList = new ArrayList<>();
        }
        annotationList.add(annotationName);

        return this;
    }

    public MethodDefinition addAnnotation(Class<?> clazz) {
        return this.addAnnotation(clazz.getSimpleName()).addImport(clazz);
    }

    public MethodDefinition addParameter(ParameterDefinition parameterDefinition) {
        parameters.add(parameterDefinition);

        if(this.importList == null) {
            this.importList = new TreeSet<>();
        }
        if(parameterDefinition.getImportList() != null) {
            this.importList.addAll(parameterDefinition.getImportList());
        }
        return this;
    }

    public MethodDefinition addBodyLines(String line, Class ...importClass) {
        bodyLines.add(line);
        if(importClass != null && importClass.length > 0) {
            for (Class aClass : importClass) {
                this.addImport(aClass);
            }
        }
        return this;
    }

    public Set<String> getImportList() {
        return importList;
    }

    public List<String> getBodyLines() {
        return bodyLines;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }
}
