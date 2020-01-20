package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CheckBox;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.SwitchBotMessageService;
import org.deviceconnect.android.message.DConnectMessageService;

public class SettingActivity extends BaseSettingActivity {
    private static final String TAG = "SettingActivity";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private SwitchBotMessageService mSwitchBotMessageService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (DEBUG) {
                Log.d(TAG, "onServiceConnected()");
            }
            mSwitchBotMessageService = (SwitchBotMessageService) ((DConnectMessageService.LocalBinder) iBinder).getMessageService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (DEBUG) {
                Log.d(TAG, "onServiceDisconnected()");
            }
            mSwitchBotMessageService = null;
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = new Intent(this, SwitchBotMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        CheckBox checkBox = findViewById(R.id.checkbox_local_oauth);
        checkBox.setChecked(Settings.getBoolean(this, Settings.KEY_LOCAL_OAUTH, true));
        checkBox.setOnCheckedChangeListener((compoundButton, value) -> {
            if(checkBox.isFocusable()) {
                if(mSwitchBotMessageService != null) {
                    Settings.setBoolean(this, Settings.KEY_LOCAL_OAUTH, value);
                    mSwitchBotMessageService.setLocalOAuthPreference(value);
                }
            }
        });

        findViewById(R.id.text_title_device_register).setOnClickListener((view) -> {
            Intent registerActivity = new Intent(this, RegisterActivity.class);
            startActivity(registerActivity);
        });

        findViewById(R.id.text_title_device_modify_and_delete).setOnClickListener((view) -> {
            Intent modifyAndDeleteActivity = new Intent(this, ModifyAndDeleteActivity.class);
            startActivity(modifyAndDeleteActivity);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}