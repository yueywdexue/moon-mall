package com.yueyedexue.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.product.entity.CategoryEntity;
import com.yueyedexue.gulimall.product.vo.webvo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 17:17:11
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> queryWithTree();

    Long[] getCatelogPath(Long catelogId);

    void updateDetail(CategoryEntity category);

    List<CategoryEntity> getCategoryLevel1();

    Map<String, List<Catelog2Vo>> getCatalogJson();
}

