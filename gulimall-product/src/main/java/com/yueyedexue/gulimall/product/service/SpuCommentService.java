package com.yueyedexue.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 17:17:11
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

