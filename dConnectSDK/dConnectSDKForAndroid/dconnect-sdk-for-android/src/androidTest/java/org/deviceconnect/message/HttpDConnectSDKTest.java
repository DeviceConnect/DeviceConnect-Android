/*
 HttpDConnectSDKTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import static android.R.attr.key;
import static android.R.attr.value;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Base64;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.deviceconnect.message.entity.FileEntity;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.deviceconnect.message.server.TestServer;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

/**
 * HttpDConnectSDKのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class HttpDConnectSDKTest {

    /**
     * テスト用のサーバ.
     */
    private TestServer mTestServer;

    @Before
    public void setUp() {
        try {
            mTestServer = new TestServer();
            mTestServer.start();
        } catch (IOException e) {
            fail("Test Server could not be started. e=" + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (mTestServer != null) {
            mTestServer.stop();
        }
    }

    public byte[] readPrivateKey(InputStream in) throws Exception {
        String lines = new String(getFile(in), Charset.defaultCharset());
        String privateKeyPEM = lines
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");
        return Base64.decode(privateKeyPEM, Base64.DEFAULT);
    }

    private void writeFile(final File file, final byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] getFile(final File file) {
        try {
            return getFile(new FileInputStream(file));
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] getFile(final InputStream fis) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int len;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            fis.close();
        } catch (IOException e) {
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out.toByteArray();
    }

    private NanoHTTPD.Response newJsonResponse(final JSONObject jsonObject) {
        return newJsonResponse(jsonObject.toString());
    }

    private NanoHTTPD.Response newJsonResponse(final String message) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", message);
    }

    private NanoHTTPD.Response newInternalServerError(final String message) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, message);
    }

    private NanoHTTPD.Response newBadRequest(final String message) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, message);
    }

    /**
     * availabilityを呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void availability() {
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/availability".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.availability();
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * availabilityを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void availability_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final AtomicReference<DConnectResponseMessage> result = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/availability".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.availability(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage r) {
                result.set(r);
                latch.countDown();
            }
        });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        DConnectResponseMessage response = result.get();
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * authorizationを呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・accessTokenにtest-accessTokeが返却されること。
     * ・expireに1999が返却されること。
     * ・scopesに配列が返却されること。
     * </pre>
     */
    @Test
    public void authorization() {
        final String appName = "test";
        final String version = "1.1";
        final String product = "test-manager";
        final String clientId = "test-clientId";
        final String accessToken = "test-accessToken";
        final String profile = "battery";
        final int expirePeriod = 1000;
        final int expire = 1999;
        final String[] scopes = {
                "serviceDiscovery",
                "serviceInformation",
                "battery"
        };
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    Uri u = Uri.parse(uri);
                    if ("/gotapi/authorization/grant".equalsIgnoreCase(u.getPath())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
                        return newJsonResponse(jsonObject);
                    } else if ("/gotapi/authorization/accessToken".equalsIgnoreCase(u.getPath())) {

                        String name = parms.get(AuthorizationProfileConstants.PARAM_APPLICATION_NAME);
                        if (!appName.equals(name)) {
                            return newBadRequest("appName is invalid. appName=" + name);
                        }

                        String cid = parms.get(AuthorizationProfileConstants.PARAM_CLIENT_ID);
                        if (!clientId.equals(cid)) {
                            return newBadRequest("clientId is invalid. clientId=" + cid);
                        }

                        String ss = parms.get(AuthorizationProfileConstants.PARAM_SCOPE);
                        for (String s : scopes) {
                            if (!ss.contains(s)) {
                                return newBadRequest("scope is invalid. scope=" + ss);
                            }
                        }

                        JSONArray scopes = new JSONArray();

                        JSONObject scope1 = new JSONObject();
                        scope1.put(AuthorizationProfileConstants.PARAM_SCOPE, profile);
                        scope1.put(AuthorizationProfileConstants.PARAM_EXPIRE_PERIOD, expirePeriod);
                        scopes.put(scope1);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, accessToken);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_SCOPES, scopes);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_EXPIRE, expire);
                        return newJsonResponse(jsonObject);
                    } else {
                        return newBadRequest("Path is not Authorization Profile.");
                    }
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.authorization(appName, scopes);
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN), is(accessToken));
        assertThat(response.getInt(AuthorizationProfileConstants.PARAM_EXPIRE), is(expire));
        assertThat(response.getList(AuthorizationProfileConstants.PARAM_SCOPES), is(notNullValue()));
    }

    /**
     * authorizationを呼び出し、OnAuthorizationListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnAuthorizationListenerに通知が来ること。
     * ・clientIdにtest-clientIdが返却されること。
     * ・accessTokenにtest-accessTokenが返却されること。
     * </pre>
     */
    @Test
    public void authorization_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String appName = "test";
        final String version = "1.1";
        final String product = "test-manager";
        final String clientId = "test-clientId";
        final String accessToken = "test-accessToken";
        final String profile = "battery";
        final int expirePeriod = 1000;
        final int expire = 1999;
        final String[] scopes = {
                "serviceDiscovery",
                "serviceInformation",
                "battery"
        };
        final AtomicReference<String> resultClientId = new AtomicReference<>();
        final AtomicReference<String> resultAccessToken = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    Uri u = Uri.parse(uri);
                    if ("/gotapi/authorization/grant".equalsIgnoreCase(u.getPath())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
                        return newJsonResponse(jsonObject);
                    } else if ("/gotapi/authorization/accessToken".equalsIgnoreCase(u.getPath())) {

                        String name = parms.get(AuthorizationProfileConstants.PARAM_APPLICATION_NAME);
                        if (!appName.equals(name)) {
                            return newBadRequest("appName is invalid. appName=" + name);
                        }

                        String cid = parms.get(AuthorizationProfileConstants.PARAM_CLIENT_ID);
                        if (!clientId.equals(cid)) {
                            return newBadRequest("clientId is invalid. clientId=" + cid);
                        }

                        String ss = parms.get(AuthorizationProfileConstants.PARAM_SCOPE);
                        for (String s : scopes) {
                            if (!ss.contains(s)) {
                                return newBadRequest("scope is invalid. scope=" + ss);
                            }
                        }

                        JSONArray scopes = new JSONArray();

                        JSONObject scope1 = new JSONObject();
                        scope1.put(AuthorizationProfileConstants.PARAM_SCOPE, profile);
                        scope1.put(AuthorizationProfileConstants.PARAM_EXPIRE_PERIOD, expirePeriod);
                        scopes.put(scope1);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, accessToken);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_SCOPES, scopes);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_EXPIRE, expire);
                        return newJsonResponse(jsonObject);
                    } else {
                        return newBadRequest("Path is not Authorization Profile.");
                    }
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.authorization(appName, scopes, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(final String clientId, final String accessToken) {
                resultClientId.set(clientId);
                resultAccessToken.set(accessToken);
                latch.countDown();
            }
            @Override
            public void onError(final int errorCode, final String errorMessage) {
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(resultClientId.get(), is(clientId));
        assertThat(resultAccessToken.get(), is(accessToken));
    }

    /**
     * authorizationを呼び出し、OnAuthorizationListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnAuthorizationListener#onError()に通知が来ること。
     * ・errorCodeとerrorMessageが通知されること。
     * </pre>
     */
    @Test
    public void authorization_listener_error() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String appName = "test";
        final String version = "1.1";
        final String product = "test-manager";
        final String clientId = "test-clientId";
        final String errorMessage = "error-message";
        final int errorCode = 10;
        final String[] scopes = {
                "serviceDiscovery",
                "serviceInformation",
                "battery"
        };
        final AtomicReference<Integer> resultErrorCode = new AtomicReference<>();
        final AtomicReference<String> resultErrorMessage = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    Uri u = Uri.parse(uri);
                    if ("/gotapi/authorization/grant".equalsIgnoreCase(u.getPath())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
                        return newJsonResponse(jsonObject);
                    } else if ("/gotapi/authorization/accessToken".equalsIgnoreCase(u.getPath())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(DConnectMessage.EXTRA_ERROR_CODE, errorCode);
                        jsonObject.put(DConnectMessage.EXTRA_ERROR_MESSAGE, errorMessage);
                        return newJsonResponse(jsonObject);
                    } else {
                        return newBadRequest("Path is not Authorization Profile.");
                    }
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.authorization(appName, scopes, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(final String clientId, final String accessToken) {
                latch.countDown();
            }
            @Override
            public void onError(final int errorCode, final String errorMessage) {
                resultErrorCode.set(errorCode);
                resultErrorMessage.set(errorMessage);
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(resultErrorCode.get(), is(errorCode));
        assertThat(resultErrorMessage.get(), is(errorMessage));
    }

    /**
     * refreshAccessToken を呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・accessTokenにtest-accessTokeが返却されること。
     * ・expireに1999が返却されること。
     * ・scopesに配列が返却されること。
     * </pre>
     */
    @Test
    public void refreshAccessToken() {
        final String appName = "test";
        final String version = "1.1";
        final String product = "test-manager";
        final String clientId = "test-clientId";
        final String accessToken = "test-accessToken";
        final String profile = "battery";
        final int expirePeriod = 1000;
        final int expire = 1999;
        final String[] scopes = {
                "serviceDiscovery",
                "serviceInformation",
                "battery"
        };
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    Uri u = Uri.parse(uri);
                    if ("/gotapi/authorization/accessToken".equalsIgnoreCase(u.getPath())) {

                        String name = parms.get(AuthorizationProfileConstants.PARAM_APPLICATION_NAME);
                        if (!appName.equals(name)) {
                            return newBadRequest("appName is invalid. appName=" + name);
                        }

                        String cid = parms.get(AuthorizationProfileConstants.PARAM_CLIENT_ID);
                        if (!clientId.equals(cid)) {
                            return newBadRequest("clientId is invalid. clientId=" + cid);
                        }

                        String ss = parms.get(AuthorizationProfileConstants.PARAM_SCOPE);
                        for (String s : scopes) {
                            if (!ss.contains(s)) {
                                return newBadRequest("scope is invalid. scope=" + ss);
                            }
                        }

                        JSONArray scopes = new JSONArray();

                        JSONObject scope1 = new JSONObject();
                        scope1.put(AuthorizationProfileConstants.PARAM_SCOPE, profile);
                        scope1.put(AuthorizationProfileConstants.PARAM_EXPIRE_PERIOD, expirePeriod);
                        scopes.put(scope1);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, accessToken);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_SCOPES, scopes);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_EXPIRE, expire);
                        return newJsonResponse(jsonObject);
                    } else {
                        return newBadRequest("Path is not Authorization Profile.");
                    }
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.refreshAccessToken(clientId, appName, scopes);
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN), is(accessToken));
        assertThat(response.getInt(AuthorizationProfileConstants.PARAM_EXPIRE), is(expire));
        assertThat(response.getList(AuthorizationProfileConstants.PARAM_SCOPES), is(notNullValue()));
    }

    /**
     * refreshAccessTokenを呼び出し、OnAuthorizationListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnAuthorizationListenerに通知が来ること。
     * ・clientIdにtest-clientIdが返却されること。
     * ・accessTokenにtest-accessTokenが返却されること。
     * </pre>
     */
    @Test
    public void refreshAccessToken_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String appName = "test";
        final String version = "1.1";
        final String product = "test-manager";
        final String clientId = "test-clientId";
        final String accessToken = "test-accessToken";
        final String profile = "battery";
        final int expirePeriod = 1000;
        final int expire = 1999;
        final String[] scopes = {
                "serviceDiscovery",
                "serviceInformation",
                "battery"
        };
        final AtomicReference<String> resultClientId = new AtomicReference<>();
        final AtomicReference<String> resultAccessToken = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    Uri u = Uri.parse(uri);
                    if ("/gotapi/authorization/accessToken".equalsIgnoreCase(u.getPath())) {

                        String name = parms.get(AuthorizationProfileConstants.PARAM_APPLICATION_NAME);
                        if (!appName.equals(name)) {
                            return newBadRequest("appName is invalid. appName=" + name);
                        }

                        String cid = parms.get(AuthorizationProfileConstants.PARAM_CLIENT_ID);
                        if (!clientId.equals(cid)) {
                            return newBadRequest("clientId is invalid. clientId=" + cid);
                        }

                        String ss = parms.get(AuthorizationProfileConstants.PARAM_SCOPE);
                        for (String s : scopes) {
                            if (!ss.contains(s)) {
                                return newBadRequest("scope is invalid. scope=" + ss);
                            }
                        }

                        JSONArray scopes = new JSONArray();

                        JSONObject scope1 = new JSONObject();
                        scope1.put(AuthorizationProfileConstants.PARAM_SCOPE, profile);
                        scope1.put(AuthorizationProfileConstants.PARAM_EXPIRE_PERIOD, expirePeriod);
                        scopes.put(scope1);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, accessToken);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_SCOPES, scopes);
                        jsonObject.put(AuthorizationProfileConstants.PARAM_EXPIRE, expire);
                        return newJsonResponse(jsonObject);
                    } else {
                        return newBadRequest("Path is not Authorization Profile.");
                    }
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.refreshAccessToken(clientId, appName, scopes, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(final String clientId, final String accessToken) {
                resultClientId.set(clientId);
                resultAccessToken.set(accessToken);
                latch.countDown();
            }
            @Override
            public void onError(final int errorCode, final String errorMessage) {
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(resultClientId.get(), is(clientId));
        assertThat(resultAccessToken.get(), is(accessToken));
    }

    /**
     * refreshAccessTokenを呼び出し、OnAuthorizationListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnAuthorizationListener#onError()に通知が来ること。
     * ・errorCodeとerrorMessageが通知されること。
     * </pre>
     */
    @Test
    public void refreshAccessToken_listener_error() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String appName = "test";
        final String version = "1.1";
        final String product = "test-manager";
        final String clientId = "test-clientId";
        final String errorMessage = "error-message";
        final int errorCode = 10;
        final String[] scopes = {
                "serviceDiscovery",
                "serviceInformation",
                "battery"
        };
        final AtomicReference<Integer> resultErrorCode = new AtomicReference<>();
        final AtomicReference<String> resultErrorMessage = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    Uri u = Uri.parse(uri);
                    if ("/gotapi/authorization/accessToken".equalsIgnoreCase(u.getPath())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
                        jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                        jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                        jsonObject.put(DConnectMessage.EXTRA_ERROR_CODE, errorCode);
                        jsonObject.put(DConnectMessage.EXTRA_ERROR_MESSAGE, errorMessage);
                        return newJsonResponse(jsonObject);
                    } else {
                        return newBadRequest("Path is not Authorization Profile.");
                    }
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.refreshAccessToken(clientId, appName, scopes, new DConnectSDK.OnAuthorizationListener() {
            @Override
            public void onResponse(final String clientId, final String accessToken) {
                latch.countDown();
            }
            @Override
            public void onError(final int errorCode, final String errorMessage) {
                resultErrorCode.set(errorCode);
                resultErrorMessage.set(errorMessage);
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(resultErrorCode.get(), is(errorCode));
        assertThat(resultErrorMessage.get(), is(errorMessage));
    }

    /**
     * serviceDiscoveryを呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・servicesに配列が返却されること。
     * ・servicesの中身に指定されたデバイス情報が格納されていること。
     * </pre>
     */
    @Test
    public void serviceDiscovery() {
        final String version = "1.1";
        final String product = "test-manager";
        final String accessToken = "test-accessToken";
        final String[][] aservices = {
                {
                        "serviceId1",
                        "test-service1",
                        ServiceDiscoveryProfileConstants.NetworkType.WIFI.getValue(),
                        "true",
                        "config1"
                }
        };

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/serviceDiscovery".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                String at = parms.get(DConnectMessage.EXTRA_ACCESS_TOKEN);
                if (!accessToken.equals(at)) {
                    return newBadRequest("accessToken is invalid. accessToken=" + at);
                }

                try {
                    JSONArray services = new JSONArray();

                    for (String[] a : aservices) {
                        JSONObject service = new JSONObject();
                        service.put(ServiceDiscoveryProfileConstants.PARAM_ID, a[0]);
                        service.put(ServiceDiscoveryProfileConstants.PARAM_NAME, a[1]);
                        service.put(ServiceDiscoveryProfileConstants.PARAM_TYPE, a[2]);
                        service.put(ServiceDiscoveryProfileConstants.PARAM_ONLINE, "true".equals(a[3]));
                        service.put(ServiceDiscoveryProfileConstants.PARAM_CONFIG, a[4]);
                        services.put(service);
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(ServiceDiscoveryProfileConstants.PARAM_SERVICES, services);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);

        DConnectResponseMessage response = sdk.serviceDiscovery();
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES), is(notNullValue()));

        int idx = 0;
        for (Object obj : response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES)) {
            DConnectMessage service = (DConnectMessage) obj;
            assertThat(service.getString(ServiceDiscoveryProfileConstants.PARAM_ID), is(aservices[idx][0]));
            assertThat(service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME), is(aservices[idx][1]));
        }
    }


    /**
     * serviceDiscoveryを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・servicesに配列が返却されること。
     * ・servicesの中身に指定されたデバイス情報が格納されていること。
     * </pre>
     */
    @Test
    public void serviceDiscovery_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String accessToken = "test-accessToken";
        final String[][] aservices = {
                {
                        "serviceId1",
                        "test-service1",
                        ServiceDiscoveryProfileConstants.NetworkType.WIFI.getValue(),
                        "true",
                        "config1"
                }
        };
        final AtomicReference<DConnectResponseMessage> result = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/serviceDiscovery".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                String at = parms.get(DConnectMessage.EXTRA_ACCESS_TOKEN);
                if (!accessToken.equals(at)) {
                    return newBadRequest("accessToken is invalid. accessToken=" + at);
                }

                try {
                    JSONArray services = new JSONArray();

                    for (String[] a : aservices) {
                        JSONObject service = new JSONObject();
                        service.put(ServiceDiscoveryProfileConstants.PARAM_ID, a[0]);
                        service.put(ServiceDiscoveryProfileConstants.PARAM_NAME, a[1]);
                        service.put(ServiceDiscoveryProfileConstants.PARAM_TYPE, a[2]);
                        service.put(ServiceDiscoveryProfileConstants.PARAM_ONLINE, "true".equals(a[3]));
                        service.put(ServiceDiscoveryProfileConstants.PARAM_CONFIG, a[4]);
                        services.put(service);
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(ServiceDiscoveryProfileConstants.PARAM_SERVICES, services);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.serviceDiscovery(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(DConnectResponseMessage response) {
                result.set(response);
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        DConnectResponseMessage response = result.get();
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES), is(notNullValue()));

        int idx = 0;
        for (Object obj : response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES)) {
            DConnectMessage service = (DConnectMessage) obj;
            assertThat(service.getString(ServiceDiscoveryProfileConstants.PARAM_ID), is(aservices[idx][0]));
            assertThat(service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME), is(aservices[idx][1]));
        }
    }

    /**
     * serviceInformationを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・servicesに配列が返却されること。
     * ・servicesの中身に指定されたデバイス情報が格納されていること。
     * </pre>
     */
    @Test
    public void serviceInformation() {
        final String version = "1.1";
        final String product = "test-manager";
        final String accessToken = "test-accessToken";
        final String serviceId = "test-serviceId";

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/serviceInformation".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                String at = parms.get(DConnectMessage.EXTRA_ACCESS_TOKEN);
                if (!accessToken.equals(at)) {
                    return newBadRequest("accessToken is invalid. accessToken=" + at);
                }

                try {
                    JSONArray supports = new JSONArray();
                    JSONArray supportApis = new JSONArray();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(ServiceInformationProfileConstants.PARAM_SUPPORTS, supports);
                    jsonObject.put(ServiceInformationProfileConstants.PARAM_SUPPORT_APIS, supportApis);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        DConnectResponseMessage response = sdk.getServiceInformation(serviceId);
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getList(ServiceInformationProfileConstants.PARAM_SUPPORTS), is(notNullValue()));
        assertThat(response.getList(ServiceInformationProfileConstants.PARAM_SUPPORT_APIS), is(notNullValue()));
    }

    /**
     * serviceInformationを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・versionに1.1が返却されること。
     * ・servicesに配列が返却されること。
     * ・servicesの中身に指定されたデバイス情報が格納されていること。
     * </pre>
     */
    @Test
    public void serviceInformation_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String accessToken = "test-accessToken";
        final String serviceId = "test-serviceId";
        final AtomicReference<DConnectResponseMessage> result = new AtomicReference<>();

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/serviceInformation".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                String at = parms.get(DConnectMessage.EXTRA_ACCESS_TOKEN);
                if (!accessToken.equals(at)) {
                    return newBadRequest("accessToken is invalid. accessToken=" + at);
                }

                try {
                    JSONArray supports = new JSONArray();
                    JSONArray supportApis = new JSONArray();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(ServiceInformationProfileConstants.PARAM_SUPPORTS, supports);
                    jsonObject.put(ServiceInformationProfileConstants.PARAM_SUPPORT_APIS, supportApis);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.getServiceInformation(serviceId, new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(DConnectResponseMessage response) {
                result.set(response);
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        DConnectResponseMessage response = result.get();
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getList(ServiceInformationProfileConstants.PARAM_SUPPORTS), is(notNullValue()));
        assertThat(response.getList(ServiceInformationProfileConstants.PARAM_SUPPORT_APIS), is(notNullValue()));
    }

    /**
     * getを呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void get() {
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(DConnectProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(DConnectProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.get("http://localhost:4035/gotapi/availability");
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * uriにnullを設定して、getを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void get_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.get((Uri) null);
    }

    /**
     * uriにから文字列を設定して、getを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void get_uri_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.get("");
    }

    /**
     * uriにから不正なURIを設定して、getを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void get_uri_illegal() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.get("test");
    }

    /**
     * getを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void get_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final AtomicReference<DConnectResponseMessage> result = new AtomicReference<>();
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.get("http://localhost:4035/gotapi/availability", new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                result.set(response);
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        DConnectResponseMessage response = result.get();
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * postを呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void post() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        String path = context.getFilesDir() + "/test.dat";
        final byte[] fileData = "This is a test.".getBytes();
        writeFile(new File(path), fileData);

        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final String key = "key";
        final String value = "value";
        final MultipartEntity data = new MultipartEntity();
        data.add(key, new StringEntity(value));
        data.add("data", new FileEntity(new File(path)));

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.POST)) {
                    return newBadRequest("Method is not POST.");
                }

                String v = parms.get(key);
                if (!value.equals(v)) {
                    return newBadRequest("body is invalid.");
                }

                File file = new File(files.get("data"));
                if (!file.isFile() || !Arrays.equals(fileData, getFile(file))) {
                    return newBadRequest("data is invalie.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.post("http://localhost:4035/gotapi/availability", data);
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * uriにnullを設定して、postを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void post_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.post((Uri) null, null);
    }

    /**
     * uriにから文字列を設定して、postを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void post_uri_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.post("", null);
    }

    /**
     * uriにから不正なURIを設定して、postを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void post_uri_illegal() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.post("test", null);
    }

    /**
     * postを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void post_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final String key = "key";
        final String value = "value";
        final MultipartEntity data = new MultipartEntity();
        data.add(key, new StringEntity(value));
        final AtomicReference<DConnectResponseMessage> result = new AtomicReference<>();
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.POST)) {
                    return newBadRequest("Method is not POST.");
                }

                String v = parms.get(key);
                if (!value.equals(v)) {
                    return newBadRequest("body is invalid.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (Exception e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.post("http://localhost:4035/gotapi/availability", data, new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                result.set(response);
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        DConnectResponseMessage response = result.get();
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * deleteを呼び出し、レスポンスを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void delete() {
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final String key = "key";
        final String value = "value";

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.DELETE)) {
                    return newBadRequest("Method is not DELETE.");
                }

                String v = parms.get(key);
                if (!value.equals(v)) {
                    return newBadRequest("body is invalid.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.delete("http://localhost:4035/gotapi/availability?" + key + "=" + value);
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * uriにnullを設定して、deleteを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void delete_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.delete((Uri) null);
    }

    /**
     * uriにから文字列を設定して、deleteを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void delete_uri_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.delete("");
    }

    /**
     * uriにから不正なURIを設定して、deleteを呼び出す。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void delete_uri_illegal() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.delete("test");
    }

    /**
     * deleteを呼び出し、OnResponseListenerにレスポンスが通知されることを確認する。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * ・productにtest-managerが返却されること。
     * ・versionに1.1が返却されること。
     * ・nameにmanagerが返却されること。
     * ・uuidにuuidが返却されること。
     * </pre>
     */
    @Test
    public void delete_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final AtomicReference<DConnectResponseMessage> result = new AtomicReference<>();
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.DELETE)) {
                    return newBadRequest("Method is not DELETE.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_PRODUCT, product);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_NAME, name);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_UUID, uuid);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.delete("http://localhost:4035/gotapi/availability?" + key +"=" + value, new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                result.set(response);
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        DConnectResponseMessage response = result.get();
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    /**
     * デフォルト設定では特に https 通信時の接続先が制限されないこと。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに0が返却されること。
     * </pre>
     */
    @Test
    public void https_hostname_allowed() {
        makeSecure();
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);

        DConnectResponseMessage response = sdk.get("https://localhost:4035");
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
    }

    /**
     * {@link HttpsURLConnection#setDefaultHostnameVerifier}
     * によって https 通信時の接続先を制限できること。
     * <pre>
     * 【期待する動作】
     * ・OnResponseListenerにDConnectResponseMessageが返却されること。
     * ・resultに1が返却されること。
     * ・エラーコードがUNKNOWNであること。
     * </pre>
     */
    @Test
    public void https_hostname_not_allowed() {
        makeSecure();
        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.GET)) {
                    return newBadRequest("Method is not GET.");
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);

        HostnameVerifier defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(final String hostname, final SSLSession session) {
                return false;
            }
        });
        DConnectResponseMessage response = sdk.get("https://localhost:4035");
        HttpsURLConnection.setDefaultHostnameVerifier(defaultVerifier);

        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_ERROR));
        assertThat(response.getErrorCode(), is(DConnectMessage.ErrorCode.UNKNOWN.getCode()));
    }

    private void makeSecure() {
        try {
            mTestServer.stop();
            AssetManager assets = InstrumentationRegistry.getInstrumentation()
                    .getContext()
                    .getAssets();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(assets.open("test-server.pkcs12"), "0000".toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "0000".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            mTestServer.makeSecure(sslContext.getServerSocketFactory(), null);
            mTestServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * WebSocketを接続する。
     * <pre>
     * 【期待する動作】
     * ・OnWebSocketListener#onOpenが呼び出されること。
     * </pre>
     */
    @Test
    public void connectWebSocket() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();
        final String accessToken = "test-accessToken";

        mTestServer.setWebSocketCallback(new TestServer.WebSocketCallback() {
            private void sendMessage(final NanoWSD.WebSocket webSocket, final String message) {
                try {
                    webSocket.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(final NanoWSD.WebSocket webSocket) {
            }

            @Override
            public void onClose(final NanoWSD.WebSocket webSocket, final NanoWSD.WebSocketFrame.CloseCode code,
                                final String reason, final boolean initiatedByRemote) {
            }

            @Override
            public void onMessage(final NanoWSD.WebSocket webSocket, final NanoWSD.WebSocketFrame message) {
                String jsonText = message.getTextPayload();
                if (jsonText == null || jsonText.length() == 0) {
                    sendMessage(webSocket, "{\"result\":1}");
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(jsonText);

                    String at = jsonObject.getString("accessToken");
                    if (!accessToken.equals(at)) {
                        sendMessage(webSocket, "{\"result\":1}");
                        return;
                    }

                    sendMessage(webSocket, "{\"result\":0}");
                } catch (JSONException e) {
                    sendMessage(webSocket, "{\"result\":1}");
                }
            }

            @Override
            public void onPong(final NanoWSD.WebSocket webSocket, final NanoWSD.WebSocketFrame pong) {
            }

            @Override
            public void onException(final NanoWSD.WebSocket webSocket, final IOException exception) {
            }
        });


        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
            @Override
            public void onOpen() {
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onClose() {
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            sdk.disconnectWebSocket();
        }

        assertThat(result.get(), is(true));
    }

    /**
     * OnWebSocketListenerにnullを設定してWebSocketを接続する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void connectWebSocket_listener_null() {
        final String accessToken = "test-accessToken";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.connectWebSocket(null);
    }

    /**
     * addEventListenerを行いイベントを受け取れることを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectEventMessageが受け取れること。
     * </pre>
     */
    @Test
    public void addEventListener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean();
        final AtomicReference<DConnectEventMessage> event = new AtomicReference<>();
        final String version = "1.1";
        final String product = "test-manager";
        final String accessToken = "test-accessToken";
        final String profile = DeviceOrientationProfileConstants.PROFILE_NAME;
        final String attribute = DeviceOrientationProfileConstants.ATTRIBUTE_ON_DEVICE_ORIENTATION;
        final String serviceId = "abc";

        final float accelX = 1.0f;
        final float accelY = 1.5f;
        final float accelZ = 3.9f;
        final int interval = 1001;

        final NanoWSD.WebSocket[] webSocket1 = new NanoWSD.WebSocket[1];

        mTestServer.setWebSocketCallback(new TestServer.WebSocketCallback() {
            private void sendMessage(final NanoWSD.WebSocket webSocket, final String message) {
                try {
                    webSocket.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(final NanoWSD.WebSocket webSocket) {
            }

            @Override
            public void onClose(final NanoWSD.WebSocket webSocket, final NanoWSD.WebSocketFrame.CloseCode code,
                                final String reason, final boolean initiatedByRemote) {
            }

            @Override
            public void onMessage(final NanoWSD.WebSocket webSocket, final NanoWSD.WebSocketFrame message) {
                String jsonText = message.getTextPayload();
                if (jsonText == null || jsonText.length() == 0) {
                    sendMessage(webSocket, "{\"result\":1}");
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(jsonText);

                    String at = jsonObject.getString("accessToken");
                    if (!accessToken.equals(at)) {
                        sendMessage(webSocket, "{\"result\":1}");
                        return;
                    }

                    webSocket1[0] = webSocket;
                    sendMessage(webSocket, "{\"result\":0}");
                } catch (JSONException e) {
                    sendMessage(webSocket, "{\"result\":1}");
                }
            }

            @Override
            public void onPong(final NanoWSD.WebSocket webSocket, final NanoWSD.WebSocketFrame pong) {
            }

            @Override
            public void onException(final NanoWSD.WebSocket webSocket, final IOException exception) {
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
            @Override
            public void onOpen() {
                result.set(true);
                latch.countDown();
            }

            @Override
            public void onClose() {
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            sdk.disconnectWebSocket();
            fail("timeout");
        }
        assertThat(result.get(), is(true));

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.PUT)) {
                    return newBadRequest("Method is not PUT.");
                }

                Uri u = Uri.parse(uri);
                if (!"/gotapi/deviceOrientation/onDeviceOrientation".equalsIgnoreCase(u.getPath())) {
                    return newBadRequest("uri is invalid. uri=" + uri);
                }

                String at = parms.get(DConnectMessage.EXTRA_ACCESS_TOKEN);
                if (!accessToken.equals(at)) {
                    return newBadRequest("accessToken is invalid. accessToken=" + at);
                }

                try {
                    // 1秒後にイベントを送信
                    Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject orientation = new JSONObject();

                                JSONObject acceleration = new JSONObject();
                                acceleration.put(DeviceOrientationProfileConstants.PARAM_X, accelX);
                                acceleration.put(DeviceOrientationProfileConstants.PARAM_Y, accelY);
                                acceleration.put(DeviceOrientationProfileConstants.PARAM_Z, accelZ);
                                orientation.put(DeviceOrientationProfileConstants.PARAM_ACCELERATION, acceleration);
                                orientation.put(DeviceOrientationProfileConstants.PARAM_INTERVAL, interval);

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(DeviceOrientationProfileConstants.PARAM_ORIENTATION, orientation);
                                jsonObject.put(DConnectMessage.EXTRA_API, "gotapi");
                                jsonObject.put(DConnectMessage.EXTRA_PROFILE, profile);
                                jsonObject.put(DConnectMessage.EXTRA_ATTRIBUTE, attribute);
                                jsonObject.put(DConnectMessage.EXTRA_SERVICE_ID, serviceId);

                                webSocket1[0].send(jsonObject.toString());
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1, TimeUnit.SECONDS);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_VERSION, version);
                    jsonObject.put(AvailabilityProfileConstants.PARAM_PRODUCT, product);
                    return newJsonResponse(jsonObject);
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });


        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setProfile(profile);
        builder.setAttribute(attribute);
        builder.setServiceId(serviceId);

        sdk.addEventListener(builder.build(), new DConnectSDK.OnEventListener() {
            @Override
            public void onMessage(final DConnectEventMessage message) {
                event.set(message);
                latch2.countDown();
            }

            @Override
            public void onResponse(final DConnectResponseMessage response) {
                result.set(true);
            }
        });

        // イベントからのメッセージを待つ
        try {
            latch2.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            sdk.disconnectWebSocket();
        }

        DConnectEventMessage e = event.get();
        assertThat(e, is(notNullValue()));
        assertThat(e.getString(DConnectMessage.EXTRA_PROFILE), is(profile));
        assertThat(e.getString(DConnectMessage.EXTRA_ATTRIBUTE), is(attribute));
        assertThat(e.getString(DConnectMessage.EXTRA_SERVICE_ID), is(serviceId));

        DConnectMessage orientation = e.getMessage(DeviceOrientationProfileConstants.PARAM_ORIENTATION);
        assertThat(orientation, is(notNullValue()));
        assertThat(orientation.getInt(DeviceOrientationProfileConstants.PARAM_INTERVAL), is(interval));

        DConnectMessage acceleration = orientation.getMessage(DeviceOrientationProfileConstants.PARAM_ACCELERATION);
        assertThat(acceleration, is(notNullValue()));

        assertThat(acceleration.getFloat(DeviceOrientationProfileConstants.PARAM_X), is(accelX));
        assertThat(acceleration.getFloat(DeviceOrientationProfileConstants.PARAM_Y), is(accelY));
        assertThat(acceleration.getFloat(DeviceOrientationProfileConstants.PARAM_Z), is(accelZ));
    }

    /**
     * OnEventListenerにnullを設定してaddEventListenerを行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void addEventListener_listener_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setProfile("deviceOrientation");
        builder.setAttribute("onDeviceOrientation");
        builder.setServiceId("serviceId");
        sdk.addEventListener(builder.toASCIIString(), null);
    }

    /**
     * uriにnullを設定してaddEventListenerを行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void addEventListener_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.addEventListener((Uri) null, new DConnectSDK.OnEventListener() {
            @Override
            public void onMessage(final DConnectEventMessage message) {
            }
            @Override
            public void onResponse(final DConnectResponseMessage response) {
            }
        });
    }

    /**
     * uriにnullを設定してremoveEventListenerを行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void removeEventListener_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.removeEventListener((Uri) null);
    }
}
