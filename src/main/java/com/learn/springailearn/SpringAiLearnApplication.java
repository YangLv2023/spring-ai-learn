package com.learn.springailearn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@MapperScan("com.learn.springailearn.mapper") // 扫描Mapper接口
@EnableRetry // 启用重试功能
public class SpringAiLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiLearnApplication.class, args);
    }

}