package com.yueyedexue.gulimall.ware.vo;

import lombok.Data;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 17:49
 **/
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
