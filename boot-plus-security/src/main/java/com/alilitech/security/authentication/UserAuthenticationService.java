/**
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
package com.alilitech.security.authentication;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.SecurityBizProperties;
import com.alilitech.security.domain.BizUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class UserAuthenticationService implements UserDetailsService {

    private ExtensibleSecurity extensibleSecurity;

    private SecurityBizProperties securityBizProperties;

    public UserAuthenticationService(ExtensibleSecurity extensibleSecurity, SecurityBizProperties securityBizProperties) {
        this.extensibleSecurity = extensibleSecurity;
        this.securityBizProperties = securityBizProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        boolean maxAuth = securityBizProperties.getPermitAllUserNames().contains(username);

        BizUser bizUser = extensibleSecurity.loadUserByUsername(username, maxAuth);

        if(bizUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        //ArrayList, but not Arrays.asList(...)
        bizUser.setRoles(bizUser.getRoles() == null ? new ArrayList<>() : new ArrayList<>(bizUser.getRoles()));

        if(maxAuth) {
            bizUser.getRoles().add("ROLE_ALL");
        } else {
            bizUser.getRoles().add("ROLE_PUBLIC");
        }

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        List<GrantedAuthority> authorities = new ArrayList<>();
        bizUser.getRoles().forEach(role -> {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            authorities.add(simpleGrantedAuthority);
        });
        SecurityUser user = new SecurityUser(username, encoder.encode(bizUser.getPassword() == null ? "" : bizUser.getPassword()), authorities);
        user.setBizUser(bizUser);

        return user;
    }
}
