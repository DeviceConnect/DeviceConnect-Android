/*
 ThetaFileInfo
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

/**
 * Information of file in the storage of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaFileInfo {

    /**
     * The filename.
     * <p>
     * this name if same as the name of file object in the storage THETA.
     * </p>
     */
    public final String mName;

    /**
     * The file size.
     */
    public final int mSize;

    /**
     * The MIME type of this file.
     */
    public final String mMimeType;

    /**
     * The handle of file object in the storage THETA.
     */
    final int mHandle;

    /**
     * Constructor.
     *
     * @param name filename
     * @param mimeType MIME type
     * @param size file size
     * @param handle the handle of file object in the storage THETA
     */
    ThetaFileInfo(final String name, final String mimeType, final int size, final int handle) {
        mName = name;
        mSize = size;
        mMimeType = mimeType;
        mHandle = handle;
    }
}
