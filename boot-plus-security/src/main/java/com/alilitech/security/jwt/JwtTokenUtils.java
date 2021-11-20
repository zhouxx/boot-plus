/*
 *    Copyright 2017-2021 the original author or authors.
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
package com.alilitech.security.jwt;

import com.alilitech.security.SecurityBizProperties;
import com.alilitech.security.TokenUtils;
import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.domain.BizUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JwtTokenUtils extends TokenUtils implements InitializingBean {

    private static final String AUTHORITIES_NAME = "authorities";

    private static final String BIZ_USER_NAME = "bizUser";

    private ObjectMapper objectMapper = new ObjectMapper();

    //secrete algorithm
    private Algorithm algorithm;

    public JwtTokenUtils(SecurityBizProperties securityBizProperties) {
       super(securityBizProperties);
        //去除null值。减小token长度
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * generate token by user authentication
     *
     * @param authentication
     * @return
     */
    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = Date.from(Instant.now());
        Date expiration = Date.from(ZonedDateTime.now().plusMinutes(securityBizProperties.getJwt().getTimeoutMin()).toInstant());

        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        String userString = null;
        try {
            userString = objectMapper.writeValueAsString(user.getBizUser());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        //create jwt
        return JWT.create()
                .withClaim(AUTHORITIES_NAME, authorities)
                .withClaim(BIZ_USER_NAME, userString)
                .withSubject(authentication.getName())
                .withKeyId(user.getBizUser().getUserId())
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(algorithm);
    }

    /**
     * refresh token
     * @param token
     * @return
     */
    public String refreshToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        String authorityString = decodedJWT.getClaim(AUTHORITIES_NAME).asString();

        String subject = decodedJWT.getSubject();

        Date now = Date.from(Instant.now());
        Date expiration = Date.from(ZonedDateTime.now().plusMinutes(securityBizProperties.getJwt().getTimeoutMin()).toInstant());

        //create jwt
        JWTCreator.Builder builder = JWT.create()
                .withClaim(AUTHORITIES_NAME, authorityString)
                .withSubject(subject)
                .withIssuedAt(now)
                .withExpiresAt(expiration);

        Map<String, Claim> claims = decodedJWT.getClaims();
        if(!CollectionUtils.isEmpty(claims)) {
            claims.forEach((name, value) -> builder.withClaim(name, value.asString()));
        }

        return builder.sign(algorithm);
    }

    /**
     * validate token and period
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        if (token == null)
            return false;
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Calculated based on expiration time to determine if you need to refresh token
     * @param token
     * @return
     */
    public boolean compareExpireTime(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);

        Date expiresAt = decodedJWT.getExpiresAt();

        int seconds = Instant.ofEpochSecond(expiresAt.getTime()).compareTo(Instant.now());

        return seconds > 0 && seconds < securityBizProperties.getJwt().getRefreshSeconds();
    }

    /**
     * parse user authentication from token string
     *
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token) {

        DecodedJWT decodedJWT = JWT.decode(token);
        String authorityString = decodedJWT.getClaim(AUTHORITIES_NAME).asString();

        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        if (!StringUtils.isEmpty(authorityString)) {
            authorities = Arrays.asList(authorityString.split(","))
                    .stream()
                    .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }

        SecurityUser user = new SecurityUser(decodedJWT.getSubject(), "", authorities);

        String bizUserString = decodedJWT.getClaim(BIZ_USER_NAME).asString();

        try {
            user.setBizUser((BizUser) objectMapper.readValue(bizUserString, this.bizClass));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new UsernamePasswordAuthenticationToken(user, "", authorities);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        algorithm = Algorithm.HMAC256(securityBizProperties.getJwt().getSecret());
    }
}
