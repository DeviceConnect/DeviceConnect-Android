package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.hardware.camera2.CameraMetadata;

public enum WhiteBalance {
    NONE("none", null),
    OFF("off", CameraMetadata.CONTROL_AWB_MODE_OFF),
    AUTO("auto", CameraMetadata.CONTROL_AWB_MODE_AUTO),
    INCANDESCENT("incandescent", CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT),
    FLUORESCENT("fluorescent", CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT),
    WARM_FLUORESCENT("warm-fluorescent", CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT),
    DAYLIGHT("daylight", CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT),
    CLOUDY_DAYLIGHT("cloudy-daylight", CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT),
    TWILIGHT("twilight", CameraMetadata.CONTROL_AWB_MODE_TWILIGHT),
    SHADE("shade", CameraMetadata.CONTROL_AWB_MODE_SHADE);

    /** カメラの位置を表現する名前. */
    private final String mName;

    /**
     * カメラの番号.
     */
    private final Integer mValue;

    WhiteBalance(String name, Integer value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Integer getValue() {
        return mValue;
    }

    public static WhiteBalance nameOf(String name) {
        for (WhiteBalance wb : values()) {
            if (wb.mName.equals(name)) {
                return wb;
            }
        }
        return null;
    }

    public static WhiteBalance valueOf(Integer value) {
        for (WhiteBalance wb : values()) {
            if (wb.mValue == value) {
                return wb;
            }
        }
        return null;
    }
}
