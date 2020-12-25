package org.deviceconnect.android.deviceplugin.host.activity.recorder.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.HostDevicePluginBindActivity;

public class SettingsActivity extends HostDevicePluginBindActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    public int getDisplayOrientation() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getIntExtra("rotation_flag", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public String getRecorderId() {
        String recorderId = null;
        Intent intent = getIntent();
        if (intent != null) {
            recorderId = intent.getStringExtra("recorder_id");
        }

        if (recorderId == null && isBound()) {
            recorderId = getHostDevicePlugin().getHostMediaRecorderManager().getRecorder(null).getId();
        }

        return recorderId;
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
