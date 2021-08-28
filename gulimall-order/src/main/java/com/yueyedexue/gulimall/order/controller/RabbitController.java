package com.yueyedexue.gulimall.order.controller;

import com.yueyedexue.gulimall.order.entity.OrderEntity;
import com.yueyedexue.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/24 14:37
 **/
@Controller
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/sendMessage")
    public String sendMessage(){
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(1L);
                orderEntity.setCouponId(1L);
                orderEntity.setPayAmount(new BigDecimal("20000"));
                rabbitTemplate.convertAndSend("exchange-java-direct", "queue.java", orderEntity);
            } else {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setName("yueyedeuxue");
                rabbitTemplate.convertAndSend("exchange-java-direct", "ueue.java", reasonEntity);
            }
        }
        return "ok";
    }
}
