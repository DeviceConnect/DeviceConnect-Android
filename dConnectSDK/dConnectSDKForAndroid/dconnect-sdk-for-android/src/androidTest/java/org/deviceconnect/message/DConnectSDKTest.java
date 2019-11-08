/*
 DConnectSDKTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * DConnectSDKのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class DConnectSDKTest {
    /**
     * ホスト名を取得する。
     * <pre>
     * 【期待する動作】
     * ・デフォルト値であるlocalhostが取得できること。
     * </pre>
     */
    @Test
    public void getHost() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        assertThat(sdk.getHost(), is("localhost"));
    }

    /**
     * ホスト名を設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したホスト名がgetHostで取得できること。
     * </pre>
     */
    @Test
    public void setHost() {
        final String hostName = "host";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setHost(hostName);
        assertThat(sdk.getHost(), is(hostName));
    }

    /**
     * ホスト名にnullを設定する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void setHost_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setHost(null);
    }

    /**
     * ホスト名に空文字を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void setHost_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setHost("");
    }

    /**
     * ポート番号を取得する。
     * <pre>
     * 【期待する動作】
     * ・デフォルト値である4035が取得できること。
     * </pre>
     */
    @Test
    public void getPort() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        assertThat(sdk.getPort(), is(4035));
    }

    /**
     * ポート番号を設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したポート番号がgetPortで取得できること。
     * </pre>
     */
    @Test
    public void setPort() {
        final int port = 9999;
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setPort(port);
        assertThat(sdk.getPort(), is(port));
    }

    /**
     * ポート番号に負の値を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void setPort_negative() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setPort(-1);
    }

    /**
     * ポート番号に65536を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void setPort_65536() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setPort(65536);
    }

    /**
     * オリジンを設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したオリジンがgetOriginで取得できること。
     * </pre>
     */
    @Test
    public void setOrigin() {
        final String origin = "org.deviceconnect.android.test";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setOrigin(origin);
        assertThat(sdk.getOrigin(), is(origin));
    }

    /**
     * オリジンにnullを設定する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void setOrigin_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setOrigin(null);
    }

    /**
     * オリジンに空文字を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void setOrigin_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setOrigin("");
    }

    /**
     * アクセストークンを設定する。
     * <pre>
     * 【期待する動作】
     * ・設定したアクセストークンがgetAccessTokenで取得できること。
     * </pre>
     */
    @Test
    public void setAccessToken() {
        final String accessToken = "test-accessToken";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        assertThat(sdk.getAccessToken(), is(accessToken));
    }

    /**
     * アクセストークンにnullを設定する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void setAccessToken_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(null);
    }

    /**
     * アクセストークンに空文字を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void setAccessToken_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken("");
    }

    /**
     * {@link DConnectSDK#startManager(Context)}の引数コンテキストに{@code null}を指定して実行する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void startManager_context_is_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.startManager(null);
    }

    /**
     * {@link DConnectSDK#stopManager(Context)}の引数コンテキストに{@code null}を指定して実行する。
     * <pre>
     * 【期待する動作】
     * ・NullPointerExceptionが発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void stopManager_context_is_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getInstrumentation().getContext(), DConnectSDKFactory.Type.HTTP);
        sdk.stopManager(null);
    }
}
