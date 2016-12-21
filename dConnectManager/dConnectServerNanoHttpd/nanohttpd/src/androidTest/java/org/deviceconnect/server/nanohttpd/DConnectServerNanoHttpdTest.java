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
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

@RunWith(AndroidJUnit4.class)
public class DConnectServerNanoHttpdTest {

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void DConnectServerNanoHttpd_config_null() {
        try {
            new DConnectServerNanoHttpd(null, getContext());
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // 成功
        }
    }

    @Test
    public void DConnectServerNanoHttpd_context_null() {
        try {
            DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath("").build();
            new DConnectServerNanoHttpd(config, null);
            fail("No IllegalArgumentException occurred.");
        } catch (IllegalArgumentException e) {
            // 成功
        }
    }

    @Test
    public void DConnectServerNanoHttpd() {
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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get("http://localhost:4035" + path + "?" + key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void DConnectServerNanoHttpd_invalid_document_path() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<DConnectServerError> result = new AtomicReference<>();
        DConnectServerConfig config = new DConnectServerConfig.Builder().port(4035).documentRootPath("abc").build();
        DConnectServer server = new DConnectServerNanoHttpd(config, getContext());
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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.connect("PATCH", "http://localhost:4035" + path, null, null);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(not(200)));
            assertThat(response.getBody(), is(nullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void DConnectServerNanoHttpd_many_connections() {
        final int count = 8;
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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

        try {
            launchLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Failed to launch the server.");
        }

        ExecutorService es = Executors.newFixedThreadPool(8);
        for (int i = 0; i < count; i++) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    HttpUtils.Response response = HttpUtils.get("http://localhost:4035" + path + "?" + key + "=" + value);
                }
            });
        }

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get("http://localhost:4035" + path + "?" + key + "=" + value);
            assertThat(response, is(nullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            server.shutdown();
        }
    }

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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.get("http://localhost:4035" + path + "?" + key + "=" + value);
            assertThat(response.getStatusCode(), is(not(200)));
            assertThat(response.getBody(), is(nullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

        try {
            latch.await(10, TimeUnit.SECONDS);

            HttpUtils.Response response = HttpUtils.post("http://localhost:4035" + path, key + "=" + value);
            assertThat(response, is(notNullValue()));
            assertThat(response.getStatusCode(), is(200));
            assertThat(response.getBody(), is(notNullValue()));
        } catch (InterruptedException e) {
            fail("timeout");
        } finally {
            server.shutdown();
        }
    }

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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
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
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
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

    @Test
    public void DConnectServerNanoHttpd_websocket() {
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
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
                webSocket.sendMessage(message);
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

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

    @Test
    public void DConnectServerNanoHttpd_ssl_websocket() {
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
            }

            @Override
            public void onWebSocketConnected(final DConnectWebSocket webSocket) {
            }

            @Override
            public void onWebSocketDisconnected(final String webSocketId) {
            }

            @Override
            public void onWebSocketMessage(final DConnectWebSocket webSocket, final String message) {
                webSocket.sendMessage(message);
            }

            @Override
            public void onResetEventSessionKey(final String sessionKey) {
            }
        });
        server.start();

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
}
