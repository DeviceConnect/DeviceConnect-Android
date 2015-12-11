/*
 DConnectService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.message.DConnectMessage;

import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

    /** RESTfulサーバ. */
    private DConnectServer mRESTfulServer;

    /** RESTfulサーバからのイベントを受領するリスナー. */
    private DConnectServerEventListenerImpl mWebServerListener;

    @Override
    public IBinder onBind(final Intent intent) {
        return (IBinder) mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopRESTfulServer();
        super.onDestroy();
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
                if (key != null && mRESTfulServer != null && mRESTfulServer.isRunning()) {
                    if (BuildConfig.DEBUG) {
                        mLogger.info(String.format("sendEvent: %s extra: %s", key, event.getExtras()));
                    }
                    JSONObject root = new JSONObject();
                    DConnectUtil.convertBundleToJSON(root, event.getExtras());
                    mRESTfulServer.sendEvent(key, root.toString());
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
    private void startRESTfulServer() {
        mSettings.load(this);

        mWebServerListener = new DConnectServerEventListenerImpl(this);
        mWebServerListener.setFileManager(mFileMgr);

        DConnectServerConfig.Builder builder = new DConnectServerConfig.Builder();
        builder.port(mSettings.getPort()).isSsl(mSettings.isSSL())
            .documentRootPath(getFilesDir().getAbsolutePath());

        if (!mSettings.allowExternalIP()) {
            ArrayList<String> list = new ArrayList<>();
            list.add("127.0.0.1");
            list.add("::1");
            builder.ipWhiteList(list);
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("RESTful Server was Started.");
            mLogger.info("Host: " + mSettings.getHost());
            mLogger.info("Port: " + mSettings.getPort());
            mLogger.info("SSL: " + mSettings.isSSL());
            mLogger.info("External IP: " + mSettings.allowExternalIP());
        }

        if (mRESTfulServer == null) {
            mRESTfulServer = new DConnectServerNanoHttpd(builder.build(), this);
            mRESTfulServer.setServerEventListener(mWebServerListener);
            mRESTfulServer.start();
        }
    }

    /**
     * HTTPサーバを停止する.
     */
    private void stopRESTfulServer() {
        if (mRESTfulServer != null) {
            mRESTfulServer.shutdown();
            mRESTfulServer = null;
        }
        if (BuildConfig.DEBUG) {
            mLogger.info("RESTful Server was Stopped.");
        }
    }

    /**
     * DConnectManagerを起動する.
     */
    private synchronized void startInternal() {
        if (!mRunningFlag) {
            mRunningFlag = true;
            startDConnect();
            startRESTfulServer();
        }
    }

    /**
     * DConnectManagerを停止する.
     */
    private synchronized void stopInternal() {
        if (mRunningFlag) {
            mRunningFlag = false;
            stopRESTfulServer();
            stopDConnect();
        }
    }

    /**
     * バインドするためのスタブクラス.
     */
    private final IDConnectService mBinder = new IDConnectService.Stub()  {
        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public boolean isRunning() throws RemoteException {
            return mRunningFlag;
        }

        @Override
        public void start() throws RemoteException {
            startInternal();
        }

        @Override
        public void stop() throws RemoteException {
            stopInternal();
        }
    };
}
