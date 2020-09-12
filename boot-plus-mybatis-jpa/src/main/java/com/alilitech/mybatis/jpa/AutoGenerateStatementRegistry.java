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
package com.alilitech.mybatis.jpa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.2
 */
public class AutoGenerateStatementRegistry {

    private final List<String> statements = new ArrayList<>();

    private static AutoGenerateStatementRegistry registry = new AutoGenerateStatementRegistry();

    public static AutoGenerateStatementRegistry getInstance() {
        return registry;
    }

    public void addStatement(String statement) {
        statements.add(statement);
    }

    public boolean contains(String statement) {
        return statements.contains(statement);
    }

}
