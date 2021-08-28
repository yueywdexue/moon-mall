package com.yueyedexue.gulimall.order.to;

import com.yueyedexue.gulimall.order.entity.OrderEntity;
import com.yueyedexue.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 15:25
 **/
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    // 计算应付的价格
    private BigDecimal payPrice;
    // 运费
    private BigDecimal fare;
}
