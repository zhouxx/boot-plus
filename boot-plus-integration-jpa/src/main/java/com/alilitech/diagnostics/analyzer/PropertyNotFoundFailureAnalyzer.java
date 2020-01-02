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
package com.alilitech.diagnostics.analyzer;

import com.alilitech.integration.jpa.exception.PropertyNotFoundException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PropertyNotFoundFailureAnalyzer extends AbstractFailureAnalyzer<PropertyNotFoundException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, PropertyNotFoundException cause) {
        return new FailureAnalysis(getDescription(cause), "Check the statement: " + cause.getStatement(), cause);
    }

    private String getDescription(PropertyNotFoundException ex) {
        StringWriter description = new StringWriter();
        PrintWriter printer = new PrintWriter(description);
        printer.printf(
                "The domain class '%s' could not found a property named '%s'",
                ex.getDomain().getName(), ex.getProperty());
        return description.toString();
    }
}
