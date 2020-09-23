/*
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
package com.alilitech.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class BizUser implements Serializable {

    /**
     * user id
     */
    @JsonIgnore
    private String userId;

    /**
     * username: login key
     */
    private String username;

    /**
     * password
     */
    @JsonIgnore
    private String password;

    /**
     * name: display name
     */
    private String name;

    /**
     * role codes
     */
    @JsonIgnore
    protected Collection<String> roles;

    public BizUser() {

    }

    public BizUser(String username, Collection<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public BizUser(String username, String password, Collection<String> roles) {
        this(username, roles);
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }
}
