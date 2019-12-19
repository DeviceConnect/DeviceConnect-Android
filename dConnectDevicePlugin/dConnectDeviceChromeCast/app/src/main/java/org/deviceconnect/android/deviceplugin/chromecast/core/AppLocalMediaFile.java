/*
 AppLocalMediaFile.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AppLocalMediaFile implements MediaFile {

    final File mFile;

    public AppLocalMediaFile(final File file) {
        mFile = file;
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public InputStream open(final Context context) throws IOException {
        return new FileInputStream(mFile);
    }
}
