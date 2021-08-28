package com.yueyedexue.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.yueyedexue.common.constant.ProductConstant;
import com.yueyedexue.common.to.SkuReductionTo;
import com.yueyedexue.common.to.SpuBoundTo;
import com.yueyedexue.common.to.WareHasStockTo;
import com.yueyedexue.common.to.es.SkuEsModel;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.product.entity.*;
import com.yueyedexue.gulimall.product.feign.CouponFeignService;
import com.yueyedexue.gulimall.product.feign.SearchFeignService;
import com.yueyedexue.gulimall.product.feign.WareFeignService;
import com.yueyedexue.gulimall.product.service.*;
import com.yueyedexue.gulimall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Resource
    SpuImagesService spuImagesService;

    @Resource
    ProductAttrValueService productAttrValueService;

    @Resource
    AttrService attrService;

    @Resource
    SkuInfoService skuInfoService;

    @Resource
    SkuImagesService skuImagesService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    CouponFeignService couponFeignService;

    @Resource
    BrandService brandService;

    @Resource
    CategoryService categoryService;

    @Resource
    WareFeignService wareFeignService;

    @Resource
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1. 保存spu的基本信息 pms_spu_info

        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2. 保存spu的描述图片 pms_spu_info_desc

        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        List<String> decript = spuSaveVo.getDecript();
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuDescInfo(spuInfoDescEntity);

        // 3. 保存图片集 pms_spu_images

        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveSpuImage(spuInfoEntity.getId(), images);

        // 4. 保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(baseAttr.getAttrId());
            AttrEntity attrId = attrService.getById(baseAttr.getAttrId());
            productAttrValueEntity.setAttrName(attrId.getAttrName());
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttrValue(productAttrValueEntityList);

        // 5. 保存spu的积分信息 gulimall_sms -> sms_spu_bounds

        SpuBoundTo spuBoundTo = new SpuBoundTo();
        Bounds bounds = spuSaveVo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSkuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败!");
        }

        // 6. 保存当前spu的所有sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImage = "";
                List<Images> imagesList = item.getImages();
                if (imagesList != null && imagesList.size() > 0) {
                    for (Images image : imagesList) {
                        if (image.getDefaultImg() == 1) {
                            defaultImage = image.getImgUrl();
                        }
                    }
                }

                // 6.1 sku的基本信息 pms_sku_info
                /*
                 * private String skuName;
                 * private BigDecimal price;
                 * private String skuTitle;
                 * private String skuSubtitle;
                 */
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                // 6.2 sku的图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntityList = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());

                    return skuImagesEntity;
                }).filter(image -> !StringUtils.isEmpty(image.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntityList);
                // TODO 没有图片路径的无需保存 已完成
                // 6.3 sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);
                // 6.4 sku的优惠, 满减信息; gulimall_sms -> sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getReducePrice().compareTo(new BigDecimal("0")) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败!");
                    }
                }
            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("spu_name", key);
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
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void up(Long spuId){
        // 1. 查出当前spuId下的所有sku属性
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());


        // TODO 5 查询当前sku对应的可以被检索的规格参数
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrListBySpuId(spuId);
        List<Long> attrIds = attrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> searchIds = attrService.selectSearchTypeEqOneByAttrIds(attrIds);

        Set<Long> attrIdsSet = new HashSet<>(searchIds); // search_type == 1

        List<SkuEsModel.Attrs> attrsList = attrValueEntities.stream().filter(item -> {
            return attrIdsSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // 发送远程请求查看当前sku是否还有库存
        Map<Long, Boolean> hasStockMap = null;
        try {
            R r = wareFeignService.hasStockBySkuIds(skuIds);
            TypeReference<List<WareHasStockTo>> typeReference = new TypeReference<List<WareHasStockTo>>() {
            };
            List<WareHasStockTo> hasStockTos = r.getData(typeReference);
            hasStockMap = hasStockTos.stream().collect(Collectors.toMap(WareHasStockTo::getSkuId, WareHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("远程查询sku库存出现异常: {}", e);
        }
        // 2.  封装sku信息
        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> collect = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
            // skuPrice skuImg
            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
            // hasStock  hotScore
            // TODO 3 向库存服务发送远程请求查询是否还有库存; 设置热度评分
            if (finalHasStockMap != null) {
                skuEsModel.setHasStock(finalHasStockMap.get(skuEsModel.getSkuId()));
            } else {
                skuEsModel.setHasStock(true); // 远程调用有异常默认设置有库存
            }
            skuEsModel.setHotScore(0L); // 新上架的商品热度评分默认设置为0
            /**
             *     private String brandName;
             *     private String brandImg;
             *     private String catalogName;
             */
            // TODO 4 查询品牌和分类的信息
            BrandEntity brandEntity = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            // 设置sku规格参数信息
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());

        // TODO 6 给gulimall-search发送请求 让es保存检索数据
        R r = searchFeignService.productStatusUp(collect);
        if (r.getCode() == 0) {
            // 保存成功
            // TODO 修改spu当前状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 保存失败
            // TODO 重复调用, 接口幂等性; 重试机制; ...
        }



    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        return getById(skuInfo.getSpuId());
    }

}