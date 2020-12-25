package org.deviceconnect.android.deviceplugin.host.activity.recorder.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;

public class CameraActivity extends HostDevicePluginBindActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // 明示的に画面を OFF にさせない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public String getRecorderId() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getStringExtra(HostMediaRecorderManager.KEY_RECORDER_ID);
        }
        return null;
    }
}
