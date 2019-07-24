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
package com.alili.biz.security.domain;

import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class BizResource implements Serializable {

    private RequestMatcher requestMatcher;

    protected Collection<String> roles = new ArrayList<>();

    public BizResource(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }
}
