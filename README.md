### 1  Description

This guide is written for application developers who want to integrate PayBy payment solution

### 2 Pre-Condition

#### 2.1 JDK

Versions ≥ 1.8 is required.

Executing validation commands on the command line **java -version**, return similar content:

```shell
java version "1.8.0_191"

Java(TM) SE Runtime Environment (build 1.8.0_191-b12)

Java HotSpot(TM) 64-Bit Server VM (build 25.191-b12, mixed mode)
```



#### 2.2 Maven

Versions ≥ 3.0 is required.

Executing validation commands on the command line **mvn –version**, return similar content:

```shell
Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-25T02:41:47+08:00)

Maven home: D:\apache-maven-3.6.0\bin\..

Java version: 1.8.0_191, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_191\jre

Default locale: zh_CN, platform encoding: GBK

OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
```



#### 2.3  Import maven repository

Import local library or remote library, choose by yourself according to the user's R & D environment

##### 2.3.1 Download dependency

git clone https://github.com/PayBy/PayBy-java.git

Open download directory: PayBy-java/dependency

##### 2.3.2 Install local repository

Move to ‘PayBy-java/dependency’ subdirectory

```shell
mvn install:install-file -Dfile=payby-openapi-1.0.26.jar -DpomFile=payby-openapi-1.0.26.pom
mvn install:install-file -Dfile=payby-sdk-1.3.30.jar -DpomFile=payby-sdk-1.3.30.pom
```



##### 2.3.3 Deploy remote repository

```shell
mvn deploy:deploy-file -Durl=company maven repository url path -DrepositoryId=repository name -Dfile=payby-openapi-1.0.26.jar -DpomFile=payby-openapi-1.0.26.pom
mvn deploy:deploy-file -Durl=company maven repository url path -DrepositoryId=repository name -Dfile=payby-sdk-1.3.30.jar -DpomFile=payby-sdk-1.3.30.pom
```



#### 2.4 Project import maven dependency



```xml
<dependency>
       	<groupId>com.payby.gateway</groupId>
		<artifactId>payby-sdk</artifactId>
		<version>1.3.30</version>
 </dependency>
```



##### 2.4.1 Sdk cascade depends on specific content

The specific content varies according to the actual version, execute at project root 

```shell
mvn dependency:tree
```



Get results:

```shell
com.payby.gateway:payby-sdk:jar:1.3.30
 +- com.payby.gateway:payby-openapi:jar:1.0.26:compile
 +- commons-io:commons-io:jar:2.4:compile
 +- commons-codec:commons-codec:jar:1.13:compile
 +- org.projectlombok:lombok:jar:1.18.8:provided
 +- org.bouncycastle:bcprov-jdk15on:jar:1.64:compile
 +- org.bouncycastle:bcpkix-jdk15on:jar:1.64:compile
 +- com.alibaba:fastjson:jar:1.2.58:compile
 +- org.slf4j:slf4j-api:jar:1.7.26:compile
 +- org.apache.commons:commons-lang3:jar:3.9:compile
 +- com.squareup.okhttp3:okhttp:jar:3.11.0:compile
 |  \- com.squareup.okio:okio:jar:1.14.0:compile
 +- org.apache.httpcomponents:httpclient:jar:4.5.6:compile
 +- org.apache.httpcomponents:httpcore:jar:4.4.10:compile
 |  \- commons-logging:commons-logging:jar:1.2:compile
 +- com.madgag.spongycastle:bcpkix-jdk15on:jar:1.58.0.0:compile
 |  +- com.madgag.spongycastle:core:jar:1.58.0.0:compile
 |  \- com.madgag.spongycastle:prov:jar:1.58.0.0:compile
 |     \- junit:junit:jar:4.12:compile
 |        \- org.hamcrest:hamcrest-core:jar:1.3:compile
 +- ch.qos.logback:logback-classic:jar:1.2.3:compile
 |  \- ch.qos.logback:logback-core:jar:1.2.3:compile
 \- com.squareup.okhttp3:logging-interceptor:jar:3.11.0:compile
```

