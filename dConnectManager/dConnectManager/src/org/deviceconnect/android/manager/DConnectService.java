/*
 DConnectService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import java.io.IOException;
import java.util.ArrayList;

import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

/**
 * dConnect Manager本体.
 * @author NTT DOCOMO, INC.
 */
public class DConnectService extends DConnectMessageService {
    /** 内部用: 通信タイプを定義する. */
    public static final String EXTRA_INNER_TYPE = "_type";
    /** 通信タイプがHTTPであることを示す定数. */
    public static final String INNER_TYPE_HTTP = "http";

    /** 内部用: アプリケーションタイプを定義する. */
    public static final String EXTRA_INNER_APP_TYPE = "_app_type";
    /** 通信相手がWebアプリケーションであることを示す定数. */
    public static final String INNER_APP_TYPE_WEB = "web";

    /** Webサーバ. */
    private DConnectServer mWebServer;

    /** Webサーバからのイベントを受領するリスナー. */
    private DConnectServerEventListenerImpl mWebServerListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mLogger.entering(this.getClass().getName(), "onCreate");

        // HTTPサーバ起動
        startHttpServer();

        mLogger.exiting(this.getClass().getName(), "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // HTTPサーバ停止
        stopHttpServer();
    }

    @Override
    public void sendResponse(final Intent request, final Intent response) {
        Intent intent = createResponseIntent(request, response);
        if (INNER_TYPE_HTTP.equals(request.getStringExtra(EXTRA_INNER_TYPE))) {
            mWebServerListener.onResponse(intent);
        } else {
            sendBroadcast(intent);
        }
    }

    @Override
    public void sendEvent(final String receiver, final Intent event) {
        if (receiver == null || receiver.length() <= 0) {
            String key = event.getStringExtra(DConnectMessage.EXTRA_SESSION_KEY);
            try {
                if (key != null && mWebServer != null && mWebServer.isRunning()) {
                    mLogger.fine("■ sendEvent: " + key + " extra: " + event.getExtras());
                    JSONObject root = new JSONObject();
                    DConnectUtil.convertBundleToJSON(root, event.getExtras());
                    mWebServer.sendEvent(key, root.toString());
                }
            } catch (JSONException e) {
                mLogger.warning("JSONException in sendEvent: " + e.toString());
            } catch (IOException e) {
                mLogger.warning("IOException in sendEvent: " + e.toString());
            }
        } else {
            super.sendEvent(receiver, event);
        }
    }

    /**
     * HTTPサーバを開始する.
     */
    private void startHttpServer() {
        mWebServerListener = new DConnectServerEventListenerImpl(this);
        mWebServerListener.setFileManager(mFileMgr);

        DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
        builder.port(mSettings.getPort()).isSsl(mSettings.isSSL())
            .documentRootPath(getFilesDir().getAbsolutePath());

        if (!mSettings.allowExternalIP()) {
            ArrayList<String> list = new ArrayList<String>();
            list.add("127.0.0.1");
            builder.ipWhiteList(list);
        }

        mLogger.fine("Host: " + mSettings.getHost());
        mLogger.fine("Port: " + mSettings.getPort());
        mLogger.fine("SSL: " + mSettings.isSSL());
        mLogger.fine("External IP: " + mSettings.allowExternalIP());

        if (mWebServer == null) {
            mWebServer = new DConnectServerNanoHttpd(builder.build(), this);
            mWebServer.setServerEventListener(mWebServerListener);
            mWebServer.start();
        }
    }

    /**
     * HTTPサーバを停止する.
     */
    private void stopHttpServer() {
        if (mWebServer != null) {
            mWebServer.shutdown();
            mWebServer = null;
        }
    }
}
