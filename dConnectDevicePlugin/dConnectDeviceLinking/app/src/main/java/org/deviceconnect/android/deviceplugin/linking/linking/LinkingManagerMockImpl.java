/*
 LinkingManagerMockImpl.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkingManagerMockImpl implements LinkingManager {

    private ConnectListener mConnectListener;
    private RangeListener mRangeListener;
    private SensorListener mSensorListener;

    private ScheduledExecutorService mConnectService;
    private ScheduledExecutorService mRangeService;
    private ScheduledExecutorService mSensorService;

    private Map<String, LinkingDevice> mMockDevices = new ConcurrentHashMap<String, LinkingDevice>() {{
        LinkingDevice device1 = new LinkingDevice();
        device1.setDisplayName("minimum");
        device1.setName("device1");
        device1.setBdAddress("FF:FF:FF:FF:FF:FF");
        device1.setIsConnected(true);
        put("device1", device1);

        LinkingDevice device2 = new LinkingDevice();
        device2.setDisplayName("hasLED");
        device2.setName("device2");
        device2.setIllumination(new byte[4]);
        device2.setBdAddress("FF:FF:FF:FF:FF:FE");
        device2.setIsConnected(true);
        put("device2", device2);

        LinkingDevice device3 = new LinkingDevice();
        device3.setDisplayName("hasSensor");
        device3.setName("device3");
        device3.setSensor(new Object());
        device3.setBdAddress("FF:FF:FF:FF:FF:FD");
        device3.setIsConnected(true);
        put("device3", device3);

        LinkingDevice device4 = new LinkingDevice();
        device4.setDisplayName("hasVibration");
        device4.setName("device4");
        device4.setVibration(new byte[4]);
        device4.setBdAddress("FF:FF:FF:FF:FF:FC");
        device4.setIsConnected(true);
        put("device4", device4);

        LinkingDevice device5 = new LinkingDevice();
        device5.setDisplayName("hasAll");
        device5.setName("device5");
        device5.setIllumination(new byte[4]);
        device5.setVibration(new byte[4]);
        device5.setSensor(new Object());
        device5.setBdAddress("FF:FF:FF:FF:FF:FB");
        device5.setIsConnected(true);
        put("device5", device5);

    }};

    @Override
    public List<LinkingDevice> getDevices() {
        List<LinkingDevice> devices = new ArrayList<>();
        for (Map.Entry<String, LinkingDevice> entry : mMockDevices.entrySet()) {
            devices.add(entry.getValue());
        }
        return devices;
    }

    @Override
    public void sendNotification(LinkingDevice device, LinkingNotification notification) {

    }

    @Override
    public synchronized void setConnectListener(final ConnectListener listener) {
        mConnectListener = listener;
        if (listener == null && mConnectService != null) {
            mConnectService.shutdownNow();
            mConnectService = null;
            return;
        }

        mConnectService = Executors.newSingleThreadScheduledExecutor();
        mConnectService.scheduleAtFixedRate(new Runnable() {
            int mockCount = 0;

            @Override
            public void run() {
                if (mockCount == 4) {
                    mockCount = 0;
                }
                switch (mockCount) {
                    case 0:
                        notifyOnChangeConnect(mMockDevices.get("device1"), true);
                        break;
                    case 1:
                        notifyOnChangeConnect(mMockDevices.get("device2"), false);
                        break;
                    case 2:
                        notifyOnChangeConnect(mMockDevices.get("device3"), true);
                        break;
                    case 3:
                        notifyOnChangeConnect(mMockDevices.get("device4"), false);
                        break;
                    default:
                }
                mockCount++;
            }
        }, 0, 2, TimeUnit.SECONDS);
    }


    @Override
    public synchronized void setRangeListener(RangeListener listener) {
        mRangeListener = listener;
        if (listener == null && mRangeService != null) {
            mRangeService.shutdownNow();
            mRangeService = null;
            return;
        }

        mRangeService = Executors.newSingleThreadScheduledExecutor();
        mRangeService.scheduleAtFixedRate(new Runnable() {
            int mockCount = 0;

            @Override
            public void run() {
                if (mockCount == 4) {
                    mockCount = 0;
                }
                switch (mockCount) {
                    case 0:
                        notifyOnChangeRange(mMockDevices.get("device1"), Range.IMMEDIATE);
                        notifyOnChangeRange(mMockDevices.get("device2"), Range.IMMEDIATE);
                        notifyOnChangeRange(mMockDevices.get("device3"), Range.IMMEDIATE);
                        notifyOnChangeRange(mMockDevices.get("device4"), Range.IMMEDIATE);
                        notifyOnChangeRange(mMockDevices.get("device5"), Range.IMMEDIATE);
                        break;
                    case 1:
                        notifyOnChangeRange(mMockDevices.get("device1"), Range.NEAR);
                        notifyOnChangeRange(mMockDevices.get("device2"), Range.NEAR);
                        notifyOnChangeRange(mMockDevices.get("device3"), Range.NEAR);
                        notifyOnChangeRange(mMockDevices.get("device4"), Range.NEAR);
                        notifyOnChangeRange(mMockDevices.get("device5"), Range.NEAR);
                        break;
                    case 2:
                        notifyOnChangeRange(mMockDevices.get("device1"), Range.FAR);
                        notifyOnChangeRange(mMockDevices.get("device2"), Range.FAR);
                        notifyOnChangeRange(mMockDevices.get("device3"), Range.FAR);
                        notifyOnChangeRange(mMockDevices.get("device4"), Range.FAR);
                        notifyOnChangeRange(mMockDevices.get("device5"), Range.FAR);
                        break;
                    case 3:
                        notifyOnChangeRange(mMockDevices.get("device1"), Range.UNKNOWN);
                        notifyOnChangeRange(mMockDevices.get("device2"), Range.UNKNOWN);
                        notifyOnChangeRange(mMockDevices.get("device3"), Range.UNKNOWN);
                        notifyOnChangeRange(mMockDevices.get("device4"), Range.UNKNOWN);
                        notifyOnChangeRange(mMockDevices.get("device5"), Range.UNKNOWN);
                        break;
                    default:
                }
                mockCount++;
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void setSensorListener(LinkingDevice device, SensorListener listener) {
        mSensorListener = listener;
        if (listener == null && mSensorService != null) {
            mSensorService.shutdownNow();
            mSensorService = null;
            return;
        }
        mSensorService = Executors.newSingleThreadScheduledExecutor();
        mSensorService.scheduleAtFixedRate(new Runnable() {
            int mockCount = 0;

            @Override
            public void run() {
                if (mockCount == 5) {
                    mockCount = 0;
                }
                switch (mockCount) {
                    case 0: {
                        LinkingSensorData sensor = new LinkingSensorData();
                        notifyOnChangeSensor(mMockDevices.get("device1"), sensor);
                    }
                    break;
                    case 1: {
                        LinkingSensorData sensor = new LinkingSensorData();
                        notifyOnChangeSensor(mMockDevices.get("device2"), sensor);
                    }
                    break;
                    case 2: {
                        LinkingSensorData sensor = new LinkingSensorData();
                        notifyOnChangeSensor(mMockDevices.get("device3"), sensor);
                    }
                    break;
                    case 3: {
                        LinkingSensorData sensor = new LinkingSensorData();
                        notifyOnChangeSensor(mMockDevices.get("device4"), sensor);
                    }
                    break;
                    case 4: {
                        LinkingSensorData sensor = new LinkingSensorData();
                        notifyOnChangeSensor(mMockDevices.get("device5"), sensor);
                    }
                    break;
                    default:
                }
                mockCount++;
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void sendLEDCommand(LinkingDevice device, boolean on) {

    }

    @Override
    public void sendVibrationCommand(LinkingDevice device, boolean on) {

    }

    private synchronized void notifyOnChangeConnect(LinkingDevice device, boolean isConnected) {
        if (mConnectListener == null) {
            return;
        }
        if (isConnected) {
            mConnectListener.onConnect(device);
        } else {
            mConnectListener.onDisconnect(device);
        }
    }

    private synchronized void notifyOnChangeRange(LinkingDevice device, Range range) {
        if (mRangeListener == null) {
            return;
        }
        mRangeListener.onChangeRange(device, range);
    }

    private synchronized void notifyOnChangeSensor(LinkingDevice device, LinkingSensorData sensor) {
        if (mSensorListener == null) {
            return;
        }
        mSensorListener.onChangeSensor(device, sensor);
    }

}
