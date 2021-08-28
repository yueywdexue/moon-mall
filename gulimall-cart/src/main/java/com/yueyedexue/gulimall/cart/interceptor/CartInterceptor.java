package com.yueyedexue.gulimall.cart.interceptor;

import com.yueyedexue.common.constant.AuthServerConstant;
import com.yueyedexue.common.constant.CartConstant;
import com.yueyedexue.common.vo.MemberRespVo;
import com.yueyedexue.gulimall.cart.vo.UserInfoTo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/23 8:22
 **/
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /**
     * 在目标方法执行之前将session中的数据进行包装
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);


        UserInfoTo userInfoTo = new UserInfoTo();
        if (attribute != null) {
            if (attribute instanceof MemberRespVo) {
                MemberRespVo member = (MemberRespVo) attribute;
                // 登录了
                userInfoTo.setUserId(member.getId());
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // user-key
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setUserTemp(true);
                }
            }
        }
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 目标方法执行之后, 创建一个临时用户Cookie:user-key
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isUserTemp()) {
            // 如果没有临时用户, 创建一个
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, UUID.randomUUID().toString().replaceAll("-", ""));
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
