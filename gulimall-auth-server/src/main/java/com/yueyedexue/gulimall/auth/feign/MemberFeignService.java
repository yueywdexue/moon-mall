package com.yueyedexue.gulimall.auth.feign;

import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.auth.vo.SocialUser;
import com.yueyedexue.gulimall.auth.vo.UserLoginVo;
import com.yueyedexue.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth/login")
    R login(@RequestBody SocialUser socialUser);

}
