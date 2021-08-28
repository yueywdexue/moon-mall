package com.yueyedexue.gulimall.order.vo;

import com.yueyedexue.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 14:54
 **/
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;
}
