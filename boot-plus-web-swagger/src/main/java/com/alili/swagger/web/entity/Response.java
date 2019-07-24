package com.alili.swagger.web.entity;

public class Response {

    private String description;

    private ResponseSchema schema;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResponseSchema getSchema() {
        return schema;
    }

    public void setSchema(ResponseSchema schema) {
        this.schema = schema;
    }
}
