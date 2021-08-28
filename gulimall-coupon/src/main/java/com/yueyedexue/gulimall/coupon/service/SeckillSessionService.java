package com.yueyedexue.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:16:27
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

