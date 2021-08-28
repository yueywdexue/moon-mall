package com.yueyedexue.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;
import com.yueyedexue.gulimall.product.dao.CategoryDao;
import com.yueyedexue.gulimall.product.entity.CategoryEntity;
import com.yueyedexue.gulimall.product.service.CategoryBrandRelationService;
import com.yueyedexue.gulimall.product.service.CategoryService;
import com.yueyedexue.gulimall.product.vo.webvo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    Map<String, Object> cache = new HashMap<>();

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> queryWithTree() {
        // 1. 查出所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. 组装成父子树形结构
        return entities.stream()
                // 2.1 查出所有的一级菜单
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 2.2 递归查询子菜单
                .peek((menu) -> menu.setChildren(getChildrens(menu, entities)))
                // 2.3 对子菜单排序
                .sorted(Comparator.comparingInt(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public Long[] getCatelogPath(Long catelogId) {
        ArrayList<Long> longs = new ArrayList<>();
        ArrayList<Long> catelogPath = findCatelogPath(longs, catelogId);
        Collections.reverse(catelogPath);
        return longs.toArray(new Long[catelogPath.size()]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        // 同步更新关联表
        categoryBrandRelationService.updateCategoryDetail(category.getCatId(), category.getName());

    }

    @Cacheable(value = {"category"}, key = "#root.method.getName()")
    @Override
    public List<CategoryEntity> getCategoryLevel1() {
        System.out.println("getCategoryLevel1");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }


    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = this.getCatalogJsonFromDBWithRedissonLock();
            String catalogJsonString = JSON.toJSONString(catalogJsonFromDB);
            stringRedisTemplate.opsForValue().set("catalogJson", catalogJsonString);
            return catalogJsonFromDB;
        }
//        System.out.println("缓存命中");

        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        Map<String, List<Catelog2Vo>> dataFromDB = null;
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        try {
            dataFromDB = getDataFromDB();
            return dataFromDB;
        } finally {
            lock.unlock();
        }

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        // 占分布式锁 同时设置过期时间, 这个操作必须是原子的

        String uuid = UUID.randomUUID().toString();
        // 自旋占锁
        if (stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS)) {
            System.out.println("获取分布式锁成功");
            // 占锁成功, 执行业务
            Map<String, List<Catelog2Vo>> dataFromDB = getDataFromDB();
            // 删除锁 也必须是原子操作, 判断是自己的锁才能删锁 --> lua脚本
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            Long lock = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            System.out.println("删除分布式锁");
            return dataFromDB;
        } else {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("获取分布式锁失败...等待重试");
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        // 进行双重验证缓存中是否有数据
        if (!StringUtils.isEmpty(catalogJson)) {
//            System.out.println("缓存命中...");
            Map<String, List<Catelog2Vo>> catalogJsonObject = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return catalogJsonObject;
        }
        System.out.println("缓存不命中...将要查询数据库");
        /**
         * 数据库业务优化, 一次将所有数据查出
         */
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        // 1. 获取所有的一级分类
        List<CategoryEntity> categoryLevel1 = getParent_cid(categoryEntities, 0L);
        // 2. 通过一级分类查出二级分类并将其封装成List<Catelog2Vo>
        Map<String, List<Catelog2Vo>> parent_cid = null;
        parent_cid = categoryLevel1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
                    // 2.1 通过一级分类查出二级分类
                    List<CategoryEntity> categoryLevel2 = getParent_cid(categoryEntities, l1.getCatId());

                    List<Catelog2Vo> catelog2Vos = null;
                    if (categoryLevel2 != null) {
                        // 2.2 将其封装成List<Catelog2Vo>
                        catelog2Vos = categoryLevel2.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo();
                            catelog2Vo.setCatalog1Id(l1.getCatId().toString());
                            catelog2Vo.setId(l2.getCatId().toString());
                            catelog2Vo.setName(l2.getName());

                            // 3. 通过二级分类将三级分类封装成List<Catelog2Vo.Catalog3Vo>
                            List<CategoryEntity> categoryLevel3 = getParent_cid(categoryEntities, l2.getCatId());
                            List<Catelog2Vo.Catalog3Vo> catalog3Vos = null;
                            if (categoryLevel3 != null) {
                                catalog3Vos = categoryLevel3.stream().map(l3 -> {
                                    Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo();
                                    catalog3Vo.setCatalog2Id(l2.getCatId().toString());
                                    catalog3Vo.setId(l3.getCatId().toString());
                                    catalog3Vo.setName(l3.getName());
                                    return catalog3Vo;
                                }).collect(Collectors.toList());
                            }
                            catelog2Vo.setCatalog3List(catalog3Vos); //设置三级分类
                            return catelog2Vo; // 返回二级分类三级分类集合
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }
        ));

        String catalogJsonString = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJson", catalogJsonString);
        // 将List<Catelog2Vo>封装成map并返回

        return parent_cid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {
/*        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
        if (catalogJson == null) {

            // 执行业务 ......
            // 加入缓存
            cache.put("catalogJson", parent_cid);
        }
        return catalogJson;*/

        /**
         *
         * 大量请求情况下加锁, 只让一个请求访问数据库, 其他等待, 当将其加入缓存, 释放资源,
         * 双重验证下, 缓存中有数据, 其他请求会中缓存中获得数据, 不会访问数据库
         *
         * 只要是同一把锁, 就可以锁住资源, 在单服务器下这种本地锁的方式可以实现, 解决缓存击穿
         *
         * 在分布式集群情况下, 不同服务器上的不是同一个实例, 不能锁住资源, 这样有多少台机器,
         * 就会有多少请求访问数据库, 就有多少请求访问数据库, 因此就会用到分布式锁
         */
        synchronized (this) {
            return getDataFromDB();
        }
    }

    /**
     * 在查询出来的所有数据中筛选出要的数据
     *
     * @param categoryEntities
     * @param parent_cid
     * @return
     */
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntities, Long parent_cid) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        return collect;
    }

    private ArrayList<Long> findCatelogPath(ArrayList<Long> longs, Long catelogId) {
        longs.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);

        if (categoryEntity.getParentCid() != 0) {
            findCatelogPath(longs, categoryEntity.getParentCid());
        }
        return longs;

    }

    /**
     * 递归查询子菜单
     *
     * @param root 当前菜单
     * @param all  所有菜单
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .peek((menu) -> menu.setChildren(getChildrens(menu, all))) // 递归查询子菜单
                .sorted(Comparator.comparingInt(CategoryEntity::getSort)) // 对子菜单排序
                .collect(Collectors.toList());
    }

}