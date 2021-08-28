package com.yueyedexue.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/5 15:39
 **/
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
