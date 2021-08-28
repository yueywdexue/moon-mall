package com.yueyedexue.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.yueyedexue.common.exception.BizCodeEnum;
import com.yueyedexue.common.exception.NoStockException;
import com.yueyedexue.common.to.WareHasStockTo;
import com.yueyedexue.gulimall.ware.vo.LockStockResult;
import com.yueyedexue.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yueyedexue.gulimall.ware.entity.WareSkuEntity;
import com.yueyedexue.gulimall.ware.service.WareSkuService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.R;


/**
 * 商品库存
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:42:39
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 锁定库存
     * @param vo
     * @return
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) {
        try {
            boolean stocked = wareSkuService.orderLockStock(vo);
            return R.ok();
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    // 查看当前sku还有没有库存
    @PostMapping("/hasstock")
    public R hasStockBySkuIds(@RequestBody List<Long> skuIds) {
        List<WareHasStockTo> hasStockTos = wareSkuService.hasStockBySkuIds(skuIds);
        return R.ok().setData(hasStockTos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
