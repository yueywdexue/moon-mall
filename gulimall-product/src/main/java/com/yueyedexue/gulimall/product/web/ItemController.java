package com.yueyedexue.gulimall.product.web;

import com.yueyedexue.gulimall.product.config.MyThreadPoolConfiguration;
import com.yueyedexue.gulimall.product.service.SkuInfoService;
import com.yueyedexue.gulimall.product.vo.webvo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/13 10:52
 **/
@Controller
public class ItemController {
    @Resource
    SkuInfoService skuInfoService;


    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemVo skuItem = skuInfoService.getSkuItem(skuId);
        model.addAttribute("item", skuItem);
        return "item";
    }
}
