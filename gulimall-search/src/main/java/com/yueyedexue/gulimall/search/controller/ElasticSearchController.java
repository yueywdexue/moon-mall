package com.yueyedexue.gulimall.search.controller;

import com.yueyedexue.common.exception.BizCodeEnum;
import com.yueyedexue.common.to.es.SkuEsModel;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.search.service.ProductSaveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/8 10:05
 **/
@RequestMapping("/search/save")
@RestController
public class ElasticSearchController {

    @Resource
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> esModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(esModels);
        } catch (IOException e) {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
