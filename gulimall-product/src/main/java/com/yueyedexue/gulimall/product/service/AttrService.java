package com.yueyedexue.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.product.entity.AttrEntity;
import com.yueyedexue.gulimall.product.entity.ProductAttrValueEntity;
import com.yueyedexue.gulimall.product.vo.AttrResponseVo;
import com.yueyedexue.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 17:17:11
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationByGroupId(Long attrgroupId);

    PageUtils queryAttrNoRelation(Map<String, Object> params, Long attrgroupId);

    List<ProductAttrValueEntity> getBaseListBySpuId(Long spuId);

    /**
     * 查询所有search_type == 1 的规格参数
     * @param attrIds
     * @return
     */
    List<Long> selectSearchTypeEqOneByAttrIds(List<Long> attrIds);
}

