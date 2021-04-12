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
package com.alilitech.web.jackson.ser.converter;

import com.alilitech.web.jackson.anotation.NumberFormat;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class NumberAnnotationConfig {

    /**
     * 格式化格式,如果引用此样式，则强制设置成此样式的格式，并转换成字符串
     */
    private String pattern;

    /**
     * 保留几位小数，默认是2位
     */
    private int scale;

    /**
     * 取舍模式，默认4舍五入，参考BigDecimal里的常量
     */
    private int round;

    public NumberAnnotationConfig(NumberFormat numberFormat) {
        this.pattern = numberFormat.pattern();
        this.scale = numberFormat.scale();
        this.round = numberFormat.round();
    }

    public String getPattern() {
        return pattern;
    }

    public int getScale() {
        return scale;
    }

    public int getRound() {
        return round;
    }
}
