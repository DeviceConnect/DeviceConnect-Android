/*
 LinkingManagerImpl.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.ControlSensorData;
import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.DeviceInfo;
import com.nttdocomo.android.sdaiflib.GetDeviceInformation;
import com.nttdocomo.android.sdaiflib.NotifyNotification;
import com.nttdocomo.android.sdaiflib.NotifyRange;
import com.nttdocomo.android.sdaiflib.SendNotification;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkingManagerImpl implements LinkingManager {

    private static final String TAG = "LinkingPlugIn";

    private RangeListener mRangeListener;
    private KeyEventListener mKeyEventListener;
    private Map<String, SensorListener> mSensorListenerMap = new HashMap<>();
    private Context mContext;
    private NotifyRange mNotifyRange;
    private NotifySensorData mNotifySensor;
    private NotifyNotification mNotifyNotification;

    public LinkingManagerImpl(Context context) {
        mContext = context;
    }

    @Override
    public List<LinkingDevice> getDevices() {
        List<LinkingDevice> list = new ArrayList<>();
        for (DeviceInfo info : new GetDeviceInformation(mContext).getInformation()) {
            LinkingDevice device = new LinkingDevice();
            device.setBdAddress(info.getBdaddress());
            device.setIsConnected(info.getState() == 1);
            device.setModelId(info.getModelId());
            device.setIllumination(info.getIllumination());
            device.setName(info.getName());
            device.setUniqueId(info.getUniqueId());
            device.setVibration(info.getVibration());
            if (hasSensor(info)) {
                device.setSensor(new Object());
            }
            device.setDisplayName("Linking:" + info.getName() + "(" + info.getBdaddress() + ")");
            device.setFeature(info.getFeature());
            list.add(device);
        }
        return list;
    }

    @Override
    public void sendNotification(LinkingDevice device, LinkingNotification notification) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle(notification.getTitle());
        notify.setText(notification.getDetail());
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        notify.send();
    }

    @Override
    public void setConnectListener(ConnectListener listener) {

    }

    @Override
    public synchronized void setRangeListener(RangeListener listener) {
        mRangeListener = listener;
        if (mRangeListener == null) {
            return;
        }
        if (mNotifyRange == null) {
            mNotifyRange = new NotifyRange(mContext, new NotifyRange.RangeInterface() {
                @Override
                public void onRangeChange() {
                    SharedPreferences preference = mContext.getSharedPreferences(Define.RangeInfo, Context.MODE_PRIVATE);
                    String bdAddress = preference.getString("BD_ADDRESS", "");
                    int range = preference.getInt("RANGE", -1);//0:in,1:out
                    int rangeSetting = preference.getInt("RANGE_SETTING", -1);//1:immidiate(3m),2:near(10m),3:far(20m)
                    for (LinkingDevice device : getDevices()) {
                        if (device.getBdAddress().equals(bdAddress)) {
                            Range r = Range.UNKNOWN;
                            switch (rangeSetting) {
                                case 1:
                                    if (range == 0) {
                                        r = Range.IMMEDIATE;
                                    } else {
                                        r = Range.NEAR;
                                    }
                                    break;
                                case 2:
                                    if (range == 0) {
                                        r = Range.NEAR;
                                    } else {
                                        r = Range.FAR;
                                    }
                                    break;
                                case 3:
                                    if (range == 0) {
                                        r = Range.FAR;
                                    } else {
                                        r = Range.UNKNOWN;
                                    }
                                    break;
                                default:
                                    break;
                            }
                            notifyOnChangeRange(device, r);
                            break;
                        }
                    }
                }
            });
        } else {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyRange is already exists.");
            }
        }
    }

    @Override
    public void setKeyEventListener(KeyEventListener listener) {
        mKeyEventListener = listener;
        if (mKeyEventListener == null) {
            return ;
        }
        if (mNotifyNotification == null) {
            mNotifyNotification = new NotifyNotification(mContext, new NotifyNotification.NotificationInterface() {
                @Override
                public void onNotify() {
                    SharedPreferences preference = mContext.getSharedPreferences(Define.NotificationInfo, Context.MODE_PRIVATE);
                    int deviceId = preference.getInt("DEVICE_ID", -1);
                    int uniqueId = preference.getInt("DEVICE_UID", -1);
                    int keyCode = preference.getInt("DEVICE_BUTTON_ID", -1);

                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "NotifyNotification.NotificationInterface#onNotify");
                        Log.i(TAG, "deviceId:" + deviceId);
                        Log.i(TAG, "uniqueId:" + uniqueId);
                        Log.i(TAG, "keyCode:" + keyCode);
                    }

                    for (LinkingDevice device : getDevices()) {
                        if (device.getModelId() == deviceId && device.getUniqueId() == uniqueId) {
                            notifyOnKeyEvent(device, keyCode);
                            break;
                        }
                    }
                }
            });
        } else {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyNotification is already exists.");
            }
        }
    }

    @Override
    public void setSensorListener(LinkingDevice device, SensorListener listener) {
        mSensorListenerMap.put(device.getBdAddress(), listener);
        if (listener == null) {
            stopSensors(device.getBdAddress());

            boolean isEmpty = true;
            for (Map.Entry<String, SensorListener> entry : mSensorListenerMap.entrySet()) {
                if (entry.getValue() != null) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                if (mNotifySensor != null) {
                    mNotifySensor.release();
                }
            }

            return;
        }
        if (mNotifySensor == null) {
            mNotifySensor = new NotifySensorData(mContext, new ControlSensorData.SensorDataInterface() {

                @Override
                public void onSensorData(String bd, int type, float x, float y, float z, byte[] originalData, long time) {
                    LinkingDevice target = null;
                    for (LinkingDevice device : getDevices()) {
                        if (device.getBdAddress().equals(bd)) {
                            target = device;
                            break;
                        }
                    }
                    if (target == null) {
                        return;
                    }
                    LinkingSensorData data = new LinkingSensorData();
                    data.setBdAddress(bd);
                    data.setX(x);
                    data.setY(y);
                    data.setZ(z);
                    data.setOriginalData(originalData);
                    if (type == 0) {
                        data.setType(LinkingSensorData.SensorType.GYRO);
                    } else if (type == 1) {
                        data.setType(LinkingSensorData.SensorType.ACCELERATION);
                    } else if (type == 2) {
                        data.setType(LinkingSensorData.SensorType.COMPASS);
                    } else {
                        data.setType(LinkingSensorData.SensorType.EXTENDS);
                    }
                    data.setTime(time);
                    notifyOnChangeSensor(target, data);
                }

                @Override
                public void onStopSensor(String bd, int type, int reason) {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "onStopSensor");
                        Log.i(TAG, "bd:" + bd);
                        Log.i(TAG, "type:" + type);
                        Log.i(TAG, "reason:" + reason);
                    }
                }

            });
        }
        startAllSensor(device.getBdAddress());
    }

    private void startAllSensor(final String address) {
        startSensor(address, 0);
    }

    private void startSensor(String address, int type) {
        Intent intent = new Intent(mContext, ConfirmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.BD_ADDRESS", address);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.SENSOR_TYPE", type);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.SENSOR_INTERVAL", 100);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.SENSOR_DURATION", -1);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.X_THRESHOLD", 0.0F);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.Y_THRESHOLD", 0.0F);
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.Z_THRESHOLD", 0.0F);
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void stopSensors(String address) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Manager:stopSensors:address:" + address);
        }
        Intent intent = new Intent(mContext.getPackageName() + ".sda.action.STOP_SENSOR");
        intent.setComponent(new ComponentName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.RequestReceiver"));
        intent.putExtra(mContext.getPackageName() + ".sda.extra.BD_ADDRESS", address);
        try {
            mContext.sendBroadcast(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendLEDCommand(LinkingDevice device, boolean on) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle("title");
        notify.setText("test");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        setVibration(notify, device);
        if (!on) {
            setIllumination(notify, device);
        }
        notify.send();
    }

    @Override
    public void sendVibrationCommand(LinkingDevice device, boolean on) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle("title");
        notify.setText("test");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        if (!on) {
            setVibration(notify, device);
        }
        setIllumination(notify, device);
        notify.send();
    }

    private void setVibration(SendNotification notify, LinkingDevice device) {
        if (device.getVibration() == null) {
            return;
        }

        Integer patternId = getVibrationOffSetting(device);
        if (patternId == null) {
            return;
        }
        byte pattern = (byte) (patternId & 0xFF);
        byte[] vibration = new byte[2];
        vibration[0] = 0x10;
        vibration[1] = pattern;
        notify.setVibration(vibration);
    }

    private Integer getVibrationOffSetting(LinkingDevice device) {
        Map<String, Integer> map = PreferenceUtil.getInstance(mContext).getVibrationOffSetting();
        if (map == null || map.get(device.getBdAddress()) == null) {
            return LinkingUtil.getDefaultOffSettingOfLightId(device);
        }
        return map.get(device.getBdAddress());
    }

    private void setIllumination(SendNotification notify, LinkingDevice device) {
        if (device.getIllumination() == null) {
            return;
        }

        Integer patternId = getLightOffSetting(device);
        if (patternId == null) {
            return;
        }
        byte pattern = (byte) (patternId & 0xFF);
        byte[] illumination = new byte[4];
        illumination[0] = 0x20;
        illumination[1] = pattern;
        illumination[2] = 0x30;
        illumination[3] = 0x01;//default color id
        notify.setIllumination(illumination);
    }

    private Integer getLightOffSetting(LinkingDevice device) {
        Map<String, Integer> map = PreferenceUtil.getInstance(mContext).getLightOffSetting();
        if (map == null || map.get(device.getBdAddress()) == null) {
            return LinkingUtil.getDefaultOffSettingOfLightId(device);
        }
        return map.get(device.getBdAddress());
    }

    private boolean hasSensor(DeviceInfo deviceInfo) {
        int feature = deviceInfo.getFeature();
        final int LED = 1;
        final int GYRO = LED << 1;
        final int ACCE = LED << 2;
        final int CONP = LED << 3;
        if ((feature & GYRO) == GYRO) {
            return true;
        } else if ((feature & ACCE) == ACCE) {
            return true;
        } else if ((feature & CONP) == CONP) {
            return true;
        }
        return false;
    }

    private boolean hasLED(DeviceInfo deviceInfo) {
        int feature = deviceInfo.getFeature();
        final int LED = 1;
        return (feature & LED) == LED;
    }

    private synchronized void notifyOnChangeSensor(LinkingDevice device, LinkingSensorData data) {
        if (mSensorListenerMap.get(device.getBdAddress()) == null) {
            return;
        }
        mSensorListenerMap.get(device.getBdAddress()).onChangeSensor(device, data);
    }

    private synchronized void notifyOnKeyEvent(LinkingDevice device, int keyCode) {
        if (mKeyEventListener == null) {
            return;
        }
        mKeyEventListener.onKeyEvent(device, keyCode);
    }

    private synchronized void notifyOnChangeRange(LinkingDevice device, Range range) {
        if (mRangeListener == null) {
            return;
        }
        mRangeListener.onChangeRange(device, range);
    }
}
