package org.deviceconnect.android.deviceplugin.uvc.activity;

import android.os.Bundle;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class UVCCameraActivity extends UVCDevicePluginBindActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_camera);
        // 明示的に画面を OFF にさせない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
