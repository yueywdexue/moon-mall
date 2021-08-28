package com.yueyedexue.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/3 17:43
 **/
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
