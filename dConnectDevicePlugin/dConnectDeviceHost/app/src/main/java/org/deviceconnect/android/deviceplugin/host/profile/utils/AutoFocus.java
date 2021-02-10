package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.hardware.camera2.CameraMetadata;

public enum AutoFocus {
    NONE("none", null),
    OFF("off", CameraMetadata.CONTROL_AF_MODE_OFF),
    AUTO("auto", CameraMetadata.CONTROL_AF_MODE_AUTO),
    MACRO("macro", CameraMetadata.CONTROL_AF_MODE_MACRO),
    CONTINUOUS_VIDEO("continuous_video", CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO),
    CONTINUOUS_PICTURE("continuous_picture", CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE),
    EDOF("edof", CameraMetadata.CONTROL_AF_MODE_EDOF);

    private final String mName;
    private final Integer mValue;

    AutoFocus(String name, Integer value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Integer getValue() {
        return mValue;
    }

    public static AutoFocus nameOf(String name) {
        for (AutoFocus af : values()) {
            if (af.mName.equals(name)) {
                return af;
            }
        }
        return null;
    }

    public static AutoFocus valueOf(Integer value) {
        for (AutoFocus af : values()) {
            if (af.mValue == value) {
                return af;
            }
        }
        return null;
    }
}
