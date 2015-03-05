/*
 IntentDConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.localoauth.exception.AuthorizatonException;
import org.deviceconnect.android.test.DConnectTestCase;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.AvailabilityProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;


/**
 * Intent用のテストケース.
 * @author NTT DOCOMO, INC.
 */
public class IntentDConnectTestCase extends DConnectTestCase {
    /** Device ConnectManagerのレシーバーへのCompomentName. */
    public static final String DCCONNECT_MANAGER_RECEIVER
            = "org.deviceconnect.android.manager/.DConnectBroadcastReceiver";

    /** ポーリング時間(ms). */
    private static final int POLLING_WAIT_TIME = 100;
    /** デフォルトのタイムアウト時間(ms). */
    private static final int DEFAULT_RESTFUL_TIMEOUT = 10000;
    /** タイムアウト時間. */
    private int mTimeout = DEFAULT_RESTFUL_TIMEOUT;
    /** リクエストコード生成用シード. */
    private int mRequestCodeSeed = 0;

    /**
     * リクエスト一覧.
     * ここに登録されたリクエストコードに対するレスポンスが返ってくることを期待している。
     */
    private final Map<Integer, Intent> mRequests = new ConcurrentHashMap<Integer, Intent>();

    /**
     * コンストラクタ.
     * @param string テストタグ
     */
    public IntentDConnectTestCase(final String string) {
        super(string);
    }

    @Override
    protected void setUp() throws Exception {
        // レシーバを登録
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TEST_ACTION_RESPONSE);
        getApplicationContext().registerReceiver(mResponseReceiver, intentFilter);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        getApplicationContext().unregisterReceiver(mResponseReceiver);

        // タイムアウトしたとき、前のintentが残り、
        // その結果が次の処理で取得されることがあるよう
        mRequests.clear();

