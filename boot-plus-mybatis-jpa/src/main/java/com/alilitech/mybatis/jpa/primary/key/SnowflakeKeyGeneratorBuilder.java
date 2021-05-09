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
package com.alilitech.mybatis.jpa.primary.key;

import com.alilitech.mybatis.MybatisJpaProperties;
import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;
import com.alilitech.mybatis.jpa.primary.key.snowflake.generator.SnowflakeGenerator;
import com.alilitech.mybatis.jpa.primary.key.snowflake.generator.SnowflakeGeneratorOffsetModify;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * to build snowflake key generator
 * @author Zhou Xiaoxiang
 * @since 1.3.2
 */
public class SnowflakeKeyGeneratorBuilder {

    protected final Log log = LogFactory.getLog(this.getClass());

    private static final SnowflakeKeyGeneratorBuilder snowflakeKeyGeneratorBuilder = new SnowflakeKeyGeneratorBuilder();

    private MybatisJpaProperties mybatisJpaProperties;
    private OffsetRepository offsetRepository;

    private SnowflakeKeyGeneratorBuilder() {}

    public static SnowflakeKeyGeneratorBuilder getInstance() {
        return snowflakeKeyGeneratorBuilder;
    }

    public KeyGenerator4Snowflake build(Class<?> entityClass) {
        log.debug("Snowflake key generator build for class '" + entityClass +  "'");
        SnowflakeContext snowflakeContext = null;
        switch (mybatisJpaProperties.getSnowflake().getTimeCallbackStrategy()) {
            case WAITING:
                snowflakeContext = new SnowflakeContext(mybatisJpaProperties.getSnowflake().getGroupId(), mybatisJpaProperties.getSnowflake().getWorkerId());
                snowflakeContext.setMaxBackTime(mybatisJpaProperties.getSnowflake().getMaxBackTime());
                break;
            case EXTRA:
                snowflakeContext = new SnowflakeContext(mybatisJpaProperties.getSnowflake().getGroupId(), mybatisJpaProperties.getSnowflake().getWorkerId(), mybatisJpaProperties.getSnowflake().getExtraWorkerId());
                break;
            case OFFSET_MODIFY:
                snowflakeContext = new SnowflakeContext(mybatisJpaProperties.getSnowflake().getGroupId(), mybatisJpaProperties.getSnowflake().getWorkerId());
                snowflakeContext.setOffset(mybatisJpaProperties.getSnowflake().getOffset());
                if(offsetRepository != null) {
                    Long offset = offsetRepository.getOffset(entityClass);
                    if(offset != null) {
                        snowflakeContext.setOffset(offset);
                    }
                }
                break;
        }
        SnowflakeGenerator snowflakeGenerator = null;
        try {
            snowflakeGenerator = mybatisJpaProperties.getSnowflake().getTimeCallbackStrategy().getClazz().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("New SnowflakeGenerator Object failed");
            throw new RuntimeException(e);
        }

        if(snowflakeGenerator instanceof SnowflakeGeneratorOffsetModify) {
            ((SnowflakeGeneratorOffsetModify)snowflakeGenerator).setOffsetRepositoryAndEntityClass(offsetRepository, entityClass);
        }
        KeyGenerator4Snowflake keyGenerator4Snowflake = new KeyGenerator4Snowflake(snowflakeContext, snowflakeGenerator);
        GeneratorRegistry.getInstance().register(entityClass, keyGenerator4Snowflake);
        return keyGenerator4Snowflake;
    }

    public void setMybatisJpaProperties(MybatisJpaProperties mybatisJpaProperties) {
        this.mybatisJpaProperties = mybatisJpaProperties;
    }

    public void setOffsetRepository(OffsetRepository offsetRepository) {
        this.offsetRepository = offsetRepository;
    }
}
