package org.deviceconnect.android.libsrt;

import org.deviceconnect.android.libsrt.utils.EchoSRTServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SRTSocketTest {

    private static final String SRT_HOST = "localhost";
    private static final int SRT_PORT = 20000;

    @Test
    public void testConnect() throws Exception {
        EchoSRTServer echoSRTServer = new EchoSRTServer(SRT_PORT);
        try {
            echoSRTServer.start();

            try (SRTSocket socket = new SRTSocket()) {
                socket.connect(SRT_HOST, SRT_PORT);
                assertThat(socket.isConnected(), is(true));
            }
        } finally {
            echoSRTServer.stop();
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testConnect_twice() throws Exception {
        EchoSRTServer echoSRTServer = new EchoSRTServer(SRT_PORT);
        try {
            echoSRTServer.start();

            try (SRTSocket socket = new SRTSocket()) {
                socket.connect(SRT_HOST, SRT_PORT);
                assertThat(socket.isConnected(), is(true));
                socket.connect(SRT_HOST, SRT_PORT);
            }
        } finally {
            echoSRTServer.stop();
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testConnect_not_start_server() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.connect(SRT_HOST, SRT_PORT);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnect_address_is_null() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.connect(null, SRT_PORT);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnect_address_is_invalid() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.connect("test", SRT_PORT);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnect_port_is_invalid() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.connect(SRT_HOST, -1);
        }
    }

    @Test
    public void testClose() throws Exception {
        SRTSocket socket = new SRTSocket();
        socket.close();
    }

    @Test
    public void testClose_twice() throws Exception {
        SRTSocket socket = new SRTSocket();
        socket.close();
        socket.close();
    }

    @Test
    public void testSetOption() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(SRT.SRTO_SENDER, true);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOption_value_is_null() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(SRT.SRTO_SENDER, null);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testSetOption_value_is_invalid() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(SRT.SRTO_SENDER, 1);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testSetOption_option_is_invalid() throws Exception {
        try (SRTSocket socket = new SRTSocket()) {
            socket.setOption(-1, 1);
        }
    }

    @Test(expected = SRTSocketException.class)
    public void testSetOption_already_closed() throws Exception {
        SRTSocket socket = new SRTSocket();
        socket.close();
        socket.setOption(SRT.SRTO_SENDER, 1);
    }

    @Test(expected = SRTSocketException.class)
    public void testSend_already_closed() throws Exception {
        SRTSocket socket = new SRTSocket();
        socket.close();
        socket.send("test".getBytes());
    }

    @Test
    public void testSend() throws Exception {
        EchoSRTServer echoSRTServer = new EchoSRTServer(SRT_PORT);
        try {
            echoSRTServer.start();

            final String testText = "test_text";

            try (SRTSocket socket = new SRTSocket(SRT_HOST, SRT_PORT)) {
                assertThat(socket.isConnected(), is(true));
                socket.send(testText.getBytes());

                byte[] buf = new byte[1500];
                int len = socket.recv(buf);
                assertThat(len, is(testText.length()));
                assertThat(new String(buf, 0, len), is(testText));
            }
        } finally {
            echoSRTServer.stop();
        }
    }
}
