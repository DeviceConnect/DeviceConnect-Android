/*
 DConnectServerNanoHttpdTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.DConnectServerError;
import org.deviceconnect.server.DConnectServerEventListener;
import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.deviceconnect.server.websocket.DConnectWebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * DConnectServerNanoHttpdサーバのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class DConnectServerNanoHttpdTest {

    public static final String HTTP_LOCALHOST_4035 = "http://localhost:4035";

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    /**
     * DConnectServerConfigにnullを設定して、DConnectServerNanoHttpdを作成する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが派生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void DConnectServerNanoHttpd_config_null() {
        new DConnectServerNanoHttpd(null, getContext());
    }

    /**
     * Contextにnullを設定して、DConnectServerNanoHttpdを作成する。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentExceptionが派生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void DConnectServerNanoHttpd_context_null() {
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath("").build();
        new DConnectServerNanoHttpd(config, null);
    }

    /**
     * DConnectServerNanoHttpdを作成する。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdのインスタンスが作成できること。
     * ・DConnectServerNanoHttpdのサーバ起動し、DConnectServerEventListener#onServerLaunchedに通知が来ること。
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        assertThat(server, is(notNullValue()));

        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.GET.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get(HTTP_LOCALHOST_4035 + path + "?" + key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }


    /**
     * DConnectServerNanoHttpdを作成する。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdのインスタンスが作成できること。
     * ・DConnectServerNanoHttpdのサーバ起動し、DConnectServerEventListener#onServerLaunchedに通知が来ること。
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_duplicate_key_value() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value1 = "value1";
        final String value2 = "value2";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        assertThat(server, is(notNullValue()));

        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.GET.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value2.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get(HTTP_LOCALHOST_4035 + path + "?" + key + "=" + value1 + "&" + key + "=" + value2);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * 不正なdocumentPathを設定して、DConnectServerNanoHttpdを作成する。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdのインスタンスが作成できること。
     * ・DConnectServerNanoHttpdのサーバ起動に失敗し、DConnectServerEventListener#onErrorに通知がくること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_invalid_document_path() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<DConnectServerError> result = new AtomicReference<>();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath("abc").build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        assertThat(server, is(notNullValue()));

        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);
                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
                result.set(errorCode);
                latch.countDown();
            }

            @Override
            public void onServerLaunched() {
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            DConnectServerError errorCode = result.get();
            assertThat(errorCode, is(DConnectServerError.LAUNCH_FAILED));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * DConnectServerNanoHttpdにサポートされていないHTTPメソッド(PATCH)を指定して通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに501が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_not_support_method() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);
                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.connect("PATCH", HTTP_LOCALHOST_4035 + path, null, null);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(501));
            assertThat(response.getBody(), is(nullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * DConnectServerNanoHttpdに複数のスレッドから通信を行う。
     * <pre>
     * 【期待する動作】
     * ・各スレッドからDConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_many_connections() {
        final int count = 1000;
        final CountDownLatch latch = new CountDownLatch(count);
        final CountDownLatch launchLatch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";

        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).maxConnectionSize(8)
                .documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                launchLatch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            launchLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Failed to launch the server.");
        }

        final AtomicReferenceArray<HttpUtils.Response> array = new AtomicReferenceArray<>(count);

        ExecutorService es = Executors.newFixedThreadPool(64);
        for (int i = 0; i < count; i++) {
            final int index = i;
            es.submit(new Runnable() {
                @Override
                public void run() {
                    HttpUtils.Response response = HttpUtils.get(HTTP_LOCALHOST_4035 + path + "?" + key + "=" + value);
                    array.set(index, response);
                }
            });
        }

        try {
            latch.await(60, TimeUnit.SECONDS);
            assertThat(array.length(), is(count));
            for (int i = 0; i < count; i++) {
                assertThat(array.get(i), is(notNullValue()));
                assertThat(array.get(i).getStatusCode(), is(200));
            }
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * ホワイトリストに自分自身のIPを指定せずにDConnectServerNanoHttpdを起動し、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・ホワイトリストに弾かれ、DConnectServerNanoHttpdへの通信が失敗すること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_white_list() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";

        List<String> whiteList = new ArrayList<>();
        whiteList.add("192.168.0.1");

        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).ipWhiteList(whiteList)
                .documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);
                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get(HTTP_LOCALHOST_4035 + path + "?" + key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(not(200)));
        } catch (InterruptedException e) {
            fail("timeout");
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            server.shutdown();
        }
    }

    /**
     * 8K byteを超えるHTTPヘッダーを指定して、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200以外が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_big_headers() {
        StringBuilder v = new StringBuilder();
        for (int i = 0; i < 1024 * 8; i++) {
            v.append('a');
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = v.toString();
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.GET.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get(HTTP_LOCALHOST_4035 + path + "?" + key + "=" + value);
            assertThat(response.getStatusCode(), is(not(200)));
            assertThat(response.getBody(), is(nullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * HTTPボディにデータを指定して、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_body() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.POST.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.post(HTTP_LOCALHOST_4035 + path, key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * HTTPボディに1GBのデータを指定して、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_big_body() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";
        final String fileNameKey = "fileName";
        final File writeFile = writeBigFile("bigData.dat", 1024 * 1024 * 512);

        final Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        data.put(fileNameKey, writeFile);

        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.POST.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String path = req.getFiles().get(fileNameKey);
                File file = new File(path);
                if (writeFile.length() != file.length()) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.post(HTTP_LOCALHOST_4035 + path, data);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * HTTPボディにデータを指定して、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTP通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_big_response() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final int fileSize = 1024 * 1024 * 1024;
        final File writeFile = writeBigFile("bigData.dat", fileSize);

        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.GET.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                res.setContentLength((int) writeFile.length());
                try {
                    res.setBody(new FileInputStream(writeFile));
                } catch (FileNotFoundException e) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get(HTTP_LOCALHOST_4035 + path);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
            assertThat((int) response.getBody().length(), is(fileSize));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * SSLを有効にして、DConnectServerNanoHttpdを起動し、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTPs通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_ssl() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).isSsl(true).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.GET.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get("https://localhost:4035" + path + "?" + key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * SSLを有効にして、DConnectServerNanoHttpdを起動し、HTTPボディにデータを設定し、HTTP通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにHTTPs通信して、レスポンスのステータスコードに200が返却されること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_ssl_body() {
        final CountDownLatch latch = new CountDownLatch(1);
        final String path = "/root/path";
        final String key = "key";
        final String value = "value";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().isSsl(true).port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);

                HttpRequest.Method method = req.getMethod();
                if (!HttpRequest.Method.POST.equals(method)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String uri = req.getUri();
                if (!path.equals(uri)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                String v = req.getQueryParameters().get(key);
                if (!value.equals(v)) {
                    res.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                }

                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                latch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.post("https://localhost:4035" + path, key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * DConnectServerNanoHttpdを起動し、WebSocket通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにWebSocketの接続ができること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_websocket() {
        final CountDownLatch serverLaunchLatch = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> result = new AtomicReference<>();
        final String msg = "test-message";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);
                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                serverLaunchLatch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
                webSocket.sendMessage(message);
            }
        });
        server.start();

        try {
            serverLaunchLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        String uri = "http://localhost:4035";
        WebSocketClient client = new WebSocketClient(URI.create(uri), new Draft_17(), null, 10000) {
            @Override
            public void onOpen(final ServerHandshake handshakedata) {
                send(msg);
            }

            @Override
            public void onMessage(final String message) {
                result.set(message);
                latch.countDown();
            }

            @Override
            public void onClose(final int code, final String reason, final boolean remote) {
            }

            @Override
            public void onError(final Exception ex) {
            }
        };
        client.connect();

        try {
            boolean r = latch.await(10, TimeUnit.SECONDS);
            assertThat(r, is(true));
            assertThat(result.get(), is(msg));
            client.close();
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    /**
     * SSLを有効にして、DConnectServerNanoHttpdを起動し、WebSocket通信を行う。
     * <pre>
     * 【期待する動作】
     * ・DConnectServerNanoHttpdにSSLに対応したWebSocketの接続ができること。
     * </pre>
     */
    @Test
    public void DConnectServerNanoHttpd_ssl_websocket() {
        final CountDownLatch serverLaunchLatch = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> result = new AtomicReference<>();
        final String msg = "test-message";
        File file = getContext().getFilesDir();
        DConnectServerConfig config = new DConnectServerConfig.Builder().isSsl(true).port(4035).documentRootPath(file.getPath()).build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
        server.setServerEventListener(new DConnectServerEventListener() {
            @Override
            public boolean onReceivedHttpRequest(final HttpRequest req, final HttpResponse res) {
                res.setCode(HttpResponse.StatusCode.OK);
                return true;
            }

            @Override
            public void onError(final DConnectServerError errorCode) {
            }

            @Override
            public void onServerLaunched() {
                serverLaunchLatch.countDown();
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
                webSocket.sendMessage(message);
            }
        });
        server.start();

        try {
            serverLaunchLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        try {
            String uri = "https://localhost:4035";
            WebSocketClient client = new WebSocketClient(URI.create(uri), new Draft_17(), null, 10000) {
                @Override
                public void onOpen(final ServerHandshake handshakedata) {
                    send(msg);
                }

                @Override
                public void onMessage(final String message) {
                    result.set(message);
                    latch.countDown();
                }

                @Override
                public void onClose(final int code, final String reason, final boolean remote) {
                }

                @Override
                public void onError(final Exception ex) {
                }
            };

            SSLSocketFactory factory = createSSLSocketFactory();
            client.setSocket(factory.createSocket());
            client.connect();

            try {
                boolean r = latch.await(10, TimeUnit.SECONDS);
                assertThat(r, is(true));
                assertThat(result.get(), is(msg));
                client.close();
            } catch (InterruptedException e) {
                fail("timeout");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            server.shutdown();
        }
    }

    private SSLSocketFactory createSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        KeyManager[] keyManagers = null;
        TrustManager[] transManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(keyManagers, transManagers, new SecureRandom());
        return sslcontext.getSocketFactory();
    }

    private File writeBigFile(final String fileName, final int size) {
        File file = getContext().getFilesDir();
        FileOutputStream out = null;
        File dstFile = new File(file, fileName);
        try {
            byte[] buf = new byte[10240];
            Arrays.fill(buf, (byte) 0x01);

            out = new FileOutputStream(dstFile);
            int count = 0;
            int len = 4096;
            while (count < size) {
                out.write(buf, 0, len);
                count += len;
                if (count + len > size) {
                    len = size - count;
                    if (len <= 0) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dstFile;
    }
}
