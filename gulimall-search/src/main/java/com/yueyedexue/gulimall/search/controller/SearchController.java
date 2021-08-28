package com.yueyedexue.gulimall.search.controller;

import com.yueyedexue.gulimall.search.service.MallSearchService;
import com.yueyedexue.gulimall.search.vo.SearchParam;
import com.yueyedexue.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/11 17:22
 **/
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(/*@RequestBody*/ SearchParam param, Model model) {
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
