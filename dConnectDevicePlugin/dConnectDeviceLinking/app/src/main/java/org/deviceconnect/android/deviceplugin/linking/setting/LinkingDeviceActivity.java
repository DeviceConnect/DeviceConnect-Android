/*
 org.deviceconnect.android.deviceplugin.linking
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nttdocomo.android.sdaiflib.ControlSensorData;
import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.NotifyNotification;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.IlluminationData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManagerFactory;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.deviceplugin.linking.linking.VibrationData;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.Map;

public class LinkingDeviceActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "bdAddress";

    private LinkingDevice mDevice;
    private ControlSensorData mController;
    private NotifyNotification mNotifyNotification;
    private int mCurrentRequestType = 0;

    private ControlSensorData.SensorDataInterface mSensorInterface = new ControlSensorData.SensorDataInterface() {
        @Override
        public void onStopSensor(String bd, int type, int reason) {
            updateDataText(0, 0, 0, 0, 0);
            updateDataText(1, 0, 0, 0, 0);
            updateDataText(2, 0, 0, 0, 0);
        }

        @Override
        public void onSensorData(String bd, int type, float x, float y, float z, byte[] originalData, long time) {
            updateDataText(type, x, y, z, time);
        }
    };

    private NotifyNotification.NotificationInterface mNotificationInterface = new NotifyNotification.NotificationInterface() {
        @Override
        public void onNotify() {
            SharedPreferences preference = getSharedPreferences(Define.NotificationInfo, Context.MODE_PRIVATE);
            int deviceId = preference.getInt("DEVICE_ID", -1);
            int uniqueId = preference.getInt("DEVICE_UID", -1);
            int keyCode = preference.getInt("DEVICE_BUTTON_ID", -1);
            updateKeyEvent(deviceId, uniqueId, keyCode);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_controller);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle args = intent.getExtras();
            if (args != null) {
                test(args.getString(EXTRA_ADDRESS));
            }
        }

        setupUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 4) {
            return;
        }
        // RESULT_OK, RESULT_SENSOR_UNSUPPORTED
        if (resultCode != -1 && resultCode != 8) {
            mCurrentRequestType = 0;
            return;
        }
        if (mCurrentRequestType == 2) {
            mCurrentRequestType = 0;
            return;
        }
        if (mController == null) {
            mCurrentRequestType = 0;
            return;
        }
        mCurrentRequestType += 1;
        mController.setType(mCurrentRequestType);
        int result = mController.start();
        printResult(result);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void test(String address) {
        if (address == null) {
            return;
        }

        LinkingManager manager = LinkingManagerFactory.createManager(getApplicationContext());
        for (LinkingDevice device : manager.getDevices()) {
            if (device.getBdAddress().equals(address)) {
                setTargetDevice(device);
                return;
            }
        }
    }

    public void setTargetDevice(LinkingDevice device) {
        mDevice = device;
        TextView tv = (TextView) findViewById(R.id.device_name);
        if (tv != null) {
            tv.setText(mDevice.getDisplayName());
        }
        setupLightOffSetting();
        setupVibrationOffSetting();
    }

    private void setupLightOffSetting() {
        Button btn = (Button) findViewById(R.id.select_light_off);
        if (btn == null || mDevice == null) {
            return;
        }

        byte[] illumination = mDevice.getIllumination();
        if (illumination == null) {
            btn.setText(getString(R.string.not_selected));
            return;
        }

        Integer patternId = getLightOffSetting();
        if (patternId == null) {
            setupDefaultOffSettingOfLight(mDevice);
            return;
        }

        IlluminationData data = new IlluminationData(illumination);
        for (IlluminationData.Setting setting : data.mPattern.children) {
            if ((setting.id & 0xFF) == patternId) {
                btn.setText(setting.names[0].name);
            }
        }
    }

    private Integer getLightOffSetting() {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getLightOffSetting();
        return map == null ? null : map.get(mDevice.getBdAddress());
    }

    private void setupDefaultOffSettingOfLight(LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_light_off);
        if (btn != null) {
            IlluminationData.Setting setting = LinkingUtil.getDefaultOffSettingOfLight(device);
            if (setting != null) {
                btn.setText(setting.names[0].name);
                updateLightOffSetting(setting.id & 0xFF);
            } else {
                btn.setText(getString(R.string.not_selected));
            }
        }
    }

    private void setupVibrationOffSetting() {
        Button btn = (Button) findViewById(R.id.select_vibration_off);
        if (btn == null || mDevice == null) {
            return;
        }

        byte[] vibration = mDevice.getVibration();
        if (vibration == null) {
            btn.setText(getString(R.string.not_selected));
            return;
        }

        Integer patternId = getVibrationOffSetting();
        if (patternId == null) {
            setupDefaultOffSettingOfVibration(mDevice);
            return;
        }

        VibrationData data = new VibrationData(vibration);
        for (VibrationData.Setting setting : data.mPattern.children) {
            if ((setting.id & 0xFF) == patternId) {
                btn.setText(setting.names[0].name);
            }
        }
    }

    private Integer getVibrationOffSetting() {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getVibrationOffSetting();
        return map == null ? null : map.get(mDevice.getBdAddress());
    }

    private void setupDefaultOffSettingOfVibration(LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_vibration_off);
        if (btn != null) {
            VibrationData.Setting setting = LinkingUtil.getDefaultOffSettingOfVibration(device);
            if (setting != null) {
                btn.setText(setting.names[0].name);
                updateVibrationOffSetting(setting.id & 0xFF);
            } else {
                btn.setText(getString(R.string.not_selected));
            }
        }
    }

    private void setupUI() {
        String deviceName = getString(R.string.device_name) + getString(R.string.not_selected);
        TextView tv = (TextView) findViewById(R.id.device_name);
        if (tv != null) {
            tv.setText(deviceName);
        }
        setLightButton();
        setVibrationButton();
        setSensorButton();
        setButtonIdButton();
    }

    private void setLightButton() {
        View view = findViewById(R.id.select_light_off);
        if (view == null) {
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDevice()) {
                    return;
                }

                byte[] illumination = mDevice.getIllumination();
                if (illumination == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.device_not_support_led), Toast.LENGTH_SHORT).show();
                    return;
                }

                final IlluminationData data = new IlluminationData(illumination);

                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                final String[] items = new String[data.mPattern.children.length];
                for (int i = 0; i < data.mPattern.children.length; i++) {
                    items[i] = data.mPattern.children[i].names[0].name;
                }
                builder.setTitle(getString(R.string.pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IlluminationData.Setting selectedPattern = data.mPattern.children[which];
                        Button btn = ((Button) findViewById(R.id.select_light_off));
                        if (btn != null) {
                            btn.setText(selectedPattern.names[0].name);
                        }
                        updateLightOffSetting(selectedPattern.id & 0xFF);
                    }
                });
                builder.create().show();
            }
        });
        Button onBtn = (Button) findViewById(R.id.on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLED(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLED(false);
                }
            });
        }
    }

    private void setVibrationButton() {
        View view = findViewById(R.id.select_vibration_off);
        if (view == null) {
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkDevice()) {
                    return;
                }

                byte[] vibration = mDevice.getVibration();
                if (vibration == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.device_not_support_vibration), Toast.LENGTH_SHORT).show();
                    return;
                }

                final VibrationData data = new VibrationData(vibration);

                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                final String[] items = new String[data.mPattern.children.length];
                for (int i = 0; i < data.mPattern.children.length; i++) {
                    items[i] = data.mPattern.children[i].names[0].name;
                }
                builder.setTitle(getString(R.string.pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VibrationData.Setting selectedPattern = data.mPattern.children[which];
                        Button btn = (Button) findViewById(R.id.select_vibration_off);
                        if (btn != null) {
                            btn.setText(selectedPattern.names[0].name);
                        }
                        updateVibrationOffSetting(selectedPattern.id & 0xFF);
                    }
                });
                builder.create().show();
            }
        });
        Button onBtn = (Button) findViewById(R.id.vibration_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickVibration(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.vibration_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickVibration(false);
                }
            });
        }
    }

    private void setSensorButton() {
        Button onBtn = (Button) findViewById(R.id.sensor_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSensor(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.sensor_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSensor(false);
                }
            });
        }

        updateDataText(0, 0, 0, 0, 0);
        updateDataText(1, 0, 0, 0, 0);
        updateDataText(2, 0, 0, 0, 0);
    }

    private void setButtonIdButton() {
        Button onBtn = (Button) findViewById(R.id.button_id_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickButtonId(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.button_id_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickButtonId(false);
                }
            });
        }
    }

    private void updateLightOffSetting(Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getLightOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setLightOffSetting(map);
    }

    private void updateVibrationOffSetting(Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getVibrationOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setVibrationOffSetting(map);
    }

    private void updateDataText(int type, float x, float y, float z, long time) {
        switch (type) {
            case 0: {
                TextView view = (TextView) findViewById(R.id.gyro_text);
                if (view != null) {
                    view.setText(makeParamText(x, y, z, time));
                }
            }   break;
            case 1: {
                TextView view = (TextView) findViewById(R.id.acceleration_text);
                if (view != null) {
                    view.setText(makeParamText(x, y, z, time));
                }
            }   break;
            case 2: {
                TextView view = (TextView) findViewById(R.id.orientation_text);
                if (view != null) {
                    view.setText(makeParamText(x, y, z, time));
                }
            }   break;
        }
    }

    private String makeParamText(float x, float y, float z, long time) {
        return getString(R.string.sensor_value, x, y, z, time);
    }

    private void onClickLED(boolean isOn) {
        if (!checkDevice()) {
            return;
        }
        if (mDevice.getIllumination() == null) {
            Toast.makeText(this, getString(R.string.device_not_support_led), Toast.LENGTH_SHORT).show();
            return;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getApplicationContext());
        manager.sendLEDCommand(mDevice, isOn);
    }

    private void onClickVibration(boolean isOn) {
        if (!checkDevice()) {
            return;
        }
        if (mDevice.getVibration() == null) {
            Toast.makeText(this, getString(R.string.device_not_support_vibration), Toast.LENGTH_SHORT).show();
            return;
        }
        LinkingManager manager = LinkingManagerFactory.createManager(getApplicationContext());
        manager.sendVibrationCommand(mDevice, isOn);
    }

    private void onClickSensor(boolean isOn) {
        if (!checkDevice()) {
            return;
        }

        // TODO:check device's feature

        if (isOn) {
            if (mController == null) {
                mController = new ControlSensorData(this, mSensorInterface);
                mController.setInterval(100);
                mController.setType(0);
            } else {
                stopController();
            }
            mController.setBDaddress(mDevice.getBdAddress());
            mCurrentRequestType = 0;
            int result = mController.start();
            printResult(result);
        } else {
            stopController();
            mController = null;
        }
    }

    private void onClickButtonId(boolean isOn) {
        if (!checkDevice()) {
            return;
        }

        if (isOn) {
            if (mNotifyNotification == null) {
                mNotifyNotification = new NotifyNotification(this, mNotificationInterface);
                TextView tv = (TextView) findViewById(R.id.linking_button_id);
                if (tv != null) {
                    tv.setText("");
                }
            }
        } else {
            if (mNotifyNotification != null) {
                mNotifyNotification.release();
                mNotifyNotification = null;
            }
        }
    }

    private boolean checkDevice() {
        if (mDevice == null) {
            Toast.makeText(this, getString(R.string.device_not_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void stopController() {
        if (mController != null) {
            int result = mController.stop();
            printResult(result);
            updateDataText(0, 0, 0, 0, 0);
            updateDataText(1, 0, 0, 0, 0);
            updateDataText(2, 0, 0, 0, 0);
        }
    }

    private void updateKeyEvent(int deviceId, int uniqueId, int keyCode) {

        if (mDevice == null) {
            return;
        }

        // TODO デバイス識別
//        if (deviceId != mDevice.getModelId() || uniqueId != mDevice.getUniqueId()) {
//            return;
//        }

        TextView tv = (TextView) findViewById(R.id.linking_button_id);
        if (tv != null) {
            String text = tv.getText().toString();
            text = mDevice.getBdAddress() + ": ButtonId[" + keyCode + "]\r\n" + text;
            tv.setText(text);
        }
    }

    private void printResult(int result) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        switch (result) {
            case -1:
                Log.i("LinkingPlugIn", "RESULT_OK(-1)");
                break;
            case 1:
                Log.i("LinkingPlugIn", "RESULT_CANCEL(1)");
                break;
            case 4:
                Log.i("LinkingPlugIn", "RESULT_DEVICE_OFF(4)");
                break;
            case 5:
                Log.i("LinkingPlugIn", "RESULT_CONNECT_FAILURE(5)");
                break;
            case 6:
                Log.i("LinkingPlugIn", "RESULT_CONFLICT(6)");
                break;
            case 7:
                Log.i("LinkingPlugIn", "RESULT_PARAM_ERROR(7)");
                break;
            case 8:
                Log.i("LinkingPlugIn", "RESULT_SENSOR_UNSUPPORTED(8)");
                break;
            case 0:
                Log.i("LinkingPlugIn", "RESULT_OTHER_ERROR(0)");
                break;
            default:
                Log.i("LinkingPlugIn", "UNKNOWN_ERROR(" + result + ")");
        }
    }
}
