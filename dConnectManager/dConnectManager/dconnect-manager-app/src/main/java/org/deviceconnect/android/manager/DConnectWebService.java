/*
 DConnectWebService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;

import org.deviceconnect.android.manager.core.DConnectConst;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.util.NotificationUtil;
import org.deviceconnect.android.ssl.EndPointKeyStoreManager;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;
import org.deviceconnect.android.ssl.KeyStoreManager;
import org.deviceconnect.server.nanohttpd.DConnectWebServerNanoHttpd;

import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Webサーバ用のサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectWebService extends Service {
    /**
     * ロガー.
     */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * Notification Id.
     */
    private static final int ONGOING_NOTIFICATION_ID = 8080;

    /**
     * Webサーバ.
     */
    private DConnectWebServerNanoHttpd mWebServer;

    /**
     * DConnectの設定.
     */
    private DConnectSettings mSettings;

    /**
     * プラグイン管理クラス.
     */
    private DevicePluginManager mPluginManager;

    /**
     * キーストア管理クラス.
     */
    private KeyStoreManager mKeyStoreMgr;

    /**
     * バインドクラス.
     */
    private final IBinder mLocalBinder = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent) {
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = ((DConnectApplication) getApplication()).getSettings();

        mPluginManager = ((DConnectApplication) getApplication()).getPluginManager();

        mKeyStoreMgr = new EndPointKeyStoreManager(getApplicationContext(), DConnectConst.KEYSTORE_FILE_NAME, "0000");

        // Webサーバの起動フラグがONになっている場合には起動を行う
        if (mSettings.isWebServerStartFlag()) {
            startWebServer();
        } else {
            NotificationUtil.fakeStartForeground(this,
                    getString(R.string.web_service_on_channel_id),
                    getString(R.string.web_service_on_channel_title),
                    getString(R.string.web_service_on_channel_desc),
                    ONGOING_NOTIFICATION_ID
            );
        }
    }

    @Override
    public void onDestroy() {
        stopWebServer();
        super.onDestroy();
    }

    /**
     * ポート番号を取得します.
     * @return ポート番号
     */
    public int getPort() {
        return mSettings.getWebPort();
    }

    /**
     * Webサーバを起動する.
     */
    public void startWebServer() {
        if (mWebServer != null) {
            return;
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("Web Server was Started.");
            mLogger.info("Host: " + mSettings.getHost());
            mLogger.info("Port: " + mSettings.getWebPort());
            mLogger.info("SSL: " + mSettings.isSSL());
            mLogger.info("Document Root: " + mSettings.getDocumentRootPath());
        }

        if (mSettings.isSSL()) {
            mKeyStoreMgr.requestKeyStore(DConnectUtil.getIPAddress(this), new KeyStoreCallback() {
                @Override
                public void onSuccess(KeyStore keyStore, Certificate certificate, Certificate certificate1) {
                    try {
                        startInternal(createSSLServerSocketFactory(keyStore, "0000"));
                    } catch (GeneralSecurityException e) {
                        mSettings.setWebServerStartFlag(false);
                        stopWebServer();
                        stopSelf();
                    }
                }

                @Override
                public void onError(KeyStoreError keyStoreError) {
                    mSettings.setWebServerStartFlag(false);
                    stopWebServer();
                    stopSelf();
                }
            });
        } else {
            startInternal(null);
        }
    }

    /**
     * Webサーバを起動します.
     *
     * @param factory ファクトリー
     */
    private synchronized void startInternal(final SSLServerSocketFactory factory) {
        if (mWebServer != null) {
            return;
        }

        mWebServer = new DConnectWebServerNanoHttpd.Builder()
                .port(mSettings.getWebPort())
                .serverSocketFactory(factory)
                .addDocumentRoot(mSettings.getDocumentRootPath())
                .cors("*")
                .version(getVersion(this))
                .build();
        mWebServer.setDispatcher((uri) -> {
            Uri httpUri = Uri.parse(uri);
            Uri contentUri = convertToContentUri(httpUri);
            mLogger.info("Requested File: httpUri=" + httpUri + " -> contentUri=" + contentUri);
            if (contentUri == null) {
                return null;
            }
            String authority = contentUri.getAuthority();
            if (authority == null) {
                return null;
            }
            DevicePlugin plugin = findPluginFromAuthority(authority);
            if (plugin == null) {
                mLogger.info("Not found plug-in for requested file: contentUri=" + contentUri);
                return null;
            }
            mLogger.info("Found plug-in for requested file: name=" + plugin.getDeviceName() + ", contentUri=" + contentUri);
            try {
                ContentResolver resolver = getContentResolver();
                return resolver.openInputStream(contentUri);
            } catch (FileNotFoundException e) {
                mLogger.info("Not found file: contentUri=" + contentUri);
                return null;
            }
        });

        try {
            mWebServer.start();
            registerConnectivityReceiver();
            showNotification();
        } catch (Exception e) {
            mSettings.setWebServerStartFlag(false);
            stopWebServer();
            throw e;
        }
    }

    private DevicePlugin findPluginFromAuthority(final @NonNull String authority) {
        for (DevicePlugin plugin : mPluginManager.getDevicePlugins()) {
            if (plugin.hasContentProvider(authority)) {
                return plugin;
            }
        }
        return null;
    }

    private Uri convertToContentUri(final Uri httpUri) {
        List<String> segments = httpUri.getPathSegments();
        if (segments == null || segments.size() == 0) {
            return null;
        }
        Uri.Builder uri = new Uri.Builder()
                .scheme("content")
                .authority(segments.get(0));
        for (int i = 1; i < segments.size(); i++) {
            uri.appendPath(segments.get(i));
        }
        uri.query(httpUri.getQuery());
        return uri.build();
    }

    /**
     * Webサーバを停止する.
     */
    public synchronized void stopWebServer() {
        if (mWebServer == null) {
            return;
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("Web Server was Stopped.");
        }

        mSettings.setWebServerStartFlag(false);

        NotificationUtil.hideNotification(this);
        unregisterConnectivityReceiver();

        mWebServer.stop();
        mWebServer = null;
    }

    /**
     * Web サーバの動作中か確認します.
     *
     * @return 動作中の場合はtrue、それ以外はfalse
     */
    public synchronized boolean isRunning() {
        return mWebServer != null;
    }

    /**
     * ネットワークの変更イベントを受信する BroadcastReceiver を登録します.
     */
    private void registerConnectivityReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWiFiReceiver, filter);
    }

    /**
     * ネットワークの変更イベントを受信する BroadcastReceiver を解除します.
     */
    private void unregisterConnectivityReceiver() {
        try {
            unregisterReceiver(mWiFiReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * SSLServerSocketFactory を作成します.
     *
     * @param keyStore キーストア
     * @param password パスワード
     * @return SSLServerSocketFactoryのインスタンス
     * @throws GeneralSecurityException SSLServerSocketFactoryの作成に失敗した場合に発生
     */
    private SSLServerSocketFactory createSSLServerSocketFactory(final KeyStore keyStore, final String password) throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext.getServerSocketFactory();
    }

    /**
     * Notification に IP アドレスを表示します.
     */
    private void showNotification() {
        NotificationUtil.showNotification(this,
                DConnectUtil.getIPAddress(this) + ":" + mSettings.getWebPort(),
                getString(R.string.web_service_on_channel_id),
                getString(R.string.service_web_server),
                getString(R.string.web_service_on_channel_title),
                getString(R.string.web_service_on_channel_desc),
                ONGOING_NOTIFICATION_ID);
    }

    /**
     * バージョンコードとバージョン名からバージョンを取得します.
     *
     * @param context コンテキスト
     * @return バージョンの文字列
     */
    private static String getVersion(final Context context) {
        return getVersionName(context) + "_" + getVersionCode(context);
    }

    /**
     * バージョンコードを取得する
     *
     * @param context コンテキスト
     * @return VersionCode
     */
    private static int getVersionCode(final Context context) {
        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore.
        }
        return versionCode;
    }

    /**
     * バージョン名を取得する
     *
     * @param context コンテキスト
     * @return VersionName
     */
    private static String getVersionName(final Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore.
        }
        return versionName;
    }

    /**
     * DConnectWebServiceとバインドするためのクラス.
     */
    public class LocalBinder extends Binder {
        /**
         * DConnectWebServiceのインスタンスを取得する.
         *
         * @return DConnectWebServiceのインスタンス
         */
        public DConnectWebService getDConnectWebService() {
            return DConnectWebService.this;
        }
    }

    /**
     * ネットワークiの接続状態の変化を受け取るレシーバー.
     */
    private final BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            showNotification();
        }
    };
}
