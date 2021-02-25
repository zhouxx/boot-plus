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
package com.alilitech.mybatis.jpa.criteria.expression.operator;

import com.alilitech.mybatis.jpa.criteria.expression.AtomicExpression;
import com.alilitech.mybatis.jpa.criteria.expression.Expression;
import com.alilitech.mybatis.jpa.exception.MybatisJpaException;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public abstract class OperatorExpression<T> implements AtomicExpression<T> {

    /**
     * specify the arguments count
     */
    protected int argsQuantity = -1;

    public int getArgsQuantity() {
        return argsQuantity;
    }

    protected void validArgsQuantity(Expression<T>...expressions) {
        if(argsQuantity >= 0 && expressions.length != argsQuantity) {
            throw new MybatisJpaException("Argument count not match, argument count must be " + argsQuantity + ", but you support " + expressions.length);
        }
    }

}
