package com.alili.biz.security;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class SecurityBizMessageSource extends ResourceBundleMessageSource {
    // ~ Constructors
    // ===================================================================================================

    public SecurityBizMessageSource() {
        setBasename("security.biz.messages");
    }

    // ~ Methods
    // ========================================================================================================

    public static MessageSourceAccessor getAccessor() {
        return new MessageSourceAccessor(new SecurityBizMessageSource());
    }
}
