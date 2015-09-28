/*
 DConnectWebService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;

import java.util.logging.Logger;

/**
 * Webサーバ用のサービス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectWebService extends Service {
    /** ロガー. */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** Webサーバ. */
    private DConnectServer mWebServer;

    /** DConnectの設定. */
    private DConnectSettings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = DConnectSettings.getInstance();
        startWebServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWebServer();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /**
     * Webサーバを起動する.
     */
    private void startWebServer() {
        DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
        builder.port(mSettings.getWebPort())
                .documentRootPath(mSettings.getDocumentRootPath());

        mLogger.fine("Web Server was Started.");
        mLogger.fine("Host: " + mSettings.getHost());
        mLogger.fine("Port: " + mSettings.getWebPort());
        mLogger.fine("Document Root: " + mSettings.getDocumentRootPath());

        if (mWebServer == null) {
            mWebServer = new DConnectServerNanoHttpd(builder.build(), this);
            mWebServer.start();
        }
    }

    /**
     * Webサーバを停止する.
     */
    private void stopWebServer() {
        if (mWebServer != null) {
            mWebServer.shutdown();
            mWebServer = null;
        }
        mLogger.fine("Web Server was Stopped.");
    }
}
