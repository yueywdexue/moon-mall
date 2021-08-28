package com.yueyedexue.gulimall.auth.vo;

import lombok.Data;


@Data
public class SocialUser {

    // 用户名
    private String login;
    private Long id;
    private String name;
    private String accessToken;
}
