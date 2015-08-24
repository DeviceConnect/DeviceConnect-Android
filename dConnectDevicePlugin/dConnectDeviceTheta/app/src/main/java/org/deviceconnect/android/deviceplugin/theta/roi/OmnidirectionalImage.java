package org.deviceconnect.android.deviceplugin.theta.roi;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.IntBuffer;

public class OmnidirectionalImage {

    private final String mMimeType = "image/jpeg";
    private final String mUri;
    private final Bitmap mBitmap;

    public OmnidirectionalImage(final String uri, final String requestOrigin) throws IOException {
        mUri = uri;

        InputStream is = null;
        try {
            URLConnection conn = new URL(uri).openConnection();
            conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, requestOrigin);
            is = conn.getInputStream();
            mBitmap = BitmapFactory.decodeStream(is);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            IntBuffer buf = IntBuffer.allocate(width * height);
            IntBuffer tmp = IntBuffer.allocate(width * height);
            mBitmap.copyPixelsToBuffer(buf);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    tmp.put((height - i - 1) * width + j, buf.get(i * width + j));
                }
            }
            mBitmap.copyPixelsFromBuffer(tmp);
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
