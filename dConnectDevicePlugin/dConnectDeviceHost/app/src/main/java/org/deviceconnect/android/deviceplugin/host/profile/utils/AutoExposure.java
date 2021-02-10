package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.hardware.camera2.CameraMetadata;

public enum AutoExposure {
    NONE("none", null),
    OFF("off", CameraMetadata.CONTROL_AE_MODE_OFF),
    ON("on", CameraMetadata.CONTROL_AE_MODE_ON),
    AUTO_FLASH("on_auto_flash", CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH),
    ALWAYS_FLASH("on_always_flash", CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH),
    AUTO_FLASH_REDEYE("on_auto_flash_redeye", CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE),
    EXTERNAL_FLASH("on_external_flash", CameraMetadata.CONTROL_AE_MODE_ON_EXTERNAL_FLASH);

    private final String mName;
    private final Integer mValue;

    AutoExposure(String name, Integer value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Integer getValue() {
        return mValue;
    }

    public static AutoExposure nameOf(String name) {
        for (AutoExposure m : values()) {
            if (m.mName.equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public static AutoExposure valueOf(Integer value) {
        for (AutoExposure m : values()) {
            if (m.mValue == value) {
                return m;
            }
        }
        return null;
    }
}
