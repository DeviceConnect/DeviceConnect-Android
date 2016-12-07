package org.deviceconnect.message;

import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.message.server.TestServer;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.availability();
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    @Test
    public void availability_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final DConnectResponseMessage[] response = new DConnectResponseMessage[1];

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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.availability(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage r) {
                response[0] = r;
                latch.countDown();
            }
        });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(response[0], is(notNullValue()));
        assertThat(response[0].getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response[0].getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response[0].getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.authorization(appName, scopes);
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN), is(accessToken));
        assertThat(response.getInt(AuthorizationProfileConstants.PARAM_EXPIRE), is(expire));
        assertThat(response.getList(AuthorizationProfileConstants.PARAM_SCOPES), is(notNullValue()));
    }


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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);

        DConnectResponseMessage response = sdk.serviceDiscovery();
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES), is(notNullValue()));

        int idx = 0;
        for (Object obj : response.getList(ServiceDiscoveryProfileConstants.PARAM_SERVICES)) {
            Map service = (Map) obj;
            String id = (String) service.get(ServiceDiscoveryProfileConstants.PARAM_ID);
            String name = (String) service.get(ServiceDiscoveryProfileConstants.PARAM_NAME);
            assertThat(id, is(aservices[idx][0]));
            assertThat(name, is(aservices[idx][1]));
        }
    }

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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.get("http://localhost:4035/gotapi/availability");
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(DConnectProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(DConnectProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    @Test
    public void get_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.get(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    @Test
    public void get_uri_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.get("");
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    @Test
    public void get_uri_illegal() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.get("test");
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    @Test
    public void get_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final DConnectResponseMessage[] response = new DConnectResponseMessage[1];
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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.get("http://localhost:4035/gotapi/availability", new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage r) {
                response[0] = r;
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(response[0], notNullValue());
        assertThat(response[0].getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    @Test
    public void get_listener_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.get(null, new DConnectSDK.OnResponseListener() {
                @Override
                public void onResponse(final DConnectResponseMessage response) {
                }
            });
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    @Test
    public void post() {
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final String key = "key";
        final String value = "value";
        String body = key + "="  + value;
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
                } catch (JSONException e) {
                    return newInternalServerError(e.getMessage());
                }
            }
        });

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.post("http://localhost:4035/gotapi/availability", body.getBytes());
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    @Test
    public void post_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.post(null, null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    @Test
    public void post_uri_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.post("", null);
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    @Test
    public void post_uri_illegal() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.post("test", null);
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    @Test
    public void post_listener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final String key = "key";
        final String value = "value";
        String body = key + "="  + value;
        final DConnectResponseMessage[] response = new DConnectResponseMessage[1];
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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.post("http://localhost:4035/gotapi/availability", body.getBytes(), new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage r) {
                response[0] = r;
                latch.countDown();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        assertThat(response[0], notNullValue());
        assertThat(response[0].getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response[0].getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    @Test
    public void put() {
        final String version = "1.1";
        final String product = "test-manager";
        final String name = "manager";
        final String uuid = "uuid";
        final String key = "key";
        final String value = "value";
        String body = key + "="  + value;

        mTestServer.setServerCallback(new TestServer.ServerCallback() {
            @Override
            public NanoHTTPD.Response serve(final String uri, final NanoHTTPD.Method method, final Map<String, String> headers,
                                            final Map<String, String> parms, final Map<String, String> files) {
                if (!method.equals(NanoHTTPD.Method.PUT)) {
                    return newBadRequest("Method is not PUT.");
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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectResponseMessage response = sdk.put("http://localhost:4035/gotapi/availability", body.getBytes());
        assertThat(response, notNullValue());
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_VERSION), is(version));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_PRODUCT), is(product));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_NAME), is(name));
        assertThat(response.getString(AvailabilityProfileConstants.PARAM_UUID), is(uuid));
    }

    @Test
    public void connectWebSocket() {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];
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


        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
            @Override
            public void onOpen() {
                result[0] = true;
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

        assertThat(result[0], is(true));
    }

    @Test
    public void connectWebSocket_listener_null() {
        final String accessToken = "test-accessToken";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        try {
            sdk.connectWebSocket(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    @Test
    public void addEventListener() {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final boolean[] result = new boolean[1];
        final DConnectEventMessage[] event = new DConnectEventMessage[1];
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

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        sdk.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
            @Override
            public void onOpen() {
                result[0] = true;
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
        assertThat(result[0], is(true));

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

        sdk.addEventListener(builder.toASCIIString(), new DConnectSDK.OnEventListener() {
            @Override
            public void onMessage(final DConnectEventMessage message) {
                event[0] = message;
                latch2.countDown();
            }

            @Override
            public void onResponse(final DConnectResponseMessage response) {
                result[0] = true;
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

        assertThat(event[0], is(notNullValue()));
        assertThat(event[0].getString(DConnectMessage.EXTRA_PROFILE), is(profile));
        assertThat(event[0].getString(DConnectMessage.EXTRA_ATTRIBUTE), is(attribute));
        assertThat(event[0].getString(DConnectMessage.EXTRA_SERVICE_ID), is(serviceId));

        DConnectMessage orientation = event[0].getMessage(DeviceOrientationProfileConstants.PARAM_ORIENTATION);
        assertThat(orientation, is(notNullValue()));
        assertThat(orientation.getInt(DeviceOrientationProfileConstants.PARAM_INTERVAL), is(interval));

        DConnectMessage acceleration = orientation.getMessage(DeviceOrientationProfileConstants.PARAM_ACCELERATION);
        assertThat(acceleration, is(notNullValue()));

        assertThat(acceleration.getFloat(DeviceOrientationProfileConstants.PARAM_X), is(accelX));
        assertThat(acceleration.getFloat(DeviceOrientationProfileConstants.PARAM_Y), is(accelY));
        assertThat(acceleration.getFloat(DeviceOrientationProfileConstants.PARAM_Z), is(accelZ));
    }

    @Test
    public void addEventListener_listener_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setProfile("deviceOrientation");
        builder.setAttribute("onDeviceOrientation");
        builder.setServiceId("serviceId");
        try {
            sdk.addEventListener(builder.toASCIIString(), null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    @Test
    public void addEventListener_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.addEventListener(null, new DConnectSDK.OnEventListener() {
                @Override
                public void onMessage(final DConnectEventMessage message) {
                }
                @Override
                public void onResponse(final DConnectResponseMessage response) {
                }
            });
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    @Test
    public void removeEventListener_uri_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.removeEventListener(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }
}
