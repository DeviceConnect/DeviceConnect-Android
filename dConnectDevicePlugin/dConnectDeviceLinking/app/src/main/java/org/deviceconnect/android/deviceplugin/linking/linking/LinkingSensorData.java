/*
 LinkingSensorData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.os.Parcel;
import android.os.Parcelable;

public class LinkingSensorData implements Parcelable {
    public enum SensorType {
        GYRO(0), ACCELERATION(1), COMPASS(2), EXTENDS(3);

        private int mValue;
        SensorType(int value) {
            mValue = value;
        }
        public int getValue() {
            return mValue;
        }

        public static SensorType valueOf(int value) {
            for (SensorType type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return EXTENDS;
        }
    }

    private String bdAddress;
    private SensorType type = SensorType.GYRO;
    private float x;
    private float y;
    private float z;
    private byte[] originalData;
    private long time;

    public static final Parcelable.Creator<LinkingSensorData> CREATOR = new Parcelable.Creator<LinkingSensorData>() {
        @Override
        public LinkingSensorData createFromParcel(Parcel parcel) {
            return new LinkingSensorData(parcel);
        }

        @Override
        public LinkingSensorData[] newArray(int size) {
            return new LinkingSensorData[size];
        }
    };

    public LinkingSensorData() {

    }

    public LinkingSensorData(Parcel in) {
        bdAddress = in.readString();
        type = SensorType.values()[in.readInt()];
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        in.writeByteArray(originalData);
        originalData = in.createByteArray();
        in.writeLong(time);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bdAddress);
        parcel.writeInt(type.ordinal());
        parcel.writeFloat(x);
        parcel.writeFloat(y);
        parcel.writeFloat(z);
        parcel.writeByteArray(originalData);
        parcel.writeLong(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getBdAddress() {
        return bdAddress;
    }

    public void setBdAddress(String bdAddress) {
        this.bdAddress = bdAddress;
    }

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public byte[] getOriginalData() {
        return originalData;
    }

    public void setOriginalData(byte[] originalData) {
        this.originalData = originalData;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
