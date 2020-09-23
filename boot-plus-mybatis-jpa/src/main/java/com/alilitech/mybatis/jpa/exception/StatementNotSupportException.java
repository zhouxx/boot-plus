/*
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
package com.alilitech.mybatis.jpa.exception;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class StatementNotSupportException extends RuntimeException {

    private final String namespace;

    private final String statement;

    public StatementNotSupportException(String namespace, String statement) {
        this.namespace = namespace;
        this.statement = statement;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getStatement() {
        return statement;
    }

}
