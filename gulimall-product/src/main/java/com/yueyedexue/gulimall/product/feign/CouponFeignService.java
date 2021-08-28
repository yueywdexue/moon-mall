package com.yueyedexue.gulimall.product.feign;

import com.yueyedexue.common.to.SkuReductionTo;
import com.yueyedexue.common.to.SpuBoundTo;
import com.yueyedexue.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 1. 本服务调用CouponFeignService.saveSkuBounds(spuBoundTo)方法;
     *  1). @RequestBody SpuBoundTo 将请求参数对象转换成json
     *  2). 找到gulimall-coupon服务, , 给/coupon/spubounds/save返发送请求
     *      将上一步的json数据放在请求体中, 发送请求
     *  3).  对方服务接收请求, 从请求体中的到json数据,
     *       (@RequestBody SpuBoundsEntity) 将json数据转换成SpuBoundsEntity对象
     *  因此, 只要json数据模型是兼容的, 双方服务无需使用同一个to
     *
     * @param spuBoundTo 满减信息传输对象
     * @return 统一返回对象
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSkuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
