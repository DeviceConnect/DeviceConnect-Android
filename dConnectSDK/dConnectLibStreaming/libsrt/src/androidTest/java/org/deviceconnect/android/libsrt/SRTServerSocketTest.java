package org.deviceconnect.android.libsrt;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SRTServerSocketTest {

    private static final String SRT_HOST = "localhost";
    private static final int SRT_PORT = 20000;

    @Test
    public void testServerSocket() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket(SRT_PORT)) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean result = new AtomicBoolean(false);

            new Thread(() -> {
                try (SRTSocket socket = serverSocket.accept()) {
                    result.set(socket.isConnected());
                } catch (SRTSocketException e) {
                    // ignore.
                }
                latch.countDown();
            }).start();

            try (SRTSocket socket = new SRTSocket(SRT_HOST, SRT_PORT)) {
                assertThat(socket.isConnected(), is(true));
            }

            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("timeout");
            }

            assertThat(result.get(), is(true));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServerSocket_port_is_invalid() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket(-1)) {
            // ignore.d
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServerSocket_address_is_invalid() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket("test", SRT_PORT)) {
            // ignore.
        }
    }

    @Test
    public void testBind() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket()) {
            serverSocket.bind("0.0.0.0", SRT_PORT);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBind_port_is_invalid() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket()) {
            serverSocket.bind("0.0.0.0", -1);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBind_address_is_invalid() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket()) {
            serverSocket.bind("test", SRT_PORT);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testBind_already_closed() throws Exception {
        SRTServerSocket serverSocket = new SRTServerSocket();
        serverSocket.close();
        serverSocket.bind("0.0.0.0", SRT_PORT);
    }

    @Test(expected = SRTSocketException.class)
    public void testBind_already_opened() throws Exception {
        try (SRTServerSocket serverSocket = new SRTServerSocket(SRT_PORT)) {
            serverSocket.bind("0.0.0.0", SRT_PORT);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testAccept_already_closed() throws Exception {
        SRTServerSocket serverSocket = new SRTServerSocket();
        serverSocket.close();
        try (SRTSocket socket = serverSocket.accept()) {
            // ignore.
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testAccept_not_opened() throws Exception {
        SRTServerSocket serverSocket = new SRTServerSocket();
        try (SRTSocket socket = serverSocket.accept()) {
            // ignore.
        }
    }
}
