package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.p2p.WebClient;

import java.util.ArrayList;
import java.util.List;

public class AWSIotWebClientManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS-Local";

    private List<WebClient> mWebClientList = new ArrayList<>();

    private AWSIotLocalManager mLocalManager;
    private Context mContext;

    public AWSIotWebClientManager(final Context context, final AWSIotLocalManager manager) {
        mContext = context;
        mLocalManager = manager;
    }

    public void destroy() {
        for (WebClient client : mWebClientList) {
            client.close();
        }
        mWebClientList.clear();
    }

    public void onReceivedSignaling(final String message) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotWebClientManager#onReceivedSignaling:");
            Log.i(TAG, "message=" + message);
        }

        WebClient webClient = new WebClient(mContext) {
            @Override
            public void onNotifySignaling(final String signaling) {
                mLocalManager.publish(mLocalManager.createP2P(signaling));
            }
            @Override
            public void onDisconnected(final WebClient webClient) {
                if (DEBUG) {
                    Log.i(TAG, "AWSIotWebClientManager#onDisconnected: " + webClient);
                }
                mWebClientList.remove(webClient);
            }
        };
        webClient.onReceivedSignaling(message);
        mWebClientList.add(webClient);
    }
}
