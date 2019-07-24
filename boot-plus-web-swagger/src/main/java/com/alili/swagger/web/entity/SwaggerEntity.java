package com.alili.swagger.web.entity;

import java.util.List;
import java.util.Map;

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
