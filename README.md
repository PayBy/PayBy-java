# 1  Description

This guide is written for application developers who want to integrate Payby payment solution

# 2 Pre-Condition

## 2.1 JDK

Versions ≥ 1.8 is required.

Executing validation commands on the command line java -version, return similar content:

```shell
java version "1.8.0_191"

Java(TM) SE Runtime Environment (build 1.8.0_191-b12)

Java HotSpot(TM) 64-Bit Server VM (build 25.191-b12, mixed mode)
```



## 2.2 Maven

Versions ≥ 3.0 is required.

Executing validation commands on the command line mvn –version, return similar content:

```shell
Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-25T02:41:47+08:00)

Maven home: D:\apache-maven-3.6.0\bin\..

Java version: 1.8.0_191, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_191\jre

Default locale: zh_CN, platform encoding: GBK

OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
```



## 2.3  Import maven repository

Import local library or remote library, choose by yourself according to the user's R & D environment

### 2.3.1 Download dependency

git clone https://github.com/PayBy/PayBy-java.git

Open download directory: PayBy-java/dependency

### 2.3.2 Install local repository

Move to ‘PayBy-java/dependency’ subdirectory

```shell
mvn install:install-file -Dfile=payby-api-1.1.jar -DpomFile=payby-api-1.1.pom
```



### 2.3.3 Deploy remote repository

```shell
mvn deploy:deploy-file -Durl=company maven repository url path -DrepositoryId=repository name -Dfile=payby-api-1.1.jar -DpomFile=payby-api-1.1.pom
```



## 2.4 Project import maven dependency



```xml
<dependency>
       <groupId>com.payby</groupId>
       <artifactId>payby-api</artifactId>
       <version>1.1</version>
 </dependency>
```



### 2.4.1 Sdk cascade depends on specific content

The specific content varies according to the actual version, execute at project root 

mvn dependency:tree

Get results:

```shell
com.payby:payby-api:jar:1.1

 +- commons-io:commons-io:jar:2.4:compile

 +- commons-codec:commons-codec:jar:1.13:compile

 +- org.projectlombok:lombok:jar:1.18.8:provided

 +- org.bouncycastle:bcprov-jdk15on:jar:1.64:compile

 +- org.bouncycastle:bcpkix-jdk15on:jar:1.64:compile

 +- com.alibaba:fastjson:jar:1.2.58:compile

 +- org.slf4j:slf4j-api:jar:1.7.26:compile

 +- org.apache.commons:commons-lang3:jar:3.9:compile

 +- com.squareup.okhttp3:okhttp:jar:3.11.0:compile

 | \- com.squareup.okio:okio:jar:1.14.0:compile

 +- org.apache.httpcomponents:httpclient:jar:4.5.6:compile

 | +- org.apache.httpcomponents:httpcore:jar:4.4.10:compile

 | \- commons-logging:commons-logging:jar:1.2:compile

 \- com.madgag.spongycastle:bcpkix-jdk15on:jar:1.58.0.0:compile

  +- com.madgag.spongycastle:core:jar:1.58.0.0:compile

  \- com.madgag.spongycastle:prov:jar:1.58.0.0:compile

     \- junit:junit:jar:4.12:compile

      \- org.hamcrest:hamcrest-core:jar:1.3:compile
```

 

# 3  Digital signature

## 3.1  Preparing keys



Recommended operation for private key generation

```shell
### Generate private key
# PayBy_key.pem Private key file name
# 2048 Private key size, at least 2048
openssl genrsa -out PayBy_key.pem 2048

### Export public key
# PayBy_key.pem Private key generated in the previous step # PayBy_key_public.pem Exported public key name
openssl rsa -in PayBy_key.pem -out PayBy_key_public.pem -pubout

### Export private key for Java
# PayBy_key.pem Private key generated in the 1st step
# PayBy_key_Private.pem
openssl pkcs8 -in PayBy_key.pem -topk8 -nocrypt -out PayBy_key_private.pem

```

## 3.2  Signature algorithm

1. Using SHA256WithRSA for signature algorithm. The private key is issued by the merchant itself.
2. Original signature rule: Requesting the original content of the body.
3. Using UTF-8 to encode the original content.
4. Using Base64 to encode the resulting signature.

## 3.3  Encryption Algorithm

1. The encryption algorithm uses RSA public key encryption, and the public key is issued by PayBy.
2. The encrypted field should not be too large, generally no more than 200 bytes.
3. Encryption rules: RSA (encrypted original encryption).
4. UTF-8 encoding is used for plaintext.
5. The encryption result is encoded with Base64.

## 3.4  Verify Signature Algorithm

1. Using SHA256WithRSA for verify signature algorithm. The rsa public key is downloaded from the payby merchant console.
2. Using Base64 to decode the signature, ie. decoded_sign_data.
3. Using UTF-8 to decode the original content, ie. decoded_content_data
4. verify signature using parameters as listed (rsa_public_key, decoded_sign_data, decoded_content_data).

 

# 4   API description

## 4.1  Function description

### 4.1.1 HttpClient

```java
public static HttpClient getHttpClient()
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
```





### 4.1.2  Order creation

```java
        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo","M202005120001");
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

```





### 4.1.3  Order cancellation

```java
        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", "M202005120001");
        wrap.put("bizContent", req);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request =
            new HttpRequest.Builder().api("/acquire2/cancelOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("cancelOrder response=>" + response);
```

 

### 4.1.4  Order query

```java
       HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", "M202005120001");
        wrap.put("bizContent", req);
        System.out.println("queryOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request = new HttpRequest.Builder().api("/acquire2/getOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("queryOrder response=>" + response);
```

 

