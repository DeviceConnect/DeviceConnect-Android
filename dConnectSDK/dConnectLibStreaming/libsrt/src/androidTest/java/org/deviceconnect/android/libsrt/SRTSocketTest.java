package org.deviceconnect.android.libsrt;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SRTSocketTest {

    private static final String SRT_HOST = "localhost";
    private static final int SRT_PORT = 20000;

    @Test(expected = IllegalArgumentException.class)
    public void test1() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(SRT.SRTO_SENDER, null);
        }
    }

    @Test
    public void test2() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(SRT.SRTO_SENDER, true);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void test3() throws Exception {
        SRTSocket socket = new SRTSocket();
        socket.close();
        socket.setOption(SRT.SRTO_SENDER, 1);
    }

    @Test(expected = SRTSocketException.class)
    public void test4() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(SRT.SRTO_SENDER, 1);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void test5() throws Exception {
        SRTSocket socket = new SRTSocket();
        socket.close();
        socket.send("test".getBytes());
    }

    @Test
    public void test6() throws Exception {
        new Thread(() -> {
            try (SRTServerSocket serverSocket = new SRTServerSocket(SRT_PORT)) {
                serverSocket.open();

                try (SRTSocket socket = serverSocket.accept()) {
                    byte[] buf = new byte[1500];
                    int len = socket.recv(buf);
                    if (len > 0) {
                        socket.send(buf, 0, len);
                    }
                    Thread.sleep(200);
                } catch (Exception e) {
                    // ignore.
                }
            } catch (SRTSocketException e) {
                // ignore.
            }
        }).start();

        Thread.sleep(200);

        final String testText = "test_text";

        try (SRTSocket socket = new SRTSocket(SRT_HOST, SRT_PORT)) {
            assertThat(socket.isConnected(), is(true));
            socket.send(testText.getBytes());

            byte[] buf = new byte[1500];
            int len = socket.recv(buf);
            assertThat(len, is(testText.length()));
            assertThat(new String(buf, 0, len), is(testText));
        }
    }
}
