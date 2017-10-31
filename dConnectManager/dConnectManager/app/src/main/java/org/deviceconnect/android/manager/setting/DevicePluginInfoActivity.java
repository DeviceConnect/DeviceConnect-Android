/*
 DevicePluginInfoActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.Connection;
import org.deviceconnect.android.manager.plugin.ConnectionError;
import org.deviceconnect.android.manager.plugin.ConnectionState;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;

import java.util.List;

/**
 * Device Connect Manager device plug-in Information Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginInfoActivity extends BaseSettingActivity {

    /**
     * プラグインIDを格納するExtraのキーを定義する.
     */
    public static final String EXTRA_PLUGIN_ID = "pluginId";

    /**
     * プラグインのパッケージ名を格納するExtraのキーを定義する.
     */
    public static final String EXTRA_PACKAGE_NAME = "packageName";

    /** フラグメントのタグ. */
    private static final String TAG = "info";

    /** プラグイン有効化スイッチ. */
    private SwitchCompat mStatusSwitch;

    /** プラグインID. */
    private String mPluginId;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (Connection.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                String pluginId = intent.getStringExtra(Connection.EXTRA_PLUGIN_ID);
                if (mPluginId != null && mPluginId.equals(pluginId)) {
                    final ConnectionState state = (ConnectionState) intent.getSerializableExtra(Connection.EXTRA_CONNECTION_STATE);
                    final ConnectionError error = (ConnectionError) intent.getSerializableExtra(Connection.EXTRA_CONNECTION_ERROR);
                    if (state == null) {
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 接続処理中表示
                            switch (state) {
                                case CONNECTING:
                                    mProgressCircle.setVisibility(View.VISIBLE);
                                    break;
                                case CONNECTED:
                                case SUSPENDED:
                                    mProgressCircle.setVisibility(View.INVISIBLE);
                                    break;
                                default:
                                    break;
                            }

                            // 接続エラー表示
                            DevicePluginInfoFragment infoFragment = getInfoFragment();
                            if (infoFragment != null) {
                                infoFragment.updateErrorState(error);
                            }
                        }
                    });
                }
            }
        }
    };

    /** 接続中であることを示すビュー. */
    private View mProgressCircle;

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Connection.ACTION_CONNECTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        DevicePlugin plugin = findDevicePlugin();
        if (plugin == null) {
            finish();
            return;
        }
        mPluginId = plugin.getPluginId();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);
            actionBar.setCustomView(R.layout.action_bar_plugin_enable_status);

            mStatusSwitch = (SwitchCompat) actionBar.getCustomView().findViewById(R.id.switch_plugin_enable_status);
            mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton button, final boolean isOn) {
                    // 再起動ボタンの有効状態を変更
                    FragmentManager fm = getSupportFragmentManager();
                    Fragment f = fm.findFragmentByTag(TAG);
                    if (f != null && f.isResumed() && f instanceof DevicePluginInfoFragment) {
                        ((DevicePluginInfoFragment) f).onEnabled(isOn);
                    }

                    // プラグインの有効状態を変更
                    requestPluginStateChange(isOn);
                }
            });
            mStatusSwitch.setChecked(plugin.isEnabled());

            mProgressCircle = actionBar.getCustomView().findViewById(R.id.progress_plugin_enable_status);
        }

        if (!hasSavedInstance()) {
            Fragment f = new DevicePluginInfoFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_PLUGIN_ID, plugin.getPluginId());
            f.setArguments(args);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            t.add(android.R.id.content, f, TAG);
            t.commit();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onManagerBonded() {
        init();
    }

    private DevicePluginInfoFragment getInfoFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (DevicePluginInfoFragment) fm.findFragmentByTag(TAG);
    }

    private void requestPluginStateChange(final boolean isOn) {
        String action = isOn ?
                DConnectMessageService.ACTION_ENABLE_PLUGIN :
                DConnectMessageService.ACTION_DISABLE_PLUGIN;
        Intent request = new Intent(this, DConnectService.class);
        request.setAction(action);
        request.putExtra(DConnectMessageService.EXTRA_PLUGIN_ID, mPluginId);
        startService(request);
    }

    private DevicePlugin findDevicePluginById(final String pluginId) {
        DevicePluginManager mgr = getPluginManager();
        if (mgr != null && pluginId != null) {
            return mgr.getDevicePlugin(pluginId);
        }
        return null;
    }

    private DevicePlugin findDevicePluginByPackageName(final String packageName) {
        DevicePluginManager mgr = getPluginManager();
        if (mgr != null && packageName != null) {
            List<DevicePlugin> plugins = mgr.getDevicePlugins();
            for (DevicePlugin plugin : plugins) {
                if (packageName.equals(plugin.getPackageName())) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private DevicePlugin findDevicePlugin() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        String pluginId = intent.getStringExtra(EXTRA_PLUGIN_ID);
        if (pluginId != null) {
            return findDevicePluginById(pluginId);
        } else {
            String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            return findDevicePluginByPackageName(packageName);
        }
    }
}
