/*
 URIBuilderTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * URIBuilderのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class URIBuilderTest {

    /**
     * URIBuilderの生成を確認する。
     * <pre>
     * 【期待する動作】
     * ・URIBuilderのインスタンスが生成されること。
     * ・DConnectSDKに設定してあるホスト名が設定されていること。
     * ・DConnectSDKに設定してあるポート番号が設定されていること。
     * ・DConnectSDKに設定してあるアクセストークンが設定されていること。
     * </pre>
     */
    @Test
    public void uriBuilder() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        assertThat(builder, is(notNullValue()));
        assertThat(builder.getHost(), is(sdk.getHost()));
        assertThat(builder.getPort(), is(sdk.getPort()));
        assertThat(builder.getAccessToken(), is(sdk.getAccessToken()));
    }

    /**
     * ホスト名を設定できることを確認する。
     * <pre>
     * 【期待する動作】
     * ・設定したホスト名がgetHostで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_host() {
        final String host = "test.com";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setHost(host);
        assertThat(builder.getHost(), is(host));
    }

    /**
     * ホスト名にnullを設定する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_host_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.setHost(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * ホスト名にから文字列を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_host_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.setHost("");
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    /**
     * ポート番号を設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したポート番号がgetPortで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_port() {
        final int port = 9999;
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setPort(port);
        assertThat(builder.getPort(), is(port));
    }

    /**
     * ポート番号に負の値を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_port_negative() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.setPort(-1);
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    /**
     * ポート番号に65536を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_port_65536() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.setPort(65536);
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }

    /**
     * アクセストークンを設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したアクセストークンがgetAccessTokenで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_accessToken() {
        final String accessToken = "abc";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setAccessToken(accessToken);
        assertThat(builder.getAccessToken(), is(accessToken));
    }

    /**
     * アクセストークンにnullを設定する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_accessToken_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.setAccessToken(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * serviceIdを設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したserviceIdがgetServiceIdで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_serviceId() {
        final String serviceId = "test-serviceId";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setServiceId(serviceId);
        assertThat(builder.getServiceId(), is(serviceId));
    }

    /**
     * serviceIdにnullを設定する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_serviceId_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.setServiceId(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * addParameterで値が設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したkey=valueがgetParameterで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_addParameter() {
        final String key = "key";
        final String value = "value";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.addParameter(key, value);
        assertThat(builder.getParameter(key), is(value));
    }

    /**
     * keyにnullを設定してaddParameterを行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_addParameter_key_null() {
        final String value = "value";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.addParameter(null, value);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * valueにnullを設定してaddParameterを行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test
    public void uriBuilder_addParameter_value_null() {
        final String key = "key";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        try {
            builder.addParameter(key, null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }
}
