package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.SwitchBotMessageService;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.DConnectMessageService;

public class ModifyActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ModifyActivity";
    private static final Boolean DEBUG = BuildConfig.DEBUG;
    public static final String KEY_DEVICE_NAME = "key_device_name";
    private SwitchBotDevice switchBotDevice;
    private SwitchBotMessageService switchBotMessageService = null;
    private EditText deviceName;
    private EditText deviceAddress;
    private Spinner deviceMode;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (DEBUG) {
                Log.d(TAG, "onServiceConnected()");
            }
            switchBotMessageService = (SwitchBotMessageService) ((DConnectMessageService.LocalBinder) iBinder).getMessageService();
            switchBotDevice = switchBotMessageService.getSwitchBotDeviceFromDeviceName(getIntent().getStringExtra(KEY_DEVICE_NAME));
            if (switchBotDevice != null) {
                if (DEBUG) {
                    Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
                    Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
                    Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    deviceName.setText(switchBotDevice.getDeviceName());
                    deviceAddress.setText(switchBotDevice.getDeviceAddress());
                    deviceMode.setSelection(switchBotDevice.getDeviceMode().getValue());
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (DEBUG) {
                Log.d(TAG, "onServiceDisconnected()");
            }
            switchBotMessageService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        if (DEBUG) {
            Log.d(TAG, "onCreate()");
            Log.d(TAG, "savedInstanceState : " + savedInstanceState);
        }

        Intent intent = new Intent(this, SwitchBotMessageService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        deviceName = findViewById(R.id.edit_device_name);
        deviceAddress = findViewById(R.id.edit_device_address);
        deviceMode = findViewById(R.id.spinner_device_mode);
        Button button = findViewById(R.id.button_modify);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (DEBUG) {
            Log.d(TAG, "onClick()");
            Log.d(TAG, "view : " + view);
            Log.d(TAG, "view.getId() : " + viewId);
        }
        if (viewId == R.id.button_modify) {
            if (deviceName.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_modify_error_device_name_empty), Toast.LENGTH_LONG).show();
                return;
            }
            if(!switchBotMessageService.modifyDevice(
                    switchBotDevice,
                    new SwitchBotDevice(
                            switchBotMessageService,
                            deviceName.getText().toString(),
                            deviceAddress.getText().toString(),
                            SwitchBotDevice.Mode.getInstance(deviceMode.getSelectedItemPosition())
                    )
            )){
                Toast.makeText(this, getString(R.string.toast_modify_error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_modify_success), Toast.LENGTH_LONG).show();
                unbindService(connection);
                finish();
            }
        }
    }
}
