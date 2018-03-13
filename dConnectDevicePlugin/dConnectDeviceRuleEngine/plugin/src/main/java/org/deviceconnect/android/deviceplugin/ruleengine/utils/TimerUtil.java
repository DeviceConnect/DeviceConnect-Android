/*
 TimerUtil.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.utils;

import java.util.Calendar;

/**
 * TimerUtilクラス.
 * @author NTT DOCOMO, INC.
 */
public class TimerUtil {
    /** トリガーインターバル開始基準秒 : 指定なし. */
    public static final String REF_NO_SETTING = "noSetting";
    /** トリガーインターバル開始基準秒 : 毎00秒. */
    public static final String REF_00_SECONDS = "ref00Seconds";
    /** トリガーインターバル開始基準秒 : 毎10秒. */
    public static final String REF_10_SECONDS = "ref10Seconds";
    /** トリガーインターバル開始基準秒 : 毎20秒. */
    public static final String REF_20_SECONDS = "ref20Seconds";
    /** トリガーインターバル開始基準秒 : 毎30秒. */
    public static final String REF_30_SECONDS = "ref30Seconds";
    /** トリガーインターバル開始基準秒 : 毎40秒. */
    public static final String REF_40_SECONDS = "ref40Seconds";
    /** トリガーインターバル開始基準秒 : 毎50秒. */
    public static final String REF_50_SECONDS = "ref50Seconds";

    /**
     * タイマー開始時刻算出.
     * @param referenceTime タイマー開始起点設定.
     * @return 開始時刻.
     */
    public static Calendar getStartTimerTime(final String referenceTime) {
        Calendar nowTime, startTime;
        // 現在時刻の取得、開始時刻初期化.
        nowTime = startTime = Calendar.getInstance();
        // 秒の抽出.
        int second = nowTime.get(Calendar.SECOND);
        int calcSec;
        switch (referenceTime) {
            case REF_NO_SETTING:
                calcSec = ((second + 10) / 5) * 5;
                if (calcSec >= 60) {
                    calcSec -= 60;
                    startTime.add(Calendar.MINUTE, 1);
                }
                startTime.set(Calendar.SECOND, calcSec);
                break;
            case REF_00_SECONDS:
                if (second > 50) {
                    // +2 分を設定.
                    startTime.add(Calendar.MINUTE, 2);
                } else {
                    // +1 分を設定.
                    startTime.add(Calendar.MINUTE, 1);
                }
                startTime.set(Calendar.SECOND, 0);
                break;
            case REF_10_SECONDS:
                startTime.set(Calendar.SECOND, 10);
                startTime.add(Calendar.MINUTE, 1);
                break;
            case REF_20_SECONDS:
                if (second > 10) {
                    // +1 分を設定.
                    startTime.add(Calendar.MINUTE, 1);
                }
                startTime.set(Calendar.SECOND, 20);
                break;
            case REF_30_SECONDS:
                if (second > 20) {
                    // +1 分を設定.
                    startTime.add(Calendar.MINUTE, 1);
                }
                startTime.set(Calendar.SECOND, 30);
                break;
            case REF_40_SECONDS:
                if (second > 30) {
                    // +1 分を設定.
                    startTime.add(Calendar.MINUTE, 1);
                }
                startTime.set(Calendar.SECOND, 40);
                break;
            case REF_50_SECONDS:
                if (second > 40) {
                    // +1 分を設定.
                    startTime.add(Calendar.MINUTE, 1);
                }
                startTime.set(Calendar.SECOND, 50);
                break;
            default:
                return null;
        }
        return startTime;
    }


}
