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
package com.alilitech.security;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public abstract class TokenUtils {

    protected SecurityBizProperties securityBizProperties;

    protected Class bizClass;

    public TokenUtils(SecurityBizProperties securityBizProperties) {
        this.securityBizProperties = securityBizProperties;
        try {
            this.bizClass = Class.forName(securityBizProperties.getBizUserClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}