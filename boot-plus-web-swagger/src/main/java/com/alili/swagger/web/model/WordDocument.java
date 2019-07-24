package com.alili.swagger.web.model;

import java.util.List;

public class WordDocument {
    private String title;
    private String version;

    List<DocInterface> interfaces;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<DocInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<DocInterface> interfaces) {
        this.interfaces = interfaces;
    }
}
