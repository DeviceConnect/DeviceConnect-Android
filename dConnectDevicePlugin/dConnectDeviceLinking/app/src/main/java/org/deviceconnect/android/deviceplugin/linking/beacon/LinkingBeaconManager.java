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
import org.deviceconnect.android.deviceplugin.linking.util.PreferenceUtil;

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
    private LinkingBeaconUtil.ScanMode mScanMode;

    private ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mScheduledFuture;

    private final List<LinkingBeacon> mLinkingBeacons = Collections.synchronizedList(new ArrayList<LinkingBeacon>());

    private List<OnBeaconConnectListener> mOnBeaconConnectListeners = new ArrayList<>();
    private List<OnBeaconEventListener> mOnBeaconEventListeners = new ArrayList<>();
    private List<OnBeaconButtonEventListener> mOnBeaconButtonEventListeners = new ArrayList<>();
    private List<OnBeaconProximityEventListener> mOnBeaconProximityEventListeners = new ArrayList<>();
    private List<OnBeaconBatteryEventListener> mOnBeaconBatteryEventListeners = new ArrayList<>();
    private List<OnBeaconAtmosphericPressureEventListener> mOnBeaconAtmosphericPressureEventListeners = new ArrayList<>();
    private List<OnBeaconHumidityEventListener> mOnBeaconHumidityEventListeners = new ArrayList<>();
    private List<OnBeaconTemperatureEventListener> mOnBeaconTemperatureEventListeners = new ArrayList<>();
    private List<OnBeaconRawDataEventListener> mOnBeaconRawDataEventListeners = new ArrayList<>();

    private LinkingDBAdapter mDBAdapter;

    public LinkingBeaconManager(final Context context) {
        mContext = context;
        mDBAdapter = new LinkingDBAdapter(context);
        mLinkingBeacons.addAll(mDBAdapter.queryBeacons());
        startCheckConnectionOfBeacon();
    }

    public List<LinkingBeacon> getLinkingBeacons() {
        return mLinkingBeacons;
    }

    public void addOnBeaconConnectListener(final OnBeaconConnectListener listener) {
        mOnBeaconConnectListeners.add(listener);
    }

    public void removeOnBeaconConnectListener(final OnBeaconConnectListener listener) {
        mOnBeaconConnectListeners.remove(listener);
    }

    public void addOnBeaconEventListener(final OnBeaconEventListener listener) {
        mOnBeaconEventListeners.add(listener);
    }

    public void removeOnBeaconEventListener(final OnBeaconEventListener listener) {
        mOnBeaconEventListeners.remove(listener);
    }

    public void addOnBeaconButtonEventListener(final OnBeaconButtonEventListener listener) {
        mOnBeaconButtonEventListeners.add(listener);
    }

    public void removeOnBeaconButtonEventListener(final OnBeaconButtonEventListener listener) {
        mOnBeaconButtonEventListeners.remove(listener);
    }

    public void addOnBeaconProximityEventListener(final OnBeaconProximityEventListener listener) {
        mOnBeaconProximityEventListeners.add(listener);
    }

    public void removeOnBeaconProximityEventListener(final OnBeaconProximityEventListener listener) {
        mOnBeaconProximityEventListeners.remove(listener);
    }

    public void addOnBeaconBatteryEventListener(final OnBeaconBatteryEventListener listener) {
        mOnBeaconBatteryEventListeners.add(listener);
    }

    public void addOnBeaconAtmosphericPressureEventListener(final OnBeaconAtmosphericPressureEventListener listener) {
        mOnBeaconAtmosphericPressureEventListeners.add(listener);
    }

    public void removeOnBeaconAtmosphericPressureEventListener(final OnBeaconAtmosphericPressureEventListener listener) {
        mOnBeaconAtmosphericPressureEventListeners.remove(listener);
    }

    public void addOnBeaconHumidityEventListener(final OnBeaconHumidityEventListener listener) {
        mOnBeaconHumidityEventListeners.add(listener);
    }

    public void removeOnBeaconHumidityEventListener(final OnBeaconHumidityEventListener listener) {
        mOnBeaconHumidityEventListeners.remove(listener);
    }

    public void addOnBeaconTemperatureEventListener(final OnBeaconTemperatureEventListener listener) {
        mOnBeaconTemperatureEventListeners.add(listener);
    }

    public void removeOnBeaconTemperatureEventListener(final OnBeaconTemperatureEventListener listener) {
        mOnBeaconTemperatureEventListeners.remove(listener);
    }

    public void addOnBeaconRawDataEventListener(final OnBeaconRawDataEventListener listener) {
        mOnBeaconRawDataEventListeners.add(listener);
    }

    public void removeOnBeaconRawDataEventListener(final OnBeaconRawDataEventListener listener) {
        mOnBeaconRawDataEventListeners.remove(listener);
    }

    public void startBeaconScan() {
        startBeaconScan(null);
    }

    public void startBeaconScan(final LinkingBeaconUtil.ScanMode scanMode) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#startBeaconScan");
        }
        mScanMode = scanMode;

        Intent intent = new Intent();
        intent.setClassName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.beacon.BeaconService");
        intent.setAction(mContext.getPackageName() + ".sda.action.START_BEACON_SCAN");
        intent.putExtra(mContext.getPackageName() + ".sda.extra.SERVICE_ID", new int[] {0, 1, 2, 3, 4, 5, 15});
        if (scanMode != null) {
           intent.putExtra(mContext.getPackageName() + ".sda.extra.SCAN_MODE", scanMode.getValue());
        }
        mContext.startService(intent);

        PreferenceUtil.getInstance(mContext).setBeaconScanStatus(true);
    }

    public void stopBeaconScan() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#stopBeaconScan");
        }

        Intent intent = new Intent();
        intent.setClassName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.beacon.BeaconService");
        intent.setAction(mContext.getPackageName() + ".sda.action.STOP_BEACON_SCAN");
        mContext.startService(intent);

        PreferenceUtil.getInstance(mContext).setBeaconScanStatus(false);
    }

    public boolean isStartBeaconScan() {
        return PreferenceUtil.getInstance(mContext).getBeaconScanStatus();
    }

    public void destroy() {
        stopBeaconScan();
        stopCheckConnectionOfBeacon();

        mScheduledExecutorService.shutdown();

        mOnBeaconEventListeners.clear();
        mOnBeaconConnectListeners.clear();
        mOnBeaconButtonEventListeners.clear();
        mOnBeaconProximityEventListeners.clear();
        mOnBeaconBatteryEventListeners.clear();
        mOnBeaconAtmosphericPressureEventListeners.clear();
        mOnBeaconHumidityEventListeners.clear();
        mOnBeaconTemperatureEventListeners.clear();
        mOnBeaconRawDataEventListeners.clear();
    }

    public LinkingBeacon findBeacon(final int extraId, final int vendorId) {
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

    public void removeBeacon(final LinkingBeacon beacon) {
        if (beacon != null) {
            mLinkingBeacons.remove(beacon);
            mDBAdapter.delete(beacon);
        }
    }

    public void removeAllBeacons() {
        mLinkingBeacons.clear();
        mDBAdapter.deleteAll();
    }

    public void onReceivedBeacon(final Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@ LinkingBeaconManager#onReceivedBeacon");
            Log.i(TAG, "intent=" + intent);
        }

        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (LinkingBeaconUtil.ACTION_BEACON_SCAN_RESULT.equals(action)) {
            parseBeaconResult(intent);
        } else if (LinkingBeaconUtil.ACTION_BEACON_SCAN_STATE.equals(action)) {
            parseBeaconScanState(intent);
        }
    }

    private void parseBeaconScanState(final Intent intent) {
        mScanState = intent.getIntExtra(LinkingBeaconUtil.SCAN_STATE, 0);
        mScanDetail = intent.getIntExtra(LinkingBeaconUtil.DETAIL, 0);

        if (mScanState == LinkingBeaconUtil.RESULT_NG) {
            if (mScanDetail == LinkingBeaconUtil.DETAIL_TIMEOUT) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@ Restart the beacon scan.");
                }
                startBeaconScan(mScanMode);
            } else if (mScanDetail == LinkingBeaconUtil.DETAIL_META_DATA_NONE) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "@@ meta data is not defined.");
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Unknown detail. detail=" + mScanDetail);
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "mScanState: " + mScanState);
            Log.d(TAG, "mScanDetail: " + mScanDetail);
        }
    }

    private void parseBeaconResult(final Intent intent) {
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
            for (OnBeaconConnectListener listener : mOnBeaconConnectListeners) {
                listener.onConnected(beacon);
            }
        }

        if (beacon.isOnline()) {
            for (OnBeaconEventListener listener : mOnBeaconEventListeners) {
                listener.onNotify(beacon);
            }
        }
    }

    private void parseGattData(final Intent intent, final LinkingBeacon beacon) {
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

        for (OnBeaconProximityEventListener listener : mOnBeaconProximityEventListeners) {
            listener.onProximity(beacon, gatt);
        }
    }

    private void parseAtmosphericPressureData(final Intent intent, final LinkingBeacon beacon) {
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

            for (OnBeaconAtmosphericPressureEventListener listener : mOnBeaconAtmosphericPressureEventListeners) {
                listener.onAtmosphericPressure(beacon, atm);
            }
        }
    }

    private void parseBatteryData(final Intent intent, final LinkingBeacon beacon) {
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

            for (OnBeaconBatteryEventListener listener : mOnBeaconBatteryEventListeners) {
                listener.onBattery(beacon, battery);
            }
        }
    }

    private void parseHumidityData(final Intent intent, final LinkingBeacon beacon) {
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

            for (OnBeaconHumidityEventListener listener : mOnBeaconHumidityEventListeners) {
                listener.onHumidity(beacon, humidity);
            }
        }
    }

    private void parseTemperatureData(final Intent intent, final LinkingBeacon beacon) {
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

            for (OnBeaconTemperatureEventListener listener : mOnBeaconTemperatureEventListeners) {
                listener.onTemperature(beacon, temp);
            }
        }
    }

    private void parseRawData(final Intent intent, final LinkingBeacon beacon) {
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

            for (OnBeaconRawDataEventListener listener : mOnBeaconRawDataEventListeners) {
                listener.onRawData(beacon, raw);
            }
        }
    }

    private void parseButtonId(final Intent intent, final LinkingBeacon beacon) {
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
                        for (OnBeaconConnectListener listener : mOnBeaconConnectListeners) {
                            listener.onDisconnected(beacon);
                        }
                    }
                }
            }
        }
    }

    public interface OnBeaconConnectListener {
        void onConnected(LinkingBeacon beacon);
        void onDisconnected(LinkingBeacon beacon);
    }

    public interface OnBeaconEventListener {
        void onNotify(LinkingBeacon beacon);
    }

    public interface OnBeaconButtonEventListener {
        void onClickButton(LinkingBeacon beacon, int keyCode, long timeStamp);
    }

    public interface OnBeaconProximityEventListener {
        void onProximity(LinkingBeacon beacon, GattData gatt);
    }

    public interface OnBeaconBatteryEventListener {
        void onBattery(LinkingBeacon beacon, BatteryData battery);
    }

    public interface OnBeaconAtmosphericPressureEventListener {
        void onAtmosphericPressure(LinkingBeacon beacon, AtmosphericPressureData atmosphericPressure);
    }

    public interface OnBeaconHumidityEventListener {
        void onHumidity(LinkingBeacon beacon, HumidityData humidity);
    }

    public interface OnBeaconTemperatureEventListener {
        void onTemperature(LinkingBeacon beacon, TemperatureData temperature);
    }

    public interface OnBeaconRawDataEventListener {
        void onRawData(LinkingBeacon beacon, RawData rawData);
    }
}
