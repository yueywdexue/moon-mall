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
         * ????????????????????????, ???????????????????????????, ??????????????????ThreadLocal????????????
         * ????????????, ??????????????????????????????????????????????????????????????????????????????????????????
         */
        // 1 ??????????????????????????????
        List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
        orderConfirmVo.setAddress(address);

        // 2 ??????????????????????????????????????????
        List<OrderItemVo> orderItemVos = cartFeignService.getCurrentUserCartItems();
        orderConfirmVo.setItems(orderItemVos);

        // ??????????????????sku???????????????
        List<Long> skuIds = orderItemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        R r = wareFeignService.hasStockBySkuIds(skuIds);
        List<WareHasStockTo> hasStockTos = r.getData("data", new TypeReference<List<WareHasStockTo>>() {
        });
        if (hasStockTos != null && hasStockTos.size() > 0) {
            Map<Long, Boolean> stocks = hasStockTos.stream().collect(Collectors.toMap(WareHasStockTo::getSkuId, WareHasStockTo::getHasStock));
            orderConfirmVo.setStocks(stocks);
        }


        // 3 ????????????
        orderConfirmVo.setIntegration(memberRespVo.getIntegration());
        // 4 ?????? ???????????? ????????????

        // 5 TODO ????????????
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);
        return orderConfirmVo;
/*        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }, executor);*/

/*        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }, executor);*/

/*        try {
            CompletableFuture.allOf(addressFuture, itemsFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }*/

    }


    // @GlobalTransactional  ??????seata???????????????????????????????????????????????????, ???????????????????????????, ??????????????????????????????
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        submitThreadLocal.set(submitVo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        // ??????: ????????????, ?????????, ?????????, ?????????......
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        // 1 ????????????
        String deleteTokenScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = submitVo.getOrderToken();
        String orderKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId();
        // ??????lua????????????????????????????????? 0 - 1
        Long result = redisTemplate.execute(new DefaultRedisScript<>(deleteTokenScript, Long.class), Collections.singletonList(orderKey), orderToken);
        if (result == 0L) {
            // ????????????
            responseVo.setCode(1);
        } else {
            // ???????????? ????????????, ?????????, ?????????......
            // ????????????
            OrderCreateTo order = createOrder();
            // ??????
            BigDecimal payPrice = order.getOrder().getPayAmount();
            BigDecimal sub = submitVo.getPayPrice();
            if (Math.abs(payPrice.subtract(sub).doubleValue()) < 0.01) {
                // ????????????
                // ????????????
                saveOrder(order);
                // ???????????? ????????????, ????????????????????????
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
                // TODO ???????????????
                R r = wareFeignService.orderLockStock(lockVo);
                // TODO ??????????????????, ???????????????MQ
                rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                if (r.getCode() == 0) {
                    responseVo.setOrder(order.getOrder());
                } else {
                    // ??????????????????
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
            // ??????
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

        //????????????????????????
        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            //??????????????????????????????????????????
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                    .eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntities(orderItemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);
    }

    /**
     * ??????????????????????????????
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        // ????????????????????????
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);
        // ??????????????????
        if (("TRADE_SUCCESS").equals(vo.getTrade_status()) || ("FINISHED").equals(vo.getTrade_status())) {
            // ????????????
            String outTradeNo = vo.getOut_trade_no();// ?????????
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
        // ???????????????
        String orderSn = IdWorker.getTimeId();
        // ????????????
        OrderEntity orderEntity = buildOrder(orderSn);
        // ???????????????????????????
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        // ??????
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
        // ??????????????????
        orderEntity.setTotalAmount(total);
        // ?????????????????? ????????????+??????
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        // ??????????????????
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        // ??????????????????
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setIntegration(gift.intValue());
        // ?????????????????? 0???????????????
        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        // ??????????????????
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        order.setMemberId(memberRespVo.getId());
        order.setMemberUsername(memberRespVo.getNickname());
        // ??????????????????
        order.setCreateTime(new Date());

        OrderSubmitVo orderSubmitVo = submitThreadLocal.get();
        // ??????????????????
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareData = fare.getData(new TypeReference<FareVo>() {
        });
        // ??????????????????
        order.setFreightAmount(fareData.getFare());
        MemberAddressVo address = fareData.getAddress();
        // ?????????????????????
        order.setReceiverCity(address.getCity());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverRegion(address.getRegion());
        // ??????????????????
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // ????????????????????????
        order.setAutoConfirmDay(7);

        // ??????
        return order;
    }

    /**
     * ???????????????????????????
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
     * ?????????????????????
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1 ???????????? v
        // 2 ?????????spu??????
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        SpuInfoVo spuInfoVo = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId());
        itemEntity.setSpuName(spuInfoVo.getSpuName());
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());
        // 3 ?????????sku?????? v
        itemEntity.setSkuId(item.getSkuId());
        itemEntity.setSkuName(item.getTitle());
        itemEntity.setSkuPrice(item.getPrice());
        itemEntity.setSkuQuantity(item.getCount());
        itemEntity.setSkuPic(item.getImage());
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        // 4 ???????????? x
        // 5 ???????????? v
        itemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        itemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        // 6 ????????????????????????
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // ???????????????????????? ??????-?????????
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
        BigDecimal subtract = orign.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}