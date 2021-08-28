package com.yueyedexue.gulimall.product.vo;

import lombok.Data;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/2 7:23
 **/
@Data
public class AttrResponseVo extends AttrVo {
    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
