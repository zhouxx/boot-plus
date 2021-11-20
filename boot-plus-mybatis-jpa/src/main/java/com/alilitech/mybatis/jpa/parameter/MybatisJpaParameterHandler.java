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
package com.alilitech.mybatis.jpa.parameter;

import com.alilitech.mybatis.jpa.LikeContainer;
import com.alilitech.mybatis.jpa.statement.parser.LikeType;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MybatisJpaParameterHandler extends DefaultParameterHandler {

    public MybatisJpaParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        super(mappedStatement, process(mappedStatement, parameterObject, boundSql), boundSql);
    }

    @SuppressWarnings("java:S3740")
    private static Object process(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        //对like参数进行转换，防止sql注入，前提必须是有参数
        if(parameterObject != null && mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT)) {
            String methodId = mappedStatement.getId();
            LikeContainer likeContainer = LikeContainer.getInstance();

            //多参数
            if(parameterObject instanceof MapperMethod.ParamMap) {
                MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap<?>) parameterObject;
                paramMap.forEach((key, value) -> {
                    String likeKey = methodId + "." + key;
                    if(likeContainer.isExist(likeKey)
                            && needTransfer(value)) {
                        Object setVal = transferValue(likeContainer.get(likeKey), value);
                        paramMap.put(key, setVal);
                    }
                });
            } else { // 单参数
                String likeKey = methodId + "._parameter";
                if(likeContainer.isExist(likeKey)
                        && needTransfer(parameterObject)) {
                    parameterObject = transferValue(likeContainer.get(likeKey), parameterObject);
                    // 单个参数，必须手动设置，否则无效。因为传参的是值，不是引用
                    boundSql.setAdditionalParameter("_parameter", parameterObject);
                }
            }
        }
        return parameterObject;
    }

    // 是否需要转换
    private static boolean needTransfer(Object value) {
        return value != null &&   // value为null的情况不处理
                !(value.toString().startsWith("%") || value.toString().endsWith("%"));  // value已经前后有了%说明参数已经处理过了
    }

    // 转成成占位的值
    private static Object transferValue(LikeType likeType, Object value) {
        switch (likeType) {
            case BEFORE: {
                value = "%" + value;
                break;
            }
            case AFTER: {
                value = value + "%";
                break;
            }
            case CONTAIN: {
                value = "%" + value + "%";
                break;
            }
        }
        return value;
    }

}
