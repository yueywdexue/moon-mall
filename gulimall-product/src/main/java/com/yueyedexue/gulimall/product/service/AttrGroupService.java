package com.yueyedexue.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yueyedexue.gulimall.product.entity.AttrGroupEntity;
import com.yueyedexue.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.yueyedexue.gulimall.product.vo.webvo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 17:17:11
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuIdAndCatelogId(Long spuId, Long catalogId);
}

