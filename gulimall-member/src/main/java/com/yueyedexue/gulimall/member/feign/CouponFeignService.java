package com.yueyedexue.gulimall.member.feign;

import com.yueyedexue.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/7/23 9:06
 **/
// 告诉feign你要调用的是哪个远程服务
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 声明接口调用的是远程服务的哪个请求 完整路径和方法签名
     * 在启动类上标注下面的注解指定远程调用接口所在的包
     * @EnableFeignClients(basePackages = "com.yueyedexue.gulimall.member.feign")
     * 指定服务注册中心地址和模块名
     * spring:
     *   cloud:
     *     nacos:
     *       discovery:
     *         server-addr: 127.0.0.1:8848
     *   application:
     *      name: gulimall-member
     */
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupon();

}
