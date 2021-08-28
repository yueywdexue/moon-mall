package com.yueyedexue.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.product.dao.ProductAttrValueDao;
import com.yueyedexue.gulimall.product.entity.ProductAttrValueEntity;
import com.yueyedexue.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttrValue(List<ProductAttrValueEntity> productAttrValueEntityList) {
        this.saveBatch(productAttrValueEntityList);
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> attrValueEntities) {
        // 先删除之前的数据
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        // 重新插入
        List<ProductAttrValueEntity> collect
                = attrValueEntities.stream().peek(attrValueEntity -> attrValueEntity.setSpuId(spuId))
                .collect(Collectors.toList());
        this.saveBatch(collect);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListBySpuId(Long spuId) {
        List<ProductAttrValueEntity> spu_id = this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        return spu_id;
    }

}