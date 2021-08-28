package com.yueyedexue.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.yueyedexue.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    public static String app_id = "2021000118608688";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCMmI8VP4vaF8aWE2j3mJ/waxGawGaTpCgcb2MYIlUMXiVYEcmo8yL2U29VJ+Nn73F75wV+CGkogOrmlqTGBxSjBiq+BNmrYnkpId9FpCczPtJeqPafW1/LLv1rUGPxcNH/lYzk6W5SlE5tD1SzWyir5F2wuMWQ2CvSdu+YE7y58kBSKGiu1yTLdrTiCmJAmvl8YO9ssrp9Nnv+saieRkyp8I9dUXaVPCSocOGiw2y+59v/+v0Kc4kZo585epwAbKqk9ccshtMKssANkWjob+vQeJi657nMZDyQ4l1Mz5c9lq177IRoYv+7bNISBrzHReqk7sc7VgcoKah7J/g7upy7AgMBAAECggEACn/B/5Y1PV4VKFPevM6a8vsr32CKyG+zxxvSTJUTwM6u/zqk0ocFj1t/rt5BrxtEYDpKQLUkFTA7WmjZeShCNEJbosD9+DxsS9QJtkuh2cPRNdxsPk42gxfJOOBR9k+0ft+OG/IKmQQWLzK74oDmMp/lFzNVHZHnA9p53eNrbn85s6aJ89sHCi32FAGZjfQXwHgJSlqL9ntTsVMnAhoPD6ECc0KyMvbre4f5Fji0xlcJjuVunE/uM1FVZxDWj80Rbgm+uiAk+tKYg2Y9kK0HvC9vriMnrGEPRIU+pIO/PS/DaiZcbiJZYa+suJIes9FVu8AmgYzIj4N0Kcs0xhG2QQKBgQDqIMaHXa+krXdGwHOaVE01hLMqK6bng9Az7/e3mimItG73UhXB3nGfmgNuEB/Bi4Eg2CBhP/8LhhgbL9WL7tGzIYz3Kib4pPDu+noJtMrYPbn+UzzfTYN1LT3mib/h9ogIVkm2ziaUTeN/AxG+QrTh6FBtdSCc/27ALpO89AoyKwKBgQCZuvE6OLogv0QmkwL6lNyVl6aQgZqcahwz79HhrR57rzMFkI+jQrYYi0PixX6U95HWxz1kxnOFq9c4sSAc50Ej6wY48qLa+X/xMUlUKz6Hh+haS+ZkF6+7Rogn/00G73CJN/SwSJfhiiV/Z1CtcTXUr1aRwrBVvjp0G6adWExHsQKBgQCPQzr3/eXKWLLRwlV6q/cKtoaDWW6p38h432+vE1Ay7GWyRLI3jQvt42oR4R/taJW9KUK2/1frD0SU7jI3jA0ZFYCShWKPZy5erTob0lsE8tBqxThMU7wK6YIlHUki1Z3rrBEzIPYObZK1V4V17I+OZH9CWxd3uf1k75tLVfEfPQKBgGnZi2GAT8BekglRblqlhyDFc53Kb2/FrDeiqEwQjI2zO5lYSV6rf4B028BYJelicZuGQmKSNlbS1HUnmKnuHS8jE3zArJvD8XFcc1YuFHbzgGw0GKXQnWKAlR2dLydYVI9W5PdAdLuKCHmp5KYX7m3McPoaKJpRSiRKKbXrZ1JxAoGAdFZNOm4AeAXHosJX6r7KiwTRTwy8iqUEr0shzaCYuO62IPUL7NM3oxzGDf1NXFP4QOMCNPlFHNqnbkaRRA+uYKF9Jc61mdFIyFfphWHkD7qXufc+rxiigJrXyFiFPQrODwm84N6pGh62I4Mxj/lBuYwI4ESUhIieIEGxIfDj02A=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArZWexNPpFkWBFoNN66UIQHh188hSEgHFlXnBcbm5orvSFWlfAXLhkfbZgmPCdP05gSfcwm8wwBsZp0JL1SL6Sa4DBKiy33jBIkQZ9Wkv68JczDnc3o8IOvnJRxlEC/mE6UbhKxIx2i4IN8SLCYxnnAVW5iJfU9RrZWcVUWzwB5EI8+K/fQ7H/AHXGdk+M5nKzgybNA9qW+eqPJiKq022OPf8da/HTUI5kucYUz9VnDK6eKcj2Gt+0l9mbVBXcFkbIRZRoA1Zl7+D+jZHo7X9xh6hcLivTuiOmLvPPmdAGsDkIJ+fq/lNzbDyF7fyadj01WSDgUf7fkrUcyy0ZHgyyQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://mfav1yw7l9.52http.tech/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 自动收单超时时间
    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
