package com.zhisangui.zojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@MapperScan("com.zhisangui.zojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.zhisangui")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zhisangui.zojbackendserviceclient.service"})
public class ZojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZojBackendQuestionServiceApplication.class, args);
    }

}
