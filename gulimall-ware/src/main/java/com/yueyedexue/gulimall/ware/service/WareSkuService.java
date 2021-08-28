package com.yueyedexue.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.to.WareHasStockTo;
import com.yueyedexue.common.to.mq.OrderTo;
import com.yueyedexue.common.to.mq.StockLockedTo;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.ware.entity.WareSkuEntity;
import com.yueyedexue.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:42:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<WareHasStockTo> hasStockBySkuIds(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    boolean orderLockStock(WareSkuLockVo vo);

    void handleReleaseStockLocked(StockLockedTo stockLockedTo);

    void handleReleaseStockLocked(OrderTo orderTo);
}

