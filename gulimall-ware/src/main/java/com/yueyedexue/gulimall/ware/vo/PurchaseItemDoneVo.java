package com.yueyedexue.gulimall.ware.vo;

import lombok.Data;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/5 19:06
 **/
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
