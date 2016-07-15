package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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

public class LinkingDeviceManager {

    private static final String TAG = "LinkingPlugIn";

    private Context mContext;

    private LinkingNotifyConnect mNotifyConnect;
    private LinkingNotifyRange mNotifyRange;
    private LinkingNotifyNotification mNotifyKey;
    private LinkingNotifySensor mNotifySensor;

    private List<Intent> mRequestSensorType = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public LinkingDeviceManager(final Context context) {
        mContext = context;

        mNotifyConnect = new LinkingNotifyConnect(context, this);
        mNotifyRange = new LinkingNotifyRange(context);
        mNotifyKey = new LinkingNotifyNotification(context);
        mNotifySensor = new LinkingNotifySensor(context, this);
    }

    public Context getContext() {
        return mContext;
    }

    public void destroy() {
        mNotifyRange.release();
        mNotifyKey.release();
        mNotifySensor.release();
        mNotifyConnect.release();
        mRequestSensorType.clear();
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

    public synchronized void startConfirmActivity(final Intent request) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AAAAAAAAAAAA startConfirmActivity");
        }

        mRequestSensorType.add(request);
        if (mRequestSensorType.size() == 1) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContext.startActivity(request);
                }
            });
        }
    }

    public synchronized void onConfirmActivityResult(final Intent request) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AAAAAAAAAAAA onConfirmActivityResult");
        }

        if (mRequestSensorType.size() > 0) {
            mRequestSensorType.remove(0);
        }

        if (mRequestSensorType.size() > 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContext.startActivity(mRequestSensorType.get(0));
                }
            });
        }
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

    public void enableListenButtonEvent(final LinkingDevice device, final OnButtonEventListener listener) {
        mNotifyKey.enableListenNotification(device, listener);
    }

    public void disableListenButtonEvent(final LinkingDevice device, final OnButtonEventListener listener) {
        mNotifyKey.disableListenNotification(device, listener);
    }

    public void enableListenRange(final LinkingDevice device, final OnRangeListener listener) {
        mNotifyRange.enableListenRange(device, listener);
    }

    public void disableListenRange(final LinkingDevice device, final OnRangeListener listener) {
        mNotifyRange.disableListenRange(device, listener);
    }

    public void enableListenSensor(final LinkingDevice device, final OnSensorListener listener) {
        mNotifySensor.enableListenOrientation(device, listener);
    }

    public void disableListenSensor(final LinkingDevice device, final OnSensorListener listener) {
        mNotifySensor.disableListenOrientation(device, listener);
    }

    public void enableListenBattery(final LinkingDevice device, final OnBatteryListener listener) {
        mNotifySensor.enableListenBattery(device, listener);
    }

    public void disableListenBattery(final LinkingDevice device, final OnBatteryListener listener) {
        mNotifySensor.disableListenBattery(device, listener);
    }

    public void enableListenHumidity(final LinkingDevice device, final OnHumidityListener listener) {
        mNotifySensor.enableListenHumidity(device, listener);
    }

    public void disableListenHumidity(final LinkingDevice device, final OnHumidityListener listener) {
        mNotifySensor.disableListenHumidity(device, listener);
    }

    public void enableListenTemperature(final LinkingDevice device, final OnTemperatureListener listener) {
        mNotifySensor.enableListenTemperature(device, listener);
    }

    public void disableListenTemperature(final LinkingDevice device, final OnTemperatureListener listener) {
        mNotifySensor.disableListenTemperature(device, listener);
    }

    public boolean sendLEDCommand(final LinkingDevice device, final boolean on) {
        if (device == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "device is null.");
            }
            return false;
        }

        if (!device.isSupportLED()) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Not support led. name=" + device.getDisplayName());
            }
            return false;
        }
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle("title");
        notify.setText("linking");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(new int[] {device.getUniqueId()});
        setVibration(notify, getVibrationOffSetting(device));
        if (on) {
            setIllumination(notify, getLightPatternSetting(device), getLightColorSetting(device));
        } else {
            setIllumination(notify, getLightOffSetting(device), getLightColorSetting(device));
        }
        int result = notify.send();
        return (result != ErrorCode.RESULT_OK);
    }

    public boolean sendVibrationCommand(final LinkingDevice device, final boolean on) {
        if (device == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "device is null.");
            }
            return false;
        }

        if (!device.isSupportLED()) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Not support vibration. name=" + device.getDisplayName());
            }
            return false;
        }
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.dconnect_icon);
        notify.setTitle("title");
        notify.setText("linking");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(new int[] {device.getUniqueId()});
        if (on) {
            setVibration(notify, getVibrationOnSetting(device));
        } else {
            setVibration(notify, getVibrationOffSetting(device));
        }
        setIllumination(notify, getLightOffSetting(device), getLightColorSetting(device));
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
        notify.setDeviceUID(new int[] {device.getUniqueId()});
        int result = notify.send();
        return (result != ErrorCode.RESULT_OK);
    }

    public void addConnectListener(final OnConnectListener listener) {
        mNotifyConnect.addListener(listener);
    }

    public void removeConnectListener(final OnConnectListener listener) {
        mNotifyConnect.removeListener(listener);
    }

    private void setVibration(final SendNotification notify, final Integer patternId) {
        if (patternId == null) {
            return;
        }
        byte pattern = (byte) (patternId & 0xFF);
        byte[] vibration = new byte[2];
        vibration[0] = 0x10;
        vibration[1] = pattern;
        notify.setVibration(vibration);
    }

    private void setIllumination(final SendNotification notify, final Integer patternId, final Integer colorId) {
        if (patternId == null || colorId == null) {
            return;
        }
        byte pattern = (byte) (patternId & 0xFF);
        byte color = (byte) (colorId & 0xFF);
        byte[] illumination = new byte[4];
        illumination[0] = 0x20;
        illumination[1] = pattern;
        illumination[2] = 0x30;
        illumination[3] = color;
        notify.setIllumination(illumination);
    }

    private Integer getVibrationOnSetting(final LinkingDevice device) {
        return PreferenceUtil.getInstance(mContext).getVibrationOnSetting(device.getBdAddress());
    }

    private Integer getVibrationOffSetting(final LinkingDevice device) {
        return PreferenceUtil.getInstance(mContext).getVibrationOffSetting(device.getBdAddress());
    }

    private Integer getLightColorSetting(final LinkingDevice device) {
        return PreferenceUtil.getInstance(mContext).getLEDColorSetting(device.getBdAddress());
    }

    private Integer getLightPatternSetting(final LinkingDevice device) {
        return PreferenceUtil.getInstance(mContext).getLEDPatternSetting(device.getBdAddress());
    }

    private Integer getLightOffSetting(final LinkingDevice device) {
        return PreferenceUtil.getInstance(mContext).getLEDOffSetting(device.getBdAddress());
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

    public interface OnButtonEventListener {
        void onButtonEvent(LinkingDevice device, int keyCode);
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
