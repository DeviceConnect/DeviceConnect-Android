/*
 SWDeviceOrientationCache.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw;

/**
 * SonyWatchの加速度センサーをキャッシュするためのデータクラス.
 */
public class SWDeviceOrientationCache {
    /**
     * 加速度センサー(重力込み).
     * <ul>
     * <li>mValues[0]: x座標
     * <li>mValues[1]: y座標
     * <li>mValues[2]: z座標
     * </ul>
     */
    private float[] mValues;

    /**
     * インターバル.
     */
    private long mInterval;

    /**
     * 加速度センサー(重力込み)を取得する.
     * @return 加速度センサー(重力込み)
     */
    public float[] getValues() {
        return mValues;
    }

    /**
     * 加速度センサー(重力込み)を設定する.
     * @param values 加速度センサー(重力込み)
     */
    public void setValues(final float[] values) {
        mValues = values;
    }

    /**
     * インターバルを取得する.
     * @return インターバル
     */
    public long getInterval() {
        return mInterval;
    }

    /**
     * インターバルを設定する.
     * @param interval インターバル
     */
    public void setInterval(final long interval) {
        mInterval = interval;
    }
}