### 4.1.5  Order refund

```java
        HttpClient client = getHttpClient();

        String merchantOrderNo =
            FileUtils.readFileToString(new File("target/merchantOrderNo.txt"), StandardCharsets.UTF_8);

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("refundMerchantOrderNo", "M220000000001");
        // Original merchant order number Required
        req.put("originMerchantOrderNo", "M80000000001");
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
```



### 4.1.6  Order refund query

```java
        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();


        // Refund merchant order number Required
        req.put("refundMerchantOrderNo", "M220000000001");
        wrap.put("bizContent", req);
        System.out.println("queryRefundOrder request=>" + JSON.toJSONString(wrap));
        HttpRequest request =
            new HttpRequest.Builder().api("/acquire2/refund/getOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("queryRefundOrder response=>" + response);
```

 

### 4.1.7  Transfer

```java
        HttpClient client = getHttpClient();
        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", "M320000000001");
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
```



### 4.1.8  Transfer query

```java
        HttpClient client = getHttpClient();
        
        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", "M320000000001");
        wrap.put("bizContent", req);
        System.out.println("getTransferOrder request=>" + JSON.toJSONString(wrap));

        HttpRequest request =
            new HttpRequest.Builder().api("/transfer/getTransferOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("getTransferOrder response=>" + response);
```

 

### 4.1.9  Transfer to bank

```java
        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", "M320000000002");
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
```



### 4.1.10  Transfer to bank query

```java
        HttpClient client = getHttpClient();

        Map<String, Object> wrap = new HashMap<>();
        // Request time Required
        wrap.put("requestTime", System.currentTimeMillis());
        Map<String, Object> req = new HashMap<>();
        // Merchant order number Required
        req.put("merchantOrderNo", "M320000000002");
        wrap.put("bizContent", req);
        System.out.println("getTransferToBankOrder request=>" + JSON.toJSONString(wrap));
        HttpRequest request =
            new HttpRequest.Builder().api("/transfer/getTransferToBankOrder").body(JSON.toJSONBytes(wrap)).build();

        String response = client.execute(request);
        System.out.println("getTransferToBankOrder response=>" + response);
```

 

## 4.2   Result notification

### 4.2.1  Verify signature

```java
// setting payby publicKey path        
String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));
        String plain =
            "{\"notify_time\":\"20200428133706\",\"acquireOrder\":{\"product\":\"Basic Payment Gateway\",\"orderNo\":\"911588066370006619\",\"paySceneCode\":\"DYNQR\",\"subject\":\"123456\",\"accessoryContent\":{\"amountDetail\":{\"vatAmount\":{\"amount\":0.10,\"currency\":\"AED\"}},\"terminalDetail\":{\"merchantName\":\"binge test merchant\"},\"goodsDetail\":{\"goodsId\":\"GI1005\",\"body\":\"Gifts\",\"goodsName\":\"candy flower\"}},\"merchantOrderNo\":\"eaa16681-070b-4ae6-9b13-809b0db6eb89\",\"expiredTime\":1588073568745,\"requestTime\":1588066368745,\"totalAmount\":{\"amount\":0.10,\"currency\":\"AED\"},\"payeeMid\":\"200000042607\",\"notifyUrl\":\"http://yoursite.com/api/notification\",\"paymentInfo\":{\"payChannel\":\"BALANCE\",\"paidTime\":1588066471000,\"payeeFeeAmount\":{\"amount\":0.01,\"currency\":\"AED\"},\"payerFeeAmount\":{\"amount\":0.00,\"currency\":\"AED\"},\"paidAmount\":{\"amount\":0.10,\"currency\":\"AED\"},\"payerMid\":\"100000001104\"},\"status\":\"PAID_SUCCESS\"},\"_input_charset\":\"UTF-8\",\"notify_timestamp\":1588066626060,\"notify_id\":\"202004280007581901\"}";
        String sign =
            "dPVyhhidZioH00QCVglgDfXNcDXHuMXmtzYF4WCEvnvmL3nCyqP5r9DmQ2bQYOf30tLEpqx1vmJpcT85f8voual7+sKPAehGRbyL9m30BF1KHwOFOhZSnOsBO8NQDJ1WMkG34mRmndfKRWpzi6RzWwfS/twJRXTt7maY2yPt93xhqeb2JbG2hktDFx8tnk3oxXil3oZLFq75X2Gbpd1SkrsnvxTLq5Bo98i4K3Kl4jWySU/vu8nX0M2JPWF6uC3OlOMq32Wo3mDPqM0DzH9t9WBwz2X6MQVnc/aFA5GfJbMKMYFFYwCh9CPtaWbrGmAFXAC9u/sz8bt3IAyiAGdd4w==";

        System.out.println("verify result=>" + RsaUtil.verifySign(plain, Charset.forName("UTF-8"), sign, payByPubKey));

```

  

### 4.2.2  Servlet receive messages（springmvc）

```java
 @Override
 protected void  doPost(HttpServletRequest req, HttpServletResponse httpResponse) throws ServletException, IOException {

    String sign = req.getHeader("sign");

    String body = IOUtils.toString(req.getInputStream(), "UTF-8");

    // setting payby publicKey path

     String payByPubKey = new String(Files.readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));

     System.out.println("verify result=>" + RsaUtil.verifySign(body, Charset.forName("UTF-8"), sign, payByPubKey));

     //TODO Business logic
       
     
     httpResponse.setContentType("application/json;charset=UTF-8");

     httpResponse.getOutputStream().write("SUCCESS".getBytes("UTF-8"));

     httpResponse.flushBuffer();

  }
```

 