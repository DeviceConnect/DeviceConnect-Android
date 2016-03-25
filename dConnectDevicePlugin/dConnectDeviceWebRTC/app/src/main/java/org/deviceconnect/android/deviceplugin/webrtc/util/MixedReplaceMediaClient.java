/*
 MixedReplaceMediaClient.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.util.Log;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.MimeConfig;
import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Mixed Replace Media Client.
 * @author NTT DOCOMO, INC.
 */
public class MixedReplaceMediaClient {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "MRMC";

    /**
     * Defined a time out of connecting.
     * Constants value: {@value}
     */
    private static final int TIMEOUT = 3 * 60 * 1000;

    /**
     * Defined a status code of success.
     * Constants value: {@value}
     */
    private static final int HTTP_RESPONSE_SUCCESS = 200;

    /**
     * Defined a get method.
     * Constants value: {@value}
     */
    private static final String HTTP_GET = "GET";

    /**
     * Defined a content type of multipart.
     * Constants value: {@value}
     */
    private static final String CONTENT_TYPE_MULTIPART = "multipart/x-mixed-replace";

    /**
     * Defined a content type of image.
     * Constants value: {@value}
     */
    private static final String CONTENT_TYPE_IMAGE = "image/";

    /**
     * URI that the resource exists.
     */
    private String mUri;

    /**
     * Thread to access the uri.
     */
    private Thread mThread;

    /**
     * Analyze a multipart data.
     */
    private MimeStreamParser mMimeStreamParser;

    /**
     * Stop flag.
     */
    private volatile boolean mStopFlag;

    /**
     * Http.
     */
    private HttpURLConnection mConnection;

    /**
     * Listener that notifies an event.
     */
    private OnMixedReplaceMediaListener mOnMixedReplaceMediaListener;

