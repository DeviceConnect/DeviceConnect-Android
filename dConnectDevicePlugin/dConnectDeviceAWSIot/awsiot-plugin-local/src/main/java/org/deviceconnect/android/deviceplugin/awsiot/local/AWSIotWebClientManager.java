package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.p2p.WebClient;

import java.util.ArrayList;
import java.util.List;

public class AWSIotWebClientManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "ABC";

    private List<WebClient> mWebClientList = new ArrayList<>();

    private AWSIotLocalManager mIot;
    private Context mContext;

    public AWSIotWebClientManager(final Context context, final AWSIotLocalManager ctl) {
        mContext = context;
        mIot = ctl;
    }

    public void destroy() {
        for (WebClient client : mWebClientList) {
            client.close();
        }
        mWebClientList.clear();
    }

    public void onReceivedSignaling(final RemoteDeviceConnectManager remote, final String message) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotWebClientManager#onReceivedSignaling:" + remote);
            Log.i(TAG, "message=" + message);
        }

        WebClient webClient = new WebClient(mContext) {
            @Override
            public void onNotifySignaling(final String signaling) {
                mIot.publish(remote, mIot.createP2P(signaling));
            }
            @Override
            public void onDisconnected(final WebClient webClient) {
                mWebClientList.remove(webClient);
            }
        };
        webClient.onReceivedSignaling(message);
        mWebClientList.add(webClient);
    }
}
