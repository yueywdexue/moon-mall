package com.yueyedexue.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.yueyedexue.gulimall.order.config.AlipayTemplate;
import com.yueyedexue.gulimall.order.service.OrderService;
import com.yueyedexue.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/28 9:15
 **/
@Controller
public class PayWebController {
    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPay(orderSn);
        // TODO 支付成功更改订单状态
        return alipayTemplate.pay(payVo);
    }
}
