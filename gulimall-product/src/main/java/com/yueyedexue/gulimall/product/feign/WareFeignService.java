package com.yueyedexue.gulimall.product.feign;

import com.yueyedexue.common.to.WareHasStockTo;
import com.yueyedexue.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R hasStockBySkuIds(@RequestBody List<Long> skuIds);
}
