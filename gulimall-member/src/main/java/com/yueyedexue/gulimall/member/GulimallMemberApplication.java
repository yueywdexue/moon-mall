package com.yueyedexue.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

// 指定远程调用接口所在包
@EnableFeignClients(basePackages = "com.yueyedexue.gulimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
