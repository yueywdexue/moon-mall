package com.yueyedexue.gulimall.product.service.impl;

import com.yueyedexue.gulimall.product.entity.SkuImagesEntity;
import com.yueyedexue.gulimall.product.entity.SpuInfoDescEntity;
import com.yueyedexue.gulimall.product.service.*;
import com.yueyedexue.gulimall.product.vo.webvo.SkuItemSaleAttrVo;
import com.yueyedexue.gulimall.product.vo.webvo.SkuItemVo;
import com.yueyedexue.gulimall.product.vo.webvo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.product.dao.SkuInfoDao;
import com.yueyedexue.gulimall.product.entity.SkuInfoEntity;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    SkuImagesService skuImagesService;

    @Resource
    SpuInfoDescService spuInfoDescService;

    @Resource
    AttrGroupService attrGroupService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        try {
            if (Integer.parseInt(min) <= Integer.parseInt(max)) {
                if (!StringUtils.isEmpty(min)) {
                    min = Integer.parseInt(min) < 0 ? "0" : min;
                    queryWrapper.ge("price", min);
                }
                if (!StringUtils.isEmpty(max) && Integer.parseInt(max) > 0) {
                    queryWrapper.le("price", max);
                }
            }
        } catch (Exception ignored) {
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo getSkuItem(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1、sku基本信息的获取  pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.baseMapper.selectById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        //3、获取spu的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);


        //4、获取spu的介绍
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            SpuInfoDescEntity descEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(descEntity);
        }, executor);

        //5、获取spu的规格参数信息
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuIdAndCatelogId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);

/*        Long catalogId = skuInfoEntity.getCatalogId();
        Long spuId = skuInfoEntity.getSpuId();*/

        //2、sku的图片信息    pms_sku_images
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getItemImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);
//        System.out.println(executor.getCorePoolSize() + " " + executor.getMaximumPoolSize() + " " + executor.getKeepAliveTime(TimeUnit.SECONDS));

        try {
            CompletableFuture.allOf(imagesFuture, saleAttrFuture, descFuture, baseAttrFuture).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return skuItemVo;
    }

}