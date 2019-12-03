/*
 ThingsActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.manager.core.DConnectManager;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.plugin.ConnectionType;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.android.manager.util.IpAddressFetcher;
import org.deviceconnect.server.nanohttpd.DConnectWebServerNanoHttpd;

/**
 * Android Things 用の Device Connect 起動用 Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThingsActivity extends Activity {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "THETA-V-Activity";

    /**
     * Device Connect Manager の設定を保持するクラス.
     */
    private DConnectSettings mSettings;

    /**
     * Device Connect Manager 本体.
     */
    private DConnectManager mManager;

    /**
     * Webサーバ.
     */
    private DConnectWebServerNanoHttpd mWebServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_things);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startManager();
        startWebServer();
        setIpAddress();
    }

    @Override
    protected void onPause() {
        stopManager();
        stopWebServer();
        super.onPause();
    }

    /**
     * DConnectManagerを起動します.
     */
    private void startManager() {
        mSettings = new DConnectSettings(this);
        mSettings.setUseALocalOAuth(false);
        mSettings.setRequireOrigin(true);
        mSettings.setAllowExternalIP(true);
        mSettings.setProductName(getString(R.string.app_name));

        mManager = new DConnectManager(this, mSettings) {
            @Override
            public Class<? extends BroadcastReceiver> getDConnectBroadcastReceiverClass() {
                return new BroadcastReceiver() {
                    @Override
                    public void onReceive(final Context context, final Intent intent) {
                    }
                }.getClass();
            }

            @Override
            public Class<? extends Activity> getSettingActivityClass() {
                return ThingsActivity.this.getClass();
            }

            @Override
            public Class<? extends Activity> getKeywordActivityClass() {
                return ThingsActivity.this.getClass();
            }
        };

        mManager.setOnEventListener(new DConnectManager.OnEventListener() {
            @Override
            public void onFinishSearchPlugin() {
            }

            @Override
            public void onStarted() {
                if (DEBUG) {
                    Log.i(TAG, "DConnectManager is started.");
                }
            }

            @Override
            public void onStopped() {
                if (DEBUG) {
                    Log.i(TAG, "DConnectManager is stopped.");
                }
            }

            @Override
            public void onChangedNetwork() {
                runOnUiThread(ThingsActivity.this::setIpAddress);
            }

            @Override
            public void onError(final Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "An error occurred in DConnectManager.", e);
                }

                // DConnectManager でエラーが発生したので終了処理をしておく
                stopManager();
                stopWebServer();
                finish();
            }
        });

        try {
            mManager.startDConnect();
        } catch (Exception e) {
            finish();
        }
    }

    /**
     * DConnectManagerを停止します.
     */
    private void stopManager() {
        if (mManager != null) {
            try {
                mManager.stopDConnect();
                mManager = null;
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    /**
     * THETA V 用プラグインを追加します.
     */
    private void addDevicePlugin() {
        String packageName = getPackageName();
        String className = HostDevicePlugin.class.getName();

        DevicePlugin plugin = new DevicePlugin.Builder(ThingsActivity.this)
                .setClassName(className)
                .setPackageName(packageName)
                .setConnectionType(ConnectionType.DIRECT)
                .setDeviceName("HOST")
                .setVersionName("1.0")
                .setPluginId(createMD5(packageName, className))
                .setPluginSdkVersionName(VersionName.parse("1.2.0"))
                .build();

        mManager.getPluginManager().addDevicePlugin(plugin);

        plugin.enable();
    }

    /**
     * プラグインのIDを作成します.
     * @param packageName パッケージ名
     * @param className クラス名
     * @return プラグインID
     */
    private String createMD5(final String packageName, final String className) {
        try {
            return DConnectUtil.toMD5(packageName + className);
        } catch (Exception e) {
            return "host_plugin_id";
        }
    }

    /**
     * Webサーバを起動します.
     */
    private void startWebServer() {
        mWebServer = new DConnectWebServerNanoHttpd.Builder()
                .port(mSettings.getWebPort())
                .addDocumentRoot(mSettings.getDocumentRootPath())
                .cors("*")
                .version(getVersion(this))
                .build();
        mWebServer.start();
    }

    /**
     * Webサーバを停止します.
     */
    private void stopWebServer() {
        if (mWebServer != null) {
            mWebServer.stop();
            mWebServer = null;
        }
    }

    /**
     * IPアドレスを更新します.
     */
    private void setIpAddress() {
        IpAddressFetcher fetcher = new IpAddressFetcher();
        TextView ipAddress = findViewById(R.id.activity_things_ip_address);
        ipAddress.setText(fetcher.getWifiIPv4Address());
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
}
