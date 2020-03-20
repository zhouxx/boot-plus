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
package com.alilitech.swagger.web.model;

import java.util.List;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DocInterface {

    private String summary;

    private String httpMethod;

    private String path;

    private List<DocParameter> parameters;

    private List<DocResponse> responses;

    private String responseType;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<DocParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<DocParameter> parameters) {
        this.parameters = parameters;
    }

    public List<DocResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<DocResponse> responses) {
        this.responses = responses;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
}
