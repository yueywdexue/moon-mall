package com.yueyedexue.gulimall.ware.vo;

import com.yueyedexue.common.vo.MemberRespVo;
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
