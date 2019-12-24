/*
 MediaStoreContent.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public class MediaStoreContent implements MediaFile {

    private final Uri mMediaUri;

    public MediaStoreContent(final Uri mediaUri) {
        if (mediaUri.getPathSegments() == null) {
            throw new IllegalArgumentException("mediaUri has no mediaId");
        }
        mMediaUri = mediaUri;
    }

    @Override
    public String getName() {
        return mMediaUri.getLastPathSegment();
    }

    @Override
    public InputStream open(final Context context) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        return resolver.openInputStream(mMediaUri);
    }
}
