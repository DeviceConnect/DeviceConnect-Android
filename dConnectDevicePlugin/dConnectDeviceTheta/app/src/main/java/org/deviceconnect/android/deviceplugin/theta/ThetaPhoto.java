/*
 ThetaPhoto
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

/**
 * THETA Photo data.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaPhoto {

    /**
     * The binary data of photo.
     */
    public final byte[] mData;

    /**
     * The filename.
     */
    public final String mFilename;

    /**
     * The MIME type of this photo.
     */
    public final String mMimeType;

    /**
     * The service ID of THETA.
     */
    public final String mServiceId;

    /**
     * Constructor.
     *
     * @param data binary data of photo.
     * @param filename filename
     * @param mimeType MIME type of this photo.
     * @param serviceId service ID of THETA
     */
    ThetaPhoto(final byte[] data, final String filename,
          final String mimeType, final String serviceId) {
        mData = data;
        mFilename = filename;
        mMimeType = mimeType;
        mServiceId = serviceId;
    }
}
