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
import org.deviceconnect.android.deviceplugin.linking.linking.data.IlluminationData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDevice;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingSensorData;
import org.deviceconnect.android.deviceplugin.linking.linking.LinkingUtil;
import org.deviceconnect.android.deviceplugin.linking.linking.data.Setting;
import org.deviceconnect.android.deviceplugin.linking.linking.data.VibrationData;
import org.deviceconnect.android.deviceplugin.linking.setting.fragment.dialog.ConfirmationDialogFragment;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

public class LinkingDeviceActivity extends AppCompatActivity implements ConfirmationDialogFragment.OnDialogEventListener {
    private static final String TAG = "LinkingPlugIn";
    public static final String EXTRA_ADDRESS = "bdAddress";

    private LinkingDevice mDevice;
    private PreferenceUtil mUtil;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linking_device);

        mUtil = PreferenceUtil.getInstance(getApplicationContext());

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

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.addConnectListener(mOnConnectListener);
    }

    @Override
    protected void onPause() {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.removeConnectListener(mOnConnectListener);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.disableListenSensor(mDevice, mOnSensorListener);
        mgr.disableListenRange(mDevice, mOnRangeListener);
        mgr.disableListenButtonEvent(mDevice, mOnButtonEventListener);
        mgr.disableListenHumidity(mDevice, mOnHumidityListener);
        mgr.disableListenTemperature(mDevice, mOnTemperatureListener);
        mgr.disableListenBattery(mDevice, mOnBatteryListener);

        super.onDestroy();
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
    public void onPositiveClick(final DialogFragment fragment) {
        finish();
    }

    @Override
    public void onNegativeClick(final DialogFragment fragment) {

    }

    private LinkingDeviceManager getLinkingDeviceManager() {
        LinkingApplication app = (LinkingApplication) getApplication();
        return app.getLinkingDeviceManager();
    }

    private LinkingDevice getLinkingDeviceByAddress(final String address) {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        return mgr.findDeviceByBdAddress(address);
    }

    private void setupUI() {
        TextView tv = (TextView) findViewById(R.id.device_name);
        if (tv != null) {
            tv.setText(mDevice.getDisplayName());
        }
        setupLightOnSetting();
        setupLightOffSetting();
        setupVibrationOnSetting();
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

    private void setupLightOnSetting() {
        Button colorBtn = (Button) findViewById(R.id.select_light_on_color);
        if (colorBtn == null || mDevice == null) {
            return;
        }

        Button patternBtn = (Button) findViewById(R.id.select_light_on_pattern);
        if (patternBtn == null || mDevice == null) {
            return;
        }

        if (!mDevice.isSupportLED()) {
            colorBtn.setText(getString(R.string.activity_device_not_selected));
            colorBtn.setEnabled(false);
            return;
        }

        Integer colorId = mUtil.getLEDColorSetting(mDevice.getBdAddress());
        Integer patternId = mUtil.getLEDPatternSetting(mDevice.getBdAddress());
        if (colorId == null || patternId == null) {
            setupDefaultOnSettingOfLight(mDevice);
            return;
        }

        byte[] illumination = mDevice.getIllumination();
        try {
            IlluminationData data = new IlluminationData(illumination);
            for (Setting setting : data.getColor().getChildren()) {
                if ((setting.getId() & 0xFF) == colorId) {
                    colorBtn.setText(setting.getName(0).getName());
                }
            }
            for (Setting setting : data.getPattern().getChildren()) {
                if ((setting.getId() & 0xFF) == patternId) {
                    patternBtn.setText(setting.getName(0).getName());
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "", e);
            }
        }
    }
    private void setupLightOffSetting() {
        Button btn = (Button) findViewById(R.id.select_light_off);
        if (btn == null || mDevice == null) {
            return;
        }

        byte[] illumination = mDevice.getIllumination();
        if (illumination == null) {
            btn.setText(getString(R.string.activity_device_not_selected));
            btn.setEnabled(false);
            return;
        }

        Integer patternId = mUtil.getLEDOffSetting(mDevice.getBdAddress());
        if (patternId == null) {
            setupDefaultOffSettingOfLight(mDevice);
            return;
        }

        try {
            IlluminationData data = new IlluminationData(illumination);
            for (Setting setting : data.getPattern().getChildren()) {
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

    private void setupDefaultOnSettingOfLight(final LinkingDevice device) {
        Button patternBtn = (Button) findViewById(R.id.select_light_on_pattern);
        if (patternBtn != null) {
            Setting setting = LinkingUtil.getDefaultPatternSettingOfLight(device);
            if (setting != null) {
                patternBtn.setText(setting.getName(0).getName());
                updateLightPatternSetting(setting.getId() & 0xFF);
            } else {
                patternBtn.setText(getString(R.string.activity_device_not_selected));
            }
        }
        Button colorBtn = (Button) findViewById(R.id.select_light_on_color);
        if (colorBtn != null) {
            Setting setting = LinkingUtil.getDefaultColorSettingOfLight(device);
            if (setting != null) {
                colorBtn.setText(setting.getName(0).getName());
                updateLightColorSetting(setting.getId() & 0xFF);
            } else {
                colorBtn.setText(getString(R.string.activity_device_not_selected));
            }
        }

    }

    private void setupDefaultOffSettingOfLight(final LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_light_off);
        if (btn != null) {
            Setting setting = LinkingUtil.getDefaultOffSettingOfLight(device);
            if (setting != null) {
                btn.setText(setting.getName(0).getName());
                updateLightOffSetting(setting.getId() & 0xFF);
            } else {
                btn.setText(getString(R.string.activity_device_not_selected));
            }
        }
    }

    private void setupVibrationOnSetting() {
        Button btn = (Button) findViewById(R.id.select_vibration_on);
        if (btn == null || mDevice == null) {
            return;
        }

        if (!mDevice.isSupportVibration()) {
            btn.setText(getString(R.string.activity_device_not_selected));
            btn.setEnabled(false);
            return;
        }

        Integer patternId = mUtil.getVibrationOnSetting(mDevice.getBdAddress());
        if (patternId == null) {
            setupDefaultOnSettingOfVibration(mDevice);
            return;
        }

        byte[] vibration = mDevice.getVibration();
        try {
            VibrationData data = new VibrationData(vibration);
            for (Setting setting : data.getPattern().getChildren()) {
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
    private void setupVibrationOffSetting() {
        Button btn = (Button) findViewById(R.id.select_vibration_off);
        if (btn == null || mDevice == null) {
            return;
        }

        if (!mDevice.isSupportVibration()) {
            btn.setText(getString(R.string.activity_device_not_selected));
            btn.setEnabled(false);
            return;
        }

        Integer patternId = mUtil.getVibrationOffSetting(mDevice.getBdAddress());
        if (patternId == null) {
            setupDefaultOffSettingOfVibration(mDevice);
            return;
        }

        byte[] vibration = mDevice.getVibration();
        try {
            VibrationData data = new VibrationData(vibration);
            for (Setting setting : data.getPattern().getChildren()) {
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

    private void setupDefaultOnSettingOfVibration(final LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_vibration_on);
        if (btn != null) {
            Setting setting = LinkingUtil.getDefaultOnSettingOfVibration(device);
            if (setting != null) {
                btn.setText(setting.getName(0).getName());
                updateVibrationOnSetting(setting.getId() & 0xFF);
            } else {
                btn.setText(getString(R.string.activity_device_not_selected));
            }
        }
    }

    private void setupDefaultOffSettingOfVibration(final LinkingDevice device) {
        Button btn = (Button) findViewById(R.id.select_vibration_off);
        if (btn != null) {
            Setting setting = LinkingUtil.getDefaultOffSettingOfVibration(device);
            if (setting != null) {
                btn.setText(setting.getName(0).getName());
                updateVibrationOffSetting(setting.getId() & 0xFF);
            } else {
                btn.setText(getString(R.string.activity_device_not_selected));
            }
        }
    }

    private void setLightButton() {
        View offView = findViewById(R.id.select_light_off);
        if (offView != null) {
            offView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    showLEDPattern(new OnSelectedListener() {
                        @Override
                        public void onSelected(final String name, final Integer id) {
                            Button btn = ((Button) findViewById(R.id.select_light_off));
                            if (btn != null) {
                                btn.setText(name);
                            }
                            updateLightOffSetting(id);
                        }
                    });
                }
            });
            offView.setEnabled(mDevice.isSupportLED());
        }

        View colorView = findViewById(R.id.select_light_on_color);
        if (colorView != null) {
            colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLEDColor(new OnSelectedListener() {
                        @Override
                        public void onSelected(final String name, final Integer id) {
                            Button btn = ((Button) findViewById(R.id.select_light_on_color));
                            if (btn != null) {
                                btn.setText(name);
                            }
                            updateLightColorSetting(id);
                        }
                    });
                }
            });
            colorView.setEnabled(mDevice.isSupportLED());
        }

        View patternView = findViewById(R.id.select_light_on_pattern);
        if (patternView != null) {
            patternView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    showLEDPattern(new OnSelectedListener() {
                        @Override
                        public void onSelected(final String name, final Integer id) {
                            Button btn = ((Button) findViewById(R.id.select_light_on_pattern));
                            if (btn != null) {
                                btn.setText(name);
                            }
                            updateLightOffSetting(id);
                        }
                    });
                }
            });
            patternView.setEnabled(mDevice.isSupportLED());
        }

        Button onBtn = (Button) findViewById(R.id.on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickLED(true);
                }
            });
            onBtn.setEnabled(mDevice.isSupportLED());
        }

        Button offBtn = (Button) findViewById(R.id.off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickLED(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportLED());
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


    private void showLEDPattern(final OnSelectedListener listener) {
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
                    Setting selectedPattern = data.getPattern().getChild(which);

                    if (listener != null) {
                        listener.onSelected(selectedPattern.getName(0).getName(), selectedPattern.getId() & 0xFF);
                    }


                }
            });
            builder.create().show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_device_not_support_led), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLEDColor(final OnSelectedListener listener) {
        byte[] illumination = mDevice.getIllumination();
        if (illumination == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_device_not_support_led), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final IlluminationData data = new IlluminationData(illumination);

            AlertDialog.Builder builder = new AlertDialog.Builder(LinkingDeviceActivity.this);
            final String[] items = new String[data.getColor().getChildren().length];
            for (int i = 0; i < data.getColor().getChildren().length; i++) {
                items[i] = data.getColor().getChild(i).getName(0).getName();
            }
            builder.setTitle(getString(R.string.activity_device_color_list)).setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    Setting selectedColor = data.getColor().getChild(which);
                    if (listener != null) {
                        listener.onSelected(selectedColor.getName(0).getName(), selectedColor.getId() & 0xFF);
                    }
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
                    showVibrationPattern(new OnSelectedListener() {
                        @Override
                        public void onSelected(final String name, final Integer id) {
                            Button btn = (Button) findViewById(R.id.select_vibration_off);
                            if (btn != null) {
                                btn.setText(name);
                            }
                            updateVibrationOffSetting(id);
                        }
                    });
                }
            });
            view.setEnabled(mDevice.isSupportVibration());
        }

        View onView = findViewById(R.id.select_vibration_on);
        if (onView != null) {
            onView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVibrationPattern(new OnSelectedListener() {
                        @Override
                        public void onSelected(final String name, final Integer id) {
                            Button btn = (Button) findViewById(R.id.select_vibration_on);
                            if (btn != null) {
                                btn.setText(name);
                            }
                            updateVibrationOnSetting(id);
                        }
                    });
                }
            });
            onView.setEnabled(mDevice.isSupportVibration());
        }

        Button onBtn = (Button) findViewById(R.id.vibration_on);
        if (onBtn != null) {
            onBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickVibration(true);
                }
            });
            onBtn.setEnabled(mDevice.isSupportVibration());
        }

        Button offBtn = (Button) findViewById(R.id.vibration_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onClickVibration(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportVibration());
        }
    }

    private void showVibrationPattern(final OnSelectedListener listener) {
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
                    Setting selectedPattern = data.getPattern().getChild(which);
                    if (listener != null) {
                        listener.onSelected(selectedPattern.getName(0).getName(), selectedPattern.getId() & 0xFF);
                    }
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
            onBtn.setEnabled(mDevice.isSupportSensor());
        }
        Button offBtn = (Button) findViewById(R.id.sensor_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickSensor(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportSensor());
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
            onBtn.setEnabled(mDevice.isSupportButton());
        }
        Button offBtn = (Button) findViewById(R.id.button_id_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickButtonId(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportButton());
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
            onBtn.setEnabled(mDevice.isSupportBattery());
        }
        Button offBtn = (Button) findViewById(R.id.battery_sensor_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickBatterySensor(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportBattery());
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
            onBtn.setEnabled(mDevice.isSupportTemperature());
        }
        Button offBtn = (Button) findViewById(R.id.battery_temperature_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickTemperatureSensor(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportTemperature());
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
            onBtn.setEnabled(mDevice.isSupportHumidity());
        }
        Button offBtn = (Button) findViewById(R.id.battery_humidity_off);
        if (offBtn != null) {
            offBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickHumiditySensor(false);
                }
            });
            offBtn.setEnabled(mDevice.isSupportHumidity());
        }
    }

    private void updateLightOffSetting(final Integer id) {
        mUtil.setLightOffSetting(mDevice.getBdAddress(), id);
    }

    private void updateLightPatternSetting(final Integer id) {
        mUtil.setLEDPatternSetting(mDevice.getBdAddress(), id);
    }

    private void updateLightColorSetting(final Integer id) {
        mUtil.setLEDColorSetting(mDevice.getBdAddress(), id);
    }

    private void updateVibrationOnSetting(final Integer id) {
        mUtil.setVibrationOnSetting(mDevice.getBdAddress(), id);
    }

    private void updateVibrationOffSetting(final Integer id) {
        mUtil.setVibrationOffSetting(mDevice.getBdAddress(), id);
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
            tv.setText(getString(R.string.activity_device_unit_percent, batteryLevel));
        }
    }

    private void updateTemperature(final float temperature) {
        TextView tv = (TextView) findViewById(R.id.temperature_text);
        if (tv != null) {
            tv.setText(getString(R.string.activity_device_unit_c, temperature));
        }
    }

    private void updateHumidity(final float humidity) {
        TextView tv = (TextView) findViewById(R.id.humidity_text);
        if (tv != null) {
            tv.setText(getString(R.string.activity_device_unit_percent, humidity));
        }
    }

    private String makeParamText(final float x, final float y, final float z, final long time) {
        return getString(R.string.activity_device_sensor_value, x, y, z, time);
    }

    private void onClickLED(boolean isOn) {
        if (!mDevice.isSupportLED()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_led), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.sendLEDCommand(mDevice, isOn);
    }

    private void onClickVibration(final boolean isOn) {
        if (!mDevice.isSupportVibration()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_vibration), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        mgr.sendVibrationCommand(mDevice, isOn);
    }

    private void onClickSensor(final boolean isOn) {
        if (!mDevice.isSupportSensor()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_sensor), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.enableListenSensor(mDevice, mOnSensorListener);
        } else {
            mgr.disableListenSensor(mDevice, mOnSensorListener);
        }
    }

    private void onClickBatterySensor(final boolean isOn) {
        if (!mDevice.isSupportBattery()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_battery), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.enableListenBattery(mDevice, mOnBatteryListener);
        } else {
            mgr.disableListenBattery(mDevice, mOnBatteryListener);
        }
    }

    private void onClickTemperatureSensor(final boolean isOn) {
        if (!mDevice.isSupportTemperature()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_temperature), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.enableListenTemperature(mDevice, mOnTemperatureListener);
        } else {
            mgr.disableListenTemperature(mDevice, mOnTemperatureListener);
        }
    }


    private void onClickHumiditySensor(final boolean isOn) {
        if (!mDevice.isSupportHumidity()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_humidity), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.enableListenHumidity(mDevice, mOnHumidityListener);
        } else {
            mgr.disableListenHumidity(mDevice, mOnHumidityListener);
        }
    }

    private void onClickButtonId(final boolean isOn) {
        if (!mDevice.isSupportButton()) {
            Toast.makeText(this, getString(R.string.activity_device_not_support_button), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.enableListenButtonEvent(mDevice, mOnButtonEventListener);
        } else {
            mgr.disableListenButtonEvent(mDevice, mOnButtonEventListener);
        }
    }

    private void onClickProximity(final boolean isOn) {
        LinkingDeviceManager mgr = getLinkingDeviceManager();
        if (isOn) {
            mgr.enableListenRange(mDevice, mOnRangeListener);
        } else {
            mgr.disableListenRange(mDevice, mOnRangeListener);
        }
    }

    private LinkingDeviceManager.OnSensorListener mOnSensorListener = new LinkingDeviceManager.OnSensorListener() {
        @Override
        public void onChangeSensor(final LinkingDevice device, final LinkingSensorData sensor) {
            updateDataText(sensor.getType(), sensor.getX(), sensor.getY(), sensor.getZ(), sensor.getTime());
        }
    };

    private LinkingDeviceManager.OnButtonEventListener mOnButtonEventListener = new LinkingDeviceManager.OnButtonEventListener() {
        @Override
        public void onButtonEvent(final LinkingDevice device, final int keyCode) {
            updateKeyEvent(device.getModelId(), device.getUniqueId(), keyCode);
        }
    };

    private LinkingDeviceManager.OnRangeListener mOnRangeListener = new LinkingDeviceManager.OnRangeListener() {
        @Override
        public void onChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
            updateRange(range);
        }
    };

    private LinkingDeviceManager.OnBatteryListener mOnBatteryListener = new LinkingDeviceManager.OnBatteryListener() {
        @Override
        public void onBattery(final LinkingDevice device, final boolean lowBatteryFlag, final float batteryLevel) {
            updateBattery(lowBatteryFlag, batteryLevel);
        }
    };

    private LinkingDeviceManager.OnTemperatureListener mOnTemperatureListener = new LinkingDeviceManager.OnTemperatureListener() {
        @Override
        public void onTemperature(final LinkingDevice device, final float temperature) {
            updateTemperature(temperature);
        }
    };

    private LinkingDeviceManager.OnHumidityListener mOnHumidityListener = new LinkingDeviceManager.OnHumidityListener() {
        @Override
        public void onHumidity(final LinkingDevice device, final float humidity) {
            updateHumidity(humidity);
        }
    };

    private interface OnSelectedListener {
        void onSelected(String name, Integer id);
    }

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
