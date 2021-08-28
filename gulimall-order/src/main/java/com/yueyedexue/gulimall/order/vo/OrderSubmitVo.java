package com.yueyedexue.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @description: 封装订单提交的数据
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 14:28
 **/
@Data
@ToString
public class OrderSubmitVo {
    // 收货地址id
    private Long addrId;
    // 支付方式
    private Integer payType;

    // 无需提交要购买的商品, 从购物车中再获一遍

    // 防重令牌
    private String orderToken;

    // 应付价格
    private BigDecimal payPrice;

    // 订单备注
    private String node;

    // 用户信息从session中获取
}
