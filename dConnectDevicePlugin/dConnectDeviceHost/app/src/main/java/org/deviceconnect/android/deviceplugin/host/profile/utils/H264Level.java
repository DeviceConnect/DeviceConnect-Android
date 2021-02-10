package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.media.MediaCodecInfo;

public enum H264Level {
    AVCLevel1("AVCLevel1", MediaCodecInfo.CodecProfileLevel.AVCLevel1),
    AVCLevel11("AVCLevel1", MediaCodecInfo.CodecProfileLevel.AVCLevel1),
    AVCLevel12("AVCLevel12", MediaCodecInfo.CodecProfileLevel.AVCLevel12),
    AVCLevel13("AVCLevel13", MediaCodecInfo.CodecProfileLevel.AVCLevel13),
    AVCLevel1b("AVCLevel1b", MediaCodecInfo.CodecProfileLevel.AVCLevel1b),
    AVCLevel2("AVCLevel2", MediaCodecInfo.CodecProfileLevel.AVCLevel2),
    AVCLevel21("AVCLevel21", MediaCodecInfo.CodecProfileLevel.AVCLevel21),
    AVCLevel22("AVCLevel22", MediaCodecInfo.CodecProfileLevel.AVCLevel22),
    AVCLevel3("AVCLevel3", MediaCodecInfo.CodecProfileLevel.AVCLevel3),
    AVCLevel31("AVCLevel31", MediaCodecInfo.CodecProfileLevel.AVCLevel31),
    AVCLevel32("AVCLevel32", MediaCodecInfo.CodecProfileLevel.AVCLevel32),
    AVCLevel4("AVCLevel4", MediaCodecInfo.CodecProfileLevel.AVCLevel4),
    AVCLevel41("AVCLevel41", MediaCodecInfo.CodecProfileLevel.AVCLevel41),
    AVCLevel42("AVCLevel42", MediaCodecInfo.CodecProfileLevel.AVCLevel42),
    AVCLevel5("AVCLevel5", MediaCodecInfo.CodecProfileLevel.AVCLevel5),
    AVCLevel51("AVCLevel51", MediaCodecInfo.CodecProfileLevel.AVCLevel51),
    AVCLevel52("AVCLevel52", MediaCodecInfo.CodecProfileLevel.AVCLevel52),
    AVCLevel6("AVCLevel6", MediaCodecInfo.CodecProfileLevel.AVCLevel6),
    AVCLevel61("AVCLevel61", MediaCodecInfo.CodecProfileLevel.AVCLevel61),
    AVCLevel62("AVCLevel62", MediaCodecInfo.CodecProfileLevel.AVCLevel62);

    private final String mName;
    private final int mValue;

    H264Level(String name, int value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public int getValue() {
        return mValue;
    }

    public static H264Level nameOf(String name) {
        for (H264Level l : values()) {
            if (l.mName.equalsIgnoreCase(name)) {
                return l;
            }
        }
        return null;
    }

    public static H264Level valueOf(int value) {
        for (H264Level l : values()) {
            if (l.mValue == value) {
                return l;
            }
        }
        return null;
    }
}
