package com.yueyedexue.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.common.vo.MemberRespVo;
import com.yueyedexue.gulimall.auth.constant.AuthServerConstant;
import com.yueyedexue.gulimall.auth.feign.MemberFeignService;
import com.yueyedexue.gulimall.auth.vo.UserLoginVo;
import com.yueyedexue.gulimall.auth.vo.UserRegisterVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/19 16:11
 **/
@Controller
public class LoginController {



    //    @GetMapping("/reg.html")
//    public String regPage(){
//        return "reg";
//    }
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            return "redirect:http://gulimall.com";
        }
        return "login";
    }

    /**
     * TODO: 重定向携带数据：利用session原理，将数据放在session中。
     * TODO:只要跳转到下一个页面取出这个数据以后，session里面的数据就会删掉
     * TODO：分布下session问题
     * RedirectAttributes：重定向也可以保留数据，不会丢失
     * 用户注册
     *
     * @return
     */
    @PostMapping(value = "/register")
    public String register(@Valid UserRegisterVo vos, BindingResult result,
                           RedirectAttributes attributes) {
        //如果有错误回到注册页面
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);

            //注册信息效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        } else {
            // 前端传递过来的验证码
            String code = vos.getCode();
            // 真正发送验证码时保存在redis中的验证码
            String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone()).split("_")[0];
            // 如果有发验证码才开始校验
            if (!StringUtils.isEmpty(redisCode)) {
                // 两者相同校验成功
                if (code.equalsIgnoreCase(redisCode)) {
                    // 删除验证码 令牌机制
                    stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());
                    // 验证通过 保存会员信息前校验验证码,调用远程服务进行注册
                    R register = memberFeignService.register(vos);
                    if (register.getCode() == 0) {
                        return "redirect:http://auth.gulimall.com/login.html";
                    } else {
                        Map<String, String> errors = new HashMap<>();
                        errors.put("msg", register.getData("msg", new TypeReference<String>() {
                        }));
                        attributes.addFlashAttribute("errors", errors);
                        return "redirect:http://auth.gulimall.com/reg.html";
                    }

                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("code", "验证码错误");
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {
        // 远程调用gulimall-member进行登录验证
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
//            System.out.println(data);
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
