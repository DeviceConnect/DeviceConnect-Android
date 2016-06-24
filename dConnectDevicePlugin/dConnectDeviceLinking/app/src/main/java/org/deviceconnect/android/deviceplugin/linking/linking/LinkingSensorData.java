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

    private String mBdAddress;
    private SensorType mType = SensorType.GYRO;
    private float mX;
    private float mY;
    private float mZ;
    private byte[] mOriginalData;
    private long mTime;

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
        mBdAddress = in.readString();
        mType = SensorType.values()[in.readInt()];
        mX = in.readFloat();
        mY = in.readFloat();
        mZ = in.readFloat();
        in.writeByteArray(mOriginalData);
        mOriginalData = in.createByteArray();
        in.writeLong(mTime);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mBdAddress);
        parcel.writeInt(mType.ordinal());
        parcel.writeFloat(mX);
        parcel.writeFloat(mY);
        parcel.writeFloat(mZ);
        parcel.writeByteArray(mOriginalData);
        parcel.writeLong(mTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getBdAddress() {
        return mBdAddress;
    }

    public void setBdAddress(String bdAddress) {
        mBdAddress = bdAddress;
    }

    public SensorType getType() {
        return mType;
    }

    public void setType(SensorType type) {
        mType = type;
    }

    public float getX() {
        return mX;
    }

    public void setX(float x) {
        mX = x;
    }

    public float getY() {
        return mY;
    }

    public void setY(float y) {
        mY = y;
    }

    public float getZ() {
        return mZ;
    }

    public void setZ(float z) {
        mZ = z;
    }

    public byte[] getOriginalData() {
        return mOriginalData;
    }

    public void setOriginalData(byte[] originalData) {
        mOriginalData = originalData;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }
}
