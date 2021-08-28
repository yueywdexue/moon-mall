package com.yueyedexue.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.auth.constant.AuthServerConstant;
import com.yueyedexue.gulimall.auth.feign.MemberFeignService;
import com.yueyedexue.gulimall.auth.utils.GiteeHttpClient;
import com.yueyedexue.common.vo.MemberRespVo;
import com.yueyedexue.gulimall.auth.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/22 8:56
 **/
@Controller
@RequestMapping("/gitee")
public class GiteeController {
    @Value("${gitee.oauth.clientid}")
    public String CLIENTID;
    @Value("${gitee.oauth.clientsecret}")
    public String CLIENTSECRET;
    @Value("${gitee.oauth.callback}")
    public String URL;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping(value = "/auth")
    public String giteeAuth(HttpSession session) {
        // 用于第三方应用防止CSRF攻击
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        session.setAttribute("state", uuid);

        // Step1：获取Authorization Code
        String url = "https://gitee.com/oauth/authorize?response_type=code" +
                "&client_id=" + CLIENTID +
                "&redirect_uri=" + URLEncoder.encode(URL) +
                "&state=" + uuid +
                "&scope=user_info";

        return "redirect:"+url;
    }

    @GetMapping("/callback")
    public String giteeCallback(HttpServletRequest request,HttpSession session) throws Exception {
        // 得到Authorization Code
        String code = request.getParameter("code");
        // 我们放在地址中的状态码
        String state = request.getParameter("state");
        String uuid = (String) session.getAttribute("state");

        // 验证信息我们发送的状态码
        if (null != uuid) {
            // 状态码不正确，直接返回登录页面
            if (!uuid.equals(state)) {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }

        // Step2：通过Authorization Code获取Access Token
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code" +
                "&client_id=" + CLIENTID +
                "&client_secret=" + CLIENTSECRET +
                "&code=" + code +
                "&redirect_uri=" + URL;
        JSONObject accessTokenJson = GiteeHttpClient.getAccessToken(url);

        // Step3: 获取用户信息
        url = "https://gitee.com/api/v5/user?access_token=" + accessTokenJson.get("access_token");
//        System.out.println(accessTokenJson.get("access_token"));
//        4ceaabedbeb1ac43d89bcc87bef28146
        JSONObject jsonObject = GiteeHttpClient.getUserInfo(url);
//        System.out.println(jsonObject);
        /**
         * 获取到用户信息之后，就该写你自己的业务逻辑了
         */
        String string = JSON.toJSONString(jsonObject);
        SocialUser socialUser = JSON.parseObject(string, new TypeReference<SocialUser>() {
        });
        socialUser.setAccessToken(accessTokenJson.get("access_token").toString());
        // 如果该社交账号是第一次登录进来, 需要执行注册流程, 将该账号和本系统的一个会员账号关联起来
        R login = memberFeignService.login(socialUser);
        if (login.getCode() == 0) {
            MemberRespVo data = login.getData("data",new TypeReference<MemberRespVo>() {
            });
//            System.out.println(data);
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
