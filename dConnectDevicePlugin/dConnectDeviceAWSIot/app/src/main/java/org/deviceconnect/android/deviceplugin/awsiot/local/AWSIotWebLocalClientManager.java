package org.deviceconnect.android.deviceplugin.awsiot.local;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.cores.p2p.WebClient;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AWSIotWebLocalClientManager {

    private static final boolean DEBUG = true;
    private static final String TAG = "AWS-Local";

    private final List<WebClient> mWebClientList = Collections.synchronizedList(new ArrayList<WebClient>());

    private AWSIotLocalManager mLocalManager;
    private Context mContext;

    public AWSIotWebLocalClientManager(final Context context, final AWSIotLocalManager manager) {
        mContext = context;
        mLocalManager = manager;
    }

    public void destroy() {
        synchronized (mWebClientList) {
            for (WebClient client : mWebClientList) {
                client.close();
            }
        }
        mWebClientList.clear();
    }

    public void onReceivedSignaling(final String message) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotWebLocalClientManager#onReceivedSignaling:");
            Log.i(TAG, "message=" + message);
        }

        WebClient webClient = new WebClient(mContext) {
            @Override
            public void onNotifySignaling(final String signaling) {
                mLocalManager.publish(AWSIotUtil.createRemoteP2P(signaling));
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
