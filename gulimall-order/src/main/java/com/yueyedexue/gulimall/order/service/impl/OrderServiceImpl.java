package com.yueyedexue.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yueyedexue.common.exception.NoStockException;
import com.yueyedexue.common.to.WareHasStockTo;
import com.yueyedexue.common.utils.R;
import com.yueyedexue.common.vo.MemberRespVo;
import com.yueyedexue.gulimall.order.constant.OrderConstant;
import com.yueyedexue.gulimall.order.entity.OrderItemEntity;
import com.yueyedexue.gulimall.order.entity.PaymentInfoEntity;
import com.yueyedexue.gulimall.order.enume.OrderStatusEnum;
import com.yueyedexue.gulimall.order.feign.CartFeignService;
import com.yueyedexue.gulimall.order.feign.MemberFeignService;
import com.yueyedexue.gulimall.order.feign.ProductFeignService;
import com.yueyedexue.gulimall.order.feign.WareFeignService;
import com.yueyedexue.gulimall.order.interceptor.LoginUserInterceptor;
import com.yueyedexue.gulimall.order.service.OrderItemService;
import com.yueyedexue.gulimall.order.service.PaymentInfoService;
import com.yueyedexue.gulimall.order.to.OrderCreateTo;
import com.yueyedexue.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yueyedexue.common.utils.PageUtils;
import com.yueyedexue.common.utils.Query;

import com.yueyedexue.gulimall.order.dao.OrderDao;
import com.yueyedexue.gulimall.order.entity.OrderEntity;
import com.yueyedexue.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitThreadLocal = new ThreadLocal<>();

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo orderConfirm() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        /**
         * 异步执行远程方法, 由于不是用一个线程, 所以不能共享ThreadLocal中的数据
         * 解决方法, 在每个异步任务执行之前将需要共享的数据在异步线程重新设置一遍
         */
        // 1 远程查询所有地址列表
        List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
        orderConfirmVo.setAddress(address);

        // 2 远程查询购物车中选中的购物项
        List<OrderItemVo> orderItemVos = cartFeignService.getCurrentUserCartItems();
        orderConfirmVo.setItems(orderItemVos);

        // 远程查询对应sku是否有库存
        List<Long> skuIds = orderItemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        R r = wareFeignService.hasStockBySkuIds(skuIds);
        List<WareHasStockTo> hasStockTos = r.getData("data", new TypeReference<List<WareHasStockTo>>() {
        });
        if (hasStockTos != null && hasStockTos.size() > 0) {
            Map<Long, Boolean> stocks = hasStockTos.stream().collect(Collectors.toMap(WareHasStockTo::getSkuId, WareHasStockTo::getHasStock));
            orderConfirmVo.setStocks(stocks);
        }


        // 3 优惠价格
        orderConfirmVo.setIntegration(memberRespVo.getIntegration());
        // 4 总价 应付价格 自动计算

        // 5 TODO 防重令牌
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);
        return orderConfirmVo;
/*        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 在当前任务线程设置主线程共享的数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }, executor);*/

/*        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            // 在当前任务线程设置主线程共享的数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }, executor);*/

