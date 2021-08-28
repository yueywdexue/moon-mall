package com.yueyedexue.gulimall.product.web;

import com.yueyedexue.gulimall.product.entity.CategoryEntity;
import com.yueyedexue.gulimall.product.service.CategoryService;
import com.yueyedexue.gulimall.product.vo.webvo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/9 14:23
 **/
@Controller
public class indexController {

    @Resource
    CategoryService categoryService;

    @Resource
    RedissonClient redissonClient;


    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities =  categoryService.getCategoryLevel1();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        return categoryService.getCatalogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        RLock mylock = redissonClient.getLock("mylock");
        mylock.lock();
        try {
            try{ TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
            return "hello";
        } finally {
            mylock.unlock();
        }

    }
}
