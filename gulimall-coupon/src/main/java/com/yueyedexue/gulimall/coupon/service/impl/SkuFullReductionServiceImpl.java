package com.yueyedexue.gulimall.coupon.service.impl;

import com.yueyedexue.common.to.MemberPrice;
import com.yueyedexue.common.to.SkuReductionTo;
import com.yueyedexue.gulimall.coupon.entity.MemberPriceEntity;
import com.yueyedexue.gulimall.coupon.entity.SkuLadderEntity;
import com.yueyedexue.gulimall.coupon.service.MemberPriceService;
import com.yueyedexue.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.coupon.dao.SkuFullReductionDao;
import com.yueyedexue.gulimall.coupon.entity.SkuFullReductionEntity;
import com.yueyedexue.gulimall.coupon.service.SkuFullReductionService;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    SkuLadderService skuLadderService;

    @Resource
    SkuFullReductionService skuFullReductionService;

    @Resource
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveReduction(SkuReductionTo skuReductionTo) {
        // sku的优惠, 满减信息; gulimall_sms -> sms_sku_ladder/sms_sku_full_reduction/sms_member_price
        // 1. sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // 2. sms_sku_full_reduction

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            skuFullReductionService.save(skuFullReductionEntity);
        }

        // 3. sms_member_price

        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceList = memberPrices.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(item.getId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
                .collect(Collectors.toList());

        memberPriceService.saveBatch(memberPriceList);

    }

}