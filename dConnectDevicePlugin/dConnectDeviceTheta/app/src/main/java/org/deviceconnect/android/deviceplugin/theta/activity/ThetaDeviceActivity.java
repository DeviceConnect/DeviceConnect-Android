/*
 ThetaDeviceActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.theta.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
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
    }

    private ThetaDeviceManager getDeviceManager() {
        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        return app.getDeviceManager();
    }

}
