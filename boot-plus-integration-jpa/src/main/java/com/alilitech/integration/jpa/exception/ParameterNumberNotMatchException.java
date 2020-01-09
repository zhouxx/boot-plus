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
package com.alilitech.integration.jpa.exception;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class ParameterNumberNotMatchException extends RuntimeException {

    private String namespace;

    private String statement;

    private Integer expect;

    private Integer actual;

    public ParameterNumberNotMatchException(String namespace, String statement, Integer expect, Integer actual) {
        this.namespace = namespace;
        this.statement = statement;
        this.expect = expect;
        this.actual = actual;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getStatement() {
        return statement;
    }

    public Integer getExpect() {
        return expect;
    }

    public Integer getActual() {
        return actual;
    }
}
