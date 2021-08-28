package com.yueyedexue.gulimall.ware.service.impl;

import com.yueyedexue.common.constant.PurchaseConstant;
import com.yueyedexue.gulimall.ware.controller.PurchaseDetailController;
import com.yueyedexue.gulimall.ware.entity.PurchaseDetailEntity;
import com.yueyedexue.gulimall.ware.entity.WareSkuEntity;
import com.yueyedexue.gulimall.ware.service.PurchaseDetailService;
import com.yueyedexue.gulimall.ware.service.WareSkuService;
import com.yueyedexue.gulimall.ware.vo.MergeVo;
import com.yueyedexue.gulimall.ware.vo.PurchaseDoneVo;
import com.yueyedexue.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.ware.dao.PurchaseDao;
import com.yueyedexue.gulimall.ware.entity.PurchaseEntity;
import com.yueyedexue.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    PurchaseDetailService purchaseDetailService;

    @Resource
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceiveList(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();

        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());

            purchaseEntity.setStatus(PurchaseConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }


        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        // TODO 确认采购单和采购需求状态是0, 1 才可以进行采购
        PurchaseEntity byId = this.getById(purchaseId);
        if (byId.getStatus() == PurchaseConstant.PurchaseStatusEnum.CREATED.getCode()
                || byId.getStatus() == PurchaseConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
            List<PurchaseDetailEntity> purchaseDetailEntityList = items.stream().map(i -> {
                PurchaseDetailEntity detailServiceById = purchaseDetailService.getById(i);
                if (detailServiceById.getStatus() == 0) {
                    detailServiceById.setStatus(PurchaseConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                }
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(i);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(detailServiceById.getStatus());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntityList);

            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(finalPurchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }

    }

    @Override
    public void received(List<Long> ids) {
        // 1. 只能领取新建和已分配的采购单
        List<PurchaseEntity> purchaseEntityList
                = ids.stream().map(this::getById).filter(item -> item.getStatus() == PurchaseConstant.PurchaseStatusEnum.CREATED.getCode() ||
                item.getStatus() == PurchaseConstant.PurchaseStatusEnum.ASSIGNED.getCode()).peek(item -> {
            item.setStatus(PurchaseConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
        }).collect(Collectors.toList());

        // 2. 修改采购单状态

        if (purchaseEntityList.size() > 0) {
            this.updateBatchById(purchaseEntityList);
        } else {
            return;
        }

        purchaseEntityList.forEach(item -> {
            List<PurchaseDetailEntity> detailEntityList = purchaseDetailService.listByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect = detailEntityList.stream().map(i -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(i.getId());
                detailEntity.setStatus(PurchaseConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            // 3. 修改采购项状态
            if (collect.size() > 0) {
                purchaseDetailService.updateBatchById(collect);
            }
        });

    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();


        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> updateList = new ArrayList<>();
        boolean flag = true;
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == PurchaseConstant.PurchaseDetailStatusEnum.ERROR.getCode()) {
                flag = false;
            } else {
                // 3. 将采购成功的进行入库
                PurchaseDetailEntity detailById = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detailById.getSkuId(), detailById.getWareId(), detailById.getSkuNum());

            }
            purchaseDetailEntity.setStatus(item.getStatus());
            purchaseDetailEntity.setId(item.getItemId());
            updateList.add(purchaseDetailEntity);
        }
        // 2. 改变采购需求的状态
        purchaseDetailService.updateBatchById(updateList);

        // 1. 改变采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? PurchaseConstant.PurchaseStatusEnum.FINISH.getCode() : PurchaseConstant.PurchaseStatusEnum.ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}