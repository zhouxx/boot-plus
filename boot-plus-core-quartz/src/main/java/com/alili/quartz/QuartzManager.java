/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * QuartzJob管理
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Component
public class QuartzManager<T extends QuartzJob> {

    public final Integer ENABLED_YES = 1;
    public final Integer ENABLED_NO = 0;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    // 主工作需要保持名称唯一
    public static List<String> jobNames = new ArrayList<>();

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 有则更新，无则创建
     * @param job
     * @throws Exception
     */
    public void saveOrUpdateJob(T job) throws Exception {
        // Keys are composed of both a name and group, and the name  must be unique within the group
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        // 获取trigger
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        // 不存在，创建一个
        if (trigger == null && job.getEnabled() == 1) {
            createJob(job);
        } else if(trigger != null) {// Trigger已存在，那么更新相应的定时设置
            updateJob(job, triggerKey, trigger);
        }
    }

    /**
     * 创建子任务
     * @param job
     * @throws Exception
     */
    protected void createJob(T job) throws Exception {
        MethodInvokingJobDetailFactoryBean methodInvJobDetailFB = new MethodInvokingJobDetailFactoryBean();
        // 设置Job组名称
        methodInvJobDetailFB.setGroup(job.getJobGroup());
        // 设置Job名称
        methodInvJobDetailFB.setName(job.getJobName()); // 注意设置的顺序，如果在管理Job类提交到计划管理类之后设置就会设置不上
        // 定义的任务类为Spring的定义的Bean则调用 getBean方法
        if (job.getSpringInstantiated() == 1) {// 是Spring中定义的Bean
            methodInvJobDetailFB.setTargetObject(applicationContext.getBean(job.getBeanName()));
        } else {// 不是就直接new
            methodInvJobDetailFB.setTargetObject(Class.forName(job.getClassName()).newInstance());
        }
        // 设置任务方法
        methodInvJobDetailFB.setTargetMethod(job.getMethodName());
        // 将管理Job类提交到计划管理类
        methodInvJobDetailFB.afterPropertiesSet();

        /** 并发设置 */
        // methodInvJobDetailFB.setConcurrent(job.getConcurrent().equals("1") ? true : false);

        JobDetail jobDetail = (JobDetail) methodInvJobDetailFB.getObject();// 动态
        jobDetail.getJobDataMap().put("scheduleJob", job);
        
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        // 按新的cronExpression表达式构建一个新的trigger
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup())
                .withSchedule(scheduleBuilder).build();

        /** 原理：因为是立即执行,没有用到表达式嘛，所以按照实际的调度创建顺序依次执行    */
        //QuartzUtil.scheduler.standby(); //暂时停止 任务都安排完之后统一启动 解决耗时任务按照顺序部署后执行紊乱的问题

       scheduler.scheduleJob(jobDetail, trigger);// 注入到管理类
       logger.debug(job.getJobGroup() + "." + job.getJobName() + " 创建完毕");
    }
    

    
    /**
     * 更新相应的定时设置 根据job_status做相应的处理
     * @param job
     * @param triggerKey
     * @param trigger
     * @throws SchedulerException
     */
    protected void updateJob(T job, TriggerKey triggerKey, CronTrigger trigger)
            throws SchedulerException {
        if (job.getEnabled() == 1) {// 0禁用 1启用
            if (!trigger.getCronExpression().equalsIgnoreCase(job.getCronExpression())) {
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
                // 按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
                // 按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, trigger);
                logger.debug(job.getJobGroup() + "." + job.getJobName() + " 更新完毕,目前cron表达式为:" + job.getCronExpression()
                        + " isSpringBean：" + job.getSpringInstantiated() + " concurrent: ");
            }
        } else if (job.getEnabled() == 0 ) {
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(trigger.getJobKey());// 删除任务
            logger.debug(job.getJobGroup() + "." + job.getJobName() + " 删除完毕");
        }

    }

}
