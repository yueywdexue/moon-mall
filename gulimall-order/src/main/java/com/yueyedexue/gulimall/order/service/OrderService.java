package com.yueyedexue.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.order.entity.OrderEntity;
import com.yueyedexue.gulimall.order.vo.*;

import java.util.Map;

/**
 * 订单
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:34:26
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页需要的数据
     * @return
     */
    OrderConfirmVo orderConfirm();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    OrderEntity getStatusByOrderSn(String orderSn);

    int closeOrder(OrderEntity orderEntity);

    /**
     * 通过订单号从数据库中得到支付信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);

}

