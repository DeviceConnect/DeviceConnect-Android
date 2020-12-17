package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onBindService() {
        for (Fragment f : getSupportFragmentManager ().getFragments()) {
            if (f instanceof NavHostFragment) {
                for (Fragment t : f.getChildFragmentManager().getFragments()) {
                    if (t instanceof OnHostDevicePluginListener) {
                        ((OnHostDevicePluginListener) t).onBindService();
                    }
                }
            } else  if (f instanceof OnHostDevicePluginListener) {
                ((OnHostDevicePluginListener) f).onBindService();
            }
        }
    }

    @Override
    protected void onUnbindService() {
        for (Fragment f : getSupportFragmentManager ().getFragments()) {
            if (f instanceof NavHostFragment) {
                for (Fragment t : f.getChildFragmentManager().getFragments()) {
                    if (t instanceof OnHostDevicePluginListener) {
                        ((OnHostDevicePluginListener) t).onUnbindService();
                    }
                }
            } else  if (f instanceof OnHostDevicePluginListener) {
                ((OnHostDevicePluginListener) f).onUnbindService();
            }
        }
    }

    public String getRecorderId() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getStringExtra(HostMediaRecorderManager.KEY_RECORDER_ID);
        }
        return null;
    }
}
