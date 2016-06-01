/*
 LinkingBeaconManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.BatteryData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.GattData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.HumidityData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.LinkingBeacon;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.RawData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.TemperatureData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkingBeaconManager {
    private static final String TAG = "LinkingPlugIn";

    private static final int INTERVAL = 30 * 1000;

    private Context mContext;
    private int mScanState;
    private int mScanDetail;

    private ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mScheduledFuture;

    private final List<LinkingBeacon> mLinkingBeacons = Collections.synchronizedList(new ArrayList<LinkingBeacon>());

    private List<OnConnectListener> mOnConnectListeners = new ArrayList<>();
    private List<OnBeaconEventListener> mOnBeaconEventListeners = new ArrayList<>();
    private List<OnBeaconButtonEventListener> mOnBeaconButtonEventListeners = new ArrayList<>();

    private LinkingDBAdapter mDBAdapter;

    public LinkingBeaconManager(Context context) {
        mContext = context;
        mDBAdapter = new LinkingDBAdapter(context);
        mLinkingBeacons.addAll(mDBAdapter.queryBeacons());
    }

    public List<LinkingBeacon> getLinkingBeacons() {
        return mLinkingBeacons;
    }

    public void addOnConnectListener(OnConnectListener listener) {
        mOnConnectListeners.add(listener);
    }

    public void removeOnConnectListener(OnConnectListener listener) {
        mOnConnectListeners.remove(listener);
    }

    public void addOnBeaconEventListener(OnBeaconEventListener listener) {
        mOnBeaconEventListeners.add(listener);
    }

    public void removeOnBeaconEventListener(OnBeaconEventListener listener) {
        mOnBeaconEventListeners.remove(listener);
    }

    public void addOnBeaconButtonEventListener(OnBeaconButtonEventListener listener) {
        mOnBeaconButtonEventListeners.add(listener);
    }

    public void removeOnBeaconButtonEventListener(OnBeaconButtonEventListener listener) {
        mOnBeaconButtonEventListeners.remove(listener);
    }

    public void startBeaconScan() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#startBeaconScan");
        }

        Intent intent = new Intent();
        intent.setClassName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.beacon.BeaconService");
        intent.setAction(mContext.getPackageName() + ".sda.action.START_BEACON_SCAN");
        intent.putExtra(mContext.getPackageName() + ".sda.extra.SERVICE_ID", new int[] {1, 2, 3, 4, 5, 15});
        mContext.startService(intent);

        startCheckConnectionOfBeacon();
    }

    public void stopBeaconScan() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#stopBeaconScan");
        }

        Intent intent = new Intent();
        intent.setClassName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.beacon.BeaconService");
        intent.setAction(mContext.getPackageName() + ".sda.action.STOP_BEACON_SCAN");
        mContext.startService(intent);

        stopCheckConnectionOfBeacon();
    }

    public void destroy() {
        stopBeaconScan();
        mScheduledExecutorService.shutdown();
    }

    public LinkingBeacon findBeacon(int extraId, int vendorId) {
        synchronized (mLinkingBeacons) {
            for (LinkingBeacon beacon : mLinkingBeacons) {
                if (beacon.getExtraId() == extraId &&
                        beacon.getVendorId() == vendorId) {
                    return beacon;
                }
            }
        }
        return null;
    }

    public void removeBeacon(LinkingBeacon beacon) {
        mLinkingBeacons.remove(beacon);
        mDBAdapter.delete(beacon);
    }

    public void onReceivedBeacon(Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@ LinkingBeaconManager#onReceivedBeacon");
            Log.i(TAG, "intent=" + intent);
        }

        String action = intent.getAction();
        if (LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT.equals(action)) {
            parseBeaconResult(intent);
        } else if (LinkingBeaconUtil.ACTION_BEACON_SCAN_STATE.equals(action)) {
            parseBeaconScanState(intent);
        }
    }

    private void parseBeaconScanState(Intent intent) {
        mScanState = intent.getIntExtra(LinkingBeaconUtil.SCAN_STATE, 0);
        mScanDetail = intent.getIntExtra(LinkingBeaconUtil.DETAIL, 0);

        if (mScanState == LinkingBeaconUtil.RESULT_OK) {
            if (mScanDetail == LinkingBeaconUtil.DETAIL_TIMEOUT) {
                startBeaconScan();
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "mScanState: : " + mScanState);
            Log.d(TAG, "detail: " + mScanDetail);
        }
    }

    private void parseBeaconResult(Intent intent) {
        int extraId = intent.getIntExtra(LinkingBeaconUtil.EXTRA_ID, -1);
        int vendorId = intent.getIntExtra(LinkingBeaconUtil.VENDOR_ID, -1);
        int version = intent.getIntExtra(LinkingBeaconUtil.VERSION, -1);

        LinkingBeacon beacon = findBeacon(extraId, vendorId);
        if (beacon == null) {
            beacon = new LinkingBeacon();
            beacon.setExtraId(extraId);
            beacon.setVendorId(vendorId);
            beacon.setVersion(version);
            mLinkingBeacons.add(beacon);

            if (!mDBAdapter.insertBeacon(beacon)) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Failed to insert LinkingBeacon.");
                }
            }
        }

        parseGattData(intent, beacon);
        parseBatteryData(intent, beacon);
        parseAtmosphericPressureData(intent, beacon);
        parseHumidityData(intent, beacon);
        parseTemperatureData(intent, beacon);
        parseRawData(intent, beacon);
        parseButtonId(intent, beacon);

        if (!beacon.isOnline()) {
            beacon.setOnline(true);
            for (OnConnectListener listener : mOnConnectListeners) {
                listener.onConnected(beacon);
            }
        }

        if (beacon.isOnline()) {
            for (OnBeaconEventListener listener : mOnBeaconEventListeners) {
                listener.onNotify(beacon);
            }
        }
    }

    private void parseGattData(Intent intent, LinkingBeacon beacon) {
        GattData gatt = beacon.getGattData();
        if (gatt == null) {
            gatt = new GattData();
            beacon.setGattData(gatt);
        }

        long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
        int rssi = intent.getIntExtra(LinkingBeaconUtil.RSSI, 0);
        int txPower = intent.getIntExtra(LinkingBeaconUtil.TX_POWER, 0);
        int distance = intent.getIntExtra(LinkingBeaconUtil.DISTANCE, -1);

        gatt.setTimeStamp(timeStamp);
        gatt.setRssi(rssi);
        gatt.setTxPower(txPower);
        gatt.setDistance(distance);

        mDBAdapter.insertGatt(beacon, gatt);
    }

    private void parseAtmosphericPressureData(Intent intent, LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.ATMOSPHERIC_PRESSURE)) {
            AtmosphericPressureData atm = beacon.getAtmosphericPressureData();
            if (atm == null) {
                atm = new AtmosphericPressureData();
                beacon.setAtmosphericPressureData(atm);
            }

            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            float value = intent.getFloatExtra(LinkingBeaconUtil.ATMOSPHERIC_PRESSURE, 0);
            atm.setTimeStamp(timeStamp);
            atm.setValue(value);

            mDBAdapter.insertAtmosphericPressure(beacon, atm);
        }
    }

    private void parseBatteryData(Intent intent, LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.LOW_BATTERY) ||
                intent.getExtras().containsKey(LinkingBeaconUtil.BATTERY_LEVEL)) {
            BatteryData battery = beacon.getBatteryData();
            if (battery == null) {
                battery = new BatteryData();
                beacon.setBatteryData(battery);
            }

            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            boolean lowBattery = intent.getBooleanExtra(LinkingBeaconUtil.LOW_BATTERY, false);
            float level = intent.getFloatExtra(LinkingBeaconUtil.BATTERY_LEVEL, 0);
            battery.setTimeStamp(timeStamp);
            battery.setLowBatteryFlag(lowBattery);
            battery.setLevel(level);

            mDBAdapter.insertBattery(beacon, battery);
        }
    }

    private void parseHumidityData(Intent intent, LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.HUMIDITY)) {
            HumidityData humidity = beacon.getHumidityData();
            if (humidity == null) {
                humidity = new HumidityData();
                beacon.setHumidityData(humidity);
            }

            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            float value = intent.getFloatExtra(LinkingBeaconUtil.HUMIDITY, 0);
            humidity.setTimeStamp(timeStamp);
            humidity.setValue(value);

            mDBAdapter.insertHumidity(beacon, humidity);
        }
    }

    private void parseTemperatureData(Intent intent, LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.TEMPERATURE)) {
            TemperatureData temp = beacon.getTemperatureData();
            if (temp == null) {
                temp = new TemperatureData();
                beacon.setTemperatureData(temp);
            }

            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            float value = intent.getFloatExtra(LinkingBeaconUtil.TEMPERATURE, 0);
            temp.setTimeStamp(timeStamp);
            temp.setValue(value);

            mDBAdapter.insertTemperature(beacon, temp);
        }
    }

    private void parseRawData(Intent intent, LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.RAW_DATA)) {
            RawData raw = beacon.getRawData();
            if (raw == null) {
                raw = new RawData();
                beacon.setRawData(raw);
            }
            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            int value = intent.getIntExtra(LinkingBeaconUtil.RAW_DATA, 0);
            raw.setTimeStamp(timeStamp);
            raw.setValue(value);

            mDBAdapter.insertRawData(beacon, raw);
        }
    }

    private void parseButtonId(Intent intent, LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.BUTTON_ID)) {
            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            int keyCode = intent.getIntExtra(LinkingBeaconUtil.BUTTON_ID, -1);
            for (OnBeaconButtonEventListener listener: mOnBeaconButtonEventListeners) {
                listener.onClickButton(beacon, keyCode, timeStamp);
            }
        }
    }

    private synchronized void startCheckConnectionOfBeacon() {
        if (mScheduledFuture == null) {
            mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    checkConnectionOfBeacon();
                }
            }, INTERVAL / 2, INTERVAL / 2, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void stopCheckConnectionOfBeacon() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(true);
            mScheduledFuture = null;
        }
    }

    private void checkConnectionOfBeacon() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#checkConnectionOfBeacon");
        }

        synchronized (mLinkingBeacons) {
            long now = System.currentTimeMillis();
            for (LinkingBeacon beacon : mLinkingBeacons) {
                if (beacon.isOnline()) {
                    if (now - beacon.getTimeStamp() > INTERVAL) {
                        beacon.setOnline(false);
                        for (OnConnectListener listener : mOnConnectListeners) {
                            listener.onDisconnected(beacon);
                        }
                    }
                }
            }
        }
    }

    public interface OnConnectListener {
        void onConnected(LinkingBeacon beacon);
        void onDisconnected(LinkingBeacon beacon);
    }

    public interface OnBeaconEventListener {
        void onNotify(LinkingBeacon beacon);
    }

    public interface OnBeaconButtonEventListener {
        void onClickButton(LinkingBeacon beacon, int keyCode, long timeStamp);
    }
}
