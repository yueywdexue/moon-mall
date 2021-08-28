package com.yueyedexue.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yueyedexue.gulimall.product.vo.AttrRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 17:17:11
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void relationDelete(AttrRelationVo[] vos);

    void saveBatch(List<AttrRelationVo> attrRelationVoList);
}

