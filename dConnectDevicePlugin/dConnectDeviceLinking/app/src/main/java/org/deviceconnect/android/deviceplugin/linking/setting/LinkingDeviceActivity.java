/*
 LinkingDeviceActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.linking.IlluminationData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingSensorData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.deviceplugin.linking.linking.VibrationData;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.ConfirmationDialogFragment;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.Map;

public class LinkingDeviceActivity extends AppCompatActivity implements ConfirmationDialogFragment.OnDialogEventListener {
    private static final String TAG = "LinkingPlugIn";
    public static final String EXTRA_ADDRESS = "bdAddress";

    private LinkingDevice mDevice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_device);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.activity_device_title));
            actionBar.setElevation(0);
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle args = intent.getExtras();
            if (args != null) {
                mDevice = getLinkingDeviceByAddress(args.getString(EXTRA_ADDRESS));
                if (mDevice != null) {
                    setupUI();
                }
            }
        }

        if (mDevice == null) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLinkingDeviceManager().addSensorListener(mOnSensorListener);
        getLinkingDeviceManager().addKeyEventListener(mOnKeyEventListener);
        getLinkingDeviceManager().addRangeListener(mOnRangeListener);
        getLinkingDeviceManager().addConnectListener(mOnConnectListener);
        getLinkingDeviceManager().addBatteryListener(mOnBatteryListener);
        getLinkingDeviceManager().addTemperatureListener(mOnTemperatureListener);
        getLinkingDeviceManager().addHumidityListener(mOnHumidityListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getLinkingDeviceManager().removeSensorListener(mOnSensorListener);
        getLinkingDeviceManager().removeKeyEventListener(mOnKeyEventListener);
        getLinkingDeviceManager().removeRangeListener(mOnRangeListener);
        getLinkingDeviceManager().removeConnectListener(mOnConnectListener);
        getLinkingDeviceManager().removeBatteryListener(mOnBatteryListener);
        getLinkingDeviceManager().removeTemperatureListener(mOnTemperatureListener);
        getLinkingDeviceManager().removeHumidityListener(mOnHumidityListener);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveClick(DialogFragment fragment) {
        finish();
    }

    @Override
    public void onNegativeClick(DialogFragment fragment) {

    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = (LinkingApplication) getApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingDevice getLinkingDeviceByAddress(final String address) {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        return mgr.findDeviceByBdAddress(address);
    }

    public void setupUI() {
        TextView tv = (TextView) findViewById(R.id.device_name);
        if (tv != null) {
            tv.setText(mDevice.getDisplayName());
        }
        setupLightOffSetting();
        setupVibrationOffSetting();
        setLightButton();
        setVibrationButton();
        setSensorButton();
        setButtonIdButton();
        setProximityButton();
        setBatteryButton();
        setTemperatureButton();
        setHumidityButton();
    }

    private void setupLightOffSetting() {
        Button btn = (Button) findViewById(R.id.select_light_off);
        if (btn == null || mDevice == null) {
            return;
        }

        byte[] illumination = mDevice.getIllumination();
        if (illumination == null) {
            btn.setText(getString(R.string.activity_device_not_selected));
            return;
        }

        Integer patternId = getLightOffSetting();
        if (patternId == null) {
            setupDefaultOffSettingOfLight(mDevice);
            return;
        }

        try {
            IlluminationData data = new IlluminationData(illumination);
            for (IlluminationData.Setting setting : data.getPattern().getChildren()) {
                if ((setting.getId() & 0xFF) == patternId) {
                    btn.setText(setting.getName(0).getName());
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private Integer getLightOffSetting() {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getLightOffSetting();
        return map == null ? null : map.get(mDevice.getBdAddress());
    }

    private void setupDefaultOffSettingOfLight(final LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_light_off);
        if (btn != null) {
            IlluminationData.Setting setting = LinkingUtil.getDefaultOffSettingOfLight(device);
            if (setting != null) {
                btn.setText(setting.getName(0).getName());
                updateLightOffSetting(setting.getId() & 0xFF);
            } else {
                btn.setText(getString(R.string.activity_device_not_selected));
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
            btn.setText(getString(R.string.activity_device_not_selected));
            return;
        }

        Integer patternId = getVibrationOffSetting();
        if (patternId == null) {
            setupDefaultOffSettingOfVibration(mDevice);
            return;
        }

        try {
            VibrationData data = new VibrationData(vibration);
            for (VibrationData.Setting setting : data.getPattern().getChildren()) {
                if ((setting.getId() & 0xFF) == patternId) {
                    btn.setText(setting.getName(0).getName());
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }

    private Integer getVibrationOffSetting() {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getVibrationOffSetting();
        return map == null ? null : map.get(mDevice.getBdAddress());
    }

    private void setupDefaultOffSettingOfVibration(final LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_vibration_off);
        if (btn != null) {
            VibrationData.Setting setting = LinkingUtil.getDefaultOffSettingOfVibration(device);
            if (setting != null) {
                btn.setText(setting.getName(0).getName());
                updateVibrationOffSetting(setting.getId() & 0xFF);
            } else {
                btn.setText(getString(R.string.activity_device_not_selected));
            }
        }
    }

    private void setLightButton() {
        View view = findViewById(R.id.select_light_off);
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    showLEDPattern();
                }
            });
        }

        Button onBtn = (Button) findViewById(R.id.on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickLED(true);
                }
            });
        }

        Button offBtn = (Button) findViewById(R.id.off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickLED(false);
                }
            });
        }
    }

    private void showDisconnectDevice() {
        String title = getString(R.string.fragment_device_error_title);
        String message = getString(R.string.fragment_device_error_disconnect, mDevice.getDisplayName());
        String positive = getString(R.string.fragment_device_error_negative);
        ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(title, message, positive, null);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "error");
    }

    private void showLEDPattern() {
        byte[] illumination = mDevice.getIllumination();
        if (illumination == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_device_not_support_led), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final IlluminationData data = new IlluminationData(illumination);

            AlertDialog.Builder builder = new AlertDialog.Builder(LinkingDeviceActivity.this);
            final String[] items = new String[data.getPattern().getChildren().length];
            for (int i = 0; i < data.getPattern().getChildren().length; i++) {
                items[i] = data.getPattern().getChild(i).getName(0).getName();
            }
            builder.setTitle(getString(R.string.activity_device_pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    IlluminationData.Setting selectedPattern = data.getPattern().getChild(which);
                    Button btn = ((Button) findViewById(R.id.select_light_off));
                    if (btn != null) {
                        btn.setText(selectedPattern.getName(0).getName());
                    }
                    updateLightOffSetting(selectedPattern.getId() & 0xFF);
                }
            });
            builder.create().show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_device_not_support_led), Toast.LENGTH_SHORT).show();
        }
    }

    private void setVibrationButton() {
        View view = findViewById(R.id.select_vibration_off);
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    showVibrationPattern();
                }
            });
        }

        Button onBtn = (Button) findViewById(R.id.vibration_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickVibration(true);
                }
            });
        }

        Button offBtn = (Button) findViewById(R.id.vibration_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickVibration(false);
                }
            });
        }
    }

    private void showVibrationPattern() {
        byte[] vibration = mDevice.getVibration();
        if (vibration == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_device_not_support_vibration), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final VibrationData data = new VibrationData(vibration);

            AlertDialog.Builder builder = new AlertDialog.Builder(LinkingDeviceActivity.this);
            final String[] items = new String[data.getPattern().getChildren().length];
            for (int i = 0; i < data.getPattern().getChildren().length; i++) {
                items[i] = data.getPattern().getChild(i).getName(0).getName();
            }
            builder.setTitle(getString(R.string.activity_device_pattern_list)).setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VibrationData.Setting selectedPattern = data.getPattern().getChild(which);
                    Button btn = (Button) findViewById(R.id.select_vibration_off);
                    if (btn != null) {
                        btn.setText(selectedPattern.getName(0).getName());
                    }
                    updateVibrationOffSetting(selectedPattern.getId() & 0xFF);
                }
            });
            builder.create().show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_device_not_support_vibration), Toast.LENGTH_SHORT).show();
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

        updateDataText(LinkingSensorData.SensorType.GYRO, 0, 0, 0, 0);
        updateDataText(LinkingSensorData.SensorType.ACCELERATION, 0, 0, 0, 0);
        updateDataText(LinkingSensorData.SensorType.COMPASS, 0, 0, 0, 0);
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

    private void setProximityButton() {
        Button onBtn = (Button) findViewById(R.id.proximity_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickProximity(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.proximity_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickProximity(false);
                }
            });
        }
    }

    private void setBatteryButton() {
        Button onBtn = (Button) findViewById(R.id.battery_sensor_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickBatterySensor(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.battery_sensor_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickBatterySensor(false);
                }
            });
        }
    }

    private void setTemperatureButton() {
        Button onBtn = (Button) findViewById(R.id.battery_temperature_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickTemperatureSensor(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.battery_temperature_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickTemperatureSensor(false);
                }
            });
        }
    }


    private void setHumidityButton() {
        Button onBtn = (Button) findViewById(R.id.battery_humidity_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickHumiditySensor(true);
                }
            });
        }
        Button offBtn = (Button) findViewById(R.id.battery_humidity_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickHumiditySensor(false);
                }
            });
        }
    }

    private void updateLightOffSetting(final Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getLightOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setLightOffSetting(map);
    }

    private void updateVibrationOffSetting(final Integer id) {
        PreferenceUtil util = PreferenceUtil.getInstance(getApplicationContext());
        Map<String, Integer> map = util.getVibrationOffSetting();
        if (map == null) {
            return;
        }
        map.put(mDevice.getBdAddress(), id);
        util.setVibrationOffSetting(map);
    }

    private void updateDataText(final LinkingSensorData.SensorType type, final float x, final float y, final float z, final long time) {
        switch (type) {
            case GYRO: {
                TextView view = (TextView) findViewById(R.id.gyro_text);
                if (view != null) {
                    view.setText(makeParamText(x, y, z, time));
                }
            }   break;
            case ACCELERATION: {
                TextView view = (TextView) findViewById(R.id.acceleration_text);
                if (view != null) {
                    view.setText(makeParamText(x, y, z, time));
                }
            }   break;
            case COMPASS: {
                TextView view = (TextView) findViewById(R.id.orientation_text);
                if (view != null) {
                    view.setText(makeParamText(x, y, z, time));
                }
            }   break;
            default:
                break;
        }
    }

    private void updateKeyEvent(final int deviceId, final int uniqueId, final int keyCode) {
        if (deviceId != mDevice.getModelId() || uniqueId != mDevice.getUniqueId()) {
            return;
        }

        TextView tv = (TextView) findViewById(R.id.linking_button_id);
        if (tv != null) {
            String text = tv.getText().toString();
            text = mDevice.getBdAddress() + ": ButtonId[" + keyCode + "]\r\n" + text;
            tv.setText(text);
        }
    }

    private void updateRange(final LinkingDeviceManager.Range range) {
        TextView tv = (TextView) findViewById(R.id.activity_device_proximity_text);
        if (tv != null) {
            switch (range) {
                case IMMEDIATE:
                    tv.setText(getString(R.string.activity_device_proximity_immediate));
                    break;
                case NEAR:
                    tv.setText(getString(R.string.activity_device_proximity_near));
                    break;
                case FAR:
                    tv.setText(getString(R.string.activity_device_proximity_far));
                    break;
                default:
                    tv.setText(getString(R.string.activity_device_proximity_unknown));
                    break;
            }
        }
    }

    private void updateBattery(final boolean lowBatteryFlag, final float batteryLevel) {
        TextView tv = (TextView) findViewById(R.id.battery_text);
        if (tv != null) {
            tv.setText(String.valueOf(batteryLevel));
        }
    }

    private void updateTemperature(final float temperature) {
        TextView tv = (TextView) findViewById(R.id.temperature_text);
        if (tv != null) {
            tv.setText(String.valueOf(temperature));
        }
    }

    private void updateHumidity(final float humidity) {
        TextView tv = (TextView) findViewById(R.id.temperature_text);
        if (tv != null) {
            tv.setText(String.valueOf(humidity));
        }
    }

    private String makeParamText(final float x, final float y, final float z, final long time) {
        return getString(R.string.activity_device_sensor_value, x, y, z, time);
    }

    private void onClickLED(boolean isOn) {
        if (!mDevice.isLED()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_led), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.sendLEDCommand(mDevice, isOn);
    }

    private void onClickVibration(final boolean isOn) {
        if (!mDevice.isVibration()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_vibration), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.sendVibrationCommand(mDevice, isOn);
    }

    private void onClickSensor(final boolean isOn) {
        if (!mDevice.isGyro() && !mDevice.isAcceleration() && !mDevice.isCompass()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_sensor), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.startSensor(mDevice);
        } else {
            mgr.stopSensor(mDevice);
        }
    }

    private void onClickBatterySensor(final boolean isOn) {
        if (!mDevice.isBattery()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_battery), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.startBattery(mDevice);
        } else {
            mgr.stopBattery(mDevice);
        }
    }

    private void onClickTemperatureSensor(final boolean isOn) {
        if (!mDevice.isTemperature()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_temperature), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.startTemperature(mDevice);
        } else {
            mgr.stopTemperature(mDevice);
        }
    }


    private void onClickHumiditySensor(final boolean isOn) {
        if (!mDevice.isHumidity()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_humidity), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.startHumidity(mDevice);
        } else {
            mgr.stopHumidity(mDevice);
        }
    }

    private void onClickButtonId(final boolean isOn) {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.startKeyEvent(mDevice);
        } else {
            mgr.stopKeyEvent(mDevice);
        }
    }

    private void onClickProximity(final boolean isOn) {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.startRange(mDevice);
        } else {
            mgr.stopRange(mDevice);
        }
    }

    private LinkingDeviceManager.OnSensorListener mOnSensorListener = new LinkingDeviceManager.OnSensorListener() {
        @Override
        public void onChangeSensor(final LinkingDevice device, final LinkingSensorData sensor) {
            if (device.equals(mDevice)) {
                updateDataText(sensor.getType(), sensor.getX(), sensor.getY(), sensor.getZ(), sensor.getTime());
            }
        }
    };

    private LinkingDeviceManager.OnKeyEventListener mOnKeyEventListener = new LinkingDeviceManager.OnKeyEventListener() {
        @Override
        public void onKeyEvent(final LinkingDevice device, final int keyCode) {
            if (device.equals(mDevice)) {
                updateKeyEvent(device.getModelId(), device.getUniqueId(), keyCode);
            }
        }
    };

    private LinkingDeviceManager.OnRangeListener mOnRangeListener = new LinkingDeviceManager.OnRangeListener() {
        @Override
        public void onChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
            if (device.equals(mDevice)) {
                updateRange(range);
            }
        }
    };

    private LinkingDeviceManager.OnBatteryListener mOnBatteryListener = new LinkingDeviceManager.OnBatteryListener() {
        @Override
        public void onBattery(final LinkingDevice device, final boolean lowBatteryFlag, final float batteryLevel) {
            if (device.equals(mDevice)) {
                updateBattery(lowBatteryFlag, batteryLevel);
            }
        }
    };

    private LinkingDeviceManager.OnTemperatureListener mOnTemperatureListener = new LinkingDeviceManager.OnTemperatureListener() {
        @Override
        public void onTemperature(final LinkingDevice device, final float temperature) {
            if (device.equals(mDevice)) {
                updateTemperature(temperature);
            }
        }
    };

    private LinkingDeviceManager.OnHumidityListener mOnHumidityListener = new LinkingDeviceManager.OnHumidityListener() {
        @Override
        public void onHumidity(final LinkingDevice device, final float humidity) {
            if (device.equals(mDevice)) {
                updateHumidity(humidity);
            }
        }
    };

    private LinkingDeviceManager.OnConnectListener mOnConnectListener = new LinkingDeviceManager.OnConnectListener() {
        @Override
        public void onConnect(final LinkingDevice device) {
        }

        @Override
        public void onDisconnect(final LinkingDevice device) {
            if (device.equals(mDevice)) {
                showDisconnectDevice();
            }
        }
    };
}
