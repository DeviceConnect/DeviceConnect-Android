package org.deviceconnect.android.deviceplugin.midi;

import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo implements Parcelable {

    static final int PORT_TYPE_INPUT = MidiDeviceInfo.PortInfo.TYPE_INPUT;

    static final int PORT_TYPE_OUTPUT = MidiDeviceInfo.PortInfo.TYPE_OUTPUT;

    private static final String MIDI_VERSION = "1.0";

    private final MidiDeviceInfo mDeviceInfo;

    private final int mPortNumber;

    private final int mPortType;

    private final String mPortName;

    private final List<String> mProfileNameList;

    private String mServiceName;

    ServiceInfo(final MidiDeviceInfo deviceInfo, final MidiDeviceInfo.PortInfo portInfo) {
        mDeviceInfo = deviceInfo;
        mPortNumber = portInfo.getPortNumber();
        mPortType = portInfo.getType();
        mPortName = portInfo.getName();
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

    public int getPortNumber() {
        return mPortNumber;
    }

    public int getPortType() {
        return mPortType;
    }

    public String getPortName() {
        return mPortName;
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
        dest.writeString(this.mServiceName);
        dest.writeParcelable(this.mDeviceInfo, flags);
        dest.writeInt(this.mPortNumber);
        dest.writeInt(this.mPortType);
        dest.writeString(this.mPortName);
        dest.writeStringList(this.mProfileNameList);
    }

    protected ServiceInfo(Parcel in) {
        this.mServiceName = in.readString();
        this.mDeviceInfo = in.readParcelable(MidiDeviceInfo.class.getClassLoader());
        this.mPortNumber = in.readInt();
        this.mPortType = in.readInt();
        this.mPortName = in.readString();
        this.mProfileNameList = in.createStringArrayList();
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
