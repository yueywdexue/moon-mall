package com.yueyedexue.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * ้่ดงๅๅ 
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:34:26
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

