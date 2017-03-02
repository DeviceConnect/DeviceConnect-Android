/*
 ButtonData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.beacon.data;

public class ButtonData {
    private long mTimeStamp;
    private int mKeyCode;

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        mTimeStamp = timeStamp;
    }

    public int getKeyCode() {
        return mKeyCode;
    }

    public void setKeyCode(int keyCode) {
        mKeyCode = keyCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("keyCode: ").append(getKeyCode()).append("\n");
        return sb.toString();
    }
}
