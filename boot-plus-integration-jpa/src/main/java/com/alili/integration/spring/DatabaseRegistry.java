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
package com.alili.integration.spring;


import com.alili.integration.jpa.DatabaseType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DatabaseRegistry {

    private final List<DatabaseRegistration> databaseRegistrations = new ArrayList<>();

    public DatabaseRegistration addDatabase(DatabaseType databaseType) {
        DatabaseRegistration databaseRegistration = new DatabaseRegistration(databaseType);
        databaseRegistrations.add(databaseRegistration);
        return databaseRegistration;
    }

    public List<DatabaseRegistration> getDatabaseRegistrations() {
        return databaseRegistrations;
    }

}
