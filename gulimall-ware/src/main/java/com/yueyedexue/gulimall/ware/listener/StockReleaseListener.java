package com.yueyedexue.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.yueyedexue.common.to.mq.OrderTo;
import com.yueyedexue.common.to.mq.StockLockedTo;
import com.yueyedexue.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/27 16:45
 **/
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleReleaseStockLocked(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("收到库存解锁的消息");
        try {
            wareSkuService.handleReleaseStockLocked(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
    @RabbitHandler
    public void handleReleaseStockLocked(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("收到订单取消的消息");
        try {
            wareSkuService.handleReleaseStockLocked(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
