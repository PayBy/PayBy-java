package payby.sgs.demo;

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
import java.util.ArrayList;
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
import com.payby.api.PayByClient;
import com.payby.api.cert.KeyCert;
import com.payby.api.config.ApiConfig;
import com.payby.api.config.ClientConfig;
import com.payby.api.config.OkHttpClientConfig;
import com.payby.api.misc.util.RsaUtil;
import com.payby.api.server.sgs.SgsApi;
import com.payby.api.server.sgs.SgsRequestWrap;
import com.payby.api.server.sgs.SgsResponseWrap;
import com.payby.api.server.sgs.model.AccessoryContent;
import com.payby.api.server.sgs.model.AmountDetail;
import com.payby.api.server.sgs.model.ExternalMoney;
import com.payby.api.server.sgs.model.GoodsDetail;
import com.payby.api.server.sgs.model.TerminalDetail;
import com.payby.api.server.sgs.request.GetStatementRequest;
import com.payby.api.server.sgs.request.OrderIndexRequest;
import com.payby.api.server.sgs.request.PlaceOrderRequest;
import com.payby.api.server.sgs.request.PlaceRefundOrderRequest;
import com.payby.api.server.sgs.request.PlaceTransferOrderRequest;
import com.payby.api.server.sgs.request.PlaceTransferToBankOrderRequest;
import com.payby.api.server.sgs.response.GetPlaceOrderResponse;
import com.payby.api.server.sgs.response.GetRefundOrderResponse;
import com.payby.api.server.sgs.response.GetTransferOrderResponse;
import com.payby.api.server.sgs.response.GetTransferToBankOrderResponse;
import com.payby.api.server.sgs.response.PlaceOrderResponse;
import com.payby.api.server.sgs.response.PlaceRefundOrderResponse;
import com.payby.api.server.sgs.response.PlaceTransferOrderResponse;
import com.payby.api.server.sgs.response.PlaceTransferToBankOrderResponse;

import okhttp3.logging.HttpLoggingInterceptor;

public class PayByDemo {

    public static List<Pair<String, String>> getFixHeaders() {
        List<Pair<String, String>> pairs = new ArrayList<>();
        // setting Partner-Id
        pairs.add(new ImmutablePair<>("partner-id", "200000042607"));
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
        cancelOrder();
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
        getOrder();
    }

    @Test
    public void refundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        refundOrder();
    }

    @Test
    public void getRefundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        refundOrder();
        getRefundOrder();
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
    public void getTransferToBankOrderCase() throws Exception {
        transfer2bank();
        getTransferToBankOrder();
    }

    public void placeOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        PayByClient client = getPayByClient();

        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        Map<String,String> paySceneParams = new HashMap<String,String>();
        paySceneParams.put("redirectUrl", "http://www/notify_url.php");
        placeOrderRequest.setPaySceneParams(paySceneParams);        
		// Merchant order number Required
        placeOrderRequest.setMerchantOrderNo(UUID.randomUUID().toString());
        // Product name Required
        placeOrderRequest.setSubject("ipad");
        // Order totalAmount Required
        ExternalMoney totalAmount = new ExternalMoney(new BigDecimal("0.1"), "AED");
        placeOrderRequest.setTotalAmount(totalAmount);
        // Payment scenario code Required
        placeOrderRequest.setPaySceneCode("DYNQR");
        // Notification URL Optional
        placeOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
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
    }

    public void refundOrder()
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

        SgsRequestWrap<PlaceRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceRefundOrderResponse> responseWrap = client.execute(SgsApi.PLACE_REFUND_ORDER, wrap);
        System.out.println("refundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundOrder body=>" + JSON.toJSONString(body));

        FileUtils.writeStringToFile(new File("target/refundMerchantOrderNo.txt"),
            placeRefundOrderRequest.getRefundMerchantOrderNo(), StandardCharsets.UTF_8);
    }

    public void cancelOrder()
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

    public void getOrder()
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

    public void getRefundOrder()
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
            .setBeneficiaryIdentity(RsaUtil.encrypt("971-585812341", Charset.forName("UTF-8"), payByPubKey, 2048));
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

    public static PayByClient getPayByClient()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {
        ApiConfig apiConfig = new ApiConfig();
        // setting interface url
        apiConfig.setDomain("https://uat.test2pay.com/sgs/api");

        // setting pkcs8 privateKey path
        String merchantPrivateKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("merchant_demo_private.pem").toURI())));

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

}
