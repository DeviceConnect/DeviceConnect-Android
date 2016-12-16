/*
 RESTfulDConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.InstrumentationRegistry;

import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.test.DConnectTestCase;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.DConnectSDKFactory;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * RESTful API用テストケース.
 * @author NTT DOCOMO, INC.
 */
public class RESTfulDConnectTestCase extends DConnectTestCase {
    public void setUp() throws Exception {
        mDConnectSDK = DConnectSDKFactory.create(InstrumentationRegistry.getContext(), DConnectSDKFactory.Type.HTTP);
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        mDConnectSDK = null;
    }

    protected String createClient() {
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(AuthorizationProfileConstants.PROFILE_NAME);
        builder.setAttribute(AuthorizationProfileConstants.ATTRIBUTE_GRANT);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        assertThat(response, is(notNullValue()));
        assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
        return response.getString(AuthorizationProfile.PARAM_CLIENT_ID);
    }

    @Override
    protected String requestAccessToken(final String[] scopes) {
        DConnectResponseMessage response = mDConnectSDK.authorization("JUnitTest", scopes);
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            return response.getString(AuthorizationProfile.PARAM_ACCESS_TOKEN);
        } else {
            return null;
        }
    }

    /**
     * デバイス一覧をRestfulAPIで取得する.
     * @return デバイス一覧
     */
    @Override
    protected List<ServiceInfo> searchServices() {
        List<ServiceInfo> services = new ArrayList<>();
        DConnectResponseMessage response = mDConnectSDK.serviceDiscovery();
        if (response.getResult() == DConnectMessage.RESULT_ERROR) {
            fail("Failed to discover services.");
        }

        List<Object> list = response.getList(ServiceDiscoveryProfile.PARAM_SERVICES);
        for (Object value : list) {
            DConnectMessage service = (DConnectMessage) value;
            String serviceId = service.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
            String deviceName = service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
            services.add(new ServiceInfo(serviceId, deviceName));

        }
        return services;
    }

    @Override
    protected List<PluginInfo> searchPlugins() {
        List<PluginInfo> plugins = new ArrayList<>();
        DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);

        DConnectResponseMessage response = mDConnectSDK.get(builder.build());
        if (response.getResult() == DConnectMessage.RESULT_ERROR) {
            fail("Failed to get plugin list.");
        }

        List<Object> objects = response.getList(SystemProfile.PARAM_PLUGINS);
        for (Object obj : objects) {
            DConnectMessage plugin = (DConnectMessage) obj;
            String id = plugin.getString(SystemProfileConstants.PARAM_ID);
            String name = plugin.getString(SystemProfileConstants.PARAM_NAME);
            plugins.add(new PluginInfo(id, name));
        }
        return plugins;
    }

    @Override
    protected boolean isManagerAvailable() {
        DConnectResponseMessage response = mDConnectSDK.availability();
        return response.getResult() == DConnectMessage.RESULT_OK;
    }

    protected final DConnectResponseMessage sendRequest(final String method, final String uri) {
        return sendRequest(method, uri, null, null);
    }

    protected final DConnectResponseMessage sendRequest(final String method, final String uri, final Object body) {
        return sendRequest(method, uri, null, body);
    }

    /**
     * 指定されたURIにリクエストを送信します.
     * @param method HTTPメソッド
     * @param uri URI
     * @param headers ヘッダー
     * @param body ボディ
     * @return レスポンス
     */
    protected final DConnectResponseMessage sendRequest(final String method, final String uri, final Map<String, String> headers, final Object body) {
        Map<String, String> requestHeaders = headers;
        if (requestHeaders == null) {
            requestHeaders = new HashMap<>();
        }
        requestHeaders.put(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());

        String nonce = toHexString(generateRandom(16));
        String requestUri = uri;
        Map requestBody = null;
        if (body != null) {
            if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("DELETE")) {
                requestUri += "?" + nonce;
                if (body instanceof Map<?, ?>) {
                    for (Map.Entry<String, String> entry : ((Map<String, String>) body).entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        requestUri += "&" + key + "=" + value;
                    }
                } else if (body instanceof String) {
                    requestUri += "&" + body;
                }
            } else {
                requestBody = (Map) body;
            }
        }

        if (method.equalsIgnoreCase("GET")) {
            return mDConnectSDK.get(requestUri);
        } else if (method.equalsIgnoreCase("PUT")) {
            return mDConnectSDK.put(requestUri, requestBody);
        } else if (method.equalsIgnoreCase("POST")) {
            return mDConnectSDK.put(requestUri, requestBody);
        } else if (method.equalsIgnoreCase("DELETE")) {
            return mDConnectSDK.delete(requestUri);
        } else {
            fail("Unknown http method.");
        }
        return null;
    }
}
