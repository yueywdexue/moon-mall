package com.yueyedexue.gulimall.cart.controller;

import com.yueyedexue.common.constant.AuthServerConstant;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.cart.interceptor.CartInterceptor;
import com.yueyedexue.gulimall.cart.service.CartService;
import com.yueyedexue.gulimall.cart.vo.Cart;
import com.yueyedexue.gulimall.cart.vo.CartItem;
import com.yueyedexue.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/23 8:04
 **/
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.count(skuId, num);
        return "redirect:http://cart.gulimall.com/cartList.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cartList.html";
    }

    @GetMapping("/checkItem")
    public String check(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked) {
        cartService.check(skuId, checked);
        return "redirect:http://cart.gulimall.com/cartList.html";
    }

    @ResponseBody
    @PostMapping("/checkItem")
    public CartItem checkPost(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked) {

        return cartService.check(skuId, checked);
    }


    /**
     * 在浏览器中有一个cookie:user-key;标识用户身份, 一个月后过期
     * 如果第一次使用购物车功能, 都会给上这个临时身份标识
     * <p>
     * 登录:session中有用户信息
     * 没登录:user-key
     * 第一次:创建一个user-key
     *
     * @return
     */
    @GetMapping("/cartList.html")
    public String cartListPage(Model model) {
        // 想要快速获得得到用户信息, 使用ThreadLocal, 在同一个线程中共享数据, 在拦截器中设置好用户信息
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * RedirectAttributes redirectAttributes;
     * redirectAttributes.addFlashAttribute()将数据放在session中,可以在页面取出, 但是只能使用一次
     * redirectAttributes.addAttribute()将数据拼接在请求路径中
     * @param skuId
     * @param num
     * @param redirectAttributes
     * @return
     */
    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("num") Integer num,
                          RedirectAttributes redirectAttributes) {
        cartService.addToCart(skuId,num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId,
                                   Model model){
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
