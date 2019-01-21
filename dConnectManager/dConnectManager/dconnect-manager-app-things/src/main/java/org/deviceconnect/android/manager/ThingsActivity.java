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
import org.deviceconnect.server.DConnectServer;
import org.deviceconnect.server.DConnectServerConfig;
import org.deviceconnect.server.nanohttpd.DConnectServerNanoHttpd;

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
    private DConnectServer mWebServer;

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
        mSettings.setRequireOrigin(false);
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
                try {
                    addDevicePlugin();
                } catch (Exception e) {
                    // ignore.
                }
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onStopped() {
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

                // DConnectManagerでエラーが発生したので終了処理をしておく
                stopManager();
                stopWebServer();
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
        DConnectServerConfig config = new DConnectServerConfig.Builder()
                .port(mSettings.getWebPort())
                .documentRootPath(mSettings.getDocumentRootPath())
                .build();

        if (DEBUG) {
            Log.d(TAG, "WebServer");
            Log.d(TAG, "    config: " + config);
        }

        mWebServer = new DConnectServerNanoHttpd(config, this, null);
        mWebServer.start();
    }

    /**
     * Webサーバを停止します.
     */
    private void stopWebServer() {
        if (mWebServer != null) {
            mWebServer.shutdown();
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
}
