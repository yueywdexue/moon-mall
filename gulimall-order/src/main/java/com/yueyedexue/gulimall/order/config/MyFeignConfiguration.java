package com.yueyedexue.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/25 14:36
 **/
@Configuration
public class MyFeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 拿到请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    // 老请求
                    HttpServletRequest request = requestAttributes.getRequest();
                    // 获得请求头中的Cookie
                    String cookie = request.getHeader("Cookie");
                    // 给新请求设置Cookie
                    template.header("Cookie", cookie);
                }
            }
        };
    }
}
