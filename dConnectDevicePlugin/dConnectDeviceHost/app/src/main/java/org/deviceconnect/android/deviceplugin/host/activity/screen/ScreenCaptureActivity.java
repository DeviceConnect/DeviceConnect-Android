package org.deviceconnect.android.deviceplugin.host.activity.screen;

import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;

public class ScreenCaptureActivity extends HostDevicePluginBindActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_capture);
    }
}
