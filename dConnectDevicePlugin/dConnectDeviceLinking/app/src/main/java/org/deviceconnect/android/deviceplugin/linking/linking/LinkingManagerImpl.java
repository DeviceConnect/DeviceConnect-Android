/*
 LinkingManagerImpl.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.DeviceInfo;
import com.nttdocomo.android.sdaiflib.GetDeviceInformation;
import com.nttdocomo.android.sdaiflib.NotifyRange;
import com.nttdocomo.android.sdaiflib.SendNotification;

import org.deviceconnect.android.deviceplugin.linking.R;
import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinkingManagerImpl implements LinkingManager {

    private RangeListener mRangeListener;
    private Context mContext;
    private NotifyRange mNotifyRange;

    public LinkingManagerImpl(Context context) {
        mContext = context;
    }

    @Override
    public List<LinkingDevice> getDevices() {
        Log.i("Linking", "getDevices()");
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
        notify.setIcon(R.mipmap.ic_launcher);
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
            }

            );
        }
    }

    @Override
    public void setSensorListener(SensorListener listener) {

    }

    @Override
    public void sendLEDCommand(LinkingDevice device, boolean on) {
        SendNotification notify = new SendNotification(mContext);
        notify.setDispNameEn("Linking Device Plug-in");
        notify.setDispNameJa("Linking Device Plug-in");
        notify.setIcon(R.mipmap.ic_launcher);
        notify.setTitle("title");
        notify.setText("test");
        notify.setDeviceID(device.getModelId());
        notify.setDeviceUID(device.getUniqueId());
        if (!on) {
            setIllumination(notify, device);
        }
        notify.send();
    }

    @Override
    public void sendVibrationCommand(LinkingDevice device, boolean on) {

    }

    private void setIllumination(SendNotification notify, LinkingDevice device) {
        Log.i("LinkingSample", "setIllumination");
        Map<String, Integer> map = PreferenceUtil.getInstance(mContext).getLightOffSetting();
        if (map == null) {
            return;
        }
        Integer patternId = map.get(device.getBdAddress());
        if (patternId == null) {
            return;
        }
        Log.i("LinkingSample", "patternId:" + patternId);
        byte pattern = (byte) (patternId & 0xFF);
        byte[] illumination = new byte[4];
        illumination[0] = 0x20;
        illumination[1] = pattern;
        illumination[2] = 0x30;
        illumination[3] = 0x01;//default color id
        notify.setIllumination(illumination);
        Log.i("LinkingSample", "illumination:" + ByteUtil.binaryToHex(illumination));
    }

    private boolean hasSensor(DeviceInfo deviceInfo) {
        int feature = deviceInfo.getFeature();
        final int LED = 1;
        final int GYRO = LED << 1;
        final int ACCE = LED << 2;
        final int CONP = LED << 3;
        if ((feature & GYRO) == GYRO) {
            return true;
        }
        if ((feature & ACCE) == ACCE) {
            return true;
        }
        if ((feature & CONP) == CONP) {
            return true;
        }
        return false;
    }

    private boolean hasLED(DeviceInfo deviceInfo) {
        int feature = deviceInfo.getFeature();
        final int LED = 1;
        return (feature & LED) == LED;
    }

    private synchronized void notifyOnChangeRange(LinkingDevice device, Range range) {
        if (mRangeListener == null) {
            return;
        }
        mRangeListener.onChangeRange(device, range);
    }

}
