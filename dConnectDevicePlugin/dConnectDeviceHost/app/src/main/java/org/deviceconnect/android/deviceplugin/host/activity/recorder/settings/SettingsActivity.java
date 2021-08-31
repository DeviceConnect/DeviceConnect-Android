package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class SettingsActivity extends HostDevicePluginBindActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setRequestedOrientation(getDisplayOrientation());
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onBindService() {
        super.onBindService();

        HostMediaRecorder recorder = getRecorder();
        ActionBar actionBar = getSupportActionBar();
        if (recorder != null && actionBar != null) {
            actionBar.setTitle(recorder.getName());
        }
    }

    public int getDisplayOrientation() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getIntExtra("rotation_flag", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public String getRecorderId() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getStringExtra("recorder_id");
        }
        return null;
    }

    public HostMediaRecorder getRecorder() {
        HostDevicePlugin plugin = getHostDevicePlugin();
        if (plugin != null) {
            return plugin.getHostMediaRecorderManager().getRecorder(getRecorderId());
        }
        return null;
    }

    public static Intent createSettingsActivityIntent(Context context, String recorderId, Integer rotationFlag) {
        Intent intent = new Intent();
        intent.setClass(context, SettingsActivity.class);
        if (recorderId != null) {
            intent.putExtra("recorder_id", recorderId);
        }
        if (rotationFlag != null) {
            intent.putExtra("rotation_flag", rotationFlag);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static void startActivity(Context context, String recorderId, Integer rotationFlag) {
        context.startActivity(createSettingsActivityIntent(context, recorderId, rotationFlag));
    }
}
