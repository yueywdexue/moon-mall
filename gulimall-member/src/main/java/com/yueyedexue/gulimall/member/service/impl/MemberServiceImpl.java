package com.yueyedexue.gulimall.member.service.impl;

import com.yueyedexue.gulimall.member.vo.MemberLoginVo;
import com.yueyedexue.gulimall.member.vo.MemberRegisterVo;
import com.yueyedexue.gulimall.member.entity.MemberLevelEntity;
import com.yueyedexue.gulimall.member.exception.PhoneNumExistException;
import com.yueyedexue.gulimall.member.exception.UsernameExistException;
import com.yueyedexue.gulimall.member.service.MemberLevelService;
import com.yueyedexue.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.member.dao.MemberDao;
import com.yueyedexue.gulimall.member.entity.MemberEntity;
import com.yueyedexue.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        MemberLevelEntity default_status = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));

        // 设置会员登记
        memberEntity.setLevelId(default_status.getId());

        // 手机号和用户名作为用户标识, 唯一不能重复
        // 检查用户名和手机号是否重复, 为了controller能感知到检查结果, 使用异常机制
        checkUserNameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());
        // 设置其他会员信息
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());
        // 密码要进行加密存储
        // 使用spring的MD5+盐值加密生成密文
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(passwordEncoder.encode(vo.getPassword()));

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameExistException {
        Integer usernameCount = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (usernameCount > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneNumExistException {
        Integer mobileCount = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobileCount > 0) {
            throw new PhoneNumExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        // 通过账号从数据库中查到会员的密文
        MemberEntity memberEntity = baseMapper.selectOne(
                        new QueryWrapper<MemberEntity>().eq("mobile", loginacct)
                                .or().eq("username", loginacct));
        if (memberEntity == null) {
            // 登录失败, 无此用户
            return null;
        } else {
            // 将登录提交过来的明文和密文进行匹配
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, memberEntity.getPassword());
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getId()));
        if (memberEntity != null) {
            // 该社交账号已经登录过本系统, 更新信息
            MemberEntity updateEntity = new MemberEntity();
            updateEntity.setId(memberEntity.getId());
            updateEntity.setAccessToken(socialUser.getAccessToken());

            baseMapper.updateById(updateEntity);

            memberEntity.setAccessToken(socialUser.getAccessToken());
            return memberEntity;
        } else {
            // 该社交账号是第一次登录本系统, 进行注册
            MemberEntity insertEntity = new MemberEntity();
            insertEntity.setSocialUid(socialUser.getId().toString());
            insertEntity.setAccessToken(socialUser.getAccessToken());
            insertEntity.setNickname(socialUser.getName());
            insertEntity.setLevelId(1L);
            baseMapper.insert(insertEntity);
            return insertEntity;
        }

    }

}