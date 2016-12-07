package org.deviceconnect.message;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DConnectSDKTest {

    @Test
    public void setHost() {
        final String hostName = "host";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setHost(hostName);
        assertThat(sdk.getHost(), is(hostName));
    }

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

    @Test
    public void setPort() {
        final int port = 9999;
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setPort(port);
        assertThat(sdk.getPort(), is(port));
    }

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

    @Test
    public void setOrigin() {
        final String origin = "org.deviceconnect.android.test";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setOrigin(origin);
        assertThat(sdk.getOrigin(), is(origin));
    }

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

    @Test
    public void setAccessToken() {
        final String accessToken = "test-accessToken";
        DConnectSDK sdk = DConnectSDKFactory.create(InstrumentationRegistry.getTargetContext(), DConnectSDKFactory.Type.HTTP);
        sdk.setAccessToken(accessToken);
        assertThat(sdk.getAccessToken(), is(accessToken));
    }

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
