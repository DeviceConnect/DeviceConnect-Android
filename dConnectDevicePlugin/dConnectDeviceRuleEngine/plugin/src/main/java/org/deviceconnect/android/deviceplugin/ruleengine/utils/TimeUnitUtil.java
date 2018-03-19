/*
 TimeUnitUtil.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine.utils;

import java.util.StringTokenizer;

/**
 * TimeUnitUtilクラス.
 * @author NTT DOCOMO, INC.
 */
public class TimeUnitUtil {
    /** mSec. */
    public static final String MSEC = "mSec";
    /** Second. */
    public static final String SECOND = "second";
    /** Minute. */
    public static final String MINUTE = "minute";
    /** Hour. */
    public static final String HOUR = "hour";
    /** Day. */
    public static final String DAY = "day";

    public TimeUnitUtil() {

    }

    /**
     * 単位文字列チェック.
     * @param unit チェック文字列.
     * @return true(単位合致) / false(不一致).
     */
    public static boolean checkTimeUnit(final String unit) {
        switch(unit) {
            case MSEC:
            case SECOND:
            case MINUTE:
            case HOUR:
            case DAY:
                return true;
            default:
                return false;
        }
    }

    /**
     * 入力値、入力単位のパラメータをミリ秒で返す.
     * @param value 数値.
     * @param unit 単位.
     * @return ミリ秒の数値 or -1(変換エラー).
     */
    public static long changeMSec(final long value, final String unit) {
        switch(unit) {
            case MSEC:
                return value;
            case SECOND:
                return (value * 1000);
            case MINUTE:
                return (value * 60000); // 1000 * 60
            case HOUR:
                return (value * 3600000); // 1000 * 60 * 60
            case DAY:
                return (value * 86400000); // 1000 * 60 * 60 * 24
            default:
                return -1L;
        }
    }
}
