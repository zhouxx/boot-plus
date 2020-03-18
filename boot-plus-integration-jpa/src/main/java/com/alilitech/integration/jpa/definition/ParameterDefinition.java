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
package com.alilitech.integration.jpa.definition;

import com.alilitech.integration.jpa.criteria.Specification;
import com.alilitech.integration.jpa.domain.Sort;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ParameterDefinition {

    private int index;

    private Class<?> parameterClass;

    private List<Annotation> annotations;

    private String name;

    public ParameterDefinition() {
        annotations = new ArrayList<>();
    }

    public ParameterDefinition(int index, Parameter parameter) {
        this.index = index;
        parameterClass = parameter.getType();
        annotations = Arrays.asList(parameter.getAnnotations());
        this.name = parameter.getName();
    }

    public ParameterDefinition(int index, Class<?> parameterClass) {
        this();
        this.index = index;
        this.parameterClass = parameterClass;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(Class<?> parameterClass) {
        this.parameterClass = parameterClass;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPage() {
        if(RowBounds.class.isAssignableFrom(parameterClass)) {
            return true;
        }
        return false;
    }

    public boolean isSpecification() {
        if(Specification.class.isAssignableFrom(parameterClass)) {
            return true;
        }
        return false;
    }

    public boolean isSort() {
        if(parameterClass.equals(Sort.class)) {
            return true;
        }
        return false;
    }
}
