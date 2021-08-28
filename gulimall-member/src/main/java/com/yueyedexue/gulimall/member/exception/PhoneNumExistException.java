package com.yueyedexue.gulimall.member.exception;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/20 14:39
 **/
public class PhoneNumExistException extends RuntimeException {
    public PhoneNumExistException() {
        super("手机号已经存在");
    }
}
