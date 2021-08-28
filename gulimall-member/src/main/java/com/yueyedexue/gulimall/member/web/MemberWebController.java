package com.yueyedexue.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/28 9:57
 **/
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;


    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum, Model model) {

        // 远程调用订单服务, 获得订单数据 渲染订单列表页
        HashMap<String, Object> map = new HashMap<>();
        map.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(map);
        System.out.println(JSON.toJSONString(r));

        model.addAttribute("orders", r);

        return "orderList";
    }
}
