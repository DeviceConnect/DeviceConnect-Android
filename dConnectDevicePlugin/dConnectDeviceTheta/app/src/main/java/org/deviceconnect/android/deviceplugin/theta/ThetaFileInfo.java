/*
 ThetaFileInfo
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Information of file in the storage of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaFileInfo {

    private static final SimpleDateFormat DATE_FORMAT_THETA = new SimpleDateFormat(
        "yyyyMMdd'T'HHmmss", Locale.getDefault());

    private static final SimpleDateFormat DATE_FORMAT_DEVICE_CONNECT = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.getDefault());

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
     * Captured date (RFC3339).
     */
    public final String mDate;

    /**
     * The handle of file object in the storage THETA.
     */
    final int mHandle;

    /**
     * Constructor.
     *
     * @param name filename
     * @param mimeType MIME type
     * @param date captured date
     * @param size file size
     * @param handle the handle of file object in the storage THETA
     */
    ThetaFileInfo(final String name, final String mimeType, String date, final int size, final int handle) {
        String parsedDate = null;
        try {
            if (date != null) {
                parsedDate = DATE_FORMAT_DEVICE_CONNECT.format(DATE_FORMAT_THETA.parse(date));
            }
        } catch (ParseException e) {
            // Nothing to do because this exception will not occur.
        }
        mDate = parsedDate;
        mName = name;
        mSize = size;
        mMimeType = mimeType;
        mHandle = handle;
    }
}
