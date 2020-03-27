/*
 LinkingBeaconManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.lib.R;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.AtmosphericPressureData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.BatteryData;
import org.deviceconnect.android.deviceplugin.linking.beacon.data.ButtonData;
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

    private static final int INTERVAL = 20 * 1000;

    private Context mContext;
    private LinkingBeaconUtil.ScanState mScanState;
    private LinkingBeaconUtil.ScanDetail mScanDetail;
    private LinkingBeaconUtil.ScanMode mScanMode;

    private ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mScheduledFuture;

    private final List<LinkingBeacon> mLinkingBeacons = Collections.synchronizedList(new ArrayList<LinkingBeacon>());

    private final List<OnBeaconConnectListener> mOnBeaconConnectListeners = Collections.synchronizedList(new ArrayList<OnBeaconConnectListener>());
    private final List<OnBeaconEventListener> mOnBeaconEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconEventListener>());
    private final List<OnBeaconButtonEventListener> mOnBeaconButtonEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconButtonEventListener>());
    private final List<OnBeaconProximityEventListener> mOnBeaconProximityEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconProximityEventListener>());
    private final List<OnBeaconBatteryEventListener> mOnBeaconBatteryEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconBatteryEventListener>());
    private final List<OnBeaconAtmosphericPressureEventListener> mOnBeaconAtmosphericPressureEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconAtmosphericPressureEventListener>());
    private final List<OnBeaconHumidityEventListener> mOnBeaconHumidityEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconHumidityEventListener>());
    private final List<OnBeaconTemperatureEventListener> mOnBeaconTemperatureEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconTemperatureEventListener>());
    private final List<OnBeaconRawDataEventListener> mOnBeaconRawDataEventListeners = Collections.synchronizedList(new ArrayList<OnBeaconRawDataEventListener>());
    private final List<OnBeaconScanStateListener> mOnBeaconScanStateListeners = Collections.synchronizedList(new ArrayList<OnBeaconScanStateListener>());

    private LinkingDBAdapter mDBAdapter;

    private TimeoutRunnable mTimeoutRunnable;

    private boolean mScanFlag;

    public LinkingBeaconManager(final Context context) {
        mContext = context;
        mDBAdapter = new LinkingDBAdapter(context);
        mLinkingBeacons.addAll(mDBAdapter.queryBeacons());
        mScanMode = LinkingBeaconUtil.ScanMode.valueOf(PreferenceUtil.getInstance(mContext).getBeaconScanMode());

        boolean scan = isStartedForceBeaconScan();
        if (scan) {
            stopForceBeaconScan();
        }

        startCheckConnectionOfBeacon();
    }

    public boolean isScanState() {
        return isStartedForceBeaconScan() || isStartedBeaconScan();
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

    public Context getContext() {
        return mContext;
    }

    public List<LinkingBeacon> getLinkingBeacons() {
        return mLinkingBeacons;
    }

    public void startForceBeaconScan() {
        startBeaconScanInternal(null);
        PreferenceUtil.getInstance(mContext).setForceBeaconScanStatus(true);
    }

    public void stopForceBeaconScan() {
        PreferenceUtil.getInstance(mContext).setForceBeaconScanStatus(false);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopForceBeaconScan: " + isStartedBeaconScan());
        }

        if (!isStartedBeaconScan()) {
            stopBeaconScanInternal();
        }
    }

    public boolean isStartedForceBeaconScan() {
        return PreferenceUtil.getInstance(mContext).getForceBeaconScanStatus();
    }

    public synchronized void startBeaconScanWithTimeout(final int timeout) {

        if (isStartedBeaconScan()) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startBeaconScanWithTimeout");
        }

        if (mTimeoutRunnable != null) {
            mTimeoutRunnable.cancel();
        }

        mTimeoutRunnable = new TimeoutRunnable(timeout) {
            @Override
            public void onTimeout() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "startBeaconScanWithTimeout:onTimeout");
                }
                if (!isStartedBeaconScan()) {
                    stopBeaconScan();
                }
            }
        };

        startBeaconScanInternal(LinkingBeaconUtil.ScanMode.HIGH);
    }

    public void startBeaconScan() {
        startBeaconScan(null);
    }

    public synchronized void startBeaconScan(final LinkingBeaconUtil.ScanMode scanMode) {
        mScanMode = scanMode;

        startBeaconScanInternal(scanMode);

        mScanFlag = true;
        if (scanMode != null) {
            PreferenceUtil.getInstance(mContext).setBeaconScanMode(mScanMode.getValue());
        }
    }

    public synchronized void stopBeaconScan() {
        mScanFlag = false;
        if (!isStartedForceBeaconScan()) {
            stopBeaconScanInternal();
        }
    }

    public synchronized boolean isStartedBeaconScan() {
        return mScanFlag;
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

    public void removeOnBeaconBatteryEventListener(final OnBeaconBatteryEventListener listener) {
        mOnBeaconBatteryEventListeners.remove(listener);
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

    public void addOnBeaconScanStateListener(final OnBeaconScanStateListener listener) {
        mOnBeaconScanStateListeners.add(listener);
    }

    public void removeOnBeaconScanStateListener(final OnBeaconScanStateListener listener) {
        mOnBeaconScanStateListeners.remove(listener);
    }

    private void startBeaconScanInternal(final LinkingBeaconUtil.ScanMode scanMode) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#startBeaconScan");
        }

        Intent intent = new Intent();
        intent.setClassName(LinkingBeaconUtil.LINKING_PACKAGE_NAME, LinkingBeaconUtil.BEACON_SERVICE_NAME);
        intent.setAction(mContext.getPackageName() + LinkingBeaconUtil.ACTION_START_BEACON_SCAN);
//        intent.putExtra(mContext.getPackageName() + LinkingBeaconUtil.EXTRA_SERVICE_ID, new int[] {0, 1, 2, 3, 4, 5, 15});
        if (scanMode != null) {
            intent.putExtra(mContext.getPackageName() + LinkingBeaconUtil.EXTRA_SCAN_MODE, scanMode.getValue());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    private void stopBeaconScanInternal() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "LinkingBeaconManager#stopBeaconScan");
        }

        Intent intent = new Intent();
        intent.setClassName(LinkingBeaconUtil.LINKING_PACKAGE_NAME, LinkingBeaconUtil.BEACON_SERVICE_NAME);
        intent.setAction(mContext.getPackageName() + LinkingBeaconUtil.ACTION_STOP_BEACON_SCAN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    private void parseBeaconScanState(final Intent intent) {
        mScanState = LinkingBeaconUtil.ScanState.valueOf(intent.getIntExtra(LinkingBeaconUtil.SCAN_STATE, 0));
        mScanDetail = LinkingBeaconUtil.ScanDetail.valueOf(intent.getIntExtra(LinkingBeaconUtil.DETAIL, 0));

        if (mScanState == LinkingBeaconUtil.ScanState.RESULT_NG) {
            switch (mScanDetail) {
                case DETAIL_TIMEOUT:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "@@ Restart the beacon scan.");
                    }
                    stopBeaconScan();
                    stopForceBeaconScan();
                    break;
                case DETAIL_META_DATA_NONE:
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "@@ meta data is not defined.");
                    }
                    break;
                default:
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Unknown detail. detail=" + mScanDetail);
                    }
                    break;
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "mScanState: " + mScanState);
            Log.d(TAG, "mScanDetail: " + mScanDetail);
        }

        for (OnBeaconScanStateListener listener : mOnBeaconScanStateListeners) {
            listener.onScanState(mScanState, mScanDetail);
        }
    }

    private void parseBeaconResult(final Intent intent) {
        if (intent.getExtras() == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "extras is null.");
            }
            return;
        }

        if (!intent.getExtras().containsKey(LinkingBeaconUtil.EXTRA_ID) ||
                !intent.getExtras().containsKey(LinkingBeaconUtil.VENDOR_ID) ||
                !intent.getExtras().containsKey(LinkingBeaconUtil.VERSION)) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "extraId, vendorId, version is null.");
            }
            return;
        }

        int extraId = intent.getIntExtra(LinkingBeaconUtil.EXTRA_ID, -1);
        int vendorId = intent.getIntExtra(LinkingBeaconUtil.VENDOR_ID, -1);
        int version = intent.getIntExtra(LinkingBeaconUtil.VERSION, -1);

        LinkingBeacon beacon = findBeacon(extraId, vendorId);
        if (beacon == null) {
            beacon = new LinkingBeacon();
            beacon.setExtraId(extraId);
            beacon.setVendorId(vendorId);
            beacon.setVersion(version);
            beacon.setDisplayName(mContext.getString(R.string.linking_beacon_display_name, extraId));
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
            notifyBeaconConnectionListener(beacon);
        }

        if (beacon.isOnline()) {
            notifyBeaconEventListener(beacon);
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

        notifyBeaconProximityEventListener(beacon, gatt);
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

            notifyBeaconAtmosphericPressureEventListener(beacon, atm);
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

            notifyBeaconBatteryEventListener(beacon, battery);
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

            notifyBeaconHumidityEventListener(beacon, humidity);
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

            notifyBeaconTemperatureEventListener(beacon, temp);
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

            notifyBeaconRawDataEventListener(beacon, raw);
        }
    }

    private void parseButtonId(final Intent intent, final LinkingBeacon beacon) {
        if (intent.getExtras().containsKey(LinkingBeaconUtil.BUTTON_ID)) {
            long timeStamp = intent.getLongExtra(LinkingBeaconUtil.TIME_STAMP, 0);
            int keyCode = intent.getIntExtra(LinkingBeaconUtil.BUTTON_ID, -1);

            ButtonData button = beacon.getButtonData();
            if (button == null) {
                button = new ButtonData();
                beacon.setButtonData(button);
            }

            button.setTimeStamp(timeStamp);
            button.setKeyCode(keyCode);

            notifyBeaconButtonEventListener(beacon, timeStamp, keyCode);
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
                        notifyBeaconConnectionListener(beacon);
                    }
                }
            }
        }
    }

    private void notifyBeaconAtmosphericPressureEventListener(final LinkingBeacon beacon, final AtmosphericPressureData atm) {
        synchronized (mOnBeaconAtmosphericPressureEventListeners) {
            for (OnBeaconAtmosphericPressureEventListener listener : mOnBeaconAtmosphericPressureEventListeners) {
                listener.onAtmosphericPressure(beacon, atm);
            }
        }
    }

    private void notifyBeaconBatteryEventListener(final LinkingBeacon beacon, final BatteryData battery) {
        synchronized (mOnBeaconBatteryEventListeners) {
            for (OnBeaconBatteryEventListener listener : mOnBeaconBatteryEventListeners) {
                listener.onBattery(beacon, battery);
            }
        }
    }

    private void notifyBeaconTemperatureEventListener(final LinkingBeacon beacon, final TemperatureData temp) {
        synchronized (mOnBeaconTemperatureEventListeners) {
            for (OnBeaconTemperatureEventListener listener : mOnBeaconTemperatureEventListeners) {
                listener.onTemperature(beacon, temp);
            }
        }
    }

    private void notifyBeaconHumidityEventListener(final LinkingBeacon beacon, final HumidityData humidity) {
        synchronized (mOnBeaconHumidityEventListeners) {
            for (OnBeaconHumidityEventListener listener : mOnBeaconHumidityEventListeners) {
                listener.onHumidity(beacon, humidity);
            }
        }
    }

    private void notifyBeaconRawDataEventListener(final LinkingBeacon beacon, final RawData raw) {
        synchronized (mOnBeaconRawDataEventListeners) {
            for (OnBeaconRawDataEventListener listener : mOnBeaconRawDataEventListeners) {
                listener.onRawData(beacon, raw);
            }
        }
    }

    private void notifyBeaconProximityEventListener(final LinkingBeacon beacon, final GattData gatt) {
        synchronized (mOnBeaconProximityEventListeners) {
            for (OnBeaconProximityEventListener listener : mOnBeaconProximityEventListeners) {
                listener.onProximity(beacon, gatt);
            }
        }
    }

    private void notifyBeaconButtonEventListener(final LinkingBeacon beacon, final long timeStamp, final int keyCode) {
        synchronized (mOnBeaconButtonEventListeners) {
            for (OnBeaconButtonEventListener listener: mOnBeaconButtonEventListeners) {
                listener.onClickButton(beacon, keyCode, timeStamp);
            }
        }
    }

    private void notifyBeaconEventListener(final LinkingBeacon beacon) {
        synchronized (mOnBeaconEventListeners) {
            for (OnBeaconEventListener listener : mOnBeaconEventListeners) {
                listener.onNotify(beacon);
            }
        }
    }

    private void notifyBeaconConnectionListener(final LinkingBeacon beacon) {
        synchronized (mOnBeaconConnectListeners) {
            for (OnBeaconConnectListener listener : mOnBeaconConnectListeners) {
                listener.onDisconnected(beacon);
            }
        }
    }

    private abstract class TimeoutRunnable implements Runnable {
        protected ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
        protected ScheduledFuture<?> mScheduledFuture;
        private boolean mDestroyFlag;

        TimeoutRunnable(final int timeout) {
            mScheduledFuture = mExecutorService.schedule(this, timeout, TimeUnit.MILLISECONDS);
        }

        @Override
        public synchronized void run() {
            if (mDestroyFlag) {
                return;
            }
            mDestroyFlag = true;

            onTimeout();

            mScheduledFuture.cancel(false);
            mExecutorService.shutdown();
        }

        public synchronized void cancel() {
            if (mDestroyFlag) {
                return;
            }
            mDestroyFlag = true;

            mScheduledFuture.cancel(false);
            mExecutorService.shutdown();
        }

        public abstract void onTimeout();
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

    public interface OnBeaconScanStateListener {
        void onScanState(LinkingBeaconUtil.ScanState state, LinkingBeaconUtil.ScanDetail detail);
    }
}
