/*
 URIBuilderTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test(expected = NullPointerException.class)
    public void uriBuilder_host_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setHost(null);
    }

    /**
     * ホスト名にから文字列を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void uriBuilder_host_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setHost("");
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test(expected = IllegalArgumentException.class)
    public void uriBuilder_port_negative() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setPort(-1);
    }

    /**
     * ポート番号に65536を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void uriBuilder_port_65536() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setPort(65536);
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test(expected = NullPointerException.class)
    public void uriBuilder_accessToken_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setAccessToken(null);
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test(expected = NullPointerException.class)
    public void uriBuilder_serviceId_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setServiceId(null);
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test(expected = NullPointerException.class)
    public void uriBuilder_addParameter_key_null() {
        final String value = "value";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.addParameter(null, value);
    }

    /**
     * valueにnullを設定してaddParameterを行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void uriBuilder_addParameter_value_null() {
        final String key = "key";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.addParameter(key, null);
    }

    /**
     * 同じkeyに対してaddParameterを行う。
     * <pre>
     * 【期待する動作】
     * ・後から設定されたvalueが設定されていること。
     * </pre>
     */
    @Test
    public void uriBuilder_addParameter_duplicate() {
        final String key = "key";
        final String value1 = "value1";
        final String value2 = "value2";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.addParameter(key, value1);
        builder.addParameter(key, value2);

        assertThat(builder.getParameter(key), is(value2));
    }

    /**
     * keyに対してremoveParameterを行い、値が削除されていることを確認する。
     * <pre>
     * 【期待する動作】
     * ・削除したkeyに対してgetParameterを行い値が取得できないこと。
     * </pre>
     */
    @Test
    public void uriBuilder_removeParameter() {
        final String key = "key";
        final String value = "value";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.addParameter(key, value);
        assertThat(builder.getParameter(key), is(value));
        builder.removeParameter(key);
        assertThat(builder.getParameter(key), is(nullValue()));
    }

    /**
     * profileが設定できることを確認する。
     * <pre>
     * 【期待する動作】
     * ・設定したプロファイルがgetProfileで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_profile() {
        final String profile = "profile";

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setProfile(profile);

        assertThat(builder.getProfile(), is(profile));
    }

    /**
     * interfaceが設定できることを確認する。
     * <pre>
     * 【期待する動作】
     * ・設定したプロファイルがgetInterfaceで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_interface() {
        final String inter = "inter";

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setInterface(inter);

        assertThat(builder.getInterface(), is(inter));
    }

    /**
     * attributeが設定できることを確認する。
     * <pre>
     * 【期待する動作】
     * ・設定したプロファイルがgetAttributeで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_attribute() {
        final String attribute = "attribute";

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("test-accessToken");

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setAttribute(attribute);

        assertThat(builder.getAttribute(), is(attribute));
    }

    /**
     * attributeが設定できることを確認する。
     * <pre>
     * 【期待する動作】
     * ・設定したプロファイルがgetAttributeで取得できること。
     * </pre>
     */
    @Test
    public void uriBuilder_toString() {
        final String host = "test.com";
        final int port = 12345;
        final String profile = "profile";
        final String attribute = "attribute";
        final String accessToken = "test-accessToken";

        final String uri = "http://" + host + ":" + port + "/gotapi/" + profile + "/" + attribute + "?accessToken=" + accessToken;

        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);

        DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
        builder.setHost(host);
        builder.setPort(port);
        builder.setProfile(profile);
        builder.setAttribute(attribute);

        assertThat(builder.toString(), is(uri));
    }
}
