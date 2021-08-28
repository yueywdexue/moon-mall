package com.yueyedexue.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/24 9:59
 **/
@Slf4j
@Configuration
public class MyRabbitMQConfiguration {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void initRabbitTemplate() {
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("correlationData=>{}, ack=>{}, cause=>{}", correlationData, ack, cause);
            }
        });
        // 设置消息抵达队列确认回调, 如果有消息转发到队列失败就会回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("message=>{},replyCode=>{},replyText=>{},exchange=>{},routingKey=>{}", message, replyCode, replyText, exchange, routingKey);
            }
        });
    }
}
