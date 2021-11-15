package com.payby.gateway.sdk.client;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.payby.gateway.openapi.SgsApi;
import com.payby.gateway.openapi.SgsRequestWrap;
import com.payby.gateway.openapi.SgsResponseWrap;
import com.payby.gateway.openapi.common.PageParam;
import com.payby.gateway.openapi.constant.ProtocolAccessType;
import com.payby.gateway.openapi.constant.ProtocolLangType;
import com.payby.gateway.openapi.model.AccessoryContent;
import com.payby.gateway.openapi.model.AmountDetail;
import com.payby.gateway.openapi.model.ExternalMoney;
import com.payby.gateway.openapi.model.Goods;
import com.payby.gateway.openapi.model.GoodsDetail;
import com.payby.gateway.openapi.model.InappSignContent;
import com.payby.gateway.openapi.model.Receipt;
import com.payby.gateway.openapi.model.TerminalDetail;
import com.payby.gateway.openapi.request.ApplyProtocolRequest;
import com.payby.gateway.openapi.request.CreateReceiptOrderRequest;
import com.payby.gateway.openapi.request.GetAddressRequest;
import com.payby.gateway.openapi.request.GetProtocolRequest;
import com.payby.gateway.openapi.request.GetRefundOrderRequest;
import com.payby.gateway.openapi.request.GetStatementRequest;
import com.payby.gateway.openapi.request.OrderIndexRequest;
import com.payby.gateway.openapi.request.PlaceCryptoOrderRequest;
import com.payby.gateway.openapi.request.PlaceCryptoRefundOrderRequest;
import com.payby.gateway.openapi.request.PlaceOrderRequest;
import com.payby.gateway.openapi.request.PlaceRefundOrderRequest;
import com.payby.gateway.openapi.request.PlaceTransferOrderRequest;
import com.payby.gateway.openapi.request.PlaceTransferToBankOrderRequest;
import com.payby.gateway.openapi.request.QueryCustomerDepositOrderPageRequest;
import com.payby.gateway.openapi.request.ReceiptOrderIndexRequest;
import com.payby.gateway.openapi.response.ApplyProtocolResponse;
import com.payby.gateway.openapi.response.CryptoOrderResponse;
import com.payby.gateway.openapi.response.CryptoRefundOrderResponse;
import com.payby.gateway.openapi.response.GetAddressResponse;
import com.payby.gateway.openapi.response.GetCustomerDepositOrderResponse;
import com.payby.gateway.openapi.response.GetPlaceOrderResponse;
import com.payby.gateway.openapi.response.GetProtocolResponse;
import com.payby.gateway.openapi.response.GetRefundOrderResponse;
import com.payby.gateway.openapi.response.GetTransferOrderResponse;
import com.payby.gateway.openapi.response.GetTransferToBankOrderResponse;
import com.payby.gateway.openapi.response.PlaceCryptoOrderResponse;
import com.payby.gateway.openapi.response.PlaceOrderResponse;
import com.payby.gateway.openapi.response.PlaceRefundOrderResponse;
import com.payby.gateway.openapi.response.PlaceTransferOrderResponse;
import com.payby.gateway.openapi.response.PlaceTransferToBankOrderResponse;
import com.payby.gateway.openapi.response.QueryCustomerDepositOrderPageResponse;
import com.payby.gateway.openapi.response.ReceiptOrderResponse;
import com.payby.gateway.sdk.PayByClient;
import com.payby.gateway.sdk.cert.KeyCert;
import com.payby.gateway.sdk.config.ApiConfig;
import com.payby.gateway.sdk.config.ClientConfig;
import com.payby.gateway.sdk.config.OkHttpClientConfig;
import com.payby.gateway.sdk.misc.util.RsaUtil;
import com.payby.gateway.sdk.misc.util.SignSerializationUtil;

import okhttp3.logging.HttpLoggingInterceptor;

public class PayByDemo {

    public static List<Pair<String, String>> getFixHeaders() {
        List<Pair<String, String>> pairs = new ArrayList<>();
        // setting Partner-Id
        // UAT
        // pairs.add(new ImmutablePair<>("partner-id", "200000057149"));
        // BH
        // pairs.add(new ImmutablePair<>("partner-id", "200000329017"));
        // SIM
        pairs.add(new ImmutablePair<>("partner-id", "200000030907"));
        // setting group-name Optional
        pairs.add(new ImmutablePair<>("group-name", ""));
        // setting branch-name Optional
        pairs.add(new ImmutablePair<>("branch-name", ""));
        return pairs;
    }

