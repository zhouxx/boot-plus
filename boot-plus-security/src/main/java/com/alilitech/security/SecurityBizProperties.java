/*
 *    Copyright 2017-2022 the original author or authors.
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
package com.alilitech.security;

import com.alilitech.security.domain.BizUser;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConfigurationProperties(prefix = "security.token")
public class SecurityBizProperties {

    /**
     * authentication uri prefix
     */
    private String authenticationPrefix = "/authentication";

    /**
     * ignore url patterns that not managed by security
     */
    private List<RequestMatcher> ignorePatterns;

    /**
     * permit all url patterns that not authorization
     */
    private List<RequestMatcher> permitAllPatterns;

    /**
     * permit all username s that not authorization
     */
    private List<String> permitAllUserNames;

    /**
     * biz user Class Name
     */
    private String bizUserClassName = BizUser.class.getName();

    /**
     * token type
     */
    private TokenType type = TokenType.JWT;

    /**
     * jwt properties
     */
    private JWT jwt = new JWT();

    public String getAuthenticationPrefix() {
        return authenticationPrefix;
    }

    public void setAuthenticationPrefix(String authenticationPrefix) {
        this.authenticationPrefix = authenticationPrefix;
    }

    public List<RequestMatcher> getIgnorePatterns() {
        return ignorePatterns;
    }

    public void setIgnorePatterns(List<RequestMatcher> ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    public List<RequestMatcher> getPermitAllPatterns() {
        if(this.permitAllPatterns == null) {
            return Collections.emptyList();
        }
        return permitAllPatterns;
    }

    public void setPermitAllPatterns(List<RequestMatcher> permitAllPatterns) {
        this.permitAllPatterns = permitAllPatterns;
    }

    public List<String> getPermitAllUserNames() {
        if(StringUtils.isEmpty(this.permitAllUserNames)) {
            return Collections.emptyList();
        }
        return permitAllUserNames;
    }

    public void setPermitAllUserNames(List<String> permitAllUserNames) {
        this.permitAllUserNames = permitAllUserNames;
    }

    public String getBizUserClassName() {
        return bizUserClassName;
    }

    public void setBizUserClassName(String bizUserClassName) {
        this.bizUserClassName = bizUserClassName;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public JWT getJwt() {
        return jwt;
    }

    public void setJwt(JWT jwt) {
        this.jwt = jwt;
    }

    public static class RequestMatcher {
        private String pattern;
        private HttpMethod method = HttpMethod.GET;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }
    }

    public static class JWT {
        /**
         * the secret to use in the verify or signing instance
         */
        private String secret = "auth_chm";

        /**
         * token validity period(unit:minute)
         */
        private Long timeoutMin = 7*24*60L;

        /**
         * seconds from the expiration date to refresh token
         * when refreshSeconds is zero, not auto refresh
         */
        private Long refreshSeconds = 24*3600L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getTimeoutMin() {
            return timeoutMin;
        }

        public void setTimeoutMin(Long timeoutMin) {
            this.timeoutMin = timeoutMin;
        }

        public Long getRefreshSeconds() {
            return refreshSeconds;
        }

        public void setRefreshSeconds(Long refreshSeconds) {
            this.refreshSeconds = refreshSeconds;
        }
    }
}
