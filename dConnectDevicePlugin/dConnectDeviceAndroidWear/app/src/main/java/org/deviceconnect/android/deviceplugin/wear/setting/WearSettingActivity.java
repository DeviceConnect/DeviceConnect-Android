/*
 WearSettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.setting;

import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

/**
 * Setting Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearSettingActivity extends DConnectSettingPageFragmentActivity {

    /** Service ID. */
    private String mServiceId;

    /** Page count. */
    private static final int PAGE_COUNT = 1;

    @Override
    public Fragment createPage(final int position) {
        Bundle mBundle = new Bundle();
        mBundle.putInt("position", position);
        WearSettingFragment mFragment = new WearSettingFragment();
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    /**
     * Get Service ID.
     * 
     * @return Service ID.
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * Set Service ID.
     * 
     * @param serviceId Service ID.
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    @Override
    public int getPageCount() {
        return PAGE_COUNT;
    }
}
