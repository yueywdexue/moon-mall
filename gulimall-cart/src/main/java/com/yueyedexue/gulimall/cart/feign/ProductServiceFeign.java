package com.yueyedexue.gulimall.cart.feign;

import com.yueyedexue.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/23 10:25
 **/
@FeignClient("gulimall-product")
public interface ProductServiceFeign {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
     List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/getPrice")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