    /**
     * Constructor.
     * @param uri uri
     */
    public MixedReplaceMediaClient(final String uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }
        mUri = uri;
    }

    /**
     * Set a listener that notifies an event.
     * @param l listener
     */
    public void setOnMixedReplaceMediaListener(final OnMixedReplaceMediaListener l) {
        mOnMixedReplaceMediaListener = l;
    }

    /**
     * Start to connect a server.
     */
    public synchronized void start() {
        if (mThread != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "this class is already running.");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "MixedReplaceMediaClient is start.");
            if (mOnMixedReplaceMediaListener == null) {
                Log.w(TAG, "OnMixedReplaceMediaListener is not set.");
            }
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(mUri);
                    mConnection = (HttpURLConnection) url.openConnection();
                    mConnection.setConnectTimeout(TIMEOUT);
                    mConnection.setRequestMethod(HTTP_GET);
                    mConnection.setDoInput(true);
                    mConnection.connect();
                    int resp = mConnection.getResponseCode();
                    if (resp == HTTP_RESPONSE_SUCCESS) {
                        String contentType = mConnection.getContentType();
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "content type: " + contentType);
                        }

                        if (mOnMixedReplaceMediaListener != null) {
                            mOnMixedReplaceMediaListener.onConnected();
                        }

                        if (contentType == null || contentType.startsWith(CONTENT_TYPE_IMAGE)) {
                            readImage(mConnection.getInputStream(), mConnection.getContentLength());
                        } else if (contentType.startsWith(CONTENT_TYPE_MULTIPART)) {
                            readMultiPart(mConnection.getInputStream(), contentType);
                        } else {
                            notifyError(MixedReplaceMediaError.MIME_TYPE_ERROR);
                        }
                    } else {
                        notifyError(MixedReplaceMediaError.HTTP_ERROR);
                    }
                } catch (MalformedURLException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "error", e);
                    }
                    notifyError(MixedReplaceMediaError.URI_ERROR);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "error", e);
                    }
                    notifyError(MixedReplaceMediaError.UNKNOWN);
                } catch (OutOfMemoryError e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "error", e);
                    }
                    notifyError(MixedReplaceMediaError.OUT_OF_MEMORY_ERROR);
                } finally {
                    if (mConnection != null) {
                        mConnection.disconnect();
                        mConnection = null;
                    }
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MixedReplaceMediaClient is close.");
                }
            }
        });
        mThread.start();
    }

    /**
     * Stop to connect a server.
     */
    public synchronized void stop() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "MixedReplaceMediaClient is stop.");
        }

        mStopFlag = true;

        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }

        if (mMimeStreamParser != null) {
            mMimeStreamParser.stop();
            mMimeStreamParser = null;
        }

        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }
    }

    /**
     * Gets a image data from server.
     * @param in InputStream
     * @param length data length
     * @throws IOException occurs if failed to get a data
     */
    private void readImage(final InputStream in, final long length) throws IOException {
        if (mStopFlag) {
            throw new IllegalStateException("");
        }

        try {
            final byte[] buf = readBuffer(in);

            int fps = 5000;
            while (!mStopFlag) {
                long oldTime = System.currentTimeMillis();
                notifyData(new ByteArrayInputStream(buf));
                long newTime = System.currentTimeMillis();
                long sleepTime = fps - (newTime - oldTime);
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            notifyError(MixedReplaceMediaError.OUT_OF_MEMORY_ERROR);
        } catch (Throwable e) {
            notifyError(MixedReplaceMediaError.UNKNOWN);
        }
    }

    /**
     * Read the buffer from InputStream.
     * @param in InputStream
     * @return buffer of image
     * @throws IOException IO error occurred
     */
    private byte[] readBuffer(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[4096];
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        return out.toByteArray();
    }

    /**
     * Gets a multipart data from server.
     * @param in InputStream
     * @param contentType content type
     * @throws MimeException occurs if data format is invalid
     * @throws IOException occurs if failed to get a data
     */
    private void readMultiPart(final InputStream in, final String contentType) throws MimeException, IOException {
        final ContentHandler contentHandler = new AbstractContentHandler() {
            @Override
            public void body(final BodyDescriptor bd, final InputStream is) throws MimeException, IOException {
                super.body(bd, is);
                if (bd.getContentLength() > 0) {
                    try {
                        notifyData(is);
                    } catch (OutOfMemoryError e) {
                        notifyError(MixedReplaceMediaError.OUT_OF_MEMORY_ERROR);
                    } catch (Throwable e) {
                        notifyError(MixedReplaceMediaError.UNKNOWN);
                    }
                }
            }
        };

        int fps = 20;
        MimeConfig config = new MimeConfig();
        config.setMaxHeaderCount(-1);
        config.setMaxHeaderLen(-1);
        config.setMaxLineLen(-1);
        config.setHeadlessParsing(contentType);
        mMimeStreamParser = new MimeStreamParser(config);
        mMimeStreamParser.setContentHandler(contentHandler);
        while (!mStopFlag) {
            long oldTime = System.currentTimeMillis();
            mMimeStreamParser.parse(in);
            long newTime = System.currentTimeMillis();
            long sleepTime = fps - (newTime - oldTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * Notifies a data that get from a server.
     * @param data image data
     */
    private void notifyData(final InputStream data) {
        if (mOnMixedReplaceMediaListener != null) {
            mOnMixedReplaceMediaListener.onReceivedData(data);
        }
    }

    /**
     * Notifies that an error has occurred.
     * @param error error data
     */
    private void notifyError(final MixedReplaceMediaError error) {
        if (mOnMixedReplaceMediaListener != null) {
            mOnMixedReplaceMediaListener.onError(error);
        }
    }

    /**
     * Defined an error of MixedReplaceMediaClient.
     */
    public enum MixedReplaceMediaError {
        /**
         * Defined an error of http communication.
         */
        HTTP_ERROR(1),
        /**
         * Defined an error that does not support a mime type.
         */
        MIME_TYPE_ERROR(2),

        /**
         * Defined an error that content length is invalid.
         */
        CONTENT_LENGTH_ERROR(3),

        /**
         * Defined an error that uri is invalid.
         */
        URI_ERROR(4),

        /**
         * Defined an out of memory error.
         */
        OUT_OF_MEMORY_ERROR(5),

        /**
         * Defined an unknown error.
         */
        UNKNOWN(6);

        /**
         * Error code.
         */
        private int mErrorCode;

        /**
         * Constructor.
         * @param errorCode error code
         */
        MixedReplaceMediaError(final int errorCode) {
            mErrorCode = errorCode;
        }

        /**
         * Get a error code.
         * @return error code
         */
        public int getErrorCode() {
            return mErrorCode;
        }

        /**
         * Get a MixedReplaceMediaError instance from value.
         * @param errorCode error code
         * @return MixedReplaceMediaError
         */
        public static MixedReplaceMediaError valueOf(final int errorCode) {
            for (MixedReplaceMediaError error : values()) {
                if (error.getErrorCode() == errorCode) {
                    return error;
                }
            }
            return null;
        }
    }

    /**
     * This listener to notify the events of MixedReplaceMediaClient.
     * @author NTT DOCOMO, INC.
     */
    public interface OnMixedReplaceMediaListener {
        void onConnected();
        /**
         * Notifies the received data.
         * @param in received data
         */
        void onReceivedData(InputStream in);

        /**
         * Notifies the error.
         * @param error error
         */
        void onError(MixedReplaceMediaError error);
    }
}