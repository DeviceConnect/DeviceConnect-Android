/*
DataLayerListenerService.java
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * このアプリで共有するGoogleApiClientを保持するアプリケーションクラス.
 */
public class WearApplication extends Application {

    /** Google API Client. */
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        destroy();
    }

    /**
     * GoogleApiClientを初期化する.
     */
    public synchronized void init() {
        // Define google play service
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        // Connect google play service
        mGoogleApiClient.connect();
    }

    /**
     * GoogleApiClientの後始末を行う.
     */
    public synchronized void destroy() {
        if (mGoogleApiClient != null) {
            // Disconnect google play service.
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    /**
     * GoogleApiClientを取得する.
     * @return GoogleApiClient
     */
    public synchronized GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
            init();
        }
        return mGoogleApiClient;
    }
}