/*        try {
            CompletableFuture.allOf(addressFuture, itemsFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }*/

    }


    // @GlobalTransactional  使用seata的分布式事务可以解决远程失败的问题, 但是会大大降低性能, 所以使用消息队列来做
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        submitThreadLocal.set(submitVo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        // 下单: 创建订单, 验令牌, 验价格, 锁库存......
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        // 1 验证令牌
        String deleteTokenScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        String orderKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId();
        // 使用lua脚本原子验证和删除令牌 0 - 1
        Long result = redisTemplate.execute(new DefaultRedisScript<>(deleteTokenScript, Long.class), Collections.singletonList(orderKey), orderToken);
        if (result == 0L) {
            // 验证失败
            responseVo.setCode(1);
        } else {
            // 验证成功 创建订单, 验价格, 锁库存......
            // 创建订单
            OrderCreateTo order = createOrder();
            // 验价
            BigDecimal payPrice = order.getOrder().getPayAmount();
            BigDecimal sub = submitVo.getPayPrice();
            if (Math.abs(payPrice.subtract(sub).doubleValue()) < 0.01) {
                // 验价成功
                // 保存订单
                saveOrder(order);
                // 库存锁定 远程调用, 出现异常回滚数据
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                // TODO 远程锁库存
                R r = wareFeignService.orderLockStock(lockVo);
                // TODO 订单创建成功, 发送消息给MQ
                rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                if (r.getCode() == 0) {
                    responseVo.setOrder(order.getOrder());
                } else {
                    // 库存锁定失败
                    responseVo.setCode(3);
                    throw new NoStockException();
                }

            } else {
                responseVo.setCode(2);
            }
        }
        return responseVo;
    }

    @Override
    public OrderEntity getStatusByOrderSn(String orderSn) {
        return baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public int closeOrder(OrderEntity orderEntity) {
        OrderEntity byId = this.getById(orderEntity.getId());
        if (byId.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            // 关单
            OrderEntity entity = new OrderEntity();
            entity.setId(orderEntity.getId());
            entity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(entity);
            return OrderStatusEnum.CANCLED.getCode();
        } else {
            return byId.getStatus();
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getStatusByOrderSn(orderSn);
        payVo.setBody(orderEntity.getNote());
        payVo.setOut_trade_no(orderSn);
        List<OrderItemEntity> orderItemEntityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        String skuName = orderItemEntityList.get(0).getSkuName();
        payVo.setSubject(skuName);
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        //遍历所有订单集合
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            //根据订单号查询订单项里的数据
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntities(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的返回数据
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        // 保存交易流水信息
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);
        // 修改订单状态
        if (("TRADE_SUCCESS").equals(vo.getTrade_status()) || ("FINISHED").equals(vo.getTrade_status())) {
            // 支付成功
            String outTradeNo = vo.getOut_trade_no();// 订单号
            baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }



    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> items = order.getOrderItems();
        orderItemService.saveBatch(items);

    }

    public OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 生成订单号
        String orderSn = IdWorker.getTimeId();
        // 创建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        // 获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        // 验价
        computePrice(orderEntity, itemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);
        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity itemEntity : itemEntities) {
            total = total.add(itemEntity.getRealAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            gift = gift.add(new BigDecimal(itemEntity.getGiftIntegration()));
            growth = growth.add(new BigDecimal(itemEntity.getGiftGrowth()));
        }
        // 设置订单总额
        orderEntity.setTotalAmount(total);
        // 设置应付总额 订单总额+运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        // 设置优惠信息
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        // 设置积分信息
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setIntegration(gift.intValue());
        // 设置删除状态 0代表未删除
        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        // 设置会员信息
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        order.setMemberId(memberRespVo.getId());
        order.setMemberUsername(memberRespVo.getNickname());
        // 设置创建时间
        order.setCreateTime(new Date());

        OrderSubmitVo orderSubmitVo = submitThreadLocal.get();
        // 获取收货信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareData = fare.getData(new TypeReference<FareVo>() {
        });
        // 设置运费信息
        order.setFreightAmount(fareData.getFare());
        MemberAddressVo address = fareData.getAddress();
        // 设置收货人信息
        order.setReceiverCity(address.getCity());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverRegion(address.getRegion());
        // 设置订单状态
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // 设置自动确认时间
        order.setAutoConfirmDay(7);

        // 验价
        return order;
    }

    /**
     * 构建所有订单项数据
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建单个订单项
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1 订单信息 v
        // 2 商品的spu信息
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        SpuInfoVo spuInfoVo = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId());
        itemEntity.setSpuName(spuInfoVo.getSpuName());
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());
        // 3 商品的sku信息 v
        itemEntity.setSkuId(item.getSkuId());
        itemEntity.setSkuName(item.getTitle());
        itemEntity.setSkuPrice(item.getPrice());
        itemEntity.setSkuQuantity(item.getCount());
        itemEntity.setSkuPic(item.getImage());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        // 4 优惠信息 x
        // 5 积分信息 v
        itemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        itemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        // 6 订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // 订单项的实际金额 总价-优惠价
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
        BigDecimal subtract = orign.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}