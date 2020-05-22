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
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.payby.api.cert.KeyCert;
import com.payby.api.client.HttpClient;
import com.payby.api.client.HttpRequest;
import com.payby.api.config.ApiConfig;
import com.payby.api.config.ClientConfig;
import com.payby.api.config.OkHttpClientConfig;
import com.payby.api.misc.util.RsaUtil;

public class PayByDemo {

    List<Pair<String, String>> getFixHeaders() {
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

        System.out.println("verify result=>" + RsaUtil.verifySign(plain, Charset.forName("UTF-8"), sign, payByPubKey));

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
    public void queryOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        placeOrder();
        queryOrder();
    }

    @Test
    public void refundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        refundOrder();
    }

    @Test
    public void queryRefundOrderCase()
        throws InvalidKeyException, InvalidKeySpecException, SignatureException, IOException, URISyntaxException {
        refundOrder();
        queryRefundOrder();
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

        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", UUID.randomUUID().toString());
        // Product name Required
        req.put("subject", "Ipad");
        Map<String, Object> amount = new HashMap<>();
        // Order currency Required
        amount.put("currency", "AED");
        // Order amount Required
        amount.put("amount", new BigDecimal("0.1"));
        req.put("totalAmount", amount);
        // Payment scenario code Required
        req.put("paySceneCode", "DYNQR");
        // Notification URL Optional
        req.put("notifyUrl", "http://yoursite.com/api/notification");
        // Accessory content Optional
        Map<String, Object> accessoryContent = new HashMap<>();
        // Amount detail Optional
        Map<String, Object> amountDetail = new HashMap<>();

        Map<String, Object> vatAmount = new HashMap<>();
        vatAmount.put("currency", "AED");
        vatAmount.put("amount", new BigDecimal("0.1"));
        // Vat amount Optional
        amountDetail.put("vatAmount", vatAmount);
        // Goods detail Optional
        Map<String, Object> goodsDetail = new HashMap<>();
        goodsDetail.put("body", "Gifts");
        goodsDetail.put("goodsName", "candy flower");
        goodsDetail.put("goodsId", "GI1005");
        // Terminal detail Optional
        Map<String, Object> terminalDetail = new HashMap<>();
        terminalDetail.put("merchantName", "candy home");
        accessoryContent.put("amountDetail", amountDetail);
        accessoryContent.put("goodsDetail", goodsDetail);
        accessoryContent.put("terminalDetail", terminalDetail);
        req.put("accessoryContent", accessoryContent);
        wrap.put("bizContent", req);
        System.out.println("placeOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request =
            new HttpRequest.Builder().api("/acquire2/placeOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("placeOrder response=>" + response);
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), req.get("merchantOrderNo").toString(),
            StandardCharsets.UTF_8);
    }

    public void refundOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        HttpClient client = getHttpClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("refundMerchantOrderNo", UUID.randomUUID().toString());
        // Original merchant order number
        req.put("originMerchantOrderNo", merchantOrderNo);
        Map<String, Object> amount = new HashMap<>();
        // Refund order currency Required
        amount.put("currency", "AED");
        // Refund order amount Required
        amount.put("amount", new BigDecimal("0.1"));
        req.put("amount", amount);
        // Refund operator name Optional
        req.put("operatorName", "JACKMA");
        // Refund reason name Optional
        req.put("reason", "reason123");
        // Notification URL Optional
        req.put("notifyUrl", "http://yoursite.com/api/notification");
        wrap.put("bizContent", req);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request =
            new HttpRequest.Builder().api("/acquire2/refund/placeOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("refundOrder response=>" + response);
        FileUtils.writeStringToFile(new File("target/refundMerchantOrderNo.txt"),
            req.get("refundMerchantOrderNo").toString(), StandardCharsets.UTF_8);
    }

    public void cancelOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        HttpClient client = getHttpClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", merchantOrderNo);
        wrap.put("bizContent", req);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request =
            new HttpRequest.Builder().api("/acquire2/cancelOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("cancelOrder response=>" + response);
    }

    public void queryOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        HttpClient client = getHttpClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", merchantOrderNo);
        wrap.put("bizContent", req);
        System.out.println("queryOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request = new HttpRequest.Builder().api("/acquire2/getOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("queryOrder response=>" + response);
    }

    public void getTransferOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        HttpClient client = getHttpClient();
        
        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", merchantOrderNo);
        wrap.put("bizContent", req);
        System.out.println("getTransferOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request =
            new HttpRequest.Builder().api("/transfer/getTransferOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("getTransferOrder response=>" + response);
    }

    public void getTransferToBankOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        
        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);
        
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", merchantOrderNo);
        wrap.put("bizContent", req);
        System.out.println("getTransferToBankOrder request=>" + JSON.toJSONString(wrap));
        HttpRequest request =
            new HttpRequest.Builder().api("/transfer/getTransferToBankOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("getTransferToBankOrder response=>" + response);
    }

    public void queryRefundOrder()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {

        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/refundMerchantOrderNo.txt"), StandardCharsets.UTF_8);

        // Refund merchant order number Required
        req.put("refundMerchantOrderNo", merchantOrderNo);
        wrap.put("bizContent", req);
        System.out.println("queryRefundOrder request=>" + JSON.toJSONString(wrap));
        HttpRequest request =
            new HttpRequest.Builder().api("/acquire2/refund/getOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("queryRefundOrder response=>" + response);
    }

    public void transfer() throws Exception {

        HttpClient client = getHttpClient();
        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", UUID.randomUUID().toString());
        // Beneficiary Identity Type Required
        req.put("beneficiaryIdentityType", "PHONE_NO");
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        // Beneficiary Identity
        req.put("beneficiaryIdentity", RsaUtil.encrypt("971-585812341", Charset.forName("UTF-8"), payByPubKey, 2048));
        req.put("beneficiaryFullName", RsaUtil.encrypt("JACKMA", Charset.forName("UTF-8"), payByPubKey, 2048));

        Map<String, Object> amount = new HashMap<>();
        // Transfer order currency Required
        amount.put("currency", "AED");
        // Transfer order amount Required
        amount.put("amount", new BigDecimal("0.1"));
        req.put("amount", amount);
        // memo Required
        req.put("memo", "Bonus");
        // Notification URL Optional
        req.put("notifyUrl", "http://yoursite.com/api/notification");
        wrap.put("bizContent", req);
        System.out.println("transfer request=>" + JSON.toJSONString(wrap));
        HttpRequest request =
            new HttpRequest.Builder().api("/transfer/placeTransferOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("transfer response=>" + response);
        
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), req.get("merchantOrderNo").toString(),
            StandardCharsets.UTF_8);
    }

    public void transfer2bank() throws Exception {

        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", UUID.randomUUID().toString());
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        // Holder Name Required
        req.put("holderName", RsaUtil.encrypt("JACKMA", Charset.forName("UTF-8"), payByPubKey, 2048));
        // Iban Required
        req.put("Iban", RsaUtil.encrypt("5000312313111", Charset.forName("UTF-8"), payByPubKey, 2048));
        // SwiftCode Optional
        req.put("swiftCode", "ARABAEADDER");

        Map<String, Object> amount = new HashMap<>();
        // Transfer order currency Required
        amount.put("currency", "AED");
        // Transfer order amount Required
        amount.put("amount", new BigDecimal("0.1"));
        req.put("amount", amount);
        // memo Required
        req.put("memo", "Bonus");
        // Notification URL Optional
        req.put("notifyUrl", "http://yoursite.com/api/notification");
        wrap.put("bizContent", req);
        System.out.println("transfer2bank request=>" + JSON.toJSONString(wrap));
        HttpRequest request =
            new HttpRequest.Builder().api("/transfer/placeTransferToBankOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("transfer2bank response=>" + response);
        FileUtils.writeStringToFile(new File("target/merchantOrderNo.txt"), req.get("merchantOrderNo").toString(),
            StandardCharsets.UTF_8);
    }

    private HttpClient getHttpClient()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {
        ApiConfig apiConfig = new ApiConfig();
        // setting interface url
        apiConfig.setDomain("https://sim.test2pay.com/sgs/api");

        // setting pkcs8 privateKey path
        String merchantPrivateKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("merchant_demo_private.pem").toURI())));

        // setting publicKey path
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));

        apiConfig.setCert(new KeyCert(merchantPrivateKey, payByPubKey));

        // setting http header params
        apiConfig.setFixHeaders(getFixHeaders());

        ClientConfig config = new OkHttpClientConfig.Builder()
            .interceptor(new OkHttpClientConfig.SignInterceptor(apiConfig.getCert())).apiConfig(apiConfig).build();

        HttpClient client = new HttpClient(config);
        return client;

    }

}
