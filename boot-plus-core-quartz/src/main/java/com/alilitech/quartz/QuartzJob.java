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
package com.alilitech.quartz;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class QuartzJob {

    /**
     * 类全名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 执行时间，定时任务表达式
     */
    private String cronExpression;

    /**
     * 是否启用(0不启用 1 启用)
     */
    private boolean enabled = true;

    /**
     * 是否是spring管理的bean
     */
    private boolean springInstantiated = true;

    /**
     * spring的beanName
     */

    private String beanName;

    /**
     * 工作分组
     */
    private String jobGroup;

    /**
     * 工作名称
     */
    private String jobName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSpringInstantiated() {
        return springInstantiated;
    }

    public void setSpringInstantiated(boolean springInstantiated) {
        this.springInstantiated = springInstantiated;
    }

    public String getBeanName() {
        if(beanName == null || beanName.equals("")) {
            beanName = this.getClassName().substring(this.className.lastIndexOf(".") + 1);
            beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
        }
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getJobGroup() {
        this.jobGroup = this.className.lastIndexOf(".") == -1 ? "" : this.getClassName().substring(0, this.className.lastIndexOf("."));
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        this.jobName = this.className.lastIndexOf(".") == -1 ? "" : this.getClassName().substring(this.className.lastIndexOf(".") + 1);
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
