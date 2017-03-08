package org.deviceconnect.android.deviceplugin.webrtc;

import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaClient;
import org.deviceconnect.android.deviceplugin.webrtc.util.MixedReplaceMediaServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MixedReplaceMediaClientTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void MixedReplaceMediaClient() {
        final String text = "This is a test 1.";
        final CountDownLatch latch = new CountDownLatch(1);
        final MixedReplaceMediaServer server = new MixedReplaceMediaServer();
        server.start();

        final AtomicReference<String> result = new AtomicReference<>();
        String uri = server.getUrl("remote");
        MixedReplaceMediaClient client = new MixedReplaceMediaClient(uri);
        client.setOnMixedReplaceMediaListener(new MixedReplaceMediaClient.OnMixedReplaceMediaListener() {
            @Override
            public void onConnected() {
            }

            @Override
            public void onReceivedData(final InputStream in) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int len;
                    byte[] buf = new byte[1024];

                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }

                    result.set(new String(out.toByteArray()));

                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(final MixedReplaceMediaClient.MixedReplaceMediaError error) {
            }
        });
        client.start();

        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                server.offerMedia("remote", text.getBytes());
                server.offerMedia("remote", text.getBytes());
            }
        }, 1, TimeUnit.SECONDS);

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(result.get(), is(text));

        server.stop();
        client.stop();
    }
}
