package com.alili.biz.security;

import com.alili.biz.security.domain.BizUser;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "security.token", ignoreUnknownFields = false)
public class SecurityBizProperties {

    /**
     * ignore url patterns that not managed by security
     */
    private String ignorePatterns = "/*.js,/*.css,/*.html,/*.png,/webjars/**,/swagger-resources/**,/v2/**,/actuator,/actuator/**";

    /**
     * permit all url patterns that not authorization
     */
    private String permitAllPatterns;

    /**
     * permit all username s that not authorization
     */
    private String permitAllUserNames;


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

    public String getIgnorePatterns() {
        return ignorePatterns;
    }

    public void setIgnorePatterns(String ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    public String getPermitAllPatterns() {
        return permitAllPatterns;
    }

    public void setPermitAllPatterns(String permitAllPatterns) {
        this.permitAllPatterns = permitAllPatterns;
    }

    public List<String> getPermitAllUrls() {
        if(StringUtils.isEmpty(this.permitAllPatterns)) {
            return Collections.EMPTY_LIST;
        }
        String[] array = StringUtils.tokenizeToStringArray(this.permitAllPatterns, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        return Arrays.asList(array);
    }

    public String getPermitAllUserNames() {
        return permitAllUserNames;
    }

    public void setPermitAllUserNames(String permitAllUserNames) {
        this.permitAllUserNames = permitAllUserNames;
    }

    public List<String> getMaxAuthUsers() {
        if(StringUtils.isEmpty(this.permitAllUserNames)) {
            return Collections.EMPTY_LIST;
        }
        String[] array = StringUtils.tokenizeToStringArray(this.permitAllUserNames, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        return Arrays.asList(array);
    }

    public String getBizUserClassName() {
        return bizUserClassName;
    }

    public void setBizUserClassName(String bizUserClassName) {
        this.bizUserClassName = bizUserClassName;
    }

    public Class getBizUserClass() {
        try {
            return Class.forName(this.getBizUserClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
