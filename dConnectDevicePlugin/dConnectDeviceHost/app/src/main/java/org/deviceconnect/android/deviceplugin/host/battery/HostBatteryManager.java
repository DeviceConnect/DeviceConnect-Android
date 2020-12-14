/*
 HostBatteryManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.deviceconnect.android.message.DevicePluginContext;

/**
 * バッテリー関連の値の処理と保持.
 */
public class HostBatteryManager {

    /** バッテリーの状態. */
    private int mStatusBattery;

    /** プラグの状態. */
    private int mStatusPlugged;

    /** バッテリーのレベル. */
    private int mValueLevel;

    /** バッテリーのスケール. */
    private int mValueScale;

    /** チャージフラグ. */
    private boolean mChargingFlag;

    /** バッテリーの温度. */
    private float mTemperature;

    /** バッテリーの状態 不明. */
    public static final int BATTERY_STATUS_UNKNOWN = 1;

    /** バッテリーの状態 充電中. */
    public static final int BATTERY_STATUS_CHARGING = 2;

    /** バッテリーの状態 放電中. */
    public static final int BATTERY_STATUS_DISCHARGING = 3;

    /** バッテリーの状態 非充電中. */
    public static final int BATTERY_STATUS_NOT_CHARGING = 4;

    /** バッテリーの状態 満杯. */
    public static final int BATTERY_STATUS_FULL = 5;

    /** 充電中 AC. */
    public static final int BATTERY_PLUGGED_AC = 1;

    /** 充電中 USB. */
    public static final int BATTERY_PLUGGED_USB = 2;

    /** Intent filter for battery charge event. */
    private IntentFilter mIfBatteryCharge;

    /** Intent filter for battery connect event. */
    private IntentFilter mIfBatteryConnect;

    /**
     * Context.
     */
    private DevicePluginContext mHostDevicePluginContext;

    private BatteryChargingEventListener mBatteryChargingEventListener;
    private BatteryStatusEventListener mBatteryStatusEventListener;

    public HostBatteryManager(final DevicePluginContext pluginContext) {
        mHostDevicePluginContext = pluginContext;

        mIfBatteryCharge = new IntentFilter();
        mIfBatteryCharge.addAction(Intent.ACTION_BATTERY_CHANGED);
        mIfBatteryCharge.addAction(Intent.ACTION_BATTERY_LOW);
        mIfBatteryCharge.addAction(Intent.ACTION_BATTERY_OKAY);

        mIfBatteryConnect = new IntentFilter();
        mIfBatteryConnect.addAction(Intent.ACTION_POWER_CONNECTED);
        mIfBatteryConnect.addAction(Intent.ACTION_POWER_DISCONNECTED);
    }

    private Context getContext() {
        return mHostDevicePluginContext.getContext();
    }

    public void destroy() {
        clear();
    }

    public void clear() {
        unregisterBatteryChargeBroadcastReceiver();
        unregisterBatteryConnectBroadcastReceiver();
    }

    /**
     * バッテリーのIntentから情報を取得.
     */
    public void getBatteryInfo() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus;
        int i = 0;
        do {
            batteryStatus = getContext().registerReceiver(null, filter);
        } while (i++ < 3 && batteryStatus == null);

        if (batteryStatus == null) {
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_UNKNOWN;
            mValueLevel = 0;
            mValueScale = 0;
            mTemperature = 0;
            return;
        }

        // バッテリーの変化を取得
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        switch (status) {
        case BatteryManager.BATTERY_STATUS_CHARGING:
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_CHARGING;
            break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_DISCHARGING;
            break;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_NOT_CHARGING;
            break;
        case BatteryManager.BATTERY_STATUS_FULL:
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_FULL;
            break;
        case BatteryManager.BATTERY_STATUS_UNKNOWN:
        default:
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_UNKNOWN;
            break;
        }

        // プラグの状態を取得
        int plugged = batteryStatus.getIntExtra("plugged", 0);
        switch (plugged) {
        case BatteryManager.BATTERY_PLUGGED_AC:
            mStatusPlugged = BATTERY_PLUGGED_AC;
            break;
        case BatteryManager.BATTERY_PLUGGED_USB:
            mStatusPlugged = BATTERY_PLUGGED_USB;
            break;
        default:
            break;
        }

        // チャージングフラグ
        mChargingFlag = (plugged != 0);

        // バッテリー残量
        mValueLevel = batteryStatus.getIntExtra("level", 0);
        mValueScale = batteryStatus.getIntExtra("scale", 0);

