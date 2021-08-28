package com.yueyedexue.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1. 逻辑删除
 * 配置全局删除逻辑规则(省略)
 * 配置逻辑删除组件Bean(高版本省略)
 * 在entity类中对应属性添加@TableLogic注解
 * <p>
 * 2. 后端校验
 * 添加 javax.validation.constraints 下面的注解进行校验
 * 并可以自定义自己的校验提示信息
 * 在对应Controller中对要校验的entity类参数前加上@Valid注解开启校验
 * 在紧跟着entity类参数后添加一个BindingResult 参数, 可以获得校验信息
 * 3. 分组校验
 * 定义分组标识接口, 表示这个接口是哪个分组校验的
 * 在Entity类的属性上的校验注解上写上groups属性, 表示这个校验动作是在哪个分组才会进行的
 * 在controller上不使用@Valid了, 使用@Validated(groups = {Class<?>[]})开启分组校验
 * 注意, 如果使用了分组校验, 如果entity的属性上的校验注解没有表明是哪个分组, 那么这个注解是不会起作用的\
 * 但是在没有使用分组的校验中还是有效的
 * 4. 自定义校验注解
 * 编写一个自定义校验注解
 * 编写一个自定义校验器
 * 关联自定义校验器和自定义校验注解
 */
@EnableRedisHttpSession
@MapperScan("com.yueyedexue.gulimall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "com.yueyedexue.gulimall.product.feign")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
