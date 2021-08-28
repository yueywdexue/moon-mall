package com.yueyedexue.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.gulimall.member.vo.MemberLoginVo;
import com.yueyedexue.gulimall.member.vo.MemberRegisterVo;
import com.yueyedexue.gulimall.member.entity.MemberEntity;
import com.yueyedexue.gulimall.member.exception.PhoneNumExistException;
import com.yueyedexue.gulimall.member.exception.UsernameExistException;
import com.yueyedexue.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:51:52
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkUserNameUnique(String userName) throws UsernameExistException;

    void checkPhoneUnique(String phone) throws PhoneNumExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser);
}

