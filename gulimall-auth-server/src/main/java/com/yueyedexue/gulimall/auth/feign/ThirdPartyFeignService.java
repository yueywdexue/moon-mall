package com.yueyedexue.gulimall.auth.feign;

import com.yueyedexue.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/20 7:23
 **/
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @ResponseBody
    @PostMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code);
}
