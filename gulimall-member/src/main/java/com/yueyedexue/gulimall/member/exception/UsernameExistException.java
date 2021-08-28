package com.yueyedexue.gulimall.member.exception;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/20 14:39
 **/
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已经存在");
    }
}
