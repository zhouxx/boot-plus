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
package com.alilitech.mybatis.jpa.criteria;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class RenderContext {

    private StringBuilder scriptBuilder = new StringBuilder();

    private AtomicInteger paramIndex = new AtomicInteger(0);

    private String paramPrefixPrefix = "_parameter.paramValues.";

    private String paramPrefix = "param";

    private Map<String, Object> paramValues = new ConcurrentHashMap<>();

    public RenderContext() {
    }

    public RenderContext(String paramPrefix) {
        this.paramPrefix = paramPrefix;
    }

    public Integer getParamIndex() {
        return paramIndex.get();
    }

    public Integer getParamIndexAndIncrement() {
        return paramIndex.getAndIncrement();
    }

    public String getScript() {
        return scriptBuilder.toString();
    }

    public String getParamPrefixPrefix() {
        return paramPrefixPrefix;
    }

    public String getParamPrefix() {
        return paramPrefix;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
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