    @Test
    public void echoCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        echo();
    }

    @Test
    public void verify()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        String plain =
            "{\"notify_time\":\"20210209123000\",\"acquireOrder\":{\"product\":\"Basic Payment Gateway\",\"orderNo\":\"131612857868059068\",\"paySceneCode\":\"PAYPAGE\",\"subject\":\"PCR Test\",\"accessoryContent\":{\"amountDetail\":{\"amount\":{\"amount\":0.80,\"currency\":\"AED\"},\"vatAmount\":{\"amount\":0.20,\"currency\":\"AED\"}},\"terminalDetail\":{\"storeName\":\"10KLabStoreName\",\"terminalId\":\"10KLabTerminal101\",\"storeId\":\"10KLabStore101\",\"operatorId\":\"10KLabOperator101\",\"merchantName\":\"PayBy\"},\"goodsDetail\":{\"quantity\":1.0000,\"goodsId\":\"PCR_TEST\",\"price\":{\"amount\":4.50,\"currency\":\"AED\"},\"body\":\"PCR Test\",\"goodsName\":\"Covid 19 PCR Test\"}},\"revoked\":\"false\",\"deviceId\":\"10KLabTerminal101\",\"merchantOrderNo\":\"Biogenix9\",\"expiredTime\":1612865066353,\"requestTime\":1612857866353,\"totalAmount\":{\"amount\":1.00,\"currency\":\"AED\"},\"payeeMid\":\"200000042613\",\"notifyUrl\":\"https://odoo.g42.ai/api/payment/notification\",\"paymentInfo\":{\"payChannel\":\"BALANCE\",\"paidTime\":1612857890285,\"payerFeeAmount\":{\"amount\":0.00,\"currency\":\"AED\"},\"paidAmount\":{\"amount\":1.00,\"currency\":\"AED\"},\"payerMid\":\"100000050814\"},\"status\":\"PAID_SUCCESS\"},\"_input_charset\":\"UTF-8\",\"notify_timestamp\":1612859400030,\"notify_id\":\"202102090007745961\"}";
        String sign =
            "PpGldGMUBC41gGHqBbiaDMD6XPPdD9diorASPsHswaJujQoGkKLelyeqm3xdza+GiCEuY+g4urkykx6CBuMddUFhB8iK91dRNScGuD0vUn5YCRF+hT1ecKAI9troPvF88PqW2mxI5/aDQJQ/dAD7FV3lamC/YPDVgUPFJabxZUgS8i9gcLlF/hMirSVOZ+nnhQGAJDC8KyQ8qSg5rb0iGdPHPNdvwFehWIK4UU82sYSx1zLF2JVEbzMyk2vAmTwA37QWBvpJHhsUfn6EbvBE3b9AZ1uxOJSyjEjmPPNOCnIW5IpzpQvEPBXevp+LABH8KxyPtGfOY8jPL0fsHQalPw==";

        // assert verify sign
        Assert.assertTrue(RsaUtil.verifySign(plain, Charset.forName("UTF-8"), sign, payByPubKey));

        GetPlaceOrderResponse callbackOrder = JSON.parseObject(plain, new TypeReference<GetPlaceOrderResponse>() {});

        System.out.println("acquireOrder callback body=>" + callbackOrder);

    }

    @Test
    public void verify1()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        String payByPubKey = new String(
            Files.readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("PayBy_key11.pem").toURI())));
        // String plain =
        // "{\"notify_time\":\"20210908161716\",\"acquireOrder\":{\"product\":\"Basic Payment
        // Gateway\",\"orderNo\":\"131631103323026134\",\"paySceneCode\":\"PAYPAGE\",\"subject\":\"test\",\"accessoryContent\":{\"terminalDetail\":{\"merchantName\":\"Jindou
        // Test\"}},\"revoked\":\"false\",\"merchantOrderNo\":\"20210908201507054899\",\"expiredTime\":1631110507452,\"requestTime\":1631103307452,\"totalAmount\":{\"amount\":200,\"currency\":\"AED\"},\"payeeMid\":\"200000062604\",\"notifyUrl\":\"https://api.testsone.com/third/PayBy/payCallBack.json\",\"paymentInfo\":{\"cardInfo\":{\"expMonth\":\"05\",\"last4\":\"0008\",\"expYear\":\"31\",\"cardType\":\"DC\",\"brand\":\"MASTERCARD\"},\"payChannel\":\"BANKCARD\",\"paidTime\":1631103377845,\"payerFeeAmount\":{\"amount\":0,\"currency\":\"AED\"},\"paidAmount\":{\"amount\":200,\"currency\":\"AED\"},\"payerMid\":\"anonymousMember\"},\"status\":\"PAID_SUCCESS\"},\"_input_charset\":\"UTF-8\",\"notify_timestamp\":1631103436174,\"notify_id\":\"202109080007866651\"}";
        String plain =
            "{\"notify_time\":\"20210908161716\",\"acquireOrder\":{\"product\":\"Basic Payment Gateway\",\"orderNo\":\"131631103323026134\",\"paySceneCode\":\"PAYPAGE\",\"subject\":\"test\",\"accessoryContent\":{\"terminalDetail\":{\"merchantName\":\"Jindou Test\"}},\"revoked\":\"false\",\"merchantOrderNo\":\"20210908201507054899\",\"expiredTime\":1631110507452,\"requestTime\":1631103307452,\"totalAmount\":{\"amount\":200.00,\"currency\":\"AED\"},\"payeeMid\":\"200000062604\",\"notifyUrl\":\"https://api.testsone.com/third/PayBy/payCallBack.json\",\"paymentInfo\":{\"cardInfo\":{\"expMonth\":\"05\",\"last4\":\"0008\",\"expYear\":\"31\",\"cardType\":\"DC\",\"brand\":\"MASTERCARD\"},\"payChannel\":\"BANKCARD\",\"paidTime\":1631103377845,\"payerFeeAmount\":{\"amount\":0.00,\"currency\":\"AED\"},\"paidAmount\":{\"amount\":200.00,\"currency\":\"AED\"},\"payerMid\":\"anonymousMember\"},\"status\":\"PAID_SUCCESS\"},\"_input_charset\":\"UTF-8\",\"notify_timestamp\":1631103436174,\"notify_id\":\"202109080007866651\"}";
        String sign =
            "gHaB6LwCbE9QusYZ7csVAebSdBNxmY2jG8d9Q5Y7RU2lSfAENREGTvdDPlG6FUi0s3IT9C+KzQegJ4qA3gGMsD7MVYUdOClmVVHiIR2Ckbxm5Bpgfyw/l8qWIvxTUjblIDIQeef6t3qUaGDIyTaScufEbBSL2iFQqt1Osvz/tIoyvYMhM3ixaVYyihoAPQELwVlaJ/7hoq47/4Zmk3bPD/eeqP1R2NBhzels7tjcLroczH9Aj2gdF6OPQZM5Q0vpzYSPBCI87cBdhLTMpigZLjml7LFeJqGz8U7SHFty/34SYaSc7IPcndPSVmSLStkd+A7druRqP3zJJHuipewbMQ==";
        // assert verify sign
        Assert.assertTrue(RsaUtil.verifySign(plain, Charset.forName("UTF-8"), sign, payByPubKey));

        GetPlaceOrderResponse callbackOrder = JSON.parseObject(plain, new TypeReference<GetPlaceOrderResponse>() {});

        System.out.println("acquireOrder callback body=>" + callbackOrder);

    }

    @Test
    public void placeOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeOrder();
    }

    @Test
    public void placeCryptoOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeCryptoOrder();
    }

    @Test
    public void cancelCryptoOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeCryptoOrder();

        // case 1
        // cancelCryptoOrderByMerchantOrderNo();
        // case 2
        cancelCryptoOrderByOrderNo();

    }

    @Test
    public void revokeCryptoOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeCryptoOrder();

        // case 1
        // revokeCryptoOrderByMerchantOrderNo();
        // case 2
        revokeCryptoOrderByOrderNo();

    }

    @Test
    public void refundCryptoOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        // case 1
        refundCryptoOrderByMerchantOrderNo();

        // case 2
        // refundCryptoOrderByOrderNo();
    }

    @Test
    public void getRefundCryptoOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        refundCryptoOrderByOrderNo();

        // case 1
        getRefundCryptoOrderByMerchantOrderNo();

        // case 2
        getRefundCryptoOrderByOrderNo();
    }

    @Test
    public void getCryptoOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeCryptoOrder();

        // case 1
        getCryptoOrderByMerchantOrderNo();
        // case 2
        // getCryptoOrderByOrderNo();
    }

    @Test
    public void cancelOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeOrder();

        // case 1
        cancelOrderByMerchantOrderNo();
        // case 2
        // cancelOrderByOrderNo();

    }

    @Test
    public void revokeOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeOrder();

        // case 1
        revokeOrderByMerchantOrderNo();
        // case 2
        // revokeOrderByOrderNo();

    }

    @Test
    public void downloadCase() throws Exception {
        downloadOrderStatement();
        downloadFundStatement();
    }

    @Test
    public void getOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeOrder();

        // case 1
        getOrderByMerchantOrderNo();
        // case 2
        // getOrderByOrderNo();
    }

    @Test
    public void refundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        // case 1
        refundOrderByMerchantOrderNo();

        // case 2
        // refundOrderByOrderNo();
    }

    @Test
    public void getRefundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        refundOrderByOrderNo();

        // case 1
        getRefundOrderByMerchantOrderNo();

        // case 2
        getRefundOrderByOrderNo();
    }

    @Test
    public void transferCase() throws Exception {
        transfer();
    }

    @Test
    public void getTransferOrderCase() throws Exception {
        // transfer();
        getTransferOrder();
    }

    @Test
    public void transfer2bankCase() throws Exception {
        transfer2bank();
    }

    @Test
    public void applyProtocolCase() throws Exception {
        applyProtocol();
    }

    @Test
    public void getProtocolCase() throws Exception {
        applyProtocol();
        getProtocol();
    }

    @Test
    public void getTransferToBankOrderCase() throws Exception {
        transfer2bank();
        getTransferToBankOrder();
    }

    public void placeOrder1()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        // Merchant order number Required
        placeOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // Product name Required
        placeOrderRequest.setSubject("facepay");
        // Order totalAmount Required
        ExternalMoney totalAmount = new ExternalMoney(new BigDecimal("0.1"), "AED");
        placeOrderRequest.setTotalAmount(totalAmount);
        // Payment scenario code Required
        placeOrderRequest.setPaySceneCode("AUTODEBIT");
        // Payment scenario params Optional

        Map<String, String> paySceneParams = new HashMap<String, String>();
        paySceneParams.put("authProtocolNo", "");
        placeOrderRequest.setPaySceneParams(paySceneParams);

        SgsRequestWrap<PlaceOrderRequest> wrap = SgsRequestWrap.wrap(placeOrderRequest);

        System.out.println("placeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceOrderResponse> responseWrap = client.execute(SgsApi.PLACE_ACQUIRE_ORDER, wrap, c -> {
            SgsResponseWrap<PlaceOrderResponse> warp = new SgsResponseWrap<PlaceOrderResponse>();
            warp.setBody(JSON.parseObject(c, PlaceOrderResponse.class));
            return warp;
        });
        System.out.println("placeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceOrderResponse body = responseWrap.getBody();
        System.out.println("placeOrder body=>" + JSON.toJSONString(body));
    }

    public void placeOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        // Merchant order number Required
        placeOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // Product name Required
        placeOrderRequest.setSubject("ipad");
        // Order totalAmount Required
        ExternalMoney totalAmount = new ExternalMoney(new BigDecimal("0.1"), "AED");
        placeOrderRequest.setTotalAmount(totalAmount);
        // Payment scenario code Required
        placeOrderRequest.setPaySceneCode("PAYPAGE");
        // Payment scenario params Optional
        // For payment scenario parameter relationship, please visit https://developers.payby.com/pay
        Map<String, String> paySceneParams = new HashMap<String, String>();
        paySceneParams.put("redirectUrl", "http://www.yoursite.com/web/paydone.html?orderId=414768633924763654");
        placeOrderRequest.setPaySceneParams(paySceneParams);
        // Notification URL Optional
        placeOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeOrderRequest.setReserved("order desc");
        // Accessory content Optional
        AccessoryContent accessoryContent = new AccessoryContent();
        // Amount detail Optional
        AmountDetail amountDetail = new AmountDetail();
        // Vat amount Optional
        amountDetail.setVatAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // Goods detail Optional
        GoodsDetail goodsDetail = new GoodsDetail();
        goodsDetail.setBody("gifts");
        goodsDetail.setGoodsName("candy flower");
        goodsDetail.setGoodsId("GI1005");
        // Terminal detail Optional
        TerminalDetail terminalDetail = new TerminalDetail();
        terminalDetail.setMerchantName("candy home");
        accessoryContent.setAmountDetail(amountDetail);
        accessoryContent.setGoodsDetail(goodsDetail);
        accessoryContent.setTerminalDetail(terminalDetail);
        placeOrderRequest.setAccessoryContent(accessoryContent);

        SgsRequestWrap<PlaceOrderRequest> wrap = SgsRequestWrap.wrap(placeOrderRequest);

        System.out.println("placeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceOrderResponse> responseWrap = client.execute(SgsApi.PLACE_ACQUIRE_ORDER, wrap);
        System.out.println("placeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceOrderResponse body = responseWrap.getBody();
        System.out.println("placeOrder body=>" + JSON.toJSONString(body));
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), placeOrderRequest.getMerchantOrderNo(),
            StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/orderNo.txt"), body.getAcquireOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void placeCryptoOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        PlaceCryptoOrderRequest placeOrderRequest = new PlaceCryptoOrderRequest();
        // Merchant order number Required
        placeOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // Product name Required
        placeOrderRequest.setSubject("ipad");
        // Order totalAmount Required
        ExternalMoney totalAmount = new ExternalMoney(new BigDecimal("0.01"), "BUSD");
        placeOrderRequest.setTotalAmount(totalAmount);
        // Payment scenario code Required
        placeOrderRequest.setPaySceneCode("PAYPAGE");
        // Payment scenario params Optional
        // For payment scenario parameter relationship, please visit https://developers.payby.com/pay
        Map<String, String> paySceneParams = new HashMap<String, String>();
        paySceneParams.put("redirectUrl", "http://www.yoursite.com/web/paydone.html?orderId=414768633924763654");
        placeOrderRequest.setPaySceneParams(paySceneParams);
        // Notification URL Optional
        placeOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeOrderRequest.setReserved("order desc");
        // Accessory content Optional
        com.payby.gateway.openapi.model.crypto.AccessoryContent accessoryContent =
            new com.payby.gateway.openapi.model.crypto.AccessoryContent();
        // Amount detail Optional
        com.payby.gateway.openapi.model.crypto.AmountDetail amountDetail =
            new com.payby.gateway.openapi.model.crypto.AmountDetail();
        // Vat amount Optional
        amountDetail.setVatAmount(new ExternalMoney(new BigDecimal("0.01"), "BUSD"));
        // Goods detail Optional
        com.payby.gateway.openapi.model.crypto.GoodsDetail goodsDetail =
            new com.payby.gateway.openapi.model.crypto.GoodsDetail();
        goodsDetail.setBody("gifts");
        goodsDetail.setGoodsName("candy flower");
        goodsDetail.setGoodsId("GI1005");
        // Terminal detail Optional
        com.payby.gateway.openapi.model.crypto.TerminalDetail terminalDetail =
            new com.payby.gateway.openapi.model.crypto.TerminalDetail();
        terminalDetail.setMerchantName("candy home");
        accessoryContent.setAmountDetail(amountDetail);
        accessoryContent.setGoodsDetail(goodsDetail);
        accessoryContent.setTerminalDetail(terminalDetail);
        placeOrderRequest.setAccessoryContent(accessoryContent);

        SgsRequestWrap<PlaceCryptoOrderRequest> wrap = SgsRequestWrap.wrap(placeOrderRequest);

        System.out.println("placeCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceCryptoOrderResponse> responseWrap = client.execute(SgsApi.PLACE_CRYPTO_ORDER, wrap);
        System.out.println("placeCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceCryptoOrderResponse body = responseWrap.getBody();
        System.out.println("placeCryptoOrder body=>" + JSON.toJSONString(body));
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), placeOrderRequest.getMerchantOrderNo(),
            StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/orderNo.txt"), body.getAcquireOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void placeDirectPayOrder() throws Exception {

        PayByClient client = getPayByClient();

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        // Merchant order number Required
        placeOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // Product name Required
        placeOrderRequest.setSubject("ipad");
        // Order totalAmount Required
        ExternalMoney totalAmount = new ExternalMoney(new BigDecimal("0.1"), "AED");
        placeOrderRequest.setTotalAmount(totalAmount);
        // Payment scenario code Required
        placeOrderRequest.setPaySceneCode("DIRECTPAY");
        // Payment scenario params Optional
        // For payment scenario parameter relationship, please visit https://developers.payby.com/pay
        Map<String, String> paySceneParams = new HashMap<String, String>();

        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        paySceneParams.put("cardNo", RsaUtil.encrypt("20000000001", Charset.forName("UTF-8"), payByPubKey, 2048));
        paySceneParams.put("holderName", RsaUtil.encrypt("wang", Charset.forName("UTF-8"), payByPubKey, 2048));
        paySceneParams.put("cvv", RsaUtil.encrypt("123444", Charset.forName("UTF-8"), payByPubKey, 2048));
        paySceneParams.put("expYear", RsaUtil.encrypt("21", Charset.forName("UTF-8"), payByPubKey, 2048));
        paySceneParams.put("expMonth", RsaUtil.encrypt("12", Charset.forName("UTF-8"), payByPubKey, 2048));
        paySceneParams.put("platformType", "ANDROID");
        paySceneParams.put("realIP", "1.1.1.1");
        paySceneParams.put("customerId", "1234567");
        paySceneParams.put("email", "test@pay.com");
        placeOrderRequest.setPaySceneParams(paySceneParams);
        // Notification URL Optional
        placeOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeOrderRequest.setReserved("order desc");

        SgsRequestWrap<PlaceOrderRequest> wrap = SgsRequestWrap.wrap(placeOrderRequest);

        System.out.println("placeDirectPayOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceOrderResponse> responseWrap = client.execute(SgsApi.PLACE_ACQUIRE_ORDER, wrap);
        System.out.println("placeDirectPayOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceOrderResponse body = responseWrap.getBody();
        System.out.println("placeDirectPayOrder body=>" + JSON.toJSONString(body));
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), placeOrderRequest.getMerchantOrderNo(),
            StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/orderNo.txt"), body.getAcquireOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void refundOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        PlaceRefundOrderRequest placeRefundOrderRequest = new PlaceRefundOrderRequest();
        // Refund refund amount Required
        placeRefundOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // Merchant order number Required
        placeRefundOrderRequest.setRefundMerchantOrderNo(UUID.randomUUID().toString());
        // Original merchant order number
        placeRefundOrderRequest.setOriginMerchantOrderNo(merchantOrderNo);
        // Refund operator name Optional
        placeRefundOrderRequest.setOperatorName("JACKMA");
        // Refund reason name Optional
        placeRefundOrderRequest.setReason("reason123");
        // Notification URL Optional
        placeRefundOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeRefundOrderRequest.setReserved("merchant reserved");

        SgsRequestWrap<PlaceRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceRefundOrderResponse> responseWrap = client.execute(SgsApi.PLACE_REFUND_ORDER, wrap);
        System.out.println("refundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundOrder body=>" + JSON.toJSONString(body));

        FileUtils.writeStringToFile(new File("target/refundMerchantOrderNo.txt"),
            placeRefundOrderRequest.getRefundMerchantOrderNo(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/refundOrderNo.txt"), body.getRefundOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void refundCryptoOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        PlaceCryptoRefundOrderRequest placeRefundOrderRequest = new PlaceCryptoRefundOrderRequest();
        // Refund refund amount Required
        placeRefundOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.01"), "BUSD"));
        // Merchant order number Required
        placeRefundOrderRequest.setRefundMerchantOrderNo(UUID.randomUUID().toString());
        // Original merchant order number
        placeRefundOrderRequest.setOriginMerchantOrderNo(merchantOrderNo);
        // Refund operator name Optional
        placeRefundOrderRequest.setOperatorName("JACKMA");
        // Refund reason name Optional
        placeRefundOrderRequest.setReason("reason123");
        // Notification URL Optional
        placeRefundOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeRefundOrderRequest.setReserved("merchant reserved");

        SgsRequestWrap<PlaceCryptoRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoRefundOrderResponse> responseWrap =
            client.execute(SgsApi.PLACE_CRYPTO_REFUND_ORDER, wrap);
        System.out.println("refundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundOrder body=>" + JSON.toJSONString(body));

        FileUtils.writeStringToFile(new File("target/refundMerchantOrderNo.txt"),
            placeRefundOrderRequest.getRefundMerchantOrderNo(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/refundOrderNo.txt"), body.getRefundOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void refundOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        PlaceRefundOrderRequest placeRefundOrderRequest = new PlaceRefundOrderRequest();
        // Refund refund amount Required
        placeRefundOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // Merchant order number Required
        placeRefundOrderRequest.setRefundMerchantOrderNo(UUID.randomUUID().toString());
        // Original order number
        placeRefundOrderRequest.setOriginOrderNo(orderNo);
        // Refund operator name Optional
        placeRefundOrderRequest.setOperatorName("JACKMA");
        // Refund reason name Optional
        placeRefundOrderRequest.setReason("reason123");
        // Notification URL Optional
        placeRefundOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeRefundOrderRequest.setReserved("merchant reserved");

        SgsRequestWrap<PlaceRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceRefundOrderResponse> responseWrap = client.execute(SgsApi.PLACE_REFUND_ORDER, wrap);
        System.out.println("refundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundOrder body=>" + JSON.toJSONString(body));

        FileUtils.writeStringToFile(new File("target/refundMerchantOrderNo.txt"),
            placeRefundOrderRequest.getRefundMerchantOrderNo(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/refundOrderNo.txt"), body.getRefundOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void refundCryptoOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        PlaceCryptoRefundOrderRequest placeRefundOrderRequest = new PlaceCryptoRefundOrderRequest();
        // Refund refund amount Required
        placeRefundOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // Merchant order number Required
        placeRefundOrderRequest.setRefundMerchantOrderNo(UUID.randomUUID().toString());
        // Original order number
        placeRefundOrderRequest.setOriginOrderNo(orderNo);
        // Refund operator name Optional
        placeRefundOrderRequest.setOperatorName("JACKMA");
        // Refund reason name Optional
        placeRefundOrderRequest.setReason("reason123");
        // Notification URL Optional
        placeRefundOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeRefundOrderRequest.setReserved("merchant reserved");

        SgsRequestWrap<PlaceCryptoRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoRefundOrderResponse> responseWrap =
            client.execute(SgsApi.PLACE_CRYPTO_REFUND_ORDER, wrap);
        System.out.println("refundCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundCryptoOrder body=>" + JSON.toJSONString(body));

        FileUtils.writeStringToFile(new File("target/refundMerchantOrderNo.txt"),
            placeRefundOrderRequest.getRefundMerchantOrderNo(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(new File("target/refundOrderNo.txt"), body.getRefundOrder().getOrderNo(),
            StandardCharsets.UTF_8);
    }

    public void cancelOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_ACQUIRE_ORDER, wrap);
        System.out.println("cancelOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("cancelOrder body=>" + JSON.toJSONString(body));
    }

    public void cancelCryptoOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_CRYPTO_ORDER, wrap);
        System.out.println("cancelCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("cancelCryptoOrder body=>" + JSON.toJSONString(body));
    }

    public void cancelOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo(orderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_ACQUIRE_ORDER, wrap);
        System.out.println("cancelOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("cancelOrder body=>" + JSON.toJSONString(body));

    }

    public void cancelCryptoOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo(orderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_CRYPTO_ORDER, wrap);
        System.out.println("cancelCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("cancelCryptoOrder body=>" + JSON.toJSONString(body));

    }

    public void revokeOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo(orderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_ACQUIRE_ORDER, wrap);
        System.out.println("revokeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("revokeOrder body=>" + JSON.toJSONString(body));

    }

    public void revokeCryptoOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo(orderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_CRYPTO_ORDER, wrap);
        System.out.println("revokeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("revokeOrder body=>" + JSON.toJSONString(body));

    }

    public void revokeOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_ACQUIRE_ORDER, wrap);
        System.out.println("revokeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("revokeOrder body=>" + JSON.toJSONString(body));

    }

    public void revokeCryptoOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_CRYPTO_ORDER, wrap);
        System.out.println("revokeCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("revokeCryptoOrder body=>" + JSON.toJSONString(body));

    }

    public void getOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("36ec1d0d-9b85-4aa8-bc30-1848955b1a6d");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.GET_ACQUIRE_ORDER, wrap);
        System.out.println("getOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("getOrder body=>" + JSON.toJSONString(body));

    }

    public void getCryptoOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_ORDER, wrap);
        System.out.println("getCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoOrder body=>" + JSON.toJSONString(body));

    }

    public void getOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // order number Required
        orderIndexRequest.setOrderNo(orderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.GET_ACQUIRE_ORDER, wrap);
        System.out.println("getOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("getOrder body=>" + JSON.toJSONString(body));

    }

    public void getCryptoOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String orderNo = FileUtils.readFileToString(new File("target/orderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // order number Required
        orderIndexRequest.setOrderNo(orderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_ORDER, wrap);
        System.out.println("getCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoOrder body=>" + JSON.toJSONString(body));

    }

    public void getTransferOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo = "M061660071492";

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);

        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getTransferOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetTransferOrderResponse> responseWrap = client.execute(SgsApi.GET_TRANSFER_ORDER, wrap);
        System.out.println("getTransferOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetTransferOrderResponse body = responseWrap.getBody();
        System.out.println("getTransferOrder body=>" + JSON.toJSONString(body));

    }

    public void getTransferToBankOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);

        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getTransferToBankOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetTransferToBankOrderResponse> responseWrap =
            client.execute(SgsApi.GET_TRANSFER_TO_BANK_ORDER, wrap);
        System.out.println("getTransferToBankOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetTransferToBankOrderResponse body = responseWrap.getBody();
        System.out.println("getTransferToBankOrder body=>" + JSON.toJSONString(body));

    }

    public void getRefundOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/refundMerchantOrderNo.txt"), StandardCharsets.UTF_8);

        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // Merchant order number Required
        orderIndexRequest.setRefundMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_REFUND_ORDER, wrap);
        System.out.println("getRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getRefundOrder body=>" + JSON.toJSONString(body));

    }

    public void getRefundOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String refundOrderNo = FileUtils.readFileToString(new File("target/refundOrderNo.txt"), StandardCharsets.UTF_8);

        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // order number Required
        orderIndexRequest.setOrderNo(refundOrderNo);
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_REFUND_ORDER, wrap);
        System.out.println("getRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getRefundOrder body=>" + JSON.toJSONString(body));

    }

    public void getRefundCryptoOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        // String merchantOrderNo =
        // FileUtils.readFileToString(new File("target/refundMerchantOrderNo.txt"), StandardCharsets.UTF_8);

        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // Merchant order number Required
        orderIndexRequest.setRefundMerchantOrderNo("7c43bd35-bd35-4d95-8042-a44984e92405");
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_REFUND_ORDER, wrap);
        System.out.println("getCryptoRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoRefundOrder body=>" + JSON.toJSONString(body));

    }

    public void getRefundCryptoOrderByOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        // String refundOrderNo = FileUtils.readFileToString(new File("target/refundOrderNo.txt"),
        // StandardCharsets.UTF_8);

        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // order number Required
        orderIndexRequest.setOrderNo("131635819247004207");
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_REFUND_ORDER, wrap);
        System.out.println("getCryptoRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoRefundOrder body=>" + JSON.toJSONString(body));

    }

    public void transfer() throws Exception {

        PayByClient client = getPayByClient();

        PlaceTransferOrderRequest placeTransferOrderRequest = new PlaceTransferOrderRequest();

        // Merchant order number Required
        placeTransferOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // Beneficiary Identity Type Required
        placeTransferOrderRequest.setBeneficiaryIdentityType("PHONE_NO");
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        // Beneficiary Identity
        placeTransferOrderRequest
            .setBeneficiaryIdentity(RsaUtil.encrypt("+971-585812341", Charset.forName("UTF-8"), payByPubKey, 2048));
        placeTransferOrderRequest
            .setBeneficiaryFullName(RsaUtil.encrypt("JACKMA", Charset.forName("UTF-8"), payByPubKey, 2048));

        // Transfer order amount Required
        placeTransferOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));

        // memo Required
        placeTransferOrderRequest.setMemo("Bonus");
        // Notification URL Optional
        placeTransferOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");

        SgsRequestWrap<PlaceTransferOrderRequest> wrap = SgsRequestWrap.wrap(placeTransferOrderRequest);
        System.out.println("transfer request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceTransferOrderResponse> responseWrap = client.execute(SgsApi.PLACE_TRANSFER_ORDER, wrap);
        System.out.println("transfer response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceTransferOrderResponse body = responseWrap.getBody();
        System.out.println("transfer body=>" + JSON.toJSONString(body));

        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"),
            placeTransferOrderRequest.getMerchantOrderNo(), StandardCharsets.UTF_8);
    }

    public static void main(String[] str) throws Exception {
        String key =
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCxpDAKCCFyGUezg2UkA+QSbqCVhrQ0nP1MVqiMNgv5yFnWIinCWStY+IEFaudco42P2cXp8kPfXk1QY4dyiPqoAkYntoAtKAT4Rl/VLYWrKqcqHNJUouQNScOXz3skbAWKbkmJc8/mfyDkI/9fDA+H5Sezh6Wja7GVmO2s9A9DCF0htNycbTIujMQgPB+NvLO6UCJvg+CWKLVTHYd4zMSWEq0+iCCILBG2jYpJ2oX38da2rxBXQMp8u6QckVdI0rA6SaYSlDqBMzh3UcWSpwoHOzDnwZ+5wlKVakTcAAzqZKv3uihngW9c0XdM8mJlOb6UuLs2BHWmmQZBv9LXXb7PAgMBAAECggEAfJiLtU2Un+HJX8jGVOeBvcfoTFhp5wlBxJx3pbh21+DF2iKmJZHDjKjoXfNoiZqRvRbi8T8iifYrxjm5FAWtwc+ug0ZHSuTp73X026INQkwtaaVDhWenCwb08HLvjVFS5ZCLdaRMawA6AqqZAIKGFiwNl6eJ70CnEyVrd0bLuZhmBQHpvprl+z4S2iCNabEVLu6mhZrBgFFlMqfpS8jHR/FsqRRjIM29WoXKA8sZ9IyZ9FRFOHl9Ws2SYcpL3He1WJ2aI3tnF1F7r8Pi8Quu6QOJOVlMr+UGEAQDZAAfs0nDMTGyKUu6K9VGoHy7iO15CwXzogMsrFPAH49Arsk7SQKBgQDYIyK+ud4ABQ5mGt+UKit5Ik7mD3ZHsIphDrBV3juTnxo6bYB/aknoroic45jdgqw51KXED/6IEXtbcW5kKuKTrUH/ry9YZNsmiNn9PaqNGhFcMISEQ+nnbvfTh9iFrCQWNekelqg4ljdQhjSON2Rpy05g003PDlVMWxPLy81PLQKBgQDSZ3p4o1QZBwaEbNRkFH+Y8oFlyVT7mvJxlVXgMGxRMJMTB2El0aFpZ4Zem25Ai0ocJTpFQzeZ1lYaXp92ZrxKgZi5R0+jmnpZOLxYxRdPP9pDIx9g7I4FCdfYOyJ3sQ8calMpOHyRSJRbX5B00b2j0oEiEHlXTwV/tOXYgjSjawKBgA7d+9natyaRMnhbiN2MOQlQDtVMYvpzblFWzVJawwZYf1ldhuU8znCu8L65+R3jiy0/L1fZMczyRuX3W3yQZp154XxQ9JTD+z8fUr9UqpKiJGeO/s6KTWCaBXoLch1fxeuxMaPlARVDX/KuCJdvv/x2+6E1Z7jCZ6Ok8CuojSrRAoGAb5BK0jjYWMTu4bzLCZ4M4k6D6jLNCvRn5W4nsM48QPieo5JgIqt7CObJytRjq3mv/CiJMzym+L07vpO5vhBPeroo/uUfB0Lb6P86b19iMrVKyBqRgtUroORNPOxuJEUQHCMMoWjANFDlyWVstWSZR4pQyn0tueu+oK6MyTaRLq0CgYBUhf4Bx3rFsBPKRTjpRTaC8o7YceTcH/gltkF+r9liSkd+EBnIB4n47JiEhivhgVVATf70vDIp5hMzTFM80fy9X0oycB8Buc1o7pQC4UXbuaOkEEyWjjI5+mI3g175ihPcAnVXl7kYlIx8rlOREGgj/btcCod6OEmsSj0RqPugPg==";
        String a = RsaUtil.decrypt(
            "nNwrRMrSu7ILvXJpUuSjUEHx2HYXfe0Ifq0d0XkZSfIX6t3Si2F9LDv2gGqleLHCLLzkl75xNFVKf0kUQix/g0/2tl7prPcztXT8svv1TrnqVY/r1mqLn0IB2Z07U+7BjYeGziyctcZtSs/Qwp6OydNrxlOrZMobbelRzIQmftRBuQT2ywp0U4+XiwW5g7qO+45pXbYKxiDDW7TqaXX0voYHMU4GhEKxxbS4tqWNHYipg3qhtlxHmc5oy221nV4xXTBm6c8/bgWoo8JjwJ8r4DPQe7hWhnw7thenr6hO2xP+5liwV2JvUiPJi75Wp+3mYOMfB9MEPpAe3H03hw5VpA==",
            Charset.forName("UTF-8"), key, 2048);
        System.out.println(a + "ddd");

    }

    public void transfer2bank() throws Exception {
        PayByClient client = getPayByClient();

        PlaceTransferToBankOrderRequest placeTransferToBankOrderRequest = new PlaceTransferToBankOrderRequest();
        // Merchant order number Required
        placeTransferToBankOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("sim_payby_public_key.pem").toURI())));
        // Holder Name Required
        placeTransferToBankOrderRequest
            .setHolderName(RsaUtil.encrypt("CAN WANG", Charset.forName("UTF-8"), payByPubKey, 2048));
        // Iban Required
        placeTransferToBankOrderRequest
            .setIban(RsaUtil.encrypt("AE470200000012213138001", Charset.forName("UTF-8"), payByPubKey, 2048));
        // BeneficiaryAddress Optional
        placeTransferToBankOrderRequest.setBeneficiaryAddress(
            RsaUtil.encrypt("Liwa Street off Khaliffa Street", Charset.forName("UTF-8"), payByPubKey, 2048));
        // SwiftCode Optional
        placeTransferToBankOrderRequest.setSwiftCode("BBMEAEAD");
        // Transfer order amount Required
        placeTransferToBankOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // memo Required
        placeTransferToBankOrderRequest.setMemo("Bonus");
        // Notification URL Optional
        placeTransferToBankOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");

        SgsRequestWrap<PlaceTransferToBankOrderRequest> wrap = SgsRequestWrap.wrap(placeTransferToBankOrderRequest);
        System.out.println("transfer2bank request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<PlaceTransferToBankOrderResponse> responseWrap =
            client.execute(SgsApi.PLACE_TRANSFER_TO_BANK_ORDER, wrap);
        System.out.println("transfer2bank response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceTransferToBankOrderResponse body = responseWrap.getBody();
        System.out.println("transfer2bank body=>" + JSON.toJSONString(body));
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"),
            placeTransferToBankOrderRequest.getMerchantOrderNo(), StandardCharsets.UTF_8);
    }

    public void downloadOrderStatement() throws Exception {
        GetStatementRequest req = new GetStatementRequest();
        // setting statementDate
        req.setStatementDate("20200605");
        PayByClient client = getPayByClient();
        SgsRequestWrap<GetStatementRequest> wrap = SgsRequestWrap.wrap(req);
        String statementFilePath = "d:\\payby_statement_file";
        File statementFileDir = new File(statementFilePath);
        System.out.println("getOrderStatement request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<File> responseWrap = client.download(SgsApi.GET_ORDER_STATEMENT, wrap, statementFileDir);
        System.out.println("getOrderStatement response=>" + responseWrap);
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        System.out.println("getOrderStatement file path=>" + responseWrap.getBody().getAbsolutePath());
        System.out.println("getOrderStatement file size=>" + responseWrap.getBody().length());
    }

    public void downloadFundStatement() throws Exception {
        GetStatementRequest req = new GetStatementRequest();
        // setting statementDate
        req.setStatementDate("20200605");
        PayByClient client = getPayByClient();
        SgsRequestWrap<GetStatementRequest> wrap = SgsRequestWrap.wrap(req);
        String statementFilePath = "d:\\payby_statement_file";
        File statementFileDir = new File(statementFilePath);
        System.out.println("getFundStatement request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<File> responseWrap = client.download(SgsApi.GET_FUND_STATEMENT, wrap, statementFileDir);
        System.out.println("getFundStatement response=>" + responseWrap);
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        System.out.println("getFundStatement file path=>" + responseWrap.getBody().getAbsolutePath());
        System.out.println("getFundStatement file size=>" + responseWrap.getBody().length());
    }

    public void applyProtocol() throws Exception {
        PayByClient client = getPayByClient();

        ApplyProtocolRequest applyProtocolRequest = new ApplyProtocolRequest();
        // Merchant order number Required
        applyProtocolRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // langType Optional
        applyProtocolRequest.setLangType(ProtocolLangType.EN);
        // expiredTime Optional
        applyProtocolRequest
            .setExpiredTime(Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant()));

        String payByPubKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("sim_200000030907_payby_public_key.pem").toURI())));
        // signerMerchantId Required
        applyProtocolRequest
            .setSignerMerchantId(RsaUtil.encrypt("200000001222", Charset.forName("UTF-8"), payByPubKey, 2048));
        // protocolSceneCode Required
        applyProtocolRequest.setProtocolSceneCode("110");
        // Notification URL Optional
        applyProtocolRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // protocolSceneParams Required
        Map<String, String> protocolSceneParams = new HashMap<String, String>();
        // protocolSceneParams.put("iapDeviceId", "12344");
        // protocolSceneParams.put("appId", "20200923000000211");
        protocolSceneParams.put("redirectUrl", "http://www.163.com");
        applyProtocolRequest.setProtocolSceneParams(protocolSceneParams);

        // accessType Optional
        applyProtocolRequest.setAccessType(ProtocolAccessType.H5);

        SgsRequestWrap<ApplyProtocolRequest> wrap = SgsRequestWrap.wrap(applyProtocolRequest);
        System.out.println("applyProtocol request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<ApplyProtocolResponse> responseWrap = client.execute(SgsApi.APPLY_PROTOCOL, wrap);
        System.out.println("applyProtocol response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        ApplyProtocolResponse body = responseWrap.getBody();
        System.out.println("applyProtocol body=>" + JSON.toJSONString(body));
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), applyProtocolRequest.getMerchantOrderNo(),
            StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public void echo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {
        PayByClient client = getPayByClient();
        String req = "test echo";
        SgsRequestWrap<String> wrap = SgsRequestWrap.wrap(req);
        System.out.println("echo request=>" + JSON.toJSONString(wrap));
        String responseWrap = client.executeEcho(SgsApi.ECHO, req);
        System.out.println("echo response=>" + JSON.toJSONString(responseWrap));
    }

    public void getProtocol()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        GetProtocolRequest getProtocolRequest = new GetProtocolRequest();
        // Merchant order number Required
        getProtocolRequest.setMerchantOrderNo(merchantOrderNo);

        SgsRequestWrap<GetProtocolRequest> wrap = SgsRequestWrap.wrap(getProtocolRequest);
        System.out.println("getProtocol request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetProtocolResponse> responseWrap = client.execute(SgsApi.GET_PROTOCOL, wrap);
        System.out.println("getProtocol response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetProtocolResponse body = responseWrap.getBody();
        System.out.println("getProtocol body=>" + JSON.toJSONString(body));

    }

    public static PayByClient getPayByClient()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {
        ApiConfig apiConfig = new ApiConfig();
        // setting interface url
        // apiConfig.setDomain("https://uat.test2pay.com/sgs/api");
        // apiConfig.setDomain("https://bh-uat.test2pay.com/sgs/api");
        apiConfig.setDomain("https://sim.test2pay.com/sgs/api");

        // setting pkcs8 privateKey path
        String merchantPrivateKey = new String(Files.readAllBytes(Paths
            .get(PayByDemo.class.getClassLoader().getResource("sim_200000030907_merchant_private_key.pem").toURI())));

        // setting publicKey path
        String payByPubKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("sim_200000030907_payby_public_key.pem").toURI())));

        apiConfig.setCert(new KeyCert(merchantPrivateKey, payByPubKey));

        // setting http header params
        apiConfig.setFixHeaders(getFixHeaders());

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        ClientConfig config =
            new OkHttpClientConfig.Builder().interceptor(new OkHttpClientConfig.SignInterceptor(apiConfig.getCert()))
                .interceptor(logInterceptor).apiConfig(apiConfig).build();

        PayByClient client = new PayByClient(config);
        return client;

    }

    @Test
    public void inappSignCase()
        throws IOException, URISyntaxException, InvalidKeyException, InvalidKeySpecException, SignatureException {
        InappSignContent content = new InappSignContent();
        // iapAppId Required
        content.setIapAppId("20200625000000162");
        // iapDeviceId Required
        content.setIapDeviceId("3221448616fc5f7ce939b41c");
        // partnerId Required
        content.setIapPartnerId("200000051604");
        // token Required
        content.setToken("ee99403a-e4b4-478b-b9c8-807afceb0283");
        String merchantPrivateKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("merchant_private_key11.pem").toURI())));
        System.out.println("plain==>" + SignSerializationUtil.serialize(content));
        System.out.println("sign==>" + RsaUtil.sign(content, Charset.forName("UTF-8"), merchantPrivateKey));
        String aa =
            "iapAppId=20200625000000162&iapDeviceId=3221448616fc5f7ce939b41c&iapPartnerId=200000051604&token=ee99403a-e4b4-478b-b9c8-807afceb0283";
        System.out.println("sign==>" + RsaUtil.sign(aa, Charset.forName("UTF-8"), merchantPrivateKey));
        System.out.println(RsaUtil.verifySign(aa, Charset.forName("UTF-8"),
            RsaUtil.sign(aa, Charset.forName("UTF-8"), merchantPrivateKey),
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA808yFySejefemuUQIsTKpdRIyz0+tvO3ytLFEu4/n05SZInHJxLxteQHG9YOA1F60Zju5MHRhT0qQBi4GjRBIXSA2fJywR8aeHSixbfi3J/ai3zCb6ymVoc/DrNQBFvwuq0nB5sscYHbLjPuUPqpqeXqbivqf+iutJEDZO6AFDJaoLWrlK59uChHGGcIzE9mBiPyqZTtMW8SOr6rB+FCfy+PQe8XfuHGDh8FuqjUmAlLlRypMDa5gmhoVl7ArOfWfHlZtnzoJkgN3Ba4+DCATnNLfieeEm6erocmEzc0WZrTckd+uTH3unhgq3jSuchswsxO+1GnJmPhVyGFxif05wIDAQAB"));
    }

    @Test
    public void getAddress()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();
        GetAddressRequest getAddressRequest = new GetAddressRequest();
        getAddressRequest.setAssetCode("ETH");
        getAddressRequest.setCustomerId("test001");
        getAddressRequest.setNetwork("ETH1");

        SgsRequestWrap<GetAddressRequest> wrap = SgsRequestWrap.wrap(getAddressRequest);
        System.out.println("getAddress request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetAddressResponse> responseWrap = client.execute(SgsApi.GET_ADDRESS, wrap);
        System.out.println("getAddress response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetAddressResponse body = responseWrap.getBody();
        System.out.println("getAddress body=>" + JSON.toJSONString(body));
    }

    @Test
    public void getCustomerDepositOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // order number Required
        orderIndexRequest.setOrderNo("20210809000000012");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCustomerDepositOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetCustomerDepositOrderResponse> responseWrap =
            client.execute(SgsApi.GET_CUSTOMER_DEPOSIT_ORDER, wrap);
        System.out.println("getCustomerDepositOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetCustomerDepositOrderResponse body = responseWrap.getBody();
        System.out.println("getCustomerDepositOrder body=>" + JSON.toJSONString(body));
    }

    @Test
    public void queryCustomerDepositOrderPage()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        QueryCustomerDepositOrderPageRequest queryCustomerDepositOrderPageRequest =
            new QueryCustomerDepositOrderPageRequest();
        // startTime Required
        queryCustomerDepositOrderPageRequest.setStartTime(Date.from(Instant.now().plus(Duration.ofHours(-23L))));
        // endTime Required
        queryCustomerDepositOrderPageRequest.setEndTime(Date.from(Instant.now()));

        queryCustomerDepositOrderPageRequest.setPageParam(new PageParam(0, 10));
        SgsRequestWrap<QueryCustomerDepositOrderPageRequest> wrap =
            SgsRequestWrap.wrap(queryCustomerDepositOrderPageRequest);
        System.out.println("queryCustomerDepositOrderPage request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<QueryCustomerDepositOrderPageResponse> responseWrap =
            client.execute(SgsApi.QUERY_CUSTOMER_DEPOSIT_ORDER_PAGE, wrap);
        System.out.println("queryCustomerDepositOrderPage response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        QueryCustomerDepositOrderPageResponse body = responseWrap.getBody();
        System.out.println("queryCustomerDepositOrderPage body=>" + JSON.toJSONString(body));
    }

    @Test
    public void createReceiptOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();
        CreateReceiptOrderRequest createReceiptOrderRequest = new CreateReceiptOrderRequest();
        createReceiptOrderRequest.setReceiverMobileNumber("+971-585660747");
        createReceiptOrderRequest.setReceiverEmail("pdcwb1@163.com");

        Receipt receipt = new Receipt();
        receipt.setAddress("TCA,Abu Dhabi");
        receipt.setName("LuLu Express Fresd Market");
        receipt.setTotalAmount(new ExternalMoney(new BigDecimal("22.30"), "AED"));
        receipt.setRefundNo("662142002053429820210730153140");
        receipt.setTotalBeforeVat(new ExternalMoney(new BigDecimal("22.30"), "AED"));
        receipt.setVatAmount(new ExternalMoney(new BigDecimal("1.10"), "AED"));
        receipt.setVatRate(new BigDecimal("5"));
        receipt.setReceiptNo("534298");
        receipt.setType("Y");;
        Goods goods1 = new Goods();
        goods1.setId("9947345013207");
        goods1.setQuantity(BigDecimal.ONE);
        goods1.setName1("Norwegian Salmon Steak");
        goods1.setName2("ستيك السلمون النرويجي");
        goods1.setAmount(new ExternalMoney(new BigDecimal("13.2"), "AED"));
        Goods goods2 = new Goods();
        goods2.setId("99473450132018");
        goods2.setQuantity(new BigDecimal("5"));
        goods2.setName1("Coca cola 350ml");
        goods2.setName2("كوكا كولا 350 مل");
        goods2.setAmount(new ExternalMoney(new BigDecimal("2"), "AED"));
        receipt.setGoodsList(Arrays.asList(goods1, goods2));
        receipt.setCount(receipt.getGoodsList().size());
        receipt.setDate(new Date());
        receipt.setStore("2142");

        receipt.setCashier("Sajjad Ajij");
        receipt.setCounter("6");
        receipt.setPos("2");
        receipt.setTel("02 3090550");
        receipt.setTrn("100228723100003");
        receipt.setEmail("cutomercareauh@ae.lulumea.com");
        receipt.setNotes(
            "Keep bill for exchange within 7 days.Valid only at issued store.*T&C Apply.Thanks you for shopping. Shop online at www.luluhypermarket.com");

        receipt.setPayAmount(new ExternalMoney(new BigDecimal("23.20"), "AED"));
        receipt.setChangeAmount(new ExternalMoney(BigDecimal.ZERO, "AED"));
        receipt.setPaymentChannel("CASH");
        createReceiptOrderRequest.setReceipt(receipt);

        SgsRequestWrap<CreateReceiptOrderRequest> wrap = SgsRequestWrap.wrap(createReceiptOrderRequest);

        System.out.println("createReceiptOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<ReceiptOrderResponse> responseWrap = client.execute(SgsApi.CREATE_DIGITAL_RECEIPT_ORDER, wrap);
        System.out.println("createReceiptOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        ReceiptOrderResponse body = responseWrap.getBody();
        System.out.println("createReceiptOrder body=>" + JSON.toJSONString(body));
    }

    @Test
    public void getReceiptOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();
        ReceiptOrderIndexRequest orderIndexRequest = new ReceiptOrderIndexRequest();
        // Receipt number Required
        orderIndexRequest.setReceiptNo("534298");
        SgsRequestWrap<ReceiptOrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);

        System.out.println("getReceiptOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<ReceiptOrderResponse> responseWrap = client.execute(SgsApi.GET_DIGITAL_RECEIPT_ORDER, wrap);
        System.out.println("getReceiptOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        ReceiptOrderResponse body = responseWrap.getBody();
        System.out.println("getReceiptOrder body=>" + JSON.toJSONString(body));
    }

    @Test
    public void notifyReceiptOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();
        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("c277ddf7-4c5e-4420-a912-7fcc6ee2f612");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);

        System.out.println("notifyReceiptOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<ReceiptOrderResponse> responseWrap = client.execute(SgsApi.NOTIFY_DIGITAL_RECEIPT_ORDER, wrap);
        System.out.println("notifyReceiptOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        ReceiptOrderResponse body = responseWrap.getBody();
        System.out.println("notifyReceiptOrder body=>" + JSON.toJSONString(body));
    }

}
