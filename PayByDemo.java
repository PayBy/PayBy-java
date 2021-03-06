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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
import com.payby.gateway.openapi.constant.ProtocolLangType;
import com.payby.gateway.openapi.model.AccessoryContent;
import com.payby.gateway.openapi.model.AmountDetail;
import com.payby.gateway.openapi.model.ExternalMoney;
import com.payby.gateway.openapi.model.GoodsDetail;
import com.payby.gateway.openapi.model.InappSignContent;
import com.payby.gateway.openapi.model.TerminalDetail;
import com.payby.gateway.openapi.request.ApplyProtocolRequest;
import com.payby.gateway.openapi.request.GetProtocolRequest;
import com.payby.gateway.openapi.request.GetStatementRequest;
import com.payby.gateway.openapi.request.OrderIndexRequest;
import com.payby.gateway.openapi.request.PlaceOrderRequest;
import com.payby.gateway.openapi.request.PlaceRefundOrderRequest;
import com.payby.gateway.openapi.request.PlaceTransferOrderRequest;
import com.payby.gateway.openapi.request.PlaceTransferToBankOrderRequest;
import com.payby.gateway.openapi.response.ApplyProtocolResponse;
import com.payby.gateway.openapi.response.GetPlaceOrderResponse;
import com.payby.gateway.openapi.response.GetProtocolResponse;
import com.payby.gateway.openapi.response.GetRefundOrderResponse;
import com.payby.gateway.openapi.response.GetTransferOrderResponse;
import com.payby.gateway.openapi.response.GetTransferToBankOrderResponse;
import com.payby.gateway.openapi.response.PlaceOrderResponse;
import com.payby.gateway.openapi.response.PlaceRefundOrderResponse;
import com.payby.gateway.openapi.response.PlaceTransferOrderResponse;
import com.payby.gateway.openapi.response.PlaceTransferToBankOrderResponse;
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
        pairs.add(new ImmutablePair<>("partner-id", "200000047213"));
        // setting group-name Optional
        pairs.add(new ImmutablePair<>("group-name", ""));
        // setting branch-name Optional
        pairs.add(new ImmutablePair<>("branch-name", ""));
        return pairs;
    }

    @Test
    public void verify()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        String plain =
            "{\"notify_time\":\"20200428133706\",\"acquireOrder\":{\"product\":\"Basic Payment Gateway\",\"orderNo\":\"911588066370006619\",\"paySceneCode\":\"DYNQR\",\"subject\":\"123456\",\"accessoryContent\":{\"amountDetail\":{\"vatAmount\":{\"amount\":0.10,\"currency\":\"AED\"}},\"terminalDetail\":{\"merchantName\":\"binge test merchant\"},\"goodsDetail\":{\"goodsId\":\"GI1005\",\"body\":\"Gifts\",\"goodsName\":\"candy flower\"}},\"merchantOrderNo\":\"eaa16681-070b-4ae6-9b13-809b0db6eb89\",\"expiredTime\":1588073568745,\"requestTime\":1588066368745,\"totalAmount\":{\"amount\":0.10,\"currency\":\"AED\"},\"payeeMid\":\"200000042607\",\"notifyUrl\":\"http://yoursite.com/api/notification\",\"paymentInfo\":{\"payChannel\":\"BALANCE\",\"paidTime\":1588066471000,\"payeeFeeAmount\":{\"amount\":0.01,\"currency\":\"AED\"},\"payerFeeAmount\":{\"amount\":0.00,\"currency\":\"AED\"},\"paidAmount\":{\"amount\":0.10,\"currency\":\"AED\"},\"payerMid\":\"100000001104\"},\"status\":\"PAID_SUCCESS\"},\"_input_charset\":\"UTF-8\",\"notify_timestamp\":1588066626060,\"notify_id\":\"202004280007581901\"}";
        String sign =
            "dPVyhhidZioH00QCVglgDfXNcDXHuMXmtzYF4WCEvnvmL3nCyqP5r9DmQ2bQYOf30tLEpqx1vmJpcT85f8voual7+sKPAehGRbyL9m30BF1KHwOFOhZSnOsBO8NQDJ1WMkG34mRmndfKRWpzi6RzWwfS/twJRXTt7maY2yPt93xhqeb2JbG2hktDFx8tnk3oxXil3oZLFq75X2Gbpd1SkrsnvxTLq5Bo98i4K3Kl4jWySU/vu8nX0M2JPWF6uC3OlOMq32Wo3mDPqM0DzH9t9WBwz2X6MQVnc/aFA5GfJbMKMYFFYwCh9CPtaWbrGmAFXAC9u/sz8bt3IAyiAGdd4w==";

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
        getOrderByOrderNo();
    }

    @Test
    public void refundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        // case 1
        refundOrderByMerchantOrderNo();

        // case 2
        // refundOrderByMerchantOrderNo();
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
        transfer();
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

    public void getOrderByMerchantOrderNo()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.GET_ACQUIRE_ORDER, wrap);
        System.out.println("getOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("getOrder body=>" + JSON.toJSONString(body));

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

    public void getTransferOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

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
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo(merchantOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
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

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // order number Required
        orderIndexRequest.setOrderNo(refundOrderNo);
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_REFUND_ORDER, wrap);
        System.out.println("getRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getRefundOrder body=>" + JSON.toJSONString(body));

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

    public void transfer2bank() throws Exception {
        PayByClient client = getPayByClient();

        PlaceTransferToBankOrderRequest placeTransferToBankOrderRequest = new PlaceTransferToBankOrderRequest();
        // Merchant order number Required
        placeTransferToBankOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        // Holder Name Required
        placeTransferToBankOrderRequest
            .setHolderName(RsaUtil.encrypt("JACKMA", Charset.forName("UTF-8"), payByPubKey, 2048));
        // Iban Required
        placeTransferToBankOrderRequest
            .setIban(RsaUtil.encrypt("5000312313111", Charset.forName("UTF-8"), payByPubKey, 2048));
        // SwiftCode Optional
        placeTransferToBankOrderRequest.setSwiftCode("ARABAEADDER");
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

        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        // signerMerchantId Required
        applyProtocolRequest
            .setSignerMerchantId(RsaUtil.encrypt("200000001222", Charset.forName("UTF-8"), payByPubKey, 2048));
        // protocolSceneCode Required
        applyProtocolRequest.setProtocolSceneCode("110");
        // Notification URL Optional
        applyProtocolRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // protocolSceneParams Required
        Map<String, String> protocolSceneParams = new HashMap<String, String>();
        protocolSceneParams.put("iapDeviceId", "12344");
        protocolSceneParams.put("appId", "20200923000000211");
        applyProtocolRequest.setProtocolSceneParams(protocolSceneParams);

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
        apiConfig.setDomain("https://uat.test2pay.com/sgs/api");

        // setting pkcs8 privateKey path
        String merchantPrivateKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("merchant_private_key.pem").toURI())));

        // setting publicKey path
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));

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

}
