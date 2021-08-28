package com.yueyedexue.gulimall.order;

import com.yueyedexue.gulimall.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setCouponId(1L);
        orderEntity.setPayAmount(new BigDecimal("20000"));
        rabbitTemplate.convertAndSend("exchange-java-direct", "queue.java", orderEntity);
    }

    @Test
    public void createExchange() {
        DirectExchange exchange = new DirectExchange("exchange-java-direct", true, false);
        amqpAdmin.declareExchange(exchange);
        log.info("交换机{}创建成功", exchange.getName());
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("queue-java", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("队列{}创建成功", queue.getName());
    }

    @Test
    public void createBinding() {
        Binding binding = new Binding("queue-java",
                Binding.DestinationType.QUEUE,
                "exchange-java-direct",
                "queue.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding创建成功");
    }
}