#####  2.4.2 Bouncycastle version special description

Because many versions of Bouncycastle Library, The version we use:

```xml
		<dependency>
			<groupId>org.bouncycastle</groupId>
			<artifactId>bcprov-jdk15on</artifactId>
			<version>1.64</version>
			<scope>compile</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.bouncycastle</groupId>
			<artifactId>bcpkix-jdk15on</artifactId>
			<version>1.64</version>
			<scope>compile</scope>
			<optional>true</optional>
		</dependency>
```

Users can switch to other equivalent Maven library dependencies according to the actual situation.

Using view query provided by mvn dependency.

The BC dependency used by the host project. If there is a conflict, you can consider using the Maven exclude method to remove the old version

### 3  Digital signature

#### 3.1  Preparing keys



Recommended operation for private key generation

```shell
### Generate private key
# merchant_key.pem Private key file name
# 2048 Private key size, at least 2048
openssl genrsa -out merchant_key.pem 2048

### Export merchant public key
# merchant_key.pem Private key generated in the previous step # merchant_public_key.pem Exported public key name
openssl rsa -in merchant_key.pem -out merchant_public_key.pem -pubout

### Export private key for Java
# merchant_key.pem Private key generated in the 1st step
# merchant_private_key.pem
openssl pkcs8 -in merchant_key.pem -topk8 -nocrypt -out merchant_private_key.pem

```

#### 3.2  Signature algorithm

1. Using SHA256WithRSA for signature algorithm. The private key is issued by the merchant itself.
2. Original signature rule: Requesting the original content of the body.
3. Using UTF-8 to encode the original content.
4. Using Base64 to encode the resulting signature.

#### 3.3  Encryption Algorithm

1. The encryption algorithm uses RSA public key encryption, and the public key is issued by PayBy.
2. The encrypted field should not be too large, generally any more than 200 bytes.
3. Encryption rules: RSA (encrypted original encryption).
4. UTF-8 encoding is used for plaintext.
5. The encryption result is encoded with Base64.

#### 3.4  Verify Signature Algorithm

1. Using SHA256WithRSA for verify signature algorithm. The rsa public key is downloaded from the payby merchant console.
2. Using Base64 to decode the signature, ie. decoded_sign_data.
3. Using UTF-8 to decode the original content, ie. decoded_content_data.
4. verify signature using parameters as listed (rsa_public_key, decoded_sign_data, decoded_content_data).

 

### 4   API description

#### 4.1  Function description

##### 4.1.1 PayByClient

```java
public static PayByClient getPayByClient()
        throws InvalidKeySpecException, SignatureException, InvalidKeyException, IOException, URISyntaxException {
        ApiConfig apiConfig = new ApiConfig();
        // setting interface url
    
		 // Sandbox environment URL
        apiConfig.setDomain("https://uat.test2pay.com/sgs/api");
     	 // Production environment URL
     	 // apiConfig.setDomain("https://api.payby.com/sgs/api");

        // setting pkcs8 privateKey path
        String merchantPrivateKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("merchant_private_key.pem").toURI())));

        // setting publicKey path
        String payByPubKey = new String(Files
            .readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("Payby_public_key.pem").toURI())));

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





##### 4.1.2  Order creation

```java
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

```





##### 4.1.3  Order cancellation

###### 4.1.3.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M202005120001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_ACQUIRE_ORDER, wrap);
        System.out.println("cancelOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("cancelOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.3.2 ByOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo("190000000001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_ACQUIRE_ORDER, wrap);
        System.out.println("cancelOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("cancelOrder body=>" + JSON.toJSONString(body));
```

 

##### 4.1.4  Order query

