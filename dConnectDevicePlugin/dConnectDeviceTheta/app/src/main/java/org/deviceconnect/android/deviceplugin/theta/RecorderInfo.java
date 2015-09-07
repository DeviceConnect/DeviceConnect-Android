/*
 RecorderInfo
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.PtpipInitiator;

/**
 * Information of media recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public class RecorderInfo {

    public static final RecorderInfo PHOTO = new RecorderInfo(
        "0", "THETA", "image/jpeg", 2048, 1024);

    public static final RecorderInfo VIDEO = new RecorderInfo(
        "1", "THETA", "video/mov", 1920, 1080);

    public static final short STATUS_RECORDING =
        PtpipInitiator.DEVICE_PROP_VALUE_CAPTURE_STATUS_CONTINUOUS_SHOOTING_RUNNING;

    public static final short STATUS_INACTIVE =
        PtpipInitiator.DEVICE_PROP_VALUE_CAPTURE_STATUS_WAIT;

    public final String mId;
    public final String mMimeType;
    public final String mName;
    public final int mImageWidth;
    public final int mImageHeight;

    private RecorderInfo(final String id, final String name, final String mimeType,
                         final int imageWidth, final int imageHeight) {
        mId = id;
        mName = name;
        mMimeType = mimeType;
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
    }

}
