package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.media.MediaCodecInfo;

public enum H265Profile {
    HEVCProfileMain("HEVCProfileMain", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain),
    HEVCProfileMain10("HEVCProfileMain10", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10),
    HEVCProfileMain10HDR10("HEVCProfileMain10HDR10", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10),
    HEVCProfileMain10HDR10Plus("HEVCProfileMain10HDR10Plus", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus),
    HEVCProfileMainStill("HEVCProfileMainStill", MediaCodecInfo.CodecProfileLevel.HEVCProfileMainStill);

    private final String mName;
    private final int mValue;

    H265Profile(String name, int value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public int getValue() {
        return mValue;
    }

    public static H265Profile nameOf(String name) {
        for (H265Profile p : values()) {
            if (p.mName.equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public static H265Profile valueOf(int value) {
        for (H265Profile l : values()) {
            if (l.mValue == value) {
                return l;
            }
        }
        return null;
    }
}
