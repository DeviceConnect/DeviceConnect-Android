package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.media.MediaCodecInfo;

public enum H264Profile {
    AVCProfileBaseline("AVCProfileBaseline", MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline),
    AVCProfileConstrainedBaseline("AVCProfileConstrainedBaseline", MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedBaseline),
    AVCProfileConstrainedHigh("AVCProfileConstrainedHigh", MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedHigh),
    AVCProfileExtended("AVCProfileExtended", MediaCodecInfo.CodecProfileLevel.AVCProfileExtended),
    AVCProfileHigh("AVCProfileHigh", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh),
    AVCProfileHigh10("AVCProfileHigh10", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh10),
    AVCProfileHigh422("AVCProfileHigh422", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh422),
    AVCProfileHigh444("AVCProfileHigh444", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh444),
    AVCProfileMain("AVCProfileMain", MediaCodecInfo.CodecProfileLevel.AVCProfileMain);

    private final String mName;
    private final int mValue;

    H264Profile(String name, int value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public int getValue() {
        return mValue;
    }

    public static H264Profile nameOf(String name) {
        for (H264Profile p : values()) {
            if (p.mName.equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public static H264Profile valueOf(int value) {
        for (H264Profile l : values()) {
            if (l.mValue == value) {
                return l;
            }
        }
        return null;
    }
}
