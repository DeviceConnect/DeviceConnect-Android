/*
 HeartRateDevice
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.data;

/**
 * This class is information of a device.
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDevice {
    private int mId = -1;
    private String mName;
    private String mAddress;
    private int mSensorLocation = -1;
    private boolean mRegisterFlag;

    public int getId() {
        return mId;
    }

    public void setId(final int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(final String address) {
        mAddress = address;
    }

    public int getSensorLocation() {
        return mSensorLocation;
    }

    public void setSensorLocation(final int sensorLocation) {
        mSensorLocation = sensorLocation;
    }

    public boolean isRegisterFlag() {
        return mRegisterFlag;
    }

    public void setRegisterFlag(final boolean registerFlag) {
        mRegisterFlag = registerFlag;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HeartRateDevice that = (HeartRateDevice) o;

        if (mId != that.mId) {
            return false;
        }
        if (mAddress != null ? !mAddress.equals(that.mAddress) : that.mAddress != null) {
            return false;
        }
        if (mName != null ? !mName.equals(that.mName) : that.mName != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mAddress != null ? mAddress.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\": " + mId + ", ");
        builder.append("\"name\": " + mName + ", ");
        builder.append("\"address\": " + mAddress + ", ");
        builder.append("\"location\": " + mSensorLocation + ", ");
        builder.append("\"registerFlag\": " + mRegisterFlag + "} ");
        return builder.toString();
    }
}
