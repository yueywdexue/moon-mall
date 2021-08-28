package com.yueyedexue.common.exception;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/7/30 9:17
 **/
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "数据校验失败"),
    VALID_SMS_CODE_EXCEPTION(10002, "验证码获取太快,请稍后重试!"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架出现异常"),
    USER_EXIST_EXCEPTION(15001, "用户存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003, "账号密码错误");

    private Integer code;
    private String msg;

    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
