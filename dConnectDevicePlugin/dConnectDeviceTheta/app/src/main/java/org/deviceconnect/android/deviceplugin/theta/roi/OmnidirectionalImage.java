package org.deviceconnect.android.deviceplugin.theta.roi;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class OmnidirectionalImage {

    private final String mMimeType = "image/jpeg";
    private final String mUri;
    private final Bitmap mBitmap;

    public OmnidirectionalImage(final String uri) throws IOException {
        mUri = uri;

        InputStream is = null;
        try {
            URLConnection conn = new URL(uri).openConnection();
            is = conn.getInputStream();
            mBitmap = BitmapFactory.decodeStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String getUri() {
        return mUri;
    }

    Bitmap getData() {
        return mBitmap;
    }

    void destroy() {
        mBitmap.recycle();
    }

}
