public class SamplePseudoCode {

    public void placeOrder() throws Exception {
        //prepare httpclient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // get Merchant privateKey from filesystem
        String merchantPrivateKey = new String(Files.readAllBytes("sim_200000030907_merchant_private_key.pem"));
        PrivateKey privateKey = RsaUtil.getPrivateKey(merchantPrivateKey);
       
        // get Payby publicKey from filesystem
        String payByPubKey = new String(Files.readAllBytes("sim_200000030907_payby_public_key.pem"));
        PublicKey publicKey = RsaUtil.getPublicKey(payByPubKey);

        //create http post method,change url according to environment
        HttpPost method = new HttpPost("https://uat.test2pay.com/sgs/api/acquire2/placeOrder");

        //assemble your request
        String requestBody =
            "{\"bizContent\":{\"accessoryContent\":{\"amountDetail\":{\"vatAmount\":{\"amount\":0.1,\"currency\":\"AED\"}},\"goodsDetail\":{\"body\":\"gifts\",\"goodsId\":\"GI1005\",\"goodsName\":\"candy flower\"},\"terminalDetail\":{\"merchantName\":\"candy home\"}},\"merchantOrderNo\":\"680cb196-f5f4-4699-8070-537c4170a844\",\"notifyUrl\":\"http://yoursite.com/api/notification\",\"paySceneCode\":\"PAYPAGE\",\"paySceneParams\":{\"redirectUrl\":\"http://www.yoursite.com/web/paydone.html?orderId=414768633924763654\"},\"reserved\":\"order desc\",\"subject\":\"ipad\",\"totalAmount\":{\"amount\":0.1,\"currency\":\"AED\"}},\"requestTime\":1636957999605}\r\n";
        //set your request into http request body
        method.setEntity(new StringEntity(requestBody, Charset.forName("UTF-8")));

     
        
        // signature your request body using SHA256WithRSA
        String signature = signature(requestBody, privateKey);
        
        //add mandatory header key value
        method.addHeader("Content-type", "application/json; charset=utf-8");
        method.setHeader("Accept", "application/json");
        //using your merchant id here
        method.setHeader("partner-id", "200000030907");
        method.setHeader("api-version", "v1");
        // set signature into http header
        method.setHeader("sign", signature);

        //send request and get response
        CloseableHttpResponse res = httpClient.execute(method);
        //check get http response 200
        if (res.getStatusLine().getStatusCode() != 200 && res.getStatusLine().getStatusCode() != 201) {
            throw new Exception("SERVICE_POST_ERR");
        }

        //get response signature from response http header
        Header sign = res.getFirstHeader("sign");
        // get response from response http body
        String responseBody = EntityUtils.toString(res.getEntity());

        
        //verify http response signature using SHA256WithRSA
        boolean verifyResult = verify(responseBody, sign.getValue(), publicKey);

        if (verifyResult) {
            // unmarshall response
            Object response = unmarshall(responseBody);
            //do your business
        }
        // close everything your language required
        res.close();
        httpClient.close();
    }

    private static boolean verify(String responseBody, String sign, PublicKey publicKey) throws Exception {
        Signature signature = null;
        try {
            signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(publicKey);
            signature.update(responseBody.getBytes("UTF-8"));
            return signature.verify(sign.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String signature(String requestBody, PrivateKey merchantPrivateKey) throws Exception {

        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(merchantPrivateKey);
        signature.update(requestBody.getBytes("UTF-8"));
        return Base64.encodeBase64String(signature.sign());
    }

}
