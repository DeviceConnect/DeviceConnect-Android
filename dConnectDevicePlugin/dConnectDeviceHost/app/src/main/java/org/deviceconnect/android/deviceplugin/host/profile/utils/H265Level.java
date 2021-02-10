package org.deviceconnect.android.deviceplugin.host.profile.utils;

import android.media.MediaCodecInfo;

public enum H265Level {
    HEVCHighTierLevel1("HEVCHighTierLevel1", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel1),
    HEVCHighTierLevel2("HEVCHighTierLevel2", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel2),
    HEVCHighTierLevel21("HEVCHighTierLevel21", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel21),
    HEVCHighTierLevel3("HEVCHighTierLevel3", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel3),
    HEVCHighTierLevel31("HEVCHighTierLevel31", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel31),
    HEVCHighTierLevel4("HEVCHighTierLevel4", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel4),
    HEVCHighTierLevel41("HEVCHighTierLevel41", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel41),
    HEVCHighTierLevel5("HEVCHighTierLevel5", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel5),
    HEVCHighTierLevel51("HEVCHighTierLevel51", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel51),
    HEVCHighTierLevel52("HEVCHighTierLevel52", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel52),
    HEVCHighTierLevel6("HEVCHighTierLevel6", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel6),
    HEVCHighTierLevel61("HEVCHighTierLevel61", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel61),
    HEVCHighTierLevel62("HEVCHighTierLevel62", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel62),
    HEVCMainTierLevel1("HEVCMainTierLevel1", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel1),
    HEVCMainTierLevel2("HEVCMainTierLevel2", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel2),
    HEVCMainTierLevel21("HEVCMainTierLevel21", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel21),
    HEVCMainTierLevel3("HEVCMainTierLevel3", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel3),
    HEVCMainTierLevel31("HEVCMainTierLevel31", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel31),
    HEVCMainTierLevel4("HEVCMainTierLevel4", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel4),
    HEVCMainTierLevel41("HEVCMainTierLevel41", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel41),
    HEVCMainTierLevel5("HEVCMainTierLevel5", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel5),
    HEVCMainTierLevel51("HEVCMainTierLevel51", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel51),
    HEVCMainTierLevel52("HEVCMainTierLevel52", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel52),
    HEVCMainTierLevel6("HEVCMainTierLevel6", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel6),
    HEVCMainTierLevel61("HEVCMainTierLevel61", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel61),
    HEVCMainTierLevel62("HEVCMainTierLevel62", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel62);

    private final String mName;
    private final int mValue;

    H265Level(String name, int value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public int getValue() {
        return mValue;
    }

    public static H265Level nameOf(String name) {
        for (H265Level l : values()) {
            if (l.mName.equalsIgnoreCase(name)) {
                return l;
            }
        }
        return null;
    }

    public static H265Level valueOf(int value) {
        for (H265Level l : values()) {
            if (l.mValue == value) {
                return l;
            }
        }
        return null;
    }
}
