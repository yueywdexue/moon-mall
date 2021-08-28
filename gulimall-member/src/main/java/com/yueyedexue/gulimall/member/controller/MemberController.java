package com.yueyedexue.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.yueyedexue.common.exception.BizCodeEnum;
import com.yueyedexue.gulimall.member.vo.MemberLoginVo;
import com.yueyedexue.gulimall.member.vo.MemberRegisterVo;
import com.yueyedexue.gulimall.member.exception.PhoneNumExistException;
import com.yueyedexue.gulimall.member.exception.UsernameExistException;
import com.yueyedexue.gulimall.member.feign.CouponFeignService;
import com.yueyedexue.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yueyedexue.gulimall.member.entity.MemberEntity;
import com.yueyedexue.gulimall.member.service.MemberService;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.R;


/**
 * 会员
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:51:52
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/oauth/login")
    public R login(@RequestBody SocialUser socialUser) {
        // 判断是不是第一次登录
        // 第一次登录, 执行注册流程, 创建一个会员账号与当前社交账号关联起来
        MemberEntity memberEntity = memberService.login(socialUser);
        if (memberEntity != null) {
            return R.ok().put("data",memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity != null) {
            return R.ok().put("data",memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }

    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (UsernameExistException e) {
            R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneNumExistException e) {
            R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    @RequestMapping("/coupons")
    public R testFeignInterface() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R r = couponFeignService.memberCoupon();
        r.get("couponEntity");
        return R.ok().put("member", memberEntity).put("couponEntity", r);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
