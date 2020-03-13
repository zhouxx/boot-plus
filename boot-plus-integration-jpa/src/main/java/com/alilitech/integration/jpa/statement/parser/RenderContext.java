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
package com.alilitech.integration.jpa.statement.parser;

/**
 * render context
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class RenderContext {

    /**
     * the builder to build script
     */
    private StringBuilder scriptBuilder = new StringBuilder();

    /**
     * variable alias
     */
    private String variableAlias;

    /**
     * argument alias
     */
    private String argAlias;

    public RenderContext() {
    }


    public RenderContext(String variableAlias, String argAlias) {
        this.variableAlias = variableAlias;
        this.argAlias = argAlias;
    }

    public String getVariableAlias() {
        return variableAlias;
    }

    public String getArgAlias() {
        return argAlias;
    }

    public String getScript() {
        return scriptBuilder.toString();
    }

    public void clearScript() {
        scriptBuilder = new StringBuilder();
    }

    public void renderString(String render) {
        scriptBuilder.append(render);
    }

    public void renderBlank() {
        scriptBuilder.append(" ");
    }
}
