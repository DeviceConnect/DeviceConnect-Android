package org.deviceconnect.android.deviceplugin.test.profile.unique;

import android.content.Intent;
import android.net.Uri;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.deviceconnect.message.DConnectMessage.RESULT_OK;

public class TestDataProfile extends DConnectProfile {

    public TestDataProfile() {
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String uri = request.getStringExtra("uri");
                if (uri != null) {
                    response.putExtra("fileSize", getDataSize(uri));
                }
                setResult(response, RESULT_OK);
                return true;
            }
        });

        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String uri = request.getStringExtra("uri");
                if (uri != null) {
                    response.putExtra("fileSize", getDataSize(uri));
                }
                setResult(response, RESULT_OK);
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "dataTest";
    }

    private long getDataSize(final String uri) {
        if (uri.startsWith("content://")) {
            InputStream in = null;
            try {
                in = getContext().getContentResolver().openInputStream(Uri.parse(uri));
                return getDataSize(in);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                inputStream = connection.getInputStream();
                return getDataSize(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return -1;
    }

    private long getDataSize(InputStream in) throws IOException {
        long length = 0;
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) > 0) {
            length += len;
        }
        return length;
    }
}
