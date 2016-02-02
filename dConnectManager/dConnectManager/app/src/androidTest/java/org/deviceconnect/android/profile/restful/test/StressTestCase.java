/*
 StressTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import android.support.test.runner.AndroidJUnit4;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * dConnectManagerの負荷テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class StressTestCase extends RESTfulDConnectTestCase {

    /** リクエストの連続送信回数. */
    private static final int REQUEST_COUNT = 1000;

    /**
     * 負荷テストを実行する.
     * <p>
     * dConnectManager自身が実装するAPIに対してリクエストを行う.
     * </p>
     */
    @Test
    public void testStressTestDConnectManagerProfileSystem()  {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(SystemProfileConstants.PROFILE_NAME);
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        HttpUriRequest request = new HttpGet(builder.toString());
        try {
            JSONObject[] responses = new JSONObject[REQUEST_COUNT];
            for (int i = 0; i < responses.length; i++) {
                // リクエスト送信間隔をできるだけ短くするために、レスポンスのチェックは後まわしにする.
                responses[i] = sendRequest(request);
            }
            for (int i = 0; i < responses.length; i++) {
                assertResultOK(responses[i]);
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 負荷テストを実行する.
     * <p>
     * リクエストに含まれるバイナリデータの一時保存処理に対して負荷をかける.
     * </p>
     * @throws IOException IO Exception
     */
    @Test
    public void testStressTestDConnectManagerProfileFileSend() throws IOException  {
        final String name = "test.png";
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile(FileProfileConstants.PROFILE_NAME);
        builder.setAttribute(FileProfileConstants.ATTRIBUTE_SEND);
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(DConnectMessage.EXTRA_ACCESS_TOKEN, getAccessToken());
        builder.addParameter(FileProfileConstants.PARAM_PATH, "/test/test.png");

        Map<String, Object> body = new HashMap<>();
        body.put(FileProfileConstants.PARAM_DATA, getBytesFromAssets(name));

        try {
            JSONObject[] responses = new JSONObject[REQUEST_COUNT];
            for (int i = 0; i < responses.length; i++) {
                // リクエスト送信間隔をできるだけ短くするために、レスポンスのチェックは後まわしにする.
                responses[i] = sendRequest("POST", builder.toString(), null, body);
            }
            for (int i = 0; i < responses.length; i++) {
                assertResultOK(responses[i]);
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 負荷テストを実行する.
     * <p>
     * テスト用デバイスプラグインの実装するAPIに対してリクエストを行う.
     * </p>
     */
    @Test
    public void testStressTestDevicePluginProfile() {
        URIBuilder builder = TestURIBuilder.createURIBuilder();
        builder.setProfile("unique");
        builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
        builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
        HttpUriRequest request = new HttpGet(builder.toString());
        try {
            JSONObject[] responses = new JSONObject[REQUEST_COUNT];
            for (int i = 0; i < responses.length; i++) {
                // リクエスト送信間隔をできるだけ短くするために、レスポンスのチェックは後まわしにする.
                responses[i] = sendRequest(request);
            }
            for (int i = 0; i < responses.length; i++) {
                assertResultOK(responses[i]);
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 負荷テストを実行する.
     * <p>
     * dConnectManagerに対して複数同時リクエストを行う.
     * </p>
     * @throws InterruptedException スレッドに割り込みが発生した場合
     */
    @Test
    public void testStressTestDConnectManagerAsync() throws InterruptedException  {
        final int num = 100;
        final JSONObject[] responses = new JSONObject[num];
        final Count count = new Count(num);
        // スレッドの準備
        Thread[] threads = new Thread[num];
        for (int i = 0; i < num; i++) {
            final int pos = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    URIBuilder builder = TestURIBuilder.createURIBuilder();
                    builder.setProfile("unique");
                    builder.setAttribute("heavy");
                    builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
                    builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
                    builder.addParameter("key", Integer.toString(pos));
                    final HttpUriRequest request = new HttpGet(builder.toString());
                    responses[pos] = sendRequest(request);
                    count.signal();
                }
            });
        }
        // dConnectManagerへの複数同時アクセスを実行
        for (int i = 0; i < num; i++) {
            threads[i].start();
        }
        count.start();
        try {
            for (int i = 0; i < responses.length; i++) {
                JSONObject response = responses[i];
                assertResultOK(response);
                assertTrue(response.has("key"));
                String key = response.getString("key");
                assertEquals(Integer.toString(i), key);
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }

    /**
     * 負荷テストを実行する.
     * <p>
     * dConnectManagerに対して複数同時リクエストを行う.
     * 特に、イベント管理DBに対する負荷テストを行う.
     * </p>
     * @throws InterruptedException スレッドに割り込みが発生した場合
     */
    @Test
    public void testStressTestDConnectManagerEventRegisterAsync() throws InterruptedException  {
        stressEventAttribute("PUT");
        stressEventAttribute("DELETE");
    }

    /**
     * Stress Event Attribute.
     * @param method HTTP Method
     * @throws InterruptedException Intterrupted Exception
     */
    private void stressEventAttribute(final String method) throws InterruptedException {
        final int num = 50;
        final JSONObject[] responses = new JSONObject[num];
        final Count count = new Count(num);
        // スレッドの準備
        Thread[] threads = new Thread[num];
        for (int i = 0; i < num; i++) {
            final int pos = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    URIBuilder builder = TestURIBuilder.createURIBuilder();
                    builder.setProfile("unique");
                    builder.setAttribute("event");
                    builder.addParameter(DConnectProfileConstants.PARAM_SERVICE_ID, getServiceId());
                    builder.addParameter(DConnectProfileConstants.PARAM_SESSION_KEY, getClientId());
                    builder.addParameter(AuthorizationProfileConstants.PARAM_ACCESS_TOKEN, getAccessToken());
                    builder.addParameter("key", Integer.toString(pos));
                    final HttpUriRequest request;
                    if ("PUT".equals(method)) {
                        request = new HttpPut(builder.toString());
                    } else if ("DELETE".equals(method)) {
                        request = new HttpDelete(builder.toString());
                    } else {
                        request = null;
                    }
                    if (request != null) {
                        responses[pos] = sendRequest(request);
                    }
                    count.signal();
                }
            });
        }
        // dConnectManagerへの複数同時アクセスを実行
        for (int i = 0; i < num; i++) {
            threads[i].start();
        }
        count.start();
        try {
            for (int i = 0; i < responses.length; i++) {
                JSONObject response = responses[i];
                assertResultOK(response);
            }
        } catch (JSONException e) {
            fail("Exception in JSONObject." + e.getMessage());
        }
    }
    
    /**
     * StressTest Count.
     *
     */
    private static class Count {
        /** Count. */
        int mCount;
        /**
         * Constructor.
         * @param cnt Count
         */
        Count(final int cnt) {
            this.mCount = cnt;
        }
        /**
         * Signal.
         */
        synchronized void signal() {
            mCount--;
            notify();
        }
        /**
         * Start.
         * @throws InterruptedException Interrupted Exception
         */
        synchronized void start() throws InterruptedException {
            while (mCount > 0) {
                wait();
            }
        }
    }
}
