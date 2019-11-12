package com.alili.log.aop;

import org.aspectj.lang.Signature;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface LogExtension {

    void beforeEnter(Signature signature, Object[] args);

    Object afterExit(Signature signature, Object result);

}
