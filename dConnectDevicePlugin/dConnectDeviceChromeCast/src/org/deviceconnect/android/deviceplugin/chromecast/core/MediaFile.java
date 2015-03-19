/*
 MediaFile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;


import org.deviceconnect.android.provider.FileManager;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Media file.
 * @author NTT DOCOMO, INC.
 */
public class MediaFile {

    /** The internal path of a media file. */
    final File mFile;

    /** MimeType of a media file. */
    final String mMimeType;

    /**
     * Constructor.
     *
     * @param file the internal path of a media file
     * @param mimeType MimeType of a media file
     */
    public MediaFile(final File file, final String mimeType) {
        mFile = file;
        mMimeType = mimeType;
    }

    /**
     * Get the path to expose a media file.
     * @return
     */
    String getPath() {
        return "/" + mFile.getName();
    }
}
