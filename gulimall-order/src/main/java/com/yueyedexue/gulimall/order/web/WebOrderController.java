package com.yueyedexue.gulimall.order.web;

import com.yueyedexue.common.exception.NoStockException;
import com.yueyedexue.gulimall.order.entity.OrderSettingEntity;
import com.yueyedexue.gulimall.order.service.OrderService;
import com.yueyedexue.gulimall.order.vo.OrderConfirmVo;
import com.yueyedexue.gulimall.order.vo.OrderItemVo;
import com.yueyedexue.gulimall.order.vo.OrderSubmitVo;
import com.yueyedexue.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/25 8:58
 **/
@Controller
public class WebOrderController {
    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo orderConfirmVo = orderService.orderConfirm();
        model.addAttribute("orderConfirmData", orderConfirmVo);
//        System.out.println(orderConfirmVo);
        // 返回页面要展示的数据
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
            // 下单: 创建订单, 验令牌, 验价格, 锁库存......
            if (responseVo.getCode() == 0) {
                // 下单成功来到支付选择页
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            } else {
                String msg = "下单失败: ";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += "令牌信息过期, 请重新提交";
                        break;
                    case 2:
                        msg += "商品价格有变化, 请重新确认";
                        break;
                    case 3:
                        msg += "库存不足";
                        break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                // 下单失败回到订单确认页
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }

    }
}
