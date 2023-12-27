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
package com.alilitech.web.valid;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ValidException like hibernate validation
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ValidException extends RuntimeException {

    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    /**
     * 除validatedValue外的其它占位符
     */
    private final Map<String, Object> placeholderMap = new ConcurrentHashMap<>();

    /**
     * 每个message国际化会自带一个validatedValue占位符
     */
    private Object validatedValue = -1;

    /**
     * 字段路径，用于给接口或前端展示
     */
    private String propertyPath;

    public ValidException() {
    }

    public ValidException(String message) {
        super(message);
    }

    public ValidException addPlaceholder(String key, Object value) {
        placeholderMap.put(key, value);
        return this;
    }

    public ValidException httpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ValidException validatedValue(Object validatedValue) {
        this.validatedValue = validatedValue;
        placeholderMap.put("validatedValue",validatedValue);
        return this;
    }

    public ValidException propertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Map<String, Object> getPlaceholderMap() {
        return placeholderMap;
    }

    public Object getValidatedValue() {
        return validatedValue;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
}
