package org.deviceconnect.android.manager.test;


import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link DConnectService}クラスの自動単体テスト.
 */
@RunWith(AndroidJUnit4.class)
public class DConnectServiceTest {

    private static final URI WEBSOCKET_URL = URI.create("ws://localhost:4035/websocket");

    private WebSocketClient mWebSocketClient;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    /**
     * イベント送信処理中にRESTサーバが停止されても実行時例外が発生しないこと.
     *
     * 下記の手順を10回繰り返す。
     * ・イベント送信処理中にRESTサーバを起動し、100ミリ秒後に停止。
     *
     * @throws Exception テストの実行に失敗した場合.
     */
    @Test
    public void testSendEventToWebSocketAtServerStopPhase() throws Exception {
        Intent serviceIntent = new Intent(getContext(), DConnectService.class);
        IBinder binder = mServiceRule.bindService(serviceIntent);
        final DConnectService service = ((DConnectService.LocalBinder) binder).getDConnectService();
        final String sessionKey = "dummy";

        final Thread eventThread = Thread.currentThread();
        Thread switchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        startManager(service, sessionKey);
                        Thread.sleep(100);
                        stopManager(service);
                        Thread.sleep(100);
                    }
                    eventThread.interrupt();
                } catch (InterruptedException e) {
                    // NOP.
                }
            }
        });
        switchThread.start();

        // ダミーイベントを送信
        Intent event = new Intent();
        event.setAction(IntentDConnectMessage.ACTION_EVENT);
        event.putExtra(IntentDConnectMessage.EXTRA_SESSION_KEY, sessionKey);
        while (!eventThread.isInterrupted()) {
            sendEventToWebSocket(service, event);
        }
    }

    private void startManager(final DConnectService service, final String sessionKey)
            throws InterruptedException {
        service.startInternal();
        while (!service.isRunning()) {
            Thread.sleep(50);
        }

        final Map<String, String> headers = new HashMap<>();
        headers.put(IntentDConnectMessage.EXTRA_ORIGIN, getContext().getPackageName());
        final int timeout = 30 * 1000;
        final Object lock = new Object();
        mWebSocketClient = new WebSocketClient(WEBSOCKET_URL, new Draft_17(), headers, timeout) {
            @Override
            public void onOpen(final ServerHandshake serverHandshake) {
                mWebSocketClient.send("{\"sessionKey\":\"" + sessionKey + "\"}");

                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onMessage(final String message) {
            }

            @Override
            public void onClose(final int i, final String s, final boolean b) {
            }

            @Override
            public void onError(final Exception e) {
            }
        };
        mWebSocketClient.connect();
        synchronized (lock) {
            lock.wait(1000);
        }
    }

    private void stopManager(final DConnectService service) throws InterruptedException {
        service.stopInternal();
        while (service.isRunning()) {
            Thread.sleep(50);
        }
    }

    private void sendEventToWebSocket(final DConnectService service, final Intent event)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = DConnectService.class.getDeclaredMethod("sendEventToWebSocket", Intent.class);
        method.setAccessible(true);
        method.invoke(service, event);
    }

}
