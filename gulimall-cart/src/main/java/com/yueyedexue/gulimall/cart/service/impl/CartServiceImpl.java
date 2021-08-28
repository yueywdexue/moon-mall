package com.yueyedexue.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.gulimall.cart.feign.ProductServiceFeign;
import com.yueyedexue.gulimall.cart.interceptor.CartInterceptor;
import com.yueyedexue.gulimall.cart.service.CartService;
import com.yueyedexue.gulimall.cart.vo.Cart;
import com.yueyedexue.gulimall.cart.vo.CartItem;
import com.yueyedexue.gulimall.cart.vo.SkuInfoVo;
import com.yueyedexue.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/23 8:01
 **/
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    public static final String CART_PREFIX = "gulimall:cart:";

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    ProductServiceFeign productServiceFeign;

    @Resource
    ThreadPoolExecutor executor;

    @Override

    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        String res = (String) operations.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 如果是redis中没有这个商品, 新增操作
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                // 远程查询商品详情skuInfo
                R skuInfo = productServiceFeign.getSkuInfo(skuId);
                SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                // 设置购物车购物项
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setSkuId(skuId);
                cartItem.setTitle(skuInfoVo.getSkuTitle());
                cartItem.setPrice(skuInfoVo.getPrice());
            }, executor);
            CompletableFuture<Void> getSkuAttrFuture = CompletableFuture.runAsync(() -> {
                // 远程查询sku销售属性
                List<String> skuSaleAttrValues = productServiceFeign.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);
            try {
                CompletableFuture.allOf(getSkuAttrFuture, getSkuInfoFuture).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            // 如果redis中有这个商品, 只用增加商品数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(str, CartItem.class);
    }

    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 登录了
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 如果临时购物车中还有数据, 合并购物车
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(tempCartKey);
            if (cartItems != null && cartItems.size() > 0) {
                for (CartItem cartItem : cartItems) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                // 合并清空临时购物车
                clearCart(tempCartKey);
            }
            // 清空后再获得登录购物车
            List<CartItem> loginCartItems = getCartItems(cartKey);
            cart.setItems(loginCartItems);
        } else {
            // 没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public CartItem check(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        return cartItem;
    }

    @Override
    public void count(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 登录了
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 获得所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            if (cartItems == null) {
                return null;
            }
            return cartItems.stream().peek(cartItem -> {
                // 远程查询商品最新的价格
                cartItem.setPrice(productServiceFeign.getPrice(cartItem.getSkuId()));
            }).filter(CartItem::getCheck).collect(Collectors.toList());
        } else {
            //没登录
            return null;
        }
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = cartOps.values();
        if (values != null && values.size() > 0) {
            return values.stream()
                    .map(obj -> JSON.parseObject(obj.toString(), CartItem.class)).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取到要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }
}
