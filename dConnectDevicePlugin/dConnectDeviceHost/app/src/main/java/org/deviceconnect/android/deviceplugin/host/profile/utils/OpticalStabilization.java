package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.hardware.camera2.CaptureRequest;

public enum OpticalStabilization {
    NONE("none", null),
    OFF("off", CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF),
    On("on", CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);

    private final String mName;
    private final Integer mValue;

    OpticalStabilization(String name, Integer value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Integer getValue() {
        return mValue;
    }

    public static OpticalStabilization nameOf(String name) {
        for (OpticalStabilization s : values()) {
            if (s.mName.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static OpticalStabilization valueOf(Integer value) {
        for (OpticalStabilization s : values()) {
            if (s.mValue == value) {
                return s;
            }
        }
        return null;
    }
}
