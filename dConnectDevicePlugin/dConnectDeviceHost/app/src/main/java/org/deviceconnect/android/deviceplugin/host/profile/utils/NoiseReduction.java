package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.hardware.camera2.CameraMetadata;

public enum NoiseReduction {
    NONE("none", null),
    FAST("fast", CameraMetadata.NOISE_REDUCTION_MODE_FAST),
    HIGH_QUALITY("high_quality", CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY),
    MINIMAL("minimal", CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL),
    OFF("off", CameraMetadata.NOISE_REDUCTION_MODE_OFF),
    ZERO_SHUTTER_LAG("zero_shutter_lag", CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG);

    private final String mName;
    private final Integer mValue;

    NoiseReduction(String name, Integer value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public Integer getValue() {
        return mValue;
    }

    public static NoiseReduction nameOf(String name) {
        for (NoiseReduction s : values()) {
            if (s.mName.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static NoiseReduction valueOf(Integer value) {
        for (NoiseReduction s : values()) {
            if (s.mValue == value) {
                return s;
            }
        }
        return null;
    }
}
