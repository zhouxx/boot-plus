package com.alili.biz.security;
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
