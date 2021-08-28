package com.yueyedexue.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.UrlBasedViewResolverRegistration;

/**
 * @description: 跨域请求处理,添加允许跨域请求头
 * @author: MoonNightSnow
 * @createTime: 2021/7/28 15:43
 **/
@Configuration
public class GulimallCorsConfiguration {
    /**
     * 注意: 不要在多个地方配置跨域信息, renren-fast中也配置了CorsConfig, 把它注释掉
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 1. 配置跨域
        // 允许哪些请求头跨域
        corsConfiguration.addAllowedHeader("*");
        // 允许哪些请求方式进行跨域
        corsConfiguration.addAllowedMethod("*");
        // 允许哪些请求来源进行跨域
        corsConfiguration.addAllowedOrigin("*");
        // 允许请求跨域携带cookie
        corsConfiguration.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
