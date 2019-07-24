package com.alili.swagger.web.model;

import java.util.List;

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
