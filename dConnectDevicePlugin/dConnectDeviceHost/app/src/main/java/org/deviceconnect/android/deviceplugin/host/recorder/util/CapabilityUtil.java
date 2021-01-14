/*
 CapabilityUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

public final class CapabilityUtil {
    private CapabilityUtil() {
    }

    /**
     * MediaCodec でサポートされている解像度の最大値を取得します.
     *
     * @param mimeType マイムタイプ
     * @return サポートされている解像度の最大値
     */
    public static Size getSupportedMaxSize(String mimeType) {
        List<Size> sizeList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
            for (MediaCodecInfo codecInfo : list.getCodecInfos()) {
                if (codecInfo.isEncoder() &&
                        isHardware(codecInfo) &&
                        isMediaCodecInfo(codecInfo, mimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
                    Size size = getSizeFromCodecInfo(codecInfo, mimeType);
                    if (size != null) {
                        sizeList.add(size);
                    }
                }
            }
        } else {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                if (codecInfo.isEncoder() &&
                        isHardware(codecInfo) &&
                        isMediaCodecInfo(codecInfo, mimeType, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
                    Size size = getSizeFromCodecInfo(codecInfo, mimeType);
                    if (size != null) {
                        sizeList.add(size);
                    }
                }
            }
        }

        Size size = null;
        int max = 0;

        for (Size s : sizeList) {
            int d = s.getWidth() * s.getHeight();
            if (max < d) {
                max = d;
                size = s;
            }
        }

        return size;
    }

    /**
     * MediaCodecInfo から解像度を取得します.
     *
     * @param codecInfo コーデック情報
     * @param mimeType マイムタイプ
     * @return 解像度
     */
    private static Size getSizeFromCodecInfo(MediaCodecInfo codecInfo, String mimeType) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (!type.startsWith(mimeType)) {
                continue;
            }

            try {
                MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                if (videoCapabilities != null) {
                    int w = videoCapabilities.getSupportedWidths().getUpper();
                    int h = videoCapabilities.getSupportedHeights().getUpper();
                    return new Size(w, h);
                }
            } catch (Exception e) {
                // ignore.
            }
        }
        return null;
    }

    /**
     * 指定された MediaCodecInfo のマイムタイプとカラーフォーマットが一致するか確認します.
     *
     * @param codecInfo 確認する MediaCodecInfo
     * @param mimeType マイムタイプ
     * @param colorFormat カラーフォーマット
     * @return 一致する場合はtrue、それ以外はfalse
     */
    private static boolean isMediaCodecInfo(MediaCodecInfo codecInfo, String mimeType, int colorFormat) {
        if (!codecInfo.isEncoder()) {
            return false;
        }

        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (!type.equalsIgnoreCase(mimeType)) {
                continue;
            }

            try {
                MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                for (int i = 0; i < capabilities.colorFormats.length; i++) {
                    int format = capabilities.colorFormats[i];
                    if (colorFormat == format) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // ignore.
            }
        }

        return false;
    }

    private static boolean isHardware(MediaCodecInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return info.isHardwareAccelerated();
        } else {
            // エンコーダ名が OMX.qcom. または OMX.Exynos. から始まる場合はハードウェアエンコーダ
            // エンコーダ名が OMX.google. から始まる場合はソフトウェアエンコーダ
            String name = info.getName();
            return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Exynos.");
        }
    }
}
