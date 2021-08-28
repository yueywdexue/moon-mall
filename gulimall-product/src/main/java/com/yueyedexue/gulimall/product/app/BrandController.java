package com.yueyedexue.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.yueyedexue.common.valid.AddGroup;
import com.yueyedexue.common.valid.UpdateGroup;
import com.yueyedexue.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yueyedexue.gulimall.product.entity.BrandEntity;
import com.yueyedexue.gulimall.product.service.BrandService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.R;


/**
 * 品牌
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 18:25:13
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(value = {AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
        /*List<ObjectError> errors = null;
        if (result.hasErrors()) {
            // 如果校验不通过, 将校验信息返回
            errors = result.getAllErrors();
            Map<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach((item) -> {
                String message = item.getDefaultMessage(); // 错误提示信息
                String field = item.getField(); // 错误字段
                map.put(field, message);
            });
            return R.error(400, "校验不通过").put("data", map).put("errors",errors);
        } else {
        }
         */
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(value = {UpdateGroup.class}) @RequestBody BrandEntity brand) {
        // 保证自己修改, 与自己关联的表的数据也会同步修改
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(value = {UpdateStatusGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
