/*
 HostBatteryManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.manager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

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

    /**
     * バッテリーのIntentから情報を取得.
     * 
     * @param context Context
     */
    public void getBatteryInfo(final Context context) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = null;
        int i = 0;
        do {
            batteryStatus = context.registerReceiver(null, ifilter);
        } while (i++ < 3 && batteryStatus == null);
        if (batteryStatus == null) {
            mStatusBattery = HostBatteryManager.BATTERY_STATUS_UNKNOWN;
            mValueLevel = 0;
            mValueScale = 0;
            return;
        }

        // バッテリーの変化を取得
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
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

        mValueLevel = batteryStatus.getIntExtra("level", 0);
        mValueScale = batteryStatus.getIntExtra("scale", 0);
    }

    /**
     * バッテリーのIntentを設定.
     * 
     * @param intent Batteryの変化で取得できたIntent
     */
    public void setBatteryRequest(final Intent intent) {
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
}
