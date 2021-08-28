package com.yueyedexue.gulimall.ware.feign;

import com.yueyedexue.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/27 16:01
 **/
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/status/{orderSn}")
    R getStatusByOrderSn(@PathVariable("orderSn") String orderSn);
}
