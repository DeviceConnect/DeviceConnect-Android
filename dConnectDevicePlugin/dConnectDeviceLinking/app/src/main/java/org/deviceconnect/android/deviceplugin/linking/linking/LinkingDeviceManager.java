package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.DeviceInfo;
import com.nttdocomo.android.sdaiflib.ErrorCode;
import com.nttdocomo.android.sdaiflib.GetDeviceInformation;
import com.nttdocomo.android.sdaiflib.SendNotification;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinkingDeviceManager {

    private static final String TAG = "LinkingPlugIn";

    private Context mContext;

    private LinkingNotifyConnect mNotifyConnect;
    private LinkingNotifyRange mNotifyRange;
    private LinkingNotifyKey mNotifyKey;
    private LinkingNotifySensor mNotifySensor;

    public LinkingDeviceManager(final Context context) {
        mContext = context;

        mNotifyConnect = new LinkingNotifyConnect(context, this);
        mNotifyRange = new LinkingNotifyRange(context);
        mNotifyKey = new LinkingNotifyKey(context);
        mNotifySensor = new LinkingNotifySensor(context);
    }

    public void destroy() {
        mNotifyRange.release();
        mNotifyKey.release();
        mNotifySensor.release();
        mNotifyConnect.release();
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
            device.setDisplayName("Linking:" + info.getName() + " (" + info.getBdaddress() + ")");
            device.setFeature(info.getFeature());
            device.setExSensorType(info.getExSensorType());
            devices.add(device);

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "#### " + device);
            }
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

    public void startKeyEvent(final LinkingDevice device) {
        mNotifyKey.start(device);
    }

    public void stopKeyEvent(final LinkingDevice device) {
        mNotifyKey.stop(device);
    }

    public void startRange(final LinkingDevice device) {
        mNotifyRange.start(device);
    }

    public void stopRange(final LinkingDevice device) {
        mNotifyRange.stop(device);
    }

    public void startSensor(final LinkingDevice device) {
        mNotifySensor.startOrientation(device);
    }

    public void stopSensor(final LinkingDevice device) {
        mNotifySensor.stopOrientation(device);
    }

    public boolean isStartedSensor(final LinkingDevice device) {
        return mNotifySensor.containsOrientation(device);
    }

    public void startBattery(final LinkingDevice device) {
        mNotifySensor.startBattery(device);
    }

    public void stopBattery(final LinkingDevice device) {
        mNotifySensor.stopBattery(device);
    }

    public void startHumidity(final LinkingDevice device) {
        mNotifySensor.startHumidity(device);
    }

    public void stopHumidity(final LinkingDevice device) {
        mNotifySensor.stopHumidity(device);
    }

    public void startTemperature(final LinkingDevice device) {
        mNotifySensor.startTemperature(device);
    }

    public void stopTemperature(final LinkingDevice device) {
        mNotifySensor.stopTemperature(device);
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
        mNotifyKey.addListener(listener);
    }

    public void removeKeyEventListener(final OnKeyEventListener listener) {
        mNotifyKey.removeListener(listener);
    }

    public void addSensorListener(final OnSensorListener listener) {
        mNotifySensor.addSensorListener(listener);
    }

    public void removeSensorListener(final OnSensorListener listener) {
        mNotifySensor.removeSensorListener(listener);
    }

    public void addRangeListener(final OnRangeListener listener) {
        mNotifyRange.addListener(listener);
    }

    public void removeRangeListener(final OnRangeListener listener) {
        mNotifyRange.removeListener(listener);
    }

    public void addConnectListener(final OnConnectListener listener) {
        mNotifyConnect.addListener(listener);
    }

    public void removeConnectListener(final OnConnectListener listener) {
        mNotifyConnect.removeListener(listener);
    }

    public void addBatteryListener(final OnBatteryListener listener) {
        mNotifySensor.addBatteryListener(listener);
    }

    public void removeBatteryListener(final OnBatteryListener listener) {
        mNotifySensor.removeBatteryListener(listener);
    }

    public void addHumidityListener(final OnHumidityListener listener) {
        mNotifySensor.addHumidityListener(listener);
    }

    public void removeHumidityListener(final OnHumidityListener listener) {
        mNotifySensor.removeHumidityListener(listener);
    }

    public void addTemperatureListener(final OnTemperatureListener listener) {
        mNotifySensor.addTemperatureListener(listener);
    }

    public void removeTemperatureListener(final OnTemperatureListener listener) {
        mNotifySensor.removeTemperatureListener(listener);
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

    public interface OnBatteryListener {
        void onBattery(LinkingDevice device, boolean lowBatteryFlag, float batteryLevel);
    }

    public interface OnHumidityListener {
        void onHumidity(LinkingDevice device, float humidity);
    }

    public interface OnTemperatureListener {
        void onTemperature(LinkingDevice device, float temperature);
    }
}
