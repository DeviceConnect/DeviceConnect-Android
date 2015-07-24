/*
 ThetaDeviceInfo
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.net.wifi.WifiInfo;

/**
 * Information of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceInfo {

    /**
     * An identifier as a device on Device Connect System.
     */
    public final String mServiceId;

    /**
     * A human-readable name.
     */
    public final String mName;

    /**
     * Information of current recorder.
     */
    private final RecorderInfo mRecorderInfo;

    /**
     * Constructor.
     *
     * @param wifiInfo an instance of {@link WifiInfo}
     * @param recorderInfo an instance of {@link RecorderInfo}
     */
    ThetaDeviceInfo(final WifiInfo wifiInfo, final RecorderInfo recorderInfo) {
        mServiceId = "theta";
        mName = wifiInfo.getSSID().replace("\"", "");
        mRecorderInfo = recorderInfo;
    }

    public RecorderInfo getCurrentRecoderInfo() {
        return mRecorderInfo;
    }

    public RecorderInfo getRecorderInfo(final String targetId) {
        if (targetId == null || targetId.equals(mRecorderInfo.mId)) {
            return mRecorderInfo;
        } else {
            return null;
        }
    }
}
