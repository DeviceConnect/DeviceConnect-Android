/*
 DConnectSDKTest.java
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test
    public void setHost_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setHost(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * ホスト名に空文字を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test
    public void setHost_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setHost("");
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test
    public void setPort_negative() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setPort(-1);
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
    public void setPort_65536() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setPort(65536);
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
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
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test
    public void setOrigin_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setOrigin(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * オリジンに空文字を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test
    public void setOrigin_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setOrigin("");
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
    public void setAccessToken() {
        final String accessToken = "test-accessToken";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
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
    @Test
    public void setAccessToken_null() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setAccessToken(null);
            fail("No NullPointerException occurred.");
        } catch (NullPointerException e) {
            // テスト成功
        }
    }

    /**
     * アクセストークンに空文字を設定する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが発生すること。
     * </pre>
     */
    @Test
    public void setAccessToken_empty() {
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        try {
            sdk.setAccessToken("");
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // テスト成功
        }
    }
}
