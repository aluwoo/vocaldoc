package com.vocaldoc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 声乐课堂嗓音档案管理系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.vocaldoc.mapper")
public class VocalDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocalDocApplication.class, args);
    }

}
