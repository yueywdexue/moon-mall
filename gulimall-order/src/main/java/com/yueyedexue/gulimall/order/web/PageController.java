package com.yueyedexue.gulimall.order.web;

import com.yueyedexue.common.constant.AuthServerConstant;
import com.yueyedexue.common.vo.MemberRespVo;
import com.yueyedexue.gulimall.order.entity.OrderEntity;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/24 17:02
 **/
@Controller
public class PageController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String pageList(@PathVariable("page") String page){
//        MemberRespVo attribute = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        return page;
    }

    @ResponseBody
    @GetMapping("/order/mqtest")
    public String testSendDeadOrder(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity);
        return "ok";
    }

}
