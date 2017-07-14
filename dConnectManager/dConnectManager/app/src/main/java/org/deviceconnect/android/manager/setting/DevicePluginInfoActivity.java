/*
 DevicePluginInfoActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.plugin.ConnectionState;
import org.deviceconnect.android.manager.plugin.ConnectionStateListener;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Device Connect Manager device plug-in Information Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginInfoActivity extends AppCompatActivity {

    /** デバイスプラグインのプラグインIDのキー. */
    static final String PLUGIN_ID = "pluginId";

    private DevicePlugin mPlugin;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final ConnectionStateListener mListener = new ConnectionStateListener() {
        @Override
        public void onConnectionStateChanged(final ConnectionState state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case CONNECTING:
                            mProgressCircle.setVisibility(View.VISIBLE);
                            break;
                        case CONNECTED:
                            mProgressCircle.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
            });
        }
    };

    private View mProgressCircle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        String pluginId = intent.getStringExtra(PLUGIN_ID);
        if (pluginId == null) {
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);
            actionBar.setCustomView(R.layout.layout_plugin_enable_status);

            DConnectApplication apps = (DConnectApplication) getApplication();
            DevicePluginManager manager = apps.getDevicePluginManager();
            for (DevicePlugin plugin : manager.getDevicePlugins()) {
                if (pluginId.equals(plugin.getPluginId())) {
                    mPlugin = plugin;
                    mPlugin.addConnectionStateListener(mListener);
                    break;
                }
            }

            SwitchCompat statusSwitch = (SwitchCompat) actionBar.getCustomView().findViewById(R.id.switch_plugin_enable_status);
            statusSwitch.setChecked(mPlugin.isEnabled());
            statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton button, final boolean isOn) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isOn) {
                                mPlugin.enable();
                            } else {
                                mPlugin.disable();
                            }
                        }
                    });
                }
            });

            mProgressCircle = actionBar.getCustomView().findViewById(R.id.progress_plugin_enable_status);
        }

        if (savedInstanceState == null) {
            Fragment f = new DevicePluginInfoFragment();
            Bundle args = new Bundle();
            args.putString(PLUGIN_ID, pluginId);
            f.setArguments(args);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            t.add(android.R.id.content, f, "container");
            t.commit();
        }
    }

    @Override
    protected void onDestroy() {
        if (mPlugin != null) {
            mPlugin.removeConnectionStateListener(mListener);
        }
        super.onDestroy();
    }
}