###### 4.1.4.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M202005120001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.GET_ACQUIRE_ORDER, wrap);
        System.out.println("getOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("getOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.4.2 ByOrderNo

```java
       PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        //order number Required
        orderIndexRequest.setOrderNo("13000000023101");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.GET_ACQUIRE_ORDER, wrap);
        System.out.println("getOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("getOrder body=>" + JSON.toJSONString(body));
```



##### 4.1.5  Order refund

###### 4.1.5.1 ByOriginMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        PlaceRefundOrderRequest placeRefundOrderRequest = new PlaceRefundOrderRequest();
        // Refund refund amount Required
        placeRefundOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // Merchant order number Required
        placeRefundOrderRequest.setRefundMerchantOrderNo("M80000000001");
        // Original merchant order number
        placeRefundOrderRequest.setOriginMerchantOrderNo("M220000000001");
        // Refund operator name Optional
        placeRefundOrderRequest.setOperatorName("JACKMA");
        // Refund reason name Optional
        placeRefundOrderRequest.setReason("reason123");
        // Notification URL Optional
        placeRefundOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
        // Reserved Optional
        placeRefundOrderRequest.setReserved("order desc");
        SgsRequestWrap<PlaceRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceRefundOrderResponse> responseWrap = client.execute(SgsApi.PLACE_REFUND_ORDER, wrap);
        System.out.println("refundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.5.2 ByOriginOrderNo

```java
        PayByClient client = getPayByClient();

        PlaceRefundOrderRequest placeRefundOrderRequest = new PlaceRefundOrderRequest();
        // Refund refund amount Required
        placeRefundOrderRequest.setAmount(new ExternalMoney(new BigDecimal("0.1"), "AED"));
        // Merchant order number Required
        placeRefundOrderRequest.setRefundMerchantOrderNo("M80000000001");
        // Original order number
        placeRefundOrderRequest.setOriginOrderNo("19000000001");
        // Refund operator name Optional
        placeRefundOrderRequest.setOperatorName("JACKMA");
        // Refund reason name Optional
        placeRefundOrderRequest.setReason("reason123");
        // Notification URL Optional
        placeRefundOrderRequest.setNotifyUrl("http://yoursite.com/api/notification");
		// Reserved Optional
        placeRefundOrderRequest.setReserved("order desc");
        SgsRequestWrap<PlaceRefundOrderRequest> wrap = SgsRequestWrap.wrap(placeRefundOrderRequest);
        System.out.println("refundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<PlaceRefundOrderResponse> responseWrap = client.execute(SgsApi.PLACE_REFUND_ORDER, wrap);
        System.out.println("refundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        PlaceRefundOrderResponse body = responseWrap.getBody();
        System.out.println("refundOrder body=>" + JSON.toJSONString(body));
```



##### 4.1.6  Order refund query

###### 4.1.6.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // Merchant order number Required
        orderIndexRequest.setRefundMerchantOrderNo("M80000000001");
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_REFUND_ORDER, wrap);
        System.out.println("getRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getRefundOrder body=>" + JSON.toJSONString(body));
```



###### 4.1.6.2 ByOrderNo

```java
        PayByClient client = getPayByClient();


        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // order number Required
        orderIndexRequest.setOrderNo("1900000000001");
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_REFUND_ORDER, wrap);
        System.out.println("getRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getRefundOrder body=>" + JSON.toJSONString(body));
```



##### 4.1.7  Transfer

```java
        PayByClient client = getPayByClient();

        PlaceTransferOrderRequest placeTransferOrderRequest = new PlaceTransferOrderRequest();

        // Merchant order number Required
        placeTransferOrderRequest.setMerchantOrderNo("M320000000001");
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
```



##### 4.1.8  Transfer query

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M320000000001");

        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getTransferOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetTransferOrderResponse> responseWrap = client.execute(SgsApi.GET_TRANSFER_ORDER, wrap);
        System.out.println("getTransferOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetTransferOrderResponse body = responseWrap.getBody();
        System.out.println("getTransferOrder body=>" + JSON.toJSONString(body));
```

 

##### 4.1.9  Transfer to bank

```java
        PayByClient client = getPayByClient();

        PlaceTransferToBankOrderRequest placeTransferToBankOrderRequest = new PlaceTransferToBankOrderRequest();
        // Merchant order number Required
        placeTransferToBankOrderRequest.setMerchantOrderNo("M320000000002");
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
```