        super.tearDown();
    }

    @Override
    protected String[] createClient() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, AuthorizationProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, AuthorizationProfileConstants.ATTRIBUTE_GRANT);
        request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());

        Intent response = sendRequest(request, false);
        assertResultOK(response);
        String clientId = response.getStringExtra(AuthorizationProfileConstants.PARAM_CLIENT_ID);
        String clientSecret = response.getStringExtra(AuthorizationProfileConstants.PARAM_CLIENT_SECRET);
        return new String[] {clientId, clientSecret};
    }


    @Override
    protected String requestAccessToken(final String clientId, final String clientSecret, final String[] scopes) {
        StringBuilder paramScope = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) {
                paramScope.append(",");
            }
            paramScope.append(scopes[i]);
        }

        /* Signatureを生成して添付する */
        String signature = "";
        try {
            signature = LocalOAuth2Main.createSignature(clientId,
                    LocalOAuth2Main.AUTHORIZATION_CODE, null, scopes, clientSecret);
        } catch (AuthorizatonException e) {
            fail("Failed to create a signature.");
        }

        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, AuthorizationProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, AuthorizationProfileConstants.ATTRIBUTE_REQUEST_ACCESS_TOKEN);
        request.putExtra(AuthorizationProfileConstants.PARAM_CLIENT_ID, clientId);
        request.putExtra(AuthorizationProfileConstants.PARAM_SCOPE, paramScope.toString());
        request.putExtra(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, "dConnectManagerTest");
        request.putExtra(AuthorizationProfileConstants.PARAM_GRANT_TYPE, LocalOAuth2Main.AUTHORIZATION_CODE);
        request.putExtra(AuthorizationProfileConstants.PARAM_SIGNATURE, signature);

        Intent response = sendRequest(request, false);
        return response.getStringExtra(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN);
    }

    @Override
    protected List<DeviceInfo> searchDevices() {
        Intent intent = new Intent(IntentDConnectMessage.ACTION_GET);
        intent.putExtra(DConnectMessage.EXTRA_PROFILE,
                ServiceDiscoveryProfileConstants.PROFILE_NAME);

        Intent response = sendRequest(intent);
        assertResultOK(response);

        List<DeviceInfo> services = new ArrayList<DeviceInfo>();
        Parcelable[] servicesExtra = response.getParcelableArrayExtra(
                ServiceDiscoveryProfileConstants.PARAM_SERVICES);
        for (int i = 0; i < servicesExtra.length; i++) {
            Bundle obj = (Bundle) servicesExtra[i];
            String serviceId = obj.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
            String deviceName = obj.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
            services.add(new DeviceInfo(serviceId, deviceName));
        }
        return services;
    }

    @Override
    protected List<PluginInfo> searchPlugins() {
        Intent intent = new Intent(IntentDConnectMessage.ACTION_GET);
        intent.putExtra(DConnectMessage.EXTRA_PROFILE, SystemProfileConstants.PROFILE_NAME);

        Intent response = sendRequest(intent);
        assertResultOK(response);

        List<PluginInfo> plugins = new ArrayList<PluginInfo>();
        Parcelable[] pluginsExtra = response.getParcelableArrayExtra(SystemProfileConstants.PARAM_PLUGINS);
        for (int i = 0; i < pluginsExtra.length; i++) {
            Bundle obj = (Bundle) pluginsExtra[i];
            String id = obj.getString(SystemProfileConstants.PARAM_ID);
            String name = obj.getString(SystemProfileConstants.PARAM_NAME);
            plugins.add(new PluginInfo(id, name));
        }
        return plugins;
    }

    @Override
    protected boolean isManagerAvailable() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, AvailabilityProfileConstants.PROFILE_NAME);
        request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());
        Intent response = sendRequest(request);
        if (response == null) {
            return false;
        }
        if (!response.hasExtra(DConnectMessage.EXTRA_RESULT)) {
            return false;
        }
        int result = response.getIntExtra(DConnectMessage.EXTRA_RESULT, -1);
        return result == DConnectMessage.RESULT_OK;
    }

    /**
     * タイムアウトを設定する.
     * デフォルトでは、DEFAULT_RESTFUL_TIMEOUTが設定されている。
     * @param timeout タイムアウト時間
     */
    protected void setTimeout(final int timeout) {
        this.mTimeout = timeout;
    }

    /**
     * IntentでdConnectManagerにリクエストを出す.
     * 
     * 内部で、request_codeとreceiverのデータを付加する。
     * タイムアウトした場合にはnullを返却する。
     * 
     * @param intent リクエストのintent
     * @param afterAuth アプリ認可済みかどうかのフラグ
     * @return レスポンスのintent
     */
    protected Intent sendRequest(final Intent intent, final boolean afterAuth) {
        final byte[] nonce = generateRandom(16);
        final int requestCode = generateRequestCode();

        ComponentName cn = new ComponentName("org.deviceconnect.android.test", 
                "org.deviceconnect.android.test.TestCaseBroadcastReceiver");
        intent.setComponent(ComponentName.unflattenFromString(DCCONNECT_MANAGER_RECEIVER));
        intent.putExtra(IntentDConnectMessage.EXTRA_RECEIVER, cn);
        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        if (afterAuth) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ACCESS_TOKEN, mAccessToken);
        } else {
            intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());
        }
        intent.putExtra(IntentDConnectMessage.EXTRA_NONCE, toHexString(nonce));

        getApplicationContext().sendBroadcast(intent);

        // タイムアウト時間までレスポンスを待つ
        long now = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(POLLING_WAIT_TIME);
            } catch (InterruptedException e) {
                break;
            }
        } while (mRequests.get(requestCode) == null
                && (System.currentTimeMillis() - now) < mTimeout);

        Intent resp = mRequests.remove(requestCode);
        assertEquals(resp.getStringExtra(DConnectProfileConstants.PARAM_PRODUCT), DCONNECT_MANAGER_APP_NAME);
        assertEquals(resp.getStringExtra(DConnectProfileConstants.PARAM_VERSION), DCONNECT_MANAGER_VERSION_NAME);

        // HMACの検証
        String hmacString = resp.getStringExtra(IntentDConnectMessage.EXTRA_HMAC);
        if (hmacString == null) {
            fail("Device Connect Manager must send HMAC.");
        }
        try {
            byte[] expectedHmac = calculateHMAC(nonce);
            assertEquals(expectedHmac, toByteArray(hmacString));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The JDK does not support HMAC-SHA256.");
        }

        return resp;
    }

    /**
     * IntentでdConnectManagerにリクエストを出す.
     * <p>
     * このメソッドを呼び出す前に、アプリ認可を済ませておくこと。
     * </p>
     * <p>
     * 内部で、request_codeとreceiverのデータを付加する。
     * タイムアウトした場合にはnullを返却する。
     * </p>
     * @param intent リクエストのintent
     * @return レスポンスのintent
     */
    protected Intent sendRequest(final Intent intent) {
        return sendRequest(intent, true);
    }

    /**
     * イベントメッセージを待つ.
     * タイムアウトした場合には、nullを返却する。
     * @return 送られてきたイベントを返却する。
     */
    protected Intent waitForEvent() {
        final Object lockObj = new Object();
        final Intent[] event = new Intent[1];
        final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if (TEST_ACTION_EVENT.equals(action)) {
                    synchronized (lockObj) {
                        event[0] = intent;
                        lockObj.notify();
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TEST_ACTION_EVENT);
        getApplicationContext().registerReceiver(eventReceiver, intentFilter);
        synchronized (lockObj) {
            try {
                lockObj.wait(mTimeout);
            } catch (InterruptedException e) {
                return event[0];
            } finally {
                getApplicationContext().unregisterReceiver(eventReceiver);
            }
            return event[0];
        }
    }

    /**
     * resultの値が{@link DConnectMessage#RESULT_OK}であることをチェックする.
     * 
     * @param response レスポンス
     */
    protected static void assertResultOK(final Intent response) {
        assertResult(DConnectMessage.RESULT_OK, response);
    }

    /**
     * resultの値が{@link DConnectMessage#RESULT_ERROR}であることをチェックする.
     * 
     * @param response レスポンス
     */
    protected static void assertResultError(final Intent response) {
        assertResult(DConnectMessage.RESULT_ERROR, response);
    }

    /**
     * resultの値が指定したコードであることをチェックする.
     * 
     * @param expected 期待するresultの値
     * @param response レスポンス
     */
    protected static void assertResult(final int expected, final Intent response) {
        Assert.assertTrue(response.hasExtra(DConnectMessage.EXTRA_RESULT));
        int actual = response.getIntExtra(DConnectMessage.EXTRA_RESULT, -1);
        if (expected != actual) {
            String message =  "expected result=" + expected
                    + " but actual result=" + actual + ". " + toString(response.getExtras());
            fail(message);
        }
    }

    /**
     * 指定したエクストラを文字列として出力する.
     * 
     * @param extras エクストラ
     * @return エクストラの文字列表現
     */
    private static String toString(final Bundle extras) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Iterator<String> it =  extras.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            builder.append(key);
            builder.append(":");
            builder.append(extras.get(key));
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * リクエストコードを生成する.
     * @return リクエストコード
     */
    private synchronized int generateRequestCode() {
        mRequestCodeSeed++;
        return mRequestCodeSeed;
    }

    /**
     * レスポンスおよびイベントを受け取るためのレシーバー.
     */
    private BroadcastReceiver mResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            int requestCode = intent.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, -1);
            if (TEST_ACTION_RESPONSE.equals(action)) {
                mRequests.put(requestCode, intent);
            }
        }
    };
}
