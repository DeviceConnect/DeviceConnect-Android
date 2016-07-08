/*
 GattData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingDeviceManager;

public class GattData {
    private long mTimeStamp;
    private int mTxPower;
    private int Rssi;
    private int mDistance;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public int getTxPower() {
        return mTxPower;
    }

    public void setTxPower(final int txPower) {
        mTxPower = txPower;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(final int rssi) {
        Rssi = rssi;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(final int distance) {
        mDistance = distance;
    }

    public LinkingDeviceManager.Range getRange() {
        switch (getDistance()) {
            case 1:
                return LinkingDeviceManager.Range.IMMEDIATE;
            case 2:
                return LinkingDeviceManager.Range.NEAR;
            case 3:
                return LinkingDeviceManager.Range.FAR;
            default:
                return LinkingDeviceManager.Range.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RSSI: ").append(getRssi()).append("\n")
                .append("TxPower: ").append(getTxPower()).append("\n")
                .append("Distance: ").append(getDistance());
        return sb.toString();
    }
}
