package com.yueyedexue.gulimall.product.service.impl;

import com.yueyedexue.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.yueyedexue.gulimall.product.dao.AttrDao;
import com.yueyedexue.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yueyedexue.gulimall.product.entity.AttrEntity;
import com.yueyedexue.gulimall.product.service.AttrService;
import com.yueyedexue.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.yueyedexue.gulimall.product.vo.webvo.SpuItemAttrGroupVo;
import jdk.nashorn.internal.ir.CallNode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.product.dao.AttrGroupDao;
import com.yueyedexue.gulimall.product.entity.AttrGroupEntity;
import com.yueyedexue.gulimall.product.service.AttrGroupService;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    AttrDao attrDao;

    @Resource
    AttrAttrgroupRelationDao relationDao;

    @Resource
    AttrService attrService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        String key = (String) params.get("key");
        // 如果前端请求中携带检索字段
        // select * from pms_attr_group where catelog_id = catelogId and (attr_group_id = ? or attr_group_name like %key%)...
        if (!StringUtils.isEmpty(key)) {
            wrapper.or((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 获得所有分组信息
        List<AttrGroupEntity> catelogIds = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> attrVos = catelogIds.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            // 获得所有属性信息
            List<AttrEntity> attrs = attrService.getRelationByGroupId(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return attrVos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuIdAndCatelogId(Long spuId, Long catalogId) {
        List<SpuItemAttrGroupVo> attrGroupVos = this.baseMapper.getAttrGroupWithAttrsBySpuIdAndCatelogId(spuId, catalogId);
        return attrGroupVos;
    }


}