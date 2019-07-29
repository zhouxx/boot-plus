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
package com.alili.swagger.web.entity;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class SwaggerEntity {

    private String swagger;

    private Info info;

    private String basePath;

    private List<Tag> tags;

    private Map<String, Map<String, Path>> paths;

    private Map<String, Definition> definitions;

    public String getSwagger() {
        return swagger;
    }

    public void setSwagger(String swagger) {
        this.swagger = swagger;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Map<String, Map<String, Path>> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, Map<String, Path>> paths) {
        this.paths = paths;
    }

    public Map<String, Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, Definition> definitions) {
        this.definitions = definitions;
    }
}
