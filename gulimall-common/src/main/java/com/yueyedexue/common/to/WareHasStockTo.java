package com.yueyedexue.common.to;

import lombok.Data;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/8 9:14
 **/
@Data
public class WareHasStockTo {
    private Long skuId;
    private Boolean hasStock;
}
