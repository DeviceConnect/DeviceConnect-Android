/*
 ThetaDeviceActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.theta.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.wifi.WifiStateEventListener;
import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaGalleryFragment;

/**
 * The Sample Application of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceActivity extends FragmentActivity {

    private static final String TAG_LIST = "list";

    /**
     * An instance of {@link ThetaDeviceManager}.
     */
    private ThetaDeviceManager mDeviceMgr;
    private WifiStateEventListener mListener;
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                mListener.onNetworkChanged(wifiInfo);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        mListener.onWiFiDisabled();
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        mListener.onWiFiEnabled();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceMgr = getDeviceManager();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragment == null) {
            fragment = ThetaGalleryFragment.newInstance(mDeviceMgr);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, fragment, TAG_LIST);
            ft.commit();
        }
        mListener = getDeviceManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiReceiver);
    }

    public ThetaDeviceManager getDeviceManager() {
        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        return app.getDeviceManager();
    }

}