        // バッテリーの温度
        int raw = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        mTemperature = raw / 10.0f;
    }

    /**
     * バッテリーのIntentを設定.
     * 
     * @param intent Batteryの変化で取得できたIntent
     */
    private void setBatteryRequest(final Intent intent) {
        String mAction = intent.getAction();

        if (Intent.ACTION_BATTERY_CHANGED.equals(mAction) || Intent.ACTION_BATTERY_LOW.equals(mAction)
                || Intent.ACTION_BATTERY_OKAY.equals(mAction)) {
            // バッテリーの変化を取得
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            switch (status) {
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                mStatusBattery = HostBatteryManager.BATTERY_STATUS_UNKNOWN;
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                mStatusBattery = HostBatteryManager.BATTERY_STATUS_CHARGING;
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                mStatusBattery = HostBatteryManager.BATTERY_STATUS_DISCHARGING;
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                mStatusBattery = HostBatteryManager.BATTERY_STATUS_NOT_CHARGING;
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                mStatusBattery = HostBatteryManager.BATTERY_STATUS_FULL;
                break;
            default:
                mStatusBattery = HostBatteryManager.BATTERY_STATUS_UNKNOWN;
                break;
            }

            mValueLevel = intent.getIntExtra("level", 0);
            mValueScale = intent.getIntExtra("scale", 0);

        } else if (Intent.ACTION_POWER_CONNECTED.equals(mAction) || Intent.ACTION_POWER_DISCONNECTED.equals(mAction)) {

            mChargingFlag = Intent.ACTION_POWER_CONNECTED.equals(mAction);

            // プラグの状態を取得
            int plugged = intent.getIntExtra("plugged", 0);
            switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                mStatusPlugged = BATTERY_PLUGGED_AC;
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                mStatusPlugged = BATTERY_PLUGGED_USB;
                break;
            default:
                break;
            }
        }
    }

    /**
     * バッテリーの状態を取得.
     * 
     * @return statusBattery バッテリーの状態
     */
    public int getBatteryStatus() {
        return mStatusBattery;
    }

    /**
     * プラグの状態を取得.
     *
     * @return statusPlugged プラグの状態
     */
    public int getStatusPlugged() {
        return mStatusPlugged;
    }

    /**
     * バッテリーレベルの取得.
     * 
     * @return valueLevel バッテリーレベル
     */
    public int getBatteryLevel() {
        return mValueLevel;
    }

    /**
     * スケールの取得.
     * 
     * @return batteryStatus バッテリーの状態
     */
    public int getBatteryScale() {
        return mValueScale;
    }

    /**
     * バッテリーチャージ状態を取得.
     * @return batteryCharging バッテリーチャージ状態
     */
    public boolean isChargingFlag() {
        return mChargingFlag;
    }

    /**
     * バッテリー温度を取得.
     * @return バッテリー温度
     */
    public float getTemperature() {
        return mTemperature;
    }

    /**
     * Register broadcast receiver for battery charge event.
     */
    public void registerBatteryChargeBroadcastReceiver() {
        getContext().registerReceiver(mBatteryChargeBR, mIfBatteryCharge);
    }

    /**
     * Unregister broadcast receiver for battery charge event.
     */
    public void unregisterBatteryChargeBroadcastReceiver() {
        try {
            getContext().unregisterReceiver(mBatteryChargeBR);
        } catch (Exception e) {
            // Nop
        }
    }

    /**
     * Register broadcast receiver for battery connect event.
     */
    public void registerBatteryConnectBroadcastReceiver() {
        getContext().registerReceiver(mBatteryConnectBR, mIfBatteryConnect);
    }

    /**
     * Unregister broadcast receiver for battery connect event.
     */
    public void unregisterBatteryConnectBroadcastReceiver() {
        try {
            getContext().unregisterReceiver(mBatteryConnectBR);
        } catch (Exception e) {
            // Nop
        }
    }

    public void setBatteryChargingEventListener(final BatteryChargingEventListener listener) {
        mBatteryChargingEventListener = listener;
    }

    public void setBatteryStatusEventListener(final BatteryStatusEventListener listener) {
        mBatteryStatusEventListener = listener;
    }

    public interface BatteryChargingEventListener {
        void onChangeCharging();
    }

    public interface BatteryStatusEventListener {
        void onChangeStatus();
    }

    /**
     * Broadcast receiver for battery charge event.
     */
    private BroadcastReceiver mBatteryChargeBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action) || Intent.ACTION_BATTERY_LOW.equals(action)
                    || Intent.ACTION_BATTERY_OKAY.equals(action)) {
                setBatteryRequest(intent);
                if (mBatteryChargingEventListener != null) {
                    mBatteryChargingEventListener.onChangeCharging();
                }
            }
        }
    };

    /**
     * Broadcast receiver for battery connect event.
     */
    private BroadcastReceiver mBatteryConnectBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                setBatteryRequest(intent);
                if (mBatteryStatusEventListener != null) {
                    mBatteryStatusEventListener.onChangeStatus();
                }
            }
        }
    };
}
