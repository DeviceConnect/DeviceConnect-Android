/*
 AWSIotWebClientManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.remote;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.awsiot.cores.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.cores.p2p.WebClient;
import org.deviceconnect.android.deviceplugin.awsiot.cores.util.AWSIotUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AWSIotWebClientManager {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AWS-Remote";

    private final List<WebClient> mWebClientList = Collections.synchronizedList(new ArrayList<WebClient>());

    private AWSIotRemoteManager mManager;
    private Context mContext;

    public AWSIotWebClientManager(final Context context, final AWSIotRemoteManager manager) {
        mContext = context;
        mManager = manager;
    }

    public void destroy() {
        synchronized (mWebClientList) {
            for (WebClient client : mWebClientList) {
                client.close();
            }
        }
        mWebClientList.clear();
    }

    public void onReceivedSignaling(final RemoteDeviceConnectManager remote, final String message) {
        if (DEBUG) {
            Log.i(TAG, "AWSIotWebLocalClientManager#onReceivedSignaling: " + remote);
            Log.i(TAG, "message=" + message);
        }

        WebClient webClient = new WebClient(mContext) {
            @Override
            public void onNotifySignaling(final String signaling) {
                mManager.publish(remote, AWSIotUtil.createLocalP2P(signaling));
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
