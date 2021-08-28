package com.yueyedexue.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.yueyedexue.common.to.mq.OrderTo;
import com.yueyedexue.gulimall.order.entity.OrderEntity;
import com.yueyedexue.gulimall.order.enume.OrderStatusEnum;
import com.yueyedexue.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/27 17:35
 **/
@Service
@RabbitListener(queues = "order-release-order-queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void listenMQTest(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期订单,准备删除订单:" + orderEntity.getOrderSn());
        try {
            int i = orderService.closeOrder(orderEntity);
            if (i == OrderStatusEnum.CANCLED.getCode() || i == OrderStatusEnum.CREATE_NEW.getCode()) {
                OrderTo orderTo = new OrderTo();
                BeanUtils.copyProperties(orderEntity,orderTo);
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
