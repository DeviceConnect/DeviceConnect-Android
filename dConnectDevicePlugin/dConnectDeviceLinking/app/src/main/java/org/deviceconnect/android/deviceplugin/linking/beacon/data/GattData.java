package org.deviceconnect.android.deviceplugin.linking.beacon.data;

import org.deviceconnect.android.deviceplugin.linking.linking.LinkingManager;

public class GattData {
    private long mTimeStamp;
    private int mTxPower;
    private int Rssi;
    private int mDistance;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public int getTxPower() {
        return mTxPower;
    }

    public void setTxPower(int txPower) {
        mTxPower = txPower;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public LinkingManager.Range getRange() {
        switch (getDistance()) {
            case 1:
                return LinkingManager.Range.IMMEDIATE;
            case 2:
                return LinkingManager.Range.NEAR;
            case 3:
                return LinkingManager.Range.FAR;
            default:
                return LinkingManager.Range.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RSSI: ").append(getRssi()).append("\n")
                .append("TxPower: ").append(getTxPower()).append("\n")
                .append("Distance: ").append(getDistance()).append("\n");
        return sb.toString();
    }
}
