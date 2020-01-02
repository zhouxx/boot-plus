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

import com.alilitech.integration.jpa.domain.Page;
import com.alilitech.integration.jpa.domain.Pageable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
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

    public ParameterDefinition() {
        annotations = new ArrayList<>();
    }

    public ParameterDefinition(int index, Class<?> parameterClass) {
        this();
        this.index = index;
        if(Pageable.class.isAssignableFrom(parameterClass)) {
            this.parameterClass = Page.class;
        } else {
            this.parameterClass = parameterClass;
        }
    }

    public ParameterDefinition(int index, Class<?> parameterClass, List<Annotation> annotations) {
        this(index, parameterClass);
        this.annotations = annotations;
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
}
