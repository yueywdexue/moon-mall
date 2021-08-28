package com.yueyedexue.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:42:39
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

