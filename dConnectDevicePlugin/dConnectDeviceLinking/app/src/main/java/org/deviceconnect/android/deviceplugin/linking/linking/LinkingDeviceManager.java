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
import com.nttdocomo.android.sdaiflib.ErrorCode;
import com.nttdocomo.android.sdaiflib.GetDeviceInformation;
import com.nttdocomo.android.sdaiflib.NotifyConnect;
import com.nttdocomo.android.sdaiflib.NotifyNotification;
import com.nttdocomo.android.sdaiflib.NotifyRange;
import com.nttdocomo.android.sdaiflib.SendNotification;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class LinkingDeviceManager {

    private static final String TAG = "LinkingPlugIn";

    private Context mContext;
    private NotifyRange mNotifyRange;
    private NotifySensorData mNotifySensor;
    private NotifyNotification mNotifyNotification;
    private NotifyConnect mNotifyConnect;

    private final List<ConnectListener> mConnectListeners = new CopyOnWriteArrayList<>();
    private final List<KeyEventListener> mKeyEventListeners = new CopyOnWriteArrayList<>();
    private final List<SensorListener> mSensorListeners = new CopyOnWriteArrayList<>();
    private final List<RangeListener> mRangeListeners = new CopyOnWriteArrayList<>();

    private List<LinkingDevice> mSensorDeviceHolders = new ArrayList<>();

    public LinkingDeviceManager(final Context context) {
        mContext = context;
    }

    public void destroy() {
        stopAllSensor();
        stopKeyEvent();
        stopRange();
        stopNotifyConnect();

        mConnectListeners.clear();
        mKeyEventListeners.clear();
        mSensorListeners.clear();
        mRangeListeners.clear();
    }

    public List<LinkingDevice> getDevices() {
        List<DeviceInfo> deviceInfoList = new GetDeviceInformation(mContext).getInformation();

        List<LinkingDevice> list = new ArrayList<>();
        for (DeviceInfo info : deviceInfoList) {
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
            device.setDisplayName("Linking:" + info.getName() + " (" + info.getBdaddress() + ")");
            device.setFeature(info.getFeature());
            list.add(device);
        }
        return list;
    }

    public LinkingDevice findDeviceByDeviceId(final int deviceId, final int uniqueId) {
        for (LinkingDevice device : getDevices()) {
            if (device.getModelId() == deviceId && device.getUniqueId() == uniqueId) {
                return device;
            }
        }
        return null;
    }

    public LinkingDevice findDeviceByBdAddress(final String address) {
        if (address == null) {
            return null;
        }

        for (LinkingDevice device : getDevices()) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    public synchronized void startNotifyConnect() {
        if (mNotifyConnect != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyConnect is already running.");
            }
            return;
        }

        mNotifyConnect = new NotifyConnect(mContext, new NotifyConnect.ConnectInterface() {
            @Override
            public void onConnect() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.ConnectInfo, Context.MODE_PRIVATE);
                int deviceId = preference.getInt(LinkingUtil.DEVICE_ID, -1);
                int uniqueId = preference.getInt(LinkingUtil.DEVICE_UID, -1);
                LinkingDevice device = findDeviceByDeviceId(deviceId, uniqueId);
                if (device != null) {
                    notifyConnect(device);
                }
            }

            @Override
            public void onDisconnect() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.DisconnectInfo, Context.MODE_PRIVATE);
                int deviceId = preference.getInt(LinkingUtil.DEVICE_ID, -1);
                int uniqueId = preference.getInt(LinkingUtil.DEVICE_UID, -1);
                LinkingDevice device = findDeviceByDeviceId(deviceId, uniqueId);
                if (device != null) {
                    device.setIsConnected(false);
                    notifyDisconnect(device);
                }
            }
        });
    }

    public synchronized void stopNotifyConnect() {
        if (mNotifyConnect != null) {
            mNotifyConnect.release();
            mNotifyConnect = null;
        }
    }

    public synchronized void startKeyEvent() {
        if (isStartKeyEvent()) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "NotifyNotification is already running.");
            }
            return;
        }

        mNotifyNotification = new NotifyNotification(mContext, new NotifyNotification.NotificationInterface() {
            @Override
            public void onNotify() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.NotificationInfo, Context.MODE_PRIVATE);
                int deviceId = preference.getInt(LinkingUtil.DEVICE_ID, -1);
                int uniqueId = preference.getInt(LinkingUtil.DEVICE_UID, -1);
                int keyCode = preference.getInt(LinkingUtil.DEVICE_BUTTON_ID, -1);

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "NotifyNotification.NotificationInterface#onNotify");
                    Log.i(TAG, "deviceId:" + deviceId);
                    Log.i(TAG, "uniqueId:" + uniqueId);
                    Log.i(TAG, "keyCode:" + keyCode);
                }

                LinkingDevice device = findDeviceByDeviceId(deviceId, uniqueId);
                if (device != null) {
                    notifyOnKeyEvent(device, keyCode);
                }
            }
        });
    }

    public synchronized void stopKeyEvent() {
        if (mNotifyNotification != null) {
            mNotifyNotification.release();
            mNotifyNotification = null;
        }
    }

    public boolean isStartKeyEvent() {
        return mNotifyNotification != null;
    }

    public synchronized void startRange() {
        if (isStartRange()) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyRange is already running.");
            }
            return;
        }

        mNotifyRange = new NotifyRange(mContext, new NotifyRange.RangeInterface() {
            @Override
            public void onRangeChange() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.RangeInfo, Context.MODE_PRIVATE);
                String bdAddress = preference.getString(LinkingUtil.BD_ADDRESS, "");
                int range = preference.getInt(LinkingUtil.RANGE, -1);
                int rangeSetting = preference.getInt(LinkingUtil.RANGE_SETTING, -1);
                LinkingDevice device = findDeviceByBdAddress(bdAddress);
                if (device != null) {
                    notifyOnChangeRange(device, Range.valueOf(rangeSetting, range));
                }
            }
        });
    }

    public synchronized void stopRange() {
        if (mNotifyRange != null) {
            mNotifyRange.release();
            mNotifyRange = null;
        }
    }

    public boolean isStartRange() {
        return mNotifyRange != null;
    }

    public void startSensor(final LinkingDevice device) {
        startSensor(device, 100);
    }

    public synchronized void startSensor(final LinkingDevice device, int interval) {
        if (isStartSensor(device)) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, device.getDisplayName() + " sensor is already running.");
            }
            return;
        }

        if (interval <= 100) {
            interval = 100;
        }

        startAllSensor(device.getBdAddress(), interval);

        mSensorDeviceHolders.add(device);

        if (mNotifySensor != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifySensor is already running.");
            }
            return;
        }
        mNotifySensor = new NotifySensorData(mContext, new ControlSensorData.SensorDataInterface() {
            @Override
            public void onSensorData(final String bd, final int type,
                                     final float x, final float y, final float z,
                                     final byte[] originalData, final long time) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onSensorData bd:" + bd + " type:" + type + " time:" + time);
                    Log.i(TAG, "x: " + x + " y: " + y + " z: " + z);
                }

                LinkingDevice device = findDeviceByBdAddress(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    LinkingSensorData data = new LinkingSensorData();
                    data.setBdAddress(bd);
                    data.setX(x);
                    data.setY(y);
                    data.setZ(z);
                    data.setOriginalData(originalData);
                    data.setType(LinkingSensorData.SensorType.valueOf(type));
                    data.setTime(time);
                    notifyOnChangeSensor(device, data);
                }
            }

            @Override
            public void onStopSensor(final String bd, final int type, final int reason) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onStopSensor: type:[" + type + "] reason:[" + reason + "] bd: " + bd);
                }
            }
        });
    }

    public synchronized void stopSensor(final LinkingDevice device) {
        stopSensors(device.getBdAddress());

        mSensorDeviceHolders.remove(device);

        if (mSensorDeviceHolders.isEmpty() && mNotifySensor != null) {
            mNotifySensor.release();
            mNotifySensor = null;
        }
    }

    public boolean isStartSensor(final LinkingDevice device) {
        return mSensorDeviceHolders.contains(device);
    }

    public void stopAllSensor() {
        for (LinkingDevice device : mSensorDeviceHolders) {
            stopSensors(device.getBdAddress());
        }

        mSensorDeviceHolders.clear();

        if (mNotifySensor != null) {
            mNotifySensor.release();
            mNotifySensor = null;
        }
    }

    public boolean sendLEDCommand(final LinkingDevice device, final boolean on) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle("title");
        notify.setText("linking");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        setVibration(notify, device);
        if (!on) {
            setIllumination(notify, device);
        }
        int result = notify.send();
        return (result != ErrorCode.RESULT_OK);
    }

    public boolean sendVibrationCommand(final LinkingDevice device, final boolean on) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle("title");
        notify.setText("linking");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        if (!on) {
            setVibration(notify, device);
        }
        setIllumination(notify, device);
        int result = notify.send();
        return (result != ErrorCode.RESULT_OK);
    }

    public boolean sendNotification(final LinkingDevice device, final LinkingNotification notification) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle(notification.getTitle());
        notify.setText(notification.getDetail());
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        int result = notify.send();
        return (result != ErrorCode.RESULT_OK);
    }

    public void addKeyEventListener(final KeyEventListener listener) {
        mKeyEventListeners.add(listener);
    }

    public void removeKeyEventListener(final KeyEventListener listener) {
        mKeyEventListeners.remove(listener);
    }

    public void addSensorListener(final SensorListener listener) {
        mSensorListeners.add(listener);
    }

    public void removeSensorListener(final SensorListener listener) {
        mSensorListeners.remove(listener);
    }

    public void addRangeListener(final RangeListener listener) {
        mRangeListeners.add(listener);
    }

    public void removeRangeListener(final RangeListener listener) {
        mRangeListeners.remove(listener);
    }

    public void addConnectListener(final ConnectListener listener) {
        mConnectListeners.add(listener);
    }

    public void removeConnectListener(final ConnectListener listener) {
        mConnectListeners.remove(listener);
    }

    private void startAllSensor(final String address, final int interval) {
        startSensor(address, 0, interval);
    }

    private void startSensor(final String address, final int type, final int interval) {
        Intent intent = new Intent(mContext, ConfirmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(LinkingUtil.EXTRA_BD_ADDRESS, address);
        intent.putExtra(LinkingUtil.EXTRA_SENSOR_TYPE, type);
        intent.putExtra(LinkingUtil.EXTRA_SENSOR_INTERVAL, interval);
        intent.putExtra(LinkingUtil.EXTRA_SENSOR_DURATION, -1);
        intent.putExtra(LinkingUtil.EXTRA_X_THRESHOLD, 0.0F);
        intent.putExtra(LinkingUtil.EXTRA_Y_THRESHOLD, 0.0F);
        intent.putExtra(LinkingUtil.EXTRA_Z_THRESHOLD, 0.0F);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void stopSensors(final String address) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingDeviceManager:stopSensors:address:" + address);
        }

        Intent intent = new Intent(mContext.getPackageName() + ".sda.action.STOP_SENSOR");
        intent.setComponent(new ComponentName(LinkingUtil.PACKAGE_NAME, LinkingUtil.RECEIVER_NAME));
        intent.putExtra(mContext.getPackageName() + ".sda.extra.BD_ADDRESS", address);
        try {
            mContext.sendBroadcast(intent);
        } catch (ActivityNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void setVibration(final SendNotification notify, final LinkingDevice device) {
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

    private Integer getVibrationOffSetting(final LinkingDevice device) {
        Map<String, Integer> map = PreferenceUtil.getInstance(mContext).getVibrationOffSetting();
        if (map == null || map.get(device.getBdAddress()) == null) {
            return LinkingUtil.getDefaultOffSettingOfLightId(device);
        }
        return map.get(device.getBdAddress());
    }

    private void setIllumination(final SendNotification notify, final LinkingDevice device) {
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

    private Integer getLightOffSetting(final LinkingDevice device) {
        Map<String, Integer> map = PreferenceUtil.getInstance(mContext).getLightOffSetting();
        if (map == null || map.get(device.getBdAddress()) == null) {
            return LinkingUtil.getDefaultOffSettingOfLightId(device);
        }
        return map.get(device.getBdAddress());
    }

    private boolean hasSensor(final DeviceInfo deviceInfo) {
        int feature = deviceInfo.getFeature();
        if ((feature & LinkingDevice.GYRO) == LinkingDevice.GYRO) {
            return true;
        } else if ((feature & LinkingDevice.ACCELERATION) == LinkingDevice.ACCELERATION) {
            return true;
        } else if ((feature & LinkingDevice.COMPASS) == LinkingDevice.COMPASS) {
            return true;
        }
        return false;
    }

    private boolean hasLED(final DeviceInfo deviceInfo) {
        int feature = deviceInfo.getFeature();
        final int LED = 1;
        return (feature & LED) == LED;
    }

    private void notifyConnect(final LinkingDevice device) {
        for (ConnectListener listener : mConnectListeners) {
            listener.onConnect(device);
        }
    }

    private void notifyDisconnect(final LinkingDevice device) {
        for (ConnectListener listener : mConnectListeners) {
            listener.onDisconnect(device);
        }
    }

    private void notifyOnChangeSensor(final LinkingDevice device, final LinkingSensorData data) {
        for (SensorListener listener : mSensorListeners) {
            listener.onChangeSensor(device, data);
        }
    }

    private void notifyOnKeyEvent(final LinkingDevice device, final int keyCode) {
        for (KeyEventListener listener : mKeyEventListeners) {
            listener.onKeyEvent(device, keyCode);
        }
    }

    private void notifyOnChangeRange(final LinkingDevice device, final Range range) {
        for (RangeListener listener : mRangeListeners) {
            listener.onChangeRange(device, range);
        }
    }

    public enum Range {
        IMMEDIATE(1), NEAR(2), FAR(3), UNKNOWN(4);

        private int mValue;
        Range(int value) {
            mValue = value;
        }
        public int getValue() {
            return mValue;
        }

        public static Range valueOf(final int setting, final int range) {
            switch (setting) {
                case 1:
                    if (range == 0) {
                        return Range.IMMEDIATE;
                    } else {
                        return Range.NEAR;
                    }
                case 2:
                    if (range == 0) {
                        return Range.NEAR;
                    } else {
                        return Range.FAR;
                    }
                case 3:
                    if (range == 0) {
                        return Range.FAR;
                    } else {
                        return Range.UNKNOWN;
                    }
                default:
                    return Range.UNKNOWN;
            }
        }
    }

    public interface ConnectListener {
        void onConnect(LinkingDevice device);
        void onDisconnect(LinkingDevice device);
    }

    public interface RangeListener {
        void onChangeRange(LinkingDevice device, Range range);
    }

    public interface KeyEventListener {
        void onKeyEvent(LinkingDevice device, int keyCode);
    }

    public interface SensorListener {
        void onChangeSensor(LinkingDevice device, LinkingSensorData sensor);
    }
}
