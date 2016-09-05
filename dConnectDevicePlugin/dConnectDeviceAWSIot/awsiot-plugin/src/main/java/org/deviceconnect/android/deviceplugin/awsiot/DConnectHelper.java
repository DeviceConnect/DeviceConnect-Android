package org.deviceconnect.android.deviceplugin.awsiot;

import android.content.Intent;
import android.os.AsyncTask;

import org.deviceconnect.android.deviceplugin.awsiot.util.HttpUtil;

public class DConnectHelper {

    public static void sendRequest(final Intent request, final Callback callback) {
        (new HttpTask(request) {
            @Override
            protected void onPostExecute(final byte[] bytes) {
                if (callback != null) {
                    callback.onCallback(bytes);
                }
            }
        }).execute();
    }

    private static class HttpTask extends AsyncTask<Void, Void, byte[]> {
        private Intent mRequest;

        public HttpTask(final Intent request) {
            mRequest = request;
        }

        @Override
        protected byte[] doInBackground(final Void... params) {
            return HttpUtil.get("http://localhost:9001/gotapi/servicediscovery");
        }
    }

    public interface Callback {
        void onCallback(byte[] data);
    }
}
