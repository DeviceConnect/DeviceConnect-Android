/*
 UVCDeviceSettingsActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.activity;

import android.support.v4.app.Fragment;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;

import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceApplication;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.fragment.UVCDeviceConnectionFragment;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;


public class UVCDeviceSettingsActivity extends DConnectSettingPageFragmentActivity
    implements CameraDialog.CameraDialogParent {

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public Fragment createPage(int position) {
        return new UVCDeviceConnectionFragment();
    }

    public UVCDeviceManager getDeviceManager() {
        UVCDeviceApplication app = (UVCDeviceApplication) getApplication();
        return app.getDeviceManager();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return getDeviceManager().getUSBMonitor();
    }
}
