/*
 ServiceInfo.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * MIDI デバイス操作用サービスについての情報の構造体.
 *
 * @author NTT DOCOMO, INC.
 */
public class ServiceInfo implements Parcelable {

    public enum Direction {
        INPUT,
        OUTPUT,
        BIDIRECTIONAL,
        NONE
    }

    private static final String MIDI_VERSION = "1.0";

    private final MidiDeviceInfo mDeviceInfo;

    private final List<String> mProfileNameList;

    private String mServiceName;

    ServiceInfo(final MidiDeviceInfo deviceInfo) {
        mDeviceInfo = deviceInfo;
        mProfileNameList = new ArrayList<>();
    }

    public String getServiceName() {
        return mServiceName;
    }

    public void setServiceName(final String serviceName) {
        mServiceName = serviceName;
    }

    public String getProtocolVersion() {
        return MIDI_VERSION;
    }

    public Direction getDirectionName() {
        int inputs = mDeviceInfo.getInputPortCount();
        int outputs = mDeviceInfo.getOutputPortCount();
        if (inputs > 0 && outputs > 0) {
            return Direction.BIDIRECTIONAL;
        }
        if (inputs > 0) {
            return Direction.INPUT;
        }
        if (outputs > 0) {
            return Direction.OUTPUT;
        }
        return null;
    }

    public String getProductName() {
        return getStringProperty(MidiDeviceInfo.PROPERTY_PRODUCT);
    }

    public String getManufacturerName() {
        return getStringProperty(MidiDeviceInfo.PROPERTY_MANUFACTURER);
    }

    public List<String> getProfileNameList() {
        synchronized (mProfileNameList) {
            return new ArrayList<>(mProfileNameList);
        }
    }

    public void setProfileNameList(final List<String> profileNameList) {
        synchronized (mProfileNameList) {
            mProfileNameList.clear();
            mProfileNameList.addAll(profileNameList);
        }
    }

    private String getStringProperty(final String key) {
        Bundle props = getProperties();
        if (props == null) {
            return null;
        }
        return props.getString(key);
    }

    private Bundle getProperties() {
        return mDeviceInfo.getProperties();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDeviceInfo, flags);
        dest.writeStringList(this.mProfileNameList);
        dest.writeString(this.mServiceName);
    }

    protected ServiceInfo(Parcel in) {
        this.mDeviceInfo = in.readParcelable(MidiDeviceInfo.class.getClassLoader());
        this.mProfileNameList = in.createStringArrayList();
        this.mServiceName = in.readString();
    }

    public static final Creator<ServiceInfo> CREATOR = new Creator<ServiceInfo>() {
        @Override
        public ServiceInfo createFromParcel(Parcel source) {
            return new ServiceInfo(source);
        }

        @Override
        public ServiceInfo[] newArray(int size) {
            return new ServiceInfo[size];
        }
    };
}
