/*
 HeartRateDevice
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.data;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateDevice {
    private int mId;
    private String mName;
    private String mAddress;
    private boolean mRegisterFlag;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public boolean isRegisterFlag() {
        return mRegisterFlag;
    }

    public void setRegisterFlag(boolean registerFlag) {
        mRegisterFlag = registerFlag;
    }
}
