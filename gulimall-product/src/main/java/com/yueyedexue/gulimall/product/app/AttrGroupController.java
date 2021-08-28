package com.yueyedexue.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.yueyedexue.gulimall.product.entity.AttrEntity;
import com.yueyedexue.gulimall.product.service.AttrAttrgroupRelationService;
import com.yueyedexue.gulimall.product.service.AttrService;
import com.yueyedexue.gulimall.product.service.CategoryService;
import com.yueyedexue.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.yueyedexue.gulimall.product.vo.AttrRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yueyedexue.gulimall.product.entity.AttrGroupEntity;
import com.yueyedexue.gulimall.product.service.AttrGroupService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.R;


/**
 * 属性分组
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 18:25:13
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    // /product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupsWithAttr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> attrsVos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data", attrsVos);
    }

    // /product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R attrRelation(@RequestBody List<AttrRelationVo> attrRelationVoList) {
        relationService.saveBatch(attrRelationVoList);
        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R listCatelogId(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params/*, @PathVariable("catelogId") Long catelogId*/) {
        PageUtils page = attrGroupService.queryPage(params);
//        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }

    //    /product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrEntities = attrService.getRelationByGroupId(attrgroupId);
        return R.ok().put("data", attrEntities);
    }

    //  /product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody AttrRelationVo[] vos){
        relationService.relationDelete(vos);
        return R.ok();
    }

//    /product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils pageUtils = attrService.queryAttrNoRelation(params, attrgroupId);
        return R.ok().put("page", pageUtils);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.getCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
