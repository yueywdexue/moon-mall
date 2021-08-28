package com.yueyedexue.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.yueyedexue.gulimall.product.entity.ProductAttrValueEntity;
import com.yueyedexue.gulimall.product.service.ProductAttrValueService;
import com.yueyedexue.gulimall.product.vo.AttrResponseVo;
import com.yueyedexue.gulimall.product.vo.AttrVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yueyedexue.gulimall.product.service.AttrService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 18:25:12
 */
@RestController
@RequestMapping("product/attr")
@Slf4j
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Resource
    ProductAttrValueService productAttrValueService;

    // /product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> attrValueEntities) {
        productAttrValueService.updateSpuAttr(spuId, attrValueEntities);
        return R.ok();
    }

    // /product/attr/base/listforspu/{spuId}
    @GetMapping("/base/listforspu/{spuId}")
    public R baseListForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> baseListBySpuIdList = attrService.getBaseListBySpuId(spuId);
        return R.ok().put("data", baseListBySpuIdList);
    }

    //    /product/attr/base/list/{catelogId}
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params,
                      @PathVariable("catelogId") Long catelogId,
                      @PathVariable("attrType") String attrType) {
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, attrType);
        return R.ok().put("page", page);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息 /product/attr/info/{attrId}
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
//		AttrEntity attr = attrService.getById(attrId);

        AttrResponseVo attrInfo = attrService.getAttrInfo(attrId);
        log.info("attr: {}", attrInfo);
        return R.ok().put("attr", attrInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) {
//		attrService.updateById(attr);

        attrService.updateAttr(attr);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
