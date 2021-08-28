package com.yueyedexue.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yueyedexue.gulimall.product.dao.BrandDao;
import com.yueyedexue.gulimall.product.dao.CategoryDao;
import com.yueyedexue.gulimall.product.entity.BrandEntity;
import com.yueyedexue.gulimall.product.entity.CategoryEntity;
import com.yueyedexue.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.product.dao.CategoryBrandRelationDao;
import com.yueyedexue.gulimall.product.entity.CategoryBrandRelationEntity;
import com.yueyedexue.gulimall.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    BrandDao brandDao;

    @Resource
    CategoryDao categoryDao;

    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        // 查训品牌名
        BrandEntity brandEntity = brandDao.selectById(brandId);
        // 查询分类名
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateCategoryDetail(Long catId, String name) {
        this.update(new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId).set("catelog_name", name));
    }

    @Override
    public List<BrandEntity> getBrandsById(Long catId) {
        List<CategoryBrandRelationEntity> catelogIds = this.baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<Long> brandIds = catelogIds.stream().map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());

        Collection<BrandEntity> brandEntities = brandService.listByIds(brandIds);
        return (List<BrandEntity>) brandEntities;
    }

    @Override
    public void updateBrandDetail(Long brandId, String name) {
        this.update(new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId).set("brand_name", name));
    }

}