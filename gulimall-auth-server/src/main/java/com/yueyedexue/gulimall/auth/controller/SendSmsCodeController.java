package com.yueyedexue.gulimall.auth.controller;

import com.yueyedexue.common.exception.BizCodeEnum;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.auth.constant.AuthServerConstant;
import com.yueyedexue.gulimall.auth.feign.ThirdPartyFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/20 7:31
 **/
@RestController
public class SendSmsCodeController {

    @Resource
    ThirdPartyFeignService thirdPartyFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @PostMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            String timeout = redisCode.split("_")[1];
            if ((System.currentTimeMillis() - Long.parseLong(timeout)) < 60 * 1000) {
                return R.error(BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.VALID_SMS_CODE_EXCEPTION.getMsg());
            }
        }
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code,5, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone, code.split("_")[0]);
        return R.ok();
    }

}
