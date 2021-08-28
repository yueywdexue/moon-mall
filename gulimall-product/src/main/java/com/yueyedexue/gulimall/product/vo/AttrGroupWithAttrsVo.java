package com.yueyedexue.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.yueyedexue.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/3 11:23
 **/
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    List<AttrEntity> attrs;
}
