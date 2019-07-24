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
package com.alili.swagger.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "swagger", ignoreUnknownFields = false)
public class SwaggerProperties {

    private String title = "Application API";

    private String description = "API documentation";

    private String version = "1.0.0";

    private String termsOfServiceUrl;

    private String contactName;

    private String contactUrl;

    private String contactEmail;

    private String license;

    private String licenseUrl;

    private String defaultIncludePattern = "/.*";

    private String apiHost;

    private List<GlobalParameter> global;

    private List<Authorized> authorized;

    private String authorizedIncludePattern;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getDefaultIncludePattern() {
        return defaultIncludePattern;
    }

    public void setDefaultIncludePattern(String defaultIncludePattern) {
        this.defaultIncludePattern = defaultIncludePattern;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public List<GlobalParameter> getGlobal() {
        return global;
    }

    public void setGlobal(List<GlobalParameter> global) {
        this.global = global;
    }

    public List<Authorized> getAuthorized() {
        return authorized;
    }

    public void setAuthorized(List<Authorized> authorized) {
        this.authorized = authorized;
    }

    public String getAuthorizedIncludePattern() {
        return authorizedIncludePattern;
    }

    public void setAuthorizedIncludePattern(String authorizedIncludePattern) {
        this.authorizedIncludePattern = authorizedIncludePattern;
    }
}
