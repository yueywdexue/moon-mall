package com.yueyedexue.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.member.entity.MemberCollectSubjectEntity;

import java.util.Map;

/**
 * 会员收藏的专题活动
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:51:52
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

