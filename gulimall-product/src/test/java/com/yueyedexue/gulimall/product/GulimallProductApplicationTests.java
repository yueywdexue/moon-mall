package com.yueyedexue.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yueyedexue.gulimall.product.dao.SkuSaleAttrValueDao;
import com.yueyedexue.gulimall.product.entity.BrandEntity;
import com.yueyedexue.gulimall.product.service.AttrGroupService;
import com.yueyedexue.gulimall.product.service.BrandService;
import com.yueyedexue.gulimall.product.service.CategoryService;
import com.yueyedexue.gulimall.product.vo.webvo.SkuItemSaleAttrVo;
import com.yueyedexue.gulimall.product.vo.webvo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueDao saleAttrValueDao;

    @Test
    public void testSaleAttr(){
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = saleAttrValueDao.getSaleAttrsBySpuId(6L);
        System.out.println(saleAttrsBySpuId);

    }

    @Test
    public void test(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuIdAndCatelogId = attrGroupService.getAttrGroupWithAttrsBySpuIdAndCatelogId(3L, 225L);
        System.out.println("attrGroups: " + attrGroupWithAttrsBySpuIdAndCatelogId);

    }

    @Test
    public void redisson() {
        System.out.println(redisson);
    }

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("hello", "word" + UUID.randomUUID().toString().substring(0, 5));

        System.out.println(ops.get("hello"));
    }

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.getCatelogPath(225L);
        log.info("完整的路径 {}", Arrays.asList(catelogPath));
    }

    @Test
    public void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(6L);
//        brandEntity.setDescript("华为");
//        brandEntity.setName("华为");
//        brandService.updateById(brandEntity);
        List<BrandEntity> brandId = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        brandId.forEach(System.out::println);
        System.out.println("保存成功!");
    }

}
