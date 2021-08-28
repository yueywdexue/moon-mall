package com.yueyedexue.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.yueyedexue.common.exception.NoStockException;
import com.yueyedexue.common.to.WareHasStockTo;
import com.yueyedexue.common.to.mq.OrderTo;
import com.yueyedexue.common.to.mq.StockDetailTo;
import com.yueyedexue.common.to.mq.StockLockedTo;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.yueyedexue.gulimall.ware.entity.WareOrderTaskEntity;
import com.yueyedexue.gulimall.ware.feign.OrderFeignService;
import com.yueyedexue.gulimall.ware.feign.ProductFeignService;
import com.yueyedexue.gulimall.ware.service.PurchaseDetailService;
import com.yueyedexue.gulimall.ware.service.WareOrderTaskDetailService;
import com.yueyedexue.gulimall.ware.service.WareOrderTaskService;
import com.yueyedexue.gulimall.ware.vo.OrderVo;
import com.yueyedexue.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.ware.dao.WareSkuDao;
import com.yueyedexue.gulimall.ware.entity.WareSkuEntity;
import com.yueyedexue.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    WareSkuDao wareSkuDao;

    @Resource
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void handleReleaseStockLocked(StockLockedTo stockLockedTo){
        StockDetailTo detail = stockLockedTo.getDetail();
        Long detailId = detail.getId();
        /**
         * 如果查询到有库存锁定信息, 不能直接解锁, 需要看有没有这个订单
         *      如果有订单:
         *          看订单状态,:
         *              如果订单状态是已取消, 直接解锁
         *              如果订单状态是已支付, 无需解锁
         *      如果没有订单, 直接解锁
         * 如果没有库存锁定信息, 说明库存已经回滚了, 无需解锁
         */
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 进行解锁
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号远程查询订单状态
            R statusByOrderSn = orderFeignService.getStatusByOrderSn(orderSn);
            if (statusByOrderSn.getCode() == 0) {
                OrderVo orderVo = statusByOrderSn.getData(new TypeReference<OrderVo>() {
                });
                // 如果订单不存在 or 订单状态是已取消, 进行解锁
                if (orderVo ==null || orderVo.getStatus() == 4 || orderVo.getStatus() == 0) {
                    // 只有工作单详情是已锁定状态才能解锁  1 已锁定 2已解锁 3 已扣减
                    if (byId.getLockStatus() == 1) {
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                throw new RuntimeException("远程服务失败");
            }
        }
    }

    /**
     * 防止订单服务卡顿, 无法及时更改订单状态, 导致库存服务收到订单过期消息却因为订单状态无法解锁库存
     * @param orderTo
     */
    @Transactional
    @Override
    public void handleReleaseStockLocked(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 查一下最新库存的状态, 防止重复解锁
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        // 按照工作单找到所有没有解锁的库存, 进行解锁
        Long id = wareOrderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> taskDetailList = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        if (taskDetailList != null) {
            for (WareOrderTaskDetailEntity detailEntity : taskDetailList) {
                unLockStock(detailEntity.getSkuId(), detailEntity.getWareId(), detailEntity.getSkuNum(), detailEntity.getId());
            }
        }

    }

    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        baseMapper.unLockStock(skuId, wareId, skuNum);
        // 解锁成功, 更改库存工作单的状态
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(detailId);
        detailEntity.setLockStatus(2); // 变为已解锁
        wareOrderTaskDetailService.updateById(detailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断之前又没有库存, 没有就是新增
        List<WareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities.size() == 0) {
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            // TODO 远程查询对应的商品名字 如果查询失败, 整个事务也不要回滚
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    entity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            wareSkuDao.insert(entity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<WareHasStockTo> hasStockBySkuIds(List<Long> skuIds) {

        return skuIds.stream().map(skuId -> {
            WareHasStockTo wareHasStockTo = new WareHasStockTo();
            wareHasStockTo.setSkuId(skuId);
            Long stock = baseMapper.getStockBySkuId(skuId);
            if (stock != null) {
                wareHasStockTo.setHasStock(stock > 0);
            } else {
                wareHasStockTo.setHasStock(false);
            }
            return wareHasStockTo;
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public boolean orderLockStock(WareSkuLockVo vo) {
        // 保存订单工作项
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 找到哪个商品在哪个仓库中有库存
        List<SkuWareHasStock> collect = vo.getLocks().stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds = this.baseMapper.listWareIdHasStock(item.getSkuId());
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            Integer num = hasStock.getNum();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = baseMapper.lockSkuStock(skuId, num, wareId);
                if (count == 1) {
                    // 当前商品库存锁定成功 进行下一个商品的库存锁定
                    skuStocked = true;
                    // 锁定成功, 保存订单锁定详情
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", num, wareOrderTaskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    // 构建rabbitMQ消息传输对象
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    // 给消息队列发送消息
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }
            if (!skuStocked) {
                // 当前商品没有仓库能锁住
                throw new NoStockException(skuId);
            }
        }
        // 全部锁定成功
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}