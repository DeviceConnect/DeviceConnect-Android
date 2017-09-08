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

/**
 * Device Connect Manager device plug-in Information Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginInfoActivity extends BaseSettingActivity {

    /** デバイスプラグイン情報のキー. */
    static final String PLUGIN_INFO = "pluginInfo";

    /** デバイスプラグイン有効フラグのキー. */
    static final String PLUGIN_ENABLED = "pluginEnabled";

    /** デバイスプラグイン接続エラーのキー. */
    static final String CONNECTION_ERROR = "connectionError";

    /** フラグメントのタグ. */
    private static final String TAG = "info";

    /** プラグイン有効化スイッチ. */
    private SwitchCompat mStatusSwitch;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (Connection.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                String pluginId = intent.getStringExtra(Connection.EXTRA_PLUGIN_ID);
                if (mPluginInfo.getPluginId().equals(pluginId)) {
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

    private DevicePlugin.Info mPluginInfo;

    /** 接続中であることを示すビュー. */
    private View mProgressCircle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Connection.ACTION_CONNECTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        mPluginInfo = intent.getParcelableExtra(PLUGIN_INFO);
        if (mPluginInfo == null || !intent.hasExtra(PLUGIN_ENABLED)) {
            finish();
            return;
        }
        final boolean isEnabled;
        if (savedInstanceState != null && savedInstanceState.containsKey(PLUGIN_ENABLED)) {
            isEnabled = savedInstanceState.getBoolean(PLUGIN_ENABLED);
        } else {
            isEnabled = intent.getBooleanExtra(PLUGIN_ENABLED, true);
        }
        final ConnectionError error;
        if (savedInstanceState != null && savedInstanceState.containsKey(CONNECTION_ERROR)) {
            error = (ConnectionError) savedInstanceState.getSerializable(CONNECTION_ERROR);
        } else {
            error = (ConnectionError) intent.getSerializableExtra(CONNECTION_ERROR);
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);
            actionBar.setCustomView(R.layout.action_bar_plugin_enable_status);

            mStatusSwitch = (SwitchCompat) actionBar.getCustomView().findViewById(R.id.switch_plugin_enable_status);
            mStatusSwitch.setChecked(isEnabled);
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

            mProgressCircle = actionBar.getCustomView().findViewById(R.id.progress_plugin_enable_status);
        }

        if (!hasSavedInstance()) {
            Fragment f = new DevicePluginInfoFragment();
            Bundle args = new Bundle();
            args.putParcelable(PLUGIN_INFO, mPluginInfo);
            args.putBoolean(PLUGIN_ENABLED, isEnabled);
            args.putSerializable(CONNECTION_ERROR, error);
            f.setArguments(args);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            t.add(android.R.id.content, f, TAG);
            t.commit();
        }
    }

    private DevicePluginInfoFragment getInfoFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (DevicePluginInfoFragment) fm.findFragmentByTag(TAG);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mStatusSwitch != null) {
            outState.putBoolean(PLUGIN_ENABLED, mStatusSwitch.isChecked());
        }
    }

    private void requestPluginStateChange(final boolean isOn) {
        String action = isOn ?
                DConnectMessageService.ACTION_ENABLE_PLUGIN :
                DConnectMessageService.ACTION_DISABLE_PLUGIN;
        Intent request = new Intent(this, DConnectService.class);
        request.setAction(action);
        request.putExtra(DConnectMessageService.EXTRA_PLUGIN_ID, mPluginInfo.getPluginId());
        startService(request);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
