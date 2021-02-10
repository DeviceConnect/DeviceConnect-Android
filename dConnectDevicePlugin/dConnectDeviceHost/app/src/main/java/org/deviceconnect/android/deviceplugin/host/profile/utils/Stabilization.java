package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.hardware.camera2.CaptureRequest;

public enum Stabilization {
    NONE("none", null),
    OFF("off", CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF),
    On("on", CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);

    private final String mName;
    private final Integer mValue;

    Stabilization(String name, Integer value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Integer getValue() {
        return mValue;
    }

    public static Stabilization nameOf(String name) {
        for (Stabilization s : values()) {
            if (s.mName.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static Stabilization valueOf(Integer value) {
        for (Stabilization s : values()) {
            if (s.mValue == value) {
                return s;
            }
        }
        return null;
    }
}
