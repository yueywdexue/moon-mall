package com.yueyedexue.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 17:42
 **/
@Data
public class WareSkuLockVo {
    // 订单号
    private String orderSn;
    // 需要锁住的所有库存信息
    private List<OrderItemVo> locks;
}
