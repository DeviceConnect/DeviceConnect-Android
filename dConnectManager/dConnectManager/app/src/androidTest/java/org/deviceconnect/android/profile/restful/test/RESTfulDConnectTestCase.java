/*
 RESTfulDConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.deviceconnect.android.test.DConnectTestCase;
import org.deviceconnect.android.test.http.HttpUtil;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * RESTful API用テストケース.
 * @author NTT DOCOMO, INC.
 */
public class RESTfulDConnectTestCase extends DConnectTestCase {

    /** イベント返り値を待つ時間を定義する. */
    private static final int WAIT_FOR_EVENT = 10000;
    /** バッファサイズを定義. */
    private static final int BUF_SIZE = 8192;
    /** 最大リトライ回数. */
    private static final int RETRY_COUNT = 3;

    /**
     * アクセストークン取得APIに対するスコープ指定を取得する.
     * 
     * @param scopes スコープ指定
     * @return スコープ指定の配列を連結した文字列
     */
    private static String createScopeParameter(final String[] scopes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(scopes[i]);
        }
        return sb.toString();
    }

    @Override
    protected String createClient() {
        URIBuilder builder  = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        try {
            HttpGet request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request, false);
            assertResultOK(root);
            String clientId = root.getString(AuthorizationProfileConstants.PARAM_CLIENT_ID);
            if (clientId != null) {
                return clientId;
            }
        } catch (JSONException e) {
            fail();
        }
        return null;
    }

    @Override
    protected String requestAccessToken(final String clientId, final String[] scopes) {
        URIBuilder builder  = TestURIBuilder.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_ACCESS_TOKEN);
        builder.addParameter(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
        builder.addParameter(AuthorizationProfileConstants.PARAM_SCOPE, createScopeParameter(scopes));
        builder.addParameter(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, "dConnectManagerTest");
        try {
            HttpGet request = new HttpGet(builder.toString());
            JSONObject root = sendRequestInternal(request, false);
            assertResultOK(root);
            return root.getString(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN);
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
        return null;
    }

    /**
     * デバイス一覧をRestfulAPIで取得する.
     * @return デバイス一覧
     */
    @Override
    protected List<DeviceInfo> searchDevices() {
        List<DeviceInfo> services = new ArrayList<DeviceInfo>();
        
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(ServiceDiscoveryProfileConstants.PROFILE_NAME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN,
                getAccessToken());
        HttpGet request = new HttpGet(builder.toString());
        try {
            JSONObject root = sendRequest(request);
            assertResultOK(root);

            JSONArray servicesJson = root.getJSONArray(ServiceDiscoveryProfileConstants.PARAM_SERVICES);
            for (int i = 0; i < servicesJson.length(); i++) {
                JSONObject serviceJson = servicesJson.getJSONObject(i);
                String serviceId = serviceJson.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
                String deviceName = serviceJson.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
                services.add(new DeviceInfo(serviceId, deviceName));
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
        return services;
    }

    @Override
    protected List<PluginInfo> searchPlugins() {
        List<PluginInfo> plugins = new ArrayList<PluginInfo>();
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject response = sendRequest(request);
            assertResultOK(response);

            JSONArray pluginsJson = response.getJSONArray(SystemProfileConstants.PARAM_PLUGINS);
            for (int i = 0; i < pluginsJson.length(); i++) {
                JSONObject pluginJson = pluginsJson.getJSONObject(i);
                String id = pluginJson.getString(SystemProfileConstants.PARAM_ID);
                String name = pluginJson.getString(SystemProfileConstants.PARAM_NAME);
                plugins.add(new PluginInfo(id, name));
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
        return plugins;
    }

    @Override
    protected boolean isManagerAvailable() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(AvailabilityProfileConstants.PROFILE_NAME);
        try {
            HttpUriRequest request = new HttpGet(builder.toString());
            JSONObject root = sendRequest(request);
            if (root == null) {
                return false;
            }
            return root.getInt(DConnectMessage.EXTRA_RESULT) == DConnectMessage.RESULT_OK;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * HTTP経由でファイルデータを取得する.
     * @param uri ファイルへのURI
     * @return ファイルデータ
     */
    protected final byte[] getBytesFromHttp(final String uri) {
        HttpUriRequest request = new HttpGet(uri);
        request.setHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());
        HttpClient client = new DefaultHttpClient();
        try {
            HttpResponse response = client.execute(request);
            switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
                InputStream in = response.getEntity().getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[BUF_SIZE];
                int len;
                while ((len = in.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                return baos.toByteArray();
            case HttpStatus.SC_NOT_FOUND:
                Assert.fail("Not found page. 404: " + uri);
                break;
            default:
                Assert.fail("Connection Error. " + response.getStatusLine().getStatusCode());
                break;
            }
        } catch (ClientProtocolException e) {
            Assert.fail("ClientProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("IOException: " + e.getMessage());
        }
        return null;
    }

    /**
     * RESTfulでdConnectManagerにリクエストを出す.
     * @param request Httpリクエスト
     * @return HttpResponse
     */
    protected final HttpResponse requestHttpResponse(final HttpUriRequest request) {
        // Origin指定
        request.setHeader(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());
        
        HttpClient client = new DefaultHttpClient();
        try {
            return client.execute(request);
        } catch (ClientProtocolException e) {
            Assert.fail("ClientProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Assert.fail("IOException: " + e.getMessage());
        }
        return null;
    }

    /**
     * 指定されたURIにリクエストを送信します.
     * @param method HTTPメソッド
     * @param uri URI
     * @param headers ヘッダー
     * @param body ボディ
     * @return レスポンス
     */
    protected final JSONObject sendRequest(final String method, final String uri, final Map<String, String> headers, final String body) {
        Map<String, String> requestHeaders = headers;
        if (requestHeaders == null) {
            requestHeaders = new HashMap<>();
        }
        requestHeaders.put(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());

        final byte[] nonce = generateRandom(16);
        String requestBody = body;
        if (requestBody == null) {
            requestBody = IntentDConnectMessage.EXTRA_NONCE + "=" + toHexString(nonce);
        } else {
            requestBody += ("&" + IntentDConnectMessage.EXTRA_NONCE + "=" + toHexString(nonce));
        }

        try {
            byte[] data = HttpUtil.connect(method, uri, requestHeaders, requestBody);
            if (data == null) {
                fail("Failed to connect Device Connect Manager.");
            }
            JSONObject response = new JSONObject(new String(data));

            assertEquals(response.getString(DConnectProfileConstants.PARAM_PRODUCT), DCONNECT_MANAGER_APP_NAME);
            assertEquals(response.getString(DConnectProfileConstants.PARAM_VERSION), DCONNECT_MANAGER_VERSION_NAME);
            assertTrue("Device Connect Manager must send HMAC.", response.has(IntentDConnectMessage.EXTRA_HMAC));

            String hmacString = response.getString(IntentDConnectMessage.EXTRA_HMAC);
            byte[] expectedHmac = calculateHMAC(nonce);
            assertEquals(expectedHmac, toByteArray(hmacString));

            return response;
        } catch (JSONException e) {
            fail("Failed to parse response: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        }
        return null;
    }

    /**
     * 指定されたURIにマルチパートのリクエストを送信します.
     * @param method HTTPメソッド
     * @param uri URI
     * @param headers ヘッダー
     * @param body ボディ
     * @return レスポンス
     */
    protected final JSONObject sendRequest(final String method, final String uri, final Map<String, String> headers, final Map<String, Object> body) {
        Map<String, String> requestHeaders = headers;
        if (requestHeaders == null) {
            requestHeaders = new HashMap<>();
        }
        requestHeaders.put(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());

        final byte[] nonce = generateRandom(16);
        Map<String, Object> requestBody = body;
        if (requestBody == null) {
            requestBody = new HashMap<>();
        }
        requestBody.put(IntentDConnectMessage.EXTRA_NONCE, toHexString(nonce));

        try {
            byte[] data = HttpUtil.connect(method, uri, requestHeaders, requestBody);
            if (data == null) {
                fail("Failed to connect Device Connect Manager.");
            }
            JSONObject response = new JSONObject(new String(data));

            assertEquals(response.getString(DConnectProfileConstants.PARAM_PRODUCT), DCONNECT_MANAGER_APP_NAME);
            assertEquals(response.getString(DConnectProfileConstants.PARAM_VERSION), DCONNECT_MANAGER_VERSION_NAME);
            assertTrue("Device Connect Manager must send HMAC.", response.has(IntentDConnectMessage.EXTRA_HMAC));

            String hmacString = response.getString(IntentDConnectMessage.EXTRA_HMAC);
            byte[] expectedHmac = calculateHMAC(nonce);
            assertEquals(expectedHmac, toByteArray(hmacString));

            return response;
        } catch (JSONException e) {
            fail("Failed to parse response: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        }
        return null;
    }

    /**
     * RESTfulでDevice Connect Managerにリクエストを出す.
     * @param request HTTPリクエスト
     * @return レスポンスのJSON
     */
    protected final JSONObject sendRequest(final HttpUriRequest request) {
        return sendRequest(request, true);
    }

    /**
     * HTTPリクエストを送信する.
     * @param originalRequest HTTPリクエスト
     * @param requiredAuth 指定したリクエストを送信する前に認証を行うかどうか
     * @return レスポンス
     */
    protected final JSONObject sendRequest(final HttpUriRequest originalRequest, final boolean requiredAuth) {
        HttpUriRequest request = originalRequest;
        if (requiredAuth) {
            URIBuilder builder = TestURIBuilder.createURIBuilder(request.getURI());
            builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
            request = recreateRequest(request, builder);
        }
        final byte[] nonce = generateRandom(16);
        request = addParam(request, IntentDConnectMessage.EXTRA_NONCE, toHexString(nonce));

        JSONObject response = sendRequest(request, requiredAuth, 0);
        try {
            assertEquals(response.getString(DConnectProfileConstants.PARAM_PRODUCT), DCONNECT_MANAGER_APP_NAME);
            assertEquals(response.getString(DConnectProfileConstants.PARAM_VERSION), DCONNECT_MANAGER_VERSION_NAME);

            // HMACの検証
            assertTrue("Device Connect Manager must send HMAC.", response.has(IntentDConnectMessage.EXTRA_HMAC));
            String hmacString = response.getString(IntentDConnectMessage.EXTRA_HMAC);
            byte[] expectedHmac = calculateHMAC(nonce);
            assertEquals(expectedHmac, toByteArray(hmacString));
        } catch (JSONException e) {
            fail("Invalid JSON.");
            return null;
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        }
        return response;
    }

    /**
     * HTTPリクエストを送信する.
     * @param request HTTPリクエスト
     * @param requiredAuth 指定したリクエストを送信する前に認証を行うかどうか
     * @param count 送信回数
     * @return レスポンス
     */
    protected final JSONObject sendRequest(final HttpUriRequest request, final boolean requiredAuth,
            final int count)  {
        try {
            JSONObject response = sendRequestInternal(request, requiredAuth);
            int result = response.getInt(DConnectMessage.EXTRA_RESULT);
            if (result == DConnectMessage.RESULT_ERROR && count <= RETRY_COUNT) {
                if (!response.has(DConnectMessage.EXTRA_ERROR_CODE)) {
                    return response;
                }
            }
            return response;
        } catch (JSONException e) {
            Assert.fail("IOException: " + e.getMessage());
        }
        return null;
    }

    /**
     * RESTfulでdConnectManagerにリクエストを出す.
     * @param request Httpリクエスト
     * @param requiredAuth OAuth情報を取得していない場合に自動的に取得するかどうか.trueの場合は取得する
     * @return レスポンスのintent
     */
    protected final JSONObject sendRequestInternal(final HttpUriRequest request, final boolean requiredAuth) {
        try {
            HttpResponse response = requestHttpResponse(request);
            if (response == null) {
                return null;
            }
            switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
                String msg = EntityUtils.toString(response.getEntity(), "UTF-8");
                try {
                    return new JSONObject(msg);
                } catch (JSONException e) {
                    Assert.fail("JSON Format error: " + msg);
                }
                break;
            case HttpStatus.SC_NOT_FOUND:
                Assert.fail("Not found page. 404 ");
                break;
            default:
                Assert.fail("Connection Error. " + response.getStatusLine().getStatusCode()
                        + " : " + response.getStatusLine().getReasonPhrase());
                break;
            }
        } catch (IOException e) {
            Assert.fail("IOException: " + e.getMessage());
        }
        return null;
    }

    /**
     * イベントメッセージを1秒間待つ.
     * タイムアウトした場合には、nullを返却する。
     * @return 送られてきたイベントを返却する。
     */
    protected final JSONObject waitForEvent() {
        return waitForEvent(WAIT_FOR_EVENT);
    }

    /**
     * 指定された時間だけイベントメッセージを待つ.
     * タイムアウトした場合には、nullを返却する。
     * @param time イベントメッセージを待つ時間
     * @return 送られてきたイベントを返却する。
     */
    protected final JSONObject waitForEvent(final long time) {
        final CountDownLatch latch = new CountDownLatch(1);
        final JSONObject[] response = new JSONObject[1];
        URI uri = URI.create("ws://localhost:4035/websocket");
        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(final ServerHandshake handshake) {
                try {
                    JSONObject root = new JSONObject();
                    root.put(DConnectMessage.EXTRA_SESSION_KEY, getClientId());
                    send(root.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onMessage(final String message) {
                try {
                    response[0] = new JSONObject(message);
                } catch (JSONException e) {
                    response[0] = null;
                }
                latch.countDown();
            }
            @Override
            public void onError(final Exception ex) {
                latch.countDown();
            }
            @Override
            public void onClose(final int code, final String reason, final boolean remote) {
                latch.countDown();
            }
        };
        client.connect();

        // イベントからのメッセージを待つ
        try {
            latch.await(time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return response[0];
        } finally {
            client.close();
        }
        return response[0];
    }

    /**
     * resultの値が{@link DConnectMessage#RESULT_OK}であることをチェックする.
     * 
     * @param response レスポンス
     * @throws JSONException JSONの解析に失敗した場合
     */
    protected static void assertResultOK(final JSONObject response) throws JSONException {
        assertResult(DConnectMessage.RESULT_OK, response);
    }

    /**
     * resultの値が{@link DConnectMessage#RESULT_ERROR}であることをチェックする.
     * 
     * @param response レスポンス
     * @throws JSONException JSONの解析に失敗した場合
     */
    protected static void assertResultError(final JSONObject response) throws JSONException {
        assertResult(DConnectMessage.RESULT_ERROR, response);
    }

    /**
     * resultの値が{@link DConnectMessage#RESULT_ERROR}であることをチェックする.
     * 
     * @param errorCode 期待するエラーコード
     * @param response レスポンス
     * @throws JSONException JSONの解析に失敗した場合
     */
    protected static void assertResultError(final int errorCode, final JSONObject response) throws JSONException {
        assertResult(DConnectMessage.RESULT_ERROR, response);
        assertErrorCode(errorCode, response);
    }

    /**
     * resultの値が指定したコードであることをチェックする.
     * 
     * @param expected 期待するresultの値
     * @param response レスポンス
     * @throws JSONException JSONの解析に失敗した場合
     */
    protected static void assertResult(final int expected, final JSONObject response) throws JSONException {
        Assert.assertTrue(response.has(DConnectMessage.EXTRA_RESULT));
        int actual = response.getInt(DConnectMessage.EXTRA_RESULT);
        if (expected != actual) {
            String message =  "expected result=" + expected
                    + " but actual result=" + actual + ". " + response.toString();
            fail(message);
        }
    }

    /**
     * 期待したエラーコードが返っていることをチェックする.
     * 
     * @param expectedCode 期待するエラーコード
     * @param actualResponse 実際のレスポンス
     * @throws JSONException JSONの解析に失敗した場合
     */
    private static void assertErrorCode(final int expectedCode, 
            final JSONObject actualResponse) throws JSONException {
        if (!actualResponse.has(DConnectMessage.EXTRA_ERROR_CODE)) {
            fail("actualResponse is not a error response: " + actualResponse.toString());
        }
        int actualCode = actualResponse.getInt(DConnectMessage.EXTRA_ERROR_CODE);
        if (expectedCode != actualCode) {
            String message =  "expected=<" + expectedCode
                    + "> but actual=<" + actualCode + ">: " + actualResponse.toString();
            fail(message);
        }
    }

    /**
     * バイナリを送信するためのボディクラス.
     * @author NTT DOCOMO, INC.
     */
    protected class BinaryBody extends AbstractContentBody {
        /** ファイル名. */
        private String mFileName;
        /** 送信するバイナリデータ. */
        private byte[] mBuffer;

        /**
         * コンストラクタ.
         * @param buf バッファ
         * @param fileName ファイル名
         */
        public BinaryBody(final byte[] buf, final String fileName) {
            super(fileName);
            mBuffer = buf;
            mFileName = fileName;
        }

        @Override
        public String getFilename() {
            return mFileName;
        }

        @Override
        public String getCharset() {
            return "UTF-8";
        }

        @Override
        public long getContentLength() {
            return mBuffer.length;
        }

        @Override
        public String getTransferEncoding() {
            return "UTF-8";
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            out.write(mBuffer);
        }
    }

    /**
     * HTTPリクエストを作り直す.
     * @param request 元のリクエスト
     * @param builder URIBuilder
     * @return 作り直したHTTPリクエスト
     */
    private HttpUriRequest recreateRequest(final HttpUriRequest request, final URIBuilder builder) {
        if (request instanceof HttpGet) {
            HttpGet newGetRequest = new HttpGet(builder.toString());
            newGetRequest.setHeaders(request.getAllHeaders());
            return newGetRequest;
        } else if (request instanceof HttpPost) {
            HttpPost newPostRequest = new HttpPost(builder.toString());
            newPostRequest.setEntity(((HttpPost) request).getEntity());
            newPostRequest.setHeaders(request.getAllHeaders());
            return newPostRequest;
        } else if (request instanceof HttpPut) {
            HttpPut newPutRequest = new HttpPut(builder.toString());
            newPutRequest.setEntity(((HttpPut) request).getEntity());
            newPutRequest.setHeaders(request.getAllHeaders());
            return newPutRequest;
        } else if (request instanceof HttpDelete) {
            HttpDelete newDeleteRequest = new HttpDelete(builder.toString());
            newDeleteRequest.setHeaders(request.getAllHeaders());
            return newDeleteRequest;
        } else {
            fail("Invalid method is specified: " + request.getMethod());
            return null;
        }
    }

    /**
     * HTTPリクエストにリクエストパラメータを追加する.
     * @param request HTTPリクエスト
     * @param key パラメータキー
     * @param value パラメータ値
     * @return リクエストパラメータを追加したHTTPリクエスト
     */
    private HttpUriRequest addParam(final HttpUriRequest request, final String key, final String value) {
        URI uri = request.getURI();
        URIBuilder builder = TestURIBuilder.createURIBuilder(uri);
        builder.addParameter(key, value);
        return recreateRequest(request, builder);
    }
}
