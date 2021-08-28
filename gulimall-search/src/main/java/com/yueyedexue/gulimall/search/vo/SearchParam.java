package com.yueyedexue.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/11 19:38
 **/
@Data
public class SearchParam {
    // 全文检索条件  v
    private String keyword;
    // 分类id  v
    private Long catalog3Id;
    /**
     * 排序条件  v
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;
    /**
     * 好多的过滤条件
     * hasStock=0/1
     * skuPrice=0_5000/_5000/0_
     * brandId=1
     * attrs属性用:分割开
     */

    // 是否有库存  v
    private Integer hasStock;
    // 价格范围  v
    private String skuPrice;
    // 品牌筛选  v
    private List<Long> brandId;
    // 按照属性筛选 v
    private List<String> attrs;
    // 分页 v
    private Integer pageNum = 1;
    // 高亮显示 v
}
