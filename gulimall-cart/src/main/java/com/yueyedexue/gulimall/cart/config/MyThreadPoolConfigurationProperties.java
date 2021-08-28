package com.yueyedexue.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/19 14:54
 **/
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class MyThreadPoolConfigurationProperties {
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Integer keepAliveTime;
}
