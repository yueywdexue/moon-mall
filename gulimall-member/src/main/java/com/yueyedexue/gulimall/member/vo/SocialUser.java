package com.yueyedexue.gulimall.member.vo;

import lombok.Data;

import java.util.Date;


@Data
public class SocialUser {

    // 用户名
    private String login;
    private Long id;
    private String name;
    private String accessToken;
}
