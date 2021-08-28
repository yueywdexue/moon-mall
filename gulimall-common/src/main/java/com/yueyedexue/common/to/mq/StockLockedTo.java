package com.yueyedexue.common.to.mq;

import lombok.Data;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/27 15:20
 **/
@Data
public class StockLockedTo {
    // 订单工作项id
    private Long id;
    // 订单工作项详情id
    private StockDetailTo detail;
}
