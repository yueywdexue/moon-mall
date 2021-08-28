package com.yueyedexue.gulimall.thirdparty.controller;

import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/20 7:27
 **/
@Controller
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;

    @PostMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        System.out.println(phone + " " + code);
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }


}
