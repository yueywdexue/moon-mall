package com.yueyedexue.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/26 21:06
 **/

public class NoStockException extends RuntimeException {
    @Getter
    @Setter
    private Long skuId;

    public NoStockException() {
        super("没有库存了");
    }

    public NoStockException(Long skuId) {
        super(skuId + "没有库存了");
    }

}