##### 4.1.10  Transfer to bank query

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M320000000002");

        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getTransferToBankOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetTransferToBankOrderResponse> responseWrap =
            client.execute(SgsApi.GET_TRANSFER_TO_BANK_ORDER, wrap);
        System.out.println("getTransferToBankOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetTransferToBankOrderResponse body = responseWrap.getBody();
        System.out.println("getTransferToBankOrder body=>" + JSON.toJSONString(body));
```



##### 4.1.11  Order revoke

###### 4.1.11.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M202005120001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_ACQUIRE_ORDER, wrap);
        System.out.println("revokeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("revokeOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.11.2 ByOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo("190000000001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_ACQUIRE_ORDER, wrap);
        System.out.println("revokeOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("revokeOrder body=>" + JSON.toJSONString(body));
```

 

##### 4.1.12 Protocol apply

```java
       PayByClient client = getPayByClient();

        ApplyProtocolRequest applyProtocolRequest = new ApplyProtocolRequest();
        // Merchant order number Required
        applyProtocolRequest.setMerchantOrderNo("M320000000002");
        // langType Optional
        applyProtocolRequest.setLangType(ProtocolLangType.EN);
        // expiredTime Optional
        applyProtocolRequest        .setExpiredTime(Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant()));

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
        protocolSceneParams.put("iapDeviceId", "");
        protocolSceneParams.put("appId", "");
        applyProtocolRequest.setProtocolSceneParams(protocolSceneParams);
	
	// accessType Optional
        applyProtocolRequest.setAccessType(ProtocolAccessType.SDK);

        SgsRequestWrap<ApplyProtocolRequest> wrap = SgsRequestWrap.wrap(applyProtocolRequest);
        System.out.println("applyProtocol request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<ApplyProtocolResponse> responseWrap = client.execute(SgsApi.APPLY_PROTOCOL, wrap);
        System.out.println("applyProtocol response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        ApplyProtocolResponse body = responseWrap.getBody();
        System.out.println("applyProtocol body=>" + JSON.toJSONString(body));
```



##### 4.1.13  Protocol query

```java
        PayByClient client = getPayByClient();


        GetProtocolRequest getProtocolRequest = new GetProtocolRequest();
        // Merchant order number Required
        getProtocolRequest.setMerchantOrderNo("M320000000002");

        SgsRequestWrap<GetProtocolRequest> wrap = SgsRequestWrap.wrap(getProtocolRequest);
        System.out.println("getProtocol request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetProtocolResponse> responseWrap = client.execute(SgsApi.GET_PROTOCOL, wrap);
        System.out.println("getProtocol response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetProtocolResponse body = responseWrap.getBody();
        System.out.println("getProtocol body=>" + JSON.toJSONString(body));
```





##### 4.1.14  Test echo

```java
        PayByClient client = getPayByClient();

        String req = "test echo";
        SgsRequestWrap<String> wrap = SgsRequestWrap.wrap(req);
        System.out.println("echo request=>" + JSON.toJSONString(wrap));
        String responseWrap = client.executeEcho(SgsApi.ECHO, req);
        System.out.println("echo response=>" + JSON.toJSONString(responseWrap));

```



##### 4.1.15  Crypto currency Topup address get

```java
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
```


##### 4.1.16  CustomerDepositOrder page query

```java
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
```



##### 4.1.17  CustomerDepositOrder query

```java
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
```



##### 4.1.18 ReceiptOrder  create

```java
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
```



##### 4.1.19 ReceiptOrder  query

```java
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
```



##### 4.1.20 ReceiptOrder  notification

```java
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
```



##### 4.1.21 CryptoOrder creation

```java
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
```



##### 4.1.22  CryptoOrder cancellation

###### 4.1.22.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();
        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M0000001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_CRYPTO_ORDER, wrap);
        System.out.println("cancelCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("cancelCryptoOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.22.2 ByOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo("190000000001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("cancelOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetPlaceOrderResponse> responseWrap = client.execute(SgsApi.CANCEL_ACQUIRE_ORDER, wrap);
        System.out.println("cancelOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetPlaceOrderResponse body = responseWrap.getBody();
        System.out.println("cancelOrder body=>" + JSON.toJSONString(body));
```

 

##### 4.1.23  Order query

###### 4.1.23.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M202005120001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_ORDER, wrap);
        System.out.println("getCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.23.2 ByOrderNo

```java
       PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        //order number Required
        orderIndexRequest.setOrderNo("13000000023101");
         SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_ORDER, wrap);
        System.out.println("getCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoOrder body=>" + JSON.toJSONString(body));
```



##### 4.1.24  CryptoOrder refund

###### 4.1.24.1 ByOriginMerchantOrderNo

```java
        PayByClient client = getPayByClient();

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

```

###### 4.1.24.2 ByOriginOrderNo

```java
        PayByClient client = getPayByClient();

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
```



##### 4.1.24  CryptoOrder refund query

###### 4.1.24.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // Merchant order number Required
        orderIndexRequest.setRefundMerchantOrderNo("M80000000001");
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_REFUND_ORDER, wrap);
        System.out.println("getCryptoRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoRefundOrder body=>" + JSON.toJSONString(body));
```



###### 4.1.24.2 ByOrderNo

```java
        PayByClient client = getPayByClient();


        GetRefundOrderRequest orderIndexRequest = new GetRefundOrderRequest();
        // order number Required
        orderIndexRequest.setOrderNo("1900000000001");
        SgsRequestWrap<GetRefundOrderRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("getCryptoRefundOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoRefundOrderResponse> responseWrap = client.execute(SgsApi.GET_CRYPTO_REFUND_ORDER, wrap);
        System.out.println("getCryptoRefundOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoRefundOrderResponse body = responseWrap.getBody();
        System.out.println("getCryptoRefundOrder body=>" + JSON.toJSONString(body));
```



##### 4.1.25 CryptoOrder revoke

###### 4.1.25.1 ByMerchantOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Merchant order number Required
        orderIndexRequest.setMerchantOrderNo("M202005120001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_CRYPTO_ORDER, wrap);
        System.out.println("revokeCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("revokeCryptoOrder body=>" + JSON.toJSONString(body));
```

###### 4.1.25.2 ByOrderNo

```java
        PayByClient client = getPayByClient();

        OrderIndexRequest orderIndexRequest = new OrderIndexRequest();
        // Order number Required
        orderIndexRequest.setOrderNo("190000000001");
        SgsRequestWrap<OrderIndexRequest> wrap = SgsRequestWrap.wrap(orderIndexRequest);
        System.out.println("revokeCryptoOrder request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<CryptoOrderResponse> responseWrap = client.execute(SgsApi.REVOKE_CRYPTO_ORDER, wrap);
        System.out.println("revokeCryptoOrder response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        CryptoOrderResponse body = responseWrap.getBody();
        System.out.println("revokeCryptoOrder body=>" + JSON.toJSONString(body));
```

 

##### 4.1.26 SaveCard query

```java
        PayByClient client = getPayByClient();
        CardIndexRequest req = new CardIndexRequest();
        req.setCardToken("11199520630915416646");
        SgsRequestWrap<CardIndexRequest> wrap = SgsRequestWrap.wrap(req);

        System.out.println("getSaveCard request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<GetSaveCardResponse> responseWrap = client.execute(SgsApi.GET_ACQUIRE_SAVE_CARD, wrap);
        System.out.println("getSaveCard response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetSaveCardResponse body = responseWrap.getBody();
        System.out.println("getSaveCard body=>" + JSON.toJSONString(body));
```



##### 4.1.27 SaveCard remove

```java
        PayByClient client = getPayByClient();
        CardIndexRequest req = new CardIndexRequest();
        req.setCardToken("11199520630915416646");
        SgsRequestWrap<CardIndexRequest> wrap = SgsRequestWrap.wrap(req);

        System.out.println("removeSaveCard request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<Void> responseWrap = client.execute(SgsApi.REMOVE_ACQUIRE_SAVE_CARD, wrap);
        System.out.println("removeSaveCard response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
```

##### 4.1.28 GetCashierUrlInfo 

```java
        PayByClient client = getPayByClient();
        GetCashierUrlInfoRequest getCashierUrlInfoRequest = new GetCashierUrlInfoRequest();
        // TokenUrl Required
        getCashierUrlInfoRequest.setTokenUrl(
            "https://sim-checkout.test2pay.com/pay-page/main?BIZ_TYPE=202&ft=cd624549f4894d78b0d368e48bb27aae&t=1654519114067");

        SgsRequestWrap<GetCashierUrlInfoRequest> wrap = SgsRequestWrap.wrap(getCashierUrlInfoRequest);

        System.out.println("getCashierUrlInfo request=>" + JSON.toJSONString(wrap));

        SgsResponseWrap<GetCashierUrlInfoResult> responseWrap = client.execute(SgsApi.GET_CASHIER_URL_INFO, wrap);
        System.out.println("getCashierUrlInfo response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        GetCashierUrlInfoResult body = responseWrap.getBody();
        System.out.println("getCashierUrlInfo body=>" + JSON.toJSONString(body));
```

##### 4.1.29 GetMemberBalance

```java
        PayByClient client = getPayByClient();
        GetMemberBalanceRequest req = new GetMemberBalanceRequest();
        // CurrencyCode Required
        req.setCurrencyCode("AED");
        SgsRequestWrap<GetMemberBalanceRequest> wrap = SgsRequestWrap.wrap(req);
        System.out.println("getMemberBalance request=>" + JSON.toJSONString(wrap));
        SgsResponseWrap<MemberBalance> responseWrap = client.execute(SgsApi.GET_BALANCE, wrap);
        System.out.println("getMemberBalance response=>" + JSON.toJSONString(responseWrap));
        Assert.assertTrue(SgsApi.checkResponse(responseWrap));
        MemberBalance body = responseWrap.getBody();
        System.out.println("getMemberBalance body=>" + JSON.toJSONString(body));
```

##### 

#### 4.2   Result notification

##### 4.2.1  Verify signature

```java
// setting payby publicKey path        
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

```

  

##### 4.2.2  Servlet receive messages（springmvc）

```java
 @Override
 protected void  doPost(HttpServletRequest req, HttpServletResponse httpResponse) throws ServletException, IOException {

    String sign = req.getHeader("sign");

    String plain = IOUtils.toString(req.getInputStream(), "UTF-8");

    // setting payby publicKey path
     String payByPubKey = new String(Files.readAllBytes(Paths.get(PayByDemo.class.getClassLoader().getResource("payby_public_key.pem").toURI())));

       // assert verify sign
        Assert.assertTrue(RsaUtil.verifySign(plain, Charset.forName("UTF-8"), sign, payByPubKey));

       //TODO Business logic
        GetPlaceOrderResponse callbackOrder = JSON.parseObject(plain, new TypeReference<GetPlaceOrderResponse>() {});

        System.out.println("acquireOrder callback body=>" + callbackOrder);

     httpResponse.setContentType("application/json;charset=UTF-8");

     httpResponse.getOutputStream().write("SUCCESS".getBytes("UTF-8"));

     httpResponse.flushBuffer();

  }
```

#### 4.3 Download

##### 4.3.1 Order Statement

```java
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
```

##### 4.3.2 Fund Statement

```java
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
```

#### 4.4 InApp Signature 

```java
        InappSignContent content = new InappSignContent();
        // iapAppId Required
        content.setIapAppId("00001");
        // iapDeviceId Required
        content.setIapDeviceId("001");
        // partnerId Required
        content.setIapPartnerId("20000000076");
        // token Required
        content.setToken("daerccxxcdsda0eeda");
        String merchantPrivateKey = new String(Files.readAllBytes(
            Paths.get(PayByDemo.class.getClassLoader().getResource("merchant_demo_private.pem").toURI())));
        System.out.println("plain==>" + SignSerializationUtil.serialize(content));
        System.out.println("sign==>" + RsaUtil.sign(content, Charset.forName("UTF-8"), merchantPrivateKey));
```

