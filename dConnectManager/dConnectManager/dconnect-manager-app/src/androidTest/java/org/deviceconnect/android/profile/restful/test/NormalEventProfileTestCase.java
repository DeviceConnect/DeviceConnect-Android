/*
 NormalEventProfileTestCase.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.message.DConnectEventMessage;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Eventプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalEventProfileTestCase extends RESTfulDConnectTestCase {
    /**
     * テスト用デバイスプラグインにイベントを登録して、イベントが取得するテスト.
     * <pre>
     * 【HTTP通信】
     * Method: PUT
     * Path: /deviceOrientation/onDeviceOrientation
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・WebSocketが接続されること。
     * ・イベント登録にresultが0で返されること。
     * ・イベントにメッセージが送られてくること。
     * </pre>
     */
    @Test
    public void testEvent() throws Exception {
        String uri = "http://localhost:4035/gotapi/unique/event";
        uri += "?serviceId=" + URLEncoder.encode(getServiceId(), "UTF-8");
        uri += "&accessToken=" + URLEncoder.encode(getAccessToken(), "UTF-8");

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch eventLatch = new CountDownLatch(1);
        final AtomicReference<DConnectEventMessage> event = new AtomicReference<>();
        final AtomicReference<Boolean> connect = new AtomicReference<>();

        try {
            mDConnectSDK.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
                @Override
                public void onOpen() {
                    connect.set(true);
                    latch.countDown();
                }

                @Override
                public void onClose() {
                    connect.set(false);
                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    connect.set(false);
                    latch.countDown();

                }
            });
            latch.await(10, TimeUnit.SECONDS);

            assertThat(connect.get(), is(true));

            mDConnectSDK.addEventListener(uri, new DConnectSDK.OnEventListener() {
                @Override
                public void onMessage(DConnectEventMessage message) {
                    event.set(message);
                    eventLatch.countDown();
                }

                @Override
                public void onResponse(DConnectResponseMessage response) {
                    if (response.getResult() != DConnectMessage.RESULT_OK) {
                        eventLatch.countDown();
                    }
                }
            });
            eventLatch.await(10, TimeUnit.SECONDS);

            DConnectEventMessage e = event.get();
            assertThat(e, is(notNullValue()));
            assertThat(e.getString("serviceId"), is(getServiceId()));
            assertThat(e.getString("profile"), is(equalToIgnoringCase("unique")));
            assertThat(e.getString("attribute"), is(equalToIgnoringCase("event")));
        } finally {
            mDConnectSDK.removeEventListener(uri);
            mDConnectSDK.disconnectWebSocket();
        }
    }
}
