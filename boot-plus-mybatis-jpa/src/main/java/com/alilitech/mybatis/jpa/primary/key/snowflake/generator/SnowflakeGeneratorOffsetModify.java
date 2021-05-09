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
package com.alilitech.mybatis.jpa.primary.key.snowflake.generator;

import com.alilitech.mybatis.jpa.primary.key.OffsetRepository;
import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 削减偏移量
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public class SnowflakeGeneratorOffsetModify extends AbstractSnowflakeGenerator {

    protected static BlockingQueue<OffsetSaveDTO> offsetBlockingQueue;

    private Class<?> entityClass;

    @Override
    public synchronized long generate(SnowflakeContext context) {
        // 下一个ID生成算法
        long lastTimestamp = context.getLastTimestamp();
        long currentTimestamp = currentTimestamp();
        // 获取当前时间戳如果小于上次时间戳, 则表示时间出现回滚，减少偏移量，让时间提前
        if (currentTimestamp < lastTimestamp) {
            // 抛弃当前时间戳，offset+1
            long offsetOffset = lastTimestamp - currentTimestamp + 1;
            context.setOffset(context.getOffset() - offsetOffset);
            // 重置lastTimestamp
            context.setLastTimestamp(-1);

            if (offsetBlockingQueue != null) {
                try {
                    offsetBlockingQueue.put(new OffsetSaveDTO(entityClass, context.getOffset()));
                } catch (InterruptedException e) {
                    log.error("save offset to queue error!", e);
                }
            }
            log.warn("Clock is moving backwards. Back time is " + (lastTimestamp - currentTimestamp) + " ms.");
        }

        return calculate(context, currentTimestamp);
    }


    public void setOffsetRepositoryAndEntityClass(OffsetRepository offsetRepository, Class<?> entityClass) {
        this.entityClass = entityClass;
        if(offsetRepository != null && offsetBlockingQueue == null) {
            offsetBlockingQueue = new LinkedBlockingDeque<>();
            new Thread(new SaveOffsetThread(offsetRepository)).start();
            log.debug("save offset thread started.");
        }
    }

    // 保存偏移量的线程
    public static class SaveOffsetThread implements Runnable {
        protected final Log log = LogFactory.getLog(this.getClass());
        private OffsetRepository offsetRepository;

        public SaveOffsetThread(OffsetRepository offsetRepository) {
            this.offsetRepository = offsetRepository;
        }

        @Override
        public void run() {
            while (offsetRepository != null) {
                try {
                    OffsetSaveDTO offsetSaveDTO = offsetBlockingQueue.take();
                    log.debug("OffsetRepository save offset: " + offsetSaveDTO.getOffset() + " for class '" + offsetSaveDTO.getEntityClass() +"'");
                    offsetRepository.saveOffset(offsetSaveDTO.getEntityClass(), offsetSaveDTO.getOffset());
                } catch (InterruptedException e) {
                    log.error("take offset from queue error!", e);
                }
            }
        }
    }

    private static class OffsetSaveDTO {
        private Class<?> entityClass;
        private Long offset;

        public OffsetSaveDTO(Class<?> entityClass, Long offset) {
            this.entityClass = entityClass;
            this.offset = offset;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }

        public Long getOffset() {
            return offset;
        }

    }

}
