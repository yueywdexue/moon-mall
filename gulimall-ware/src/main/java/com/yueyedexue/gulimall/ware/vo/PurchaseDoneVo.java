package com.yueyedexue.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/5 19:05
 **/
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;
    List<PurchaseItemDoneVo> items;
}
