package com.yueyedexue.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 12:23
 **/
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
