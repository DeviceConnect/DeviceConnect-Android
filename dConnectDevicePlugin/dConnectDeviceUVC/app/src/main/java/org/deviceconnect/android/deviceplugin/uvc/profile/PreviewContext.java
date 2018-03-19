package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

class PreviewContext {

    Integer mWidth;

    Integer mHeight;

    final PreviewServer mServer;

    final Logger mLogger = Logger.getLogger("uvc.dplugin");

    PreviewContext(final PreviewServer server) {
        if (server == null) {
            throw new IllegalArgumentException();
        }
        mServer = server;
    }

    boolean willResize() {
        return mWidth != null || mHeight != null;
    }

    byte[] resize(final byte[] frame) {
        byte[] resizedBytes = null;
        try {
            Bitmap src = BitmapFactory.decodeByteArray(frame, 0, frame.length);
            if (src == null) {
                mLogger.warning("MotionJPEG Frame could not be decoded to bitmap.");
                return null;
            }

            int w = mWidth != null ? mWidth : src.getWidth();
            int h = mHeight != null ? mHeight : src.getHeight();

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(src, w, h, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            resizedBytes = baos.toByteArray();
            resizedBitmap.recycle();
        } catch (OutOfMemoryError e) {
            mLogger.warning("MotionJPEG Frame could not be decoded to bitmap for: " + e.getMessage());
        }
        return resizedBytes;
    }
}
