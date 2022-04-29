/*
 *    Copyright 2017-present the original author or authors.
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
package com.alilitech.web;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 2.0.0
 */
public class CommonBody {

    protected int status;

    protected String message;

    protected List<?> errors;

    public CommonBody(String message) {
        this(HttpStatus.BAD_REQUEST.value(), message);
    }

    public CommonBody(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public CommonBody(int status, List<?> errors) {
        this.status = status;
        this.errors = errors;
    }

    public CommonBody(List<?> errors) {
        this(HttpStatus.BAD_REQUEST.value(), errors);
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<?> getErrors() {
        return errors;
    }
}
