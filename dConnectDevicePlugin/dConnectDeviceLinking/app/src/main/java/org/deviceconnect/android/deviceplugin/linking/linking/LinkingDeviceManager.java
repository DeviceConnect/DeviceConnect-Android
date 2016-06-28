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

    private final List<OnConnectListener> mOnConnectListeners = new CopyOnWriteArrayList<>();
    private final List<OnKeyEventListener> mOnKeyEventListeners = new CopyOnWriteArrayList<>();
    private final List<OnSensorListener> mOnSensorListeners = new CopyOnWriteArrayList<>();
    private final List<OnRangeListener> mOnRangeListeners = new CopyOnWriteArrayList<>();

    private final List<LinkingDevice> mSensorDeviceHolders = new CopyOnWriteArrayList<>();
    private final List<LinkingDevice> mKeyEventDeviceHolders = new CopyOnWriteArrayList<>();
    private final List<LinkingDevice> mRangeDeviceHolders = new CopyOnWriteArrayList<>();

    public LinkingDeviceManager(final Context context) {
        mContext = context;
        startNotifyConnect();
    }

    public void destroy() {
        stopAllSensor();
        stopAllKeyEvent();
        stopAllRange();
        stopNotifyConnect();

        mOnConnectListeners.clear();
        mOnKeyEventListeners.clear();
        mOnSensorListeners.clear();
        mOnRangeListeners.clear();

        mSensorDeviceHolders.clear();
        mKeyEventDeviceHolders.clear();
        mRangeDeviceHolders.clear();
    }

    public List<LinkingDevice> getDevices() {
        List<DeviceInfo> deviceInfoList = new GetDeviceInformation(mContext).getInformation();

        List<LinkingDevice> devices = new ArrayList<>();
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
            devices.add(device);
        }
        return devices;
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

    public synchronized void startKeyEvent(final LinkingDevice device) {
        if (isStartKeyEvent(device)) {
            return;
        }

        mKeyEventDeviceHolders.add(device);

        if (mNotifyNotification == null) {
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

                    LinkingDevice device = findDeviceFromKeyHolders(deviceId, uniqueId);
                    if (device != null) {
                        notifyOnKeyEvent(device, keyCode);
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.w(TAG, "Not found a device.");
                        }
                    }
                }
            });
        }
    }

    public synchronized void stopKeyEvent(final LinkingDevice device) {

        mKeyEventDeviceHolders.remove(device);

        if (mNotifyNotification != null && mKeyEventDeviceHolders.isEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a key event.");
            }
            mNotifyNotification.release();
            mNotifyNotification = null;
        }
    }

    public synchronized void stopAllKeyEvent() {
        if (mNotifyNotification != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a key event.");
            }
            mNotifyNotification.release();
            mNotifyNotification = null;
        }
    }

    public boolean isStartKeyEvent(final LinkingDevice device) {
        return mNotifyNotification != null && mKeyEventDeviceHolders.contains(device);
    }

    public List<LinkingDevice> getStartedKeyEventDevices() {
        return mKeyEventDeviceHolders;
    }

    public synchronized void startRange(final LinkingDevice device) {
        if (isStartRange(device)) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyRange is already running.");
            }
            return;
        }

        mRangeDeviceHolders.add(device);

        if (mNotifyRange == null) {
            mNotifyRange = new NotifyRange(mContext, new NotifyRange.RangeInterface() {
                @Override
                public void onRangeChange() {
                    SharedPreferences preference = mContext.getSharedPreferences(Define.RangeInfo, Context.MODE_PRIVATE);
                    String bdAddress = preference.getString(LinkingUtil.BD_ADDRESS, "");
                    int range = preference.getInt(LinkingUtil.RANGE, -1);
                    int rangeSetting = preference.getInt(LinkingUtil.RANGE_SETTING, -1);
                    LinkingDevice device = findDeviceFromRangeHolders(bdAddress);
                    if (device != null) {
                        notifyOnChangeRange(device, Range.valueOf(rangeSetting, range));
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.w(TAG, "Not found a device.");
                        }
                    }
                }
            });
        }
    }

    public synchronized void stopRange(final LinkingDevice device) {

        mRangeDeviceHolders.remove(device);

        if (mNotifyRange != null && mRangeDeviceHolders.isEmpty()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a range event.");
            }
            mNotifyRange.release();
            mNotifyRange = null;
        }
    }

    public synchronized void stopAllRange() {
        if (mNotifyRange != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a range event.");
            }
            mNotifyRange.release();
            mNotifyRange = null;
        }
    }

    public List<LinkingDevice> getStartedRangeDevices() {
        return mSensorDeviceHolders;
    }

    public boolean isStartRange(final LinkingDevice device) {
        return mNotifyRange != null && mRangeDeviceHolders.contains(device);
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
            private final LinkingSensorData mSensorData = new LinkingSensorData();
            @Override
            public synchronized void onSensorData(final String bd, final int type,
                                                  final float x, final float y, final float z,
                                                  final byte[] originalData, final long time) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onSensorData:[" + bd + "] type:" + type + " time:" + time
                            + "( x: " + x + " y: " + y + " z: " + z + ")");
                }

                LinkingDevice device = findDeviceFromSensorHolders(bd);
                if (device == null) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not Found the device that address is " + bd);
                    }
                } else {
                    mSensorData.setBdAddress(bd);
                    mSensorData.setX(x);
                    mSensorData.setY(y);
                    mSensorData.setZ(z);
                    mSensorData.setOriginalData(originalData);
                    mSensorData.setType(LinkingSensorData.SensorType.valueOf(type));
                    mSensorData.setTime(time);
                    notifyOnChangeSensor(device, mSensorData);
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

    public List<LinkingDevice> getStartedSensorDevices() {
        return mSensorDeviceHolders;
    }

    public synchronized void stopAllSensor() {
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

    public void addKeyEventListener(final OnKeyEventListener listener) {
        mOnKeyEventListeners.add(listener);
    }

    public void removeKeyEventListener(final OnKeyEventListener listener) {
        mOnKeyEventListeners.remove(listener);
    }

    public void addSensorListener(final OnSensorListener listener) {
        mOnSensorListeners.add(listener);
    }

    public void removeSensorListener(final OnSensorListener listener) {
        mOnSensorListeners.remove(listener);
    }

    public void addRangeListener(final OnRangeListener listener) {
        mOnRangeListeners.add(listener);
    }

    public void removeRangeListener(final OnRangeListener listener) {
        mOnRangeListeners.remove(listener);
    }

    public void addConnectListener(final OnConnectListener listener) {
        mOnConnectListeners.add(listener);
    }

    public void removeConnectListener(final OnConnectListener listener) {
        mOnConnectListeners.remove(listener);
    }

    private LinkingDevice findDeviceFromSensorHolders(final String address) {
        for (LinkingDevice device : mSensorDeviceHolders) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private LinkingDevice findDeviceFromKeyHolders(final int deviceId, final int uniqueId) {
        for (LinkingDevice device : mKeyEventDeviceHolders) {
            if (device.getModelId() == deviceId && device.getUniqueId() == uniqueId) {
                return device;
            }
        }
        return null;
    }

    private LinkingDevice findDeviceFromRangeHolders(final String address) {
        for (LinkingDevice device : mRangeDeviceHolders) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private synchronized void startNotifyConnect() {
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

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@ NotifyConnect#onConnect");
                    Log.d(TAG, LinkingUtil.BD_ADDRESS + "=" + preference.getString(LinkingUtil.BD_ADDRESS, ""));
                    Log.d(TAG, LinkingUtil.DEVICE_NAME + "=" + preference.getString(LinkingUtil.DEVICE_NAME, ""));
                    Log.d(TAG, LinkingUtil.RECEIVE_TIME + "=" + preference.getLong(LinkingUtil.RECEIVE_TIME, -1));
                    Log.d(TAG, LinkingUtil.CAPABILITY + "=" + preference.getInt(LinkingUtil.CAPABILITY, -1));
                    Log.d(TAG, LinkingUtil.EX_SENSOR_TYPE + "=" + preference.getInt(LinkingUtil.EX_SENSOR_TYPE, -1));
                }

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

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@ NotifyConnect#onDisconnect");
                    Log.d(TAG, LinkingUtil.DEVICE_NAME + "=" + preference.getString(LinkingUtil.DEVICE_NAME, ""));
                    Log.d(TAG, LinkingUtil.RECEIVE_TIME + "=" + preference.getLong(LinkingUtil.RECEIVE_TIME, -1));
                }

                LinkingDevice device = findDeviceByDeviceId(deviceId, uniqueId);
                if (device != null) {
                    device.setIsConnected(false);
                    notifyDisconnect(device);
                }
            }
        });
    }

    private synchronized void stopNotifyConnect() {
        if (mNotifyConnect != null) {
            mNotifyConnect.release();
            mNotifyConnect = null;
        }
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
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Not exist pattern of Vibration. name=" + device.getDisplayName());
            }
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
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Not exist pattern of LED. name=" + device.getDisplayName());
            }
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

    private void notifyConnect(final LinkingDevice device) {
        for (OnConnectListener listener : mOnConnectListeners) {
            listener.onConnect(device);
        }
    }

    private void notifyDisconnect(final LinkingDevice device) {
        for (OnConnectListener listener : mOnConnectListeners) {
            listener.onDisconnect(device);
        }
    }

    private void notifyOnChangeSensor(final LinkingDevice device, final LinkingSensorData data) {
        for (OnSensorListener listener : mOnSensorListeners) {
            listener.onChangeSensor(device, data);
        }
    }

    private void notifyOnKeyEvent(final LinkingDevice device, final int keyCode) {
        for (OnKeyEventListener listener : mOnKeyEventListeners) {
            listener.onKeyEvent(device, keyCode);
        }
    }

    private void notifyOnChangeRange(final LinkingDevice device, final Range range) {
        for (OnRangeListener listener : mOnRangeListeners) {
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

    public interface OnConnectListener {
        void onConnect(LinkingDevice device);
        void onDisconnect(LinkingDevice device);
    }

    public interface OnRangeListener {
        void onChangeRange(LinkingDevice device, Range range);
    }

    public interface OnKeyEventListener {
        void onKeyEvent(LinkingDevice device, int keyCode);
    }

    public interface OnSensorListener {
        void onChangeSensor(LinkingDevice device, LinkingSensorData sensor);
    }
}
