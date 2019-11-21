package org.deviceconnect.android.manager.test;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.entity.FileEntity;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * 負荷テストを行う.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class StressTest extends RESTfulDConnectTestCase {
    /**
     * ServiceDiscoveryを1000回呼び出しても問題ないことを確認する。
     * <pre>
     * Method: GET
     * Path: /serviceDiscovery?accessToken=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・1000回分のレスポンスが取得できること。
     * ・全てのレスポンスのresultに0が返ってくること。
     * ・全てのレスポンスのservicesが返ってくること。
     * </pre>
     */
    @Test
    public void testManyConnections() throws Exception {
        final int countOfConnections = 1000;

        final CountDownLatch latch = new CountDownLatch(countOfConnections);
        final List<DConnectResponseMessage> responses = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(64);
        for (int i = 0; i < countOfConnections; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    DConnectResponseMessage response = mDConnectSDK.serviceDiscovery();
                    synchronized (responses) {
                        responses.add(response);
                    }
                    latch.countDown();
                }
            });
        }

        latch.await(5 * 60, TimeUnit.SECONDS);

        assertThat(responses.size(), is(countOfConnections));
        for (DConnectResponseMessage response : responses) {
            assertThat(response.getResult(), is(0));
            assertThat(response.getList("services"), is(notNullValue()));
            List services = response.getList("services");
            for (Object obj : services) {
                DConnectMessage service = (DConnectMessage) obj;
                String id = service.getString("id");
                String name = service.getString("name");
                assertThat(id, is(notNullValue()));
                assertThat(name, is(notNullValue()));
            }
        }
    }

    /**
     * Canvasプロファイルに1GBのデータを送信する。
     * <pre>
     * Method:
     * Path: /canvas/drawImage
     * Body: マルチパート
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testBigData() throws Exception {
        final int fileSize = 1024 * 1024 * 1024;
        final File writeFile = writeBigFile("bigData", ".dat", fileSize);
        try {
            DConnectSDK.URIBuilder builder = mDConnectSDK.createURIBuilder();
            builder.setProfile("dataTest");

            MultipartEntity body = new MultipartEntity();
            body.add("serviceId", new StringEntity(getServiceId()));
            body.add("accessToken", new StringEntity(getAccessToken()));
            body.add("data", new FileEntity(writeFile));

            DConnectResponseMessage response = mDConnectSDK.post(builder.build(), body);
            assertThat(response, is(notNullValue()));
            assertThat(response.getResult(), is(DConnectMessage.RESULT_OK));
            assertThat(response.getInt("fileSize"), is(fileSize));
        } finally {
            if (!writeFile.delete()) {
                // ignore
            }
        }
    }

    private File writeBigFile(final String prefix, final String suffix, final long size) throws IOException {
        File file = InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir();
        FileOutputStream out = null;
        File dstFile = File.createTempFile(prefix, suffix, file);
        try {
            byte[] buf = new byte[10240];
            Arrays.fill(buf, (byte) 0x01);

            out = new FileOutputStream(dstFile);
            long count = 0;
            int len = 4096;
            while (count < size) {
                out.write(buf, 0, len);
                count += len;
                if (count + len > size) {
                    len = (int) (size - count);
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
