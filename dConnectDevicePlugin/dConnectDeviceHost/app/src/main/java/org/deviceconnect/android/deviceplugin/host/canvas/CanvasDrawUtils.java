/*
 CanvasDrawUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.canvas;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
     * Defined a size of size.
     */
    private static final int MAX_SIZE = 2048;

    /**
     * Constructor.
     */
    private CanvasDrawUtils() {
    }

    /**
     * Gets a bitmap from data.
     *
     * @param data data
     * @return Bitmap or null on error
     */
    public static Bitmap getBitmap(final byte[] data) {
        if (data != null) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                return BitmapFactory.decodeByteArray(data, 0, data.length, options);
            } catch (OutOfMemoryError e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets binary from uri.
     *
     * @param context context
     * @param uri     uri
     * @return byte[] or null on error
     */
    public static byte[] getContentData(final Context context, final String uri) throws OutOfMemoryError {
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
        } catch (OutOfMemoryError e) {
            throw new OutOfMemoryError(e.getMessage());
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
     * Get MimeType.
     *
     * @param url file's url
     * @return file's MimeType
     */
    public static String getMimeType(final String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Checks whether data is valid.
     *
     * @param data image data
     * @return true if data is valid, false otherwise
     */
    public static boolean checkBitmap(byte[] data) {
        if (data != null) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                if (options.outWidth > MAX_SIZE || options.outHeight > MAX_SIZE) {
                    return false;
                } else {
                    return options.outWidth > 0 && options.outHeight > 0;
                }
            } catch (Throwable t) {
                // format error if an error has occurred
                return false;
            }
        }
        return false;
    }

    public static byte[] getData(String uri) throws OutOfMemoryError {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        byte[] data = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            inputStream = connection.getInputStream();
            data = readAll(inputStream);
        } catch (OutOfMemoryError e) {
            throw new OutOfMemoryError(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return data;
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = inputStream.read(buffer);
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }
}
