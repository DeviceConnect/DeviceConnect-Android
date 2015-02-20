/*
 CanvasDrawUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.canvas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

/**
 * Canvas Draw Utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class CanvasDrawUtils {

    /**
     * Defined a size of buffer.
     */
    private static final int BUF_SIZE = 4096;

    /**
     * Constructor.
     */
    private CanvasDrawUtils() {
    }

    /**
     * Gets a bitmap from uri.
     * @param context context
     * @param uri uri
     * @return Bitmap or null on error
     */
    public static Bitmap getBitmap(final Context context, final String uri) {
        byte[] buf = getContentData(context, uri);
        if (buf != null) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                return BitmapFactory.decodeByteArray(buf, 0, buf.length, options);
            } catch (OutOfMemoryError e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets binary from uri.
     * @param context context
     * @param uri uri
     * @return byte[] or null on error
     */
    public static byte[] getContentData(final Context context, final String uri) {
        if (uri == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = null;
        byte[] buf = new byte[BUF_SIZE];
        int len;
        try {
            ContentResolver r = context.getContentResolver();
            in = r.openInputStream(Uri.parse(uri));
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks whether uri is valid.
     * @param context context
     * @param uri uri
     * @return true if uri is valid, false otherwise
     */
    public static boolean checkBitmap(final Context context, final String uri) {
        byte[] buf = getContentData(context, uri);
        if (buf != null) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(buf, 0, buf.length, options);
                return options.outWidth > 0 && options.outHeight > 0;
            } catch (Exception e) {
                // format error if an error has occurred
                return false;
            }
        }
        return false;
    }
}
