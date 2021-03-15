/*
 FrameType.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

/**
 * フレームのタイプを定義します.
 *
 * @author NTT DOCOMO, INC.
 */
public enum FrameType {
    /**
     * Motion JPEG.
     */
    MJPEG(0x07),

    /**
     * 非圧縮(YUV).
     */
    UNCOMPRESSED(0x05),

    /**
     * H264.
     */
    H264(0x14),

    /**
     * 不明なフレーム.
     */
    UNKNOWN(-1);

    int mValue;

    FrameType(final int value) {
        mValue = value;
    }

    static FrameType valueOf(final int value) {
        for (FrameType type : values()) {
            if (type.mValue == value) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
