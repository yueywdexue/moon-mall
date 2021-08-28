package com.yueyedexue.gulimall.cart.service;

import com.yueyedexue.gulimall.cart.vo.Cart;
import com.yueyedexue.gulimall.cart.vo.CartItem;

import java.util.List;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num);

    /**
     * 获取购物车中的某个购物xiang
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);


    Cart getCart();

    void clearCart(String cartKey);

    CartItem check(Long skuId, Integer checked);

    void count(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCurrentUserCartItems();
}
