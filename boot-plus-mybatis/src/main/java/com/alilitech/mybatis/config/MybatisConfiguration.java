package com.alilitech.mybatis.config;

import org.springframework.context.annotation.Bean;

public class MybatisConfiguration {

    @Bean
    public MybatisMapperScanner mybatisMapperScanner() {
        return new MybatisMapperScanner();
    }

}
