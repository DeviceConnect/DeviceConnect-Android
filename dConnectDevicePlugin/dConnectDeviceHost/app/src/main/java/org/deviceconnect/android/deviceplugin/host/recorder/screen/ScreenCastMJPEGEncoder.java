package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.ImageReader;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.mjpeg.MJPEGEncoder;

import java.io.ByteArrayOutputStream;

public class ScreenCastMJPEGEncoder extends MJPEGEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ScreenCastMJPEG";

    private ScreenCastManager mScreenCastMgr;
    private EncodeThread mEncodeThread;

    ScreenCastMJPEGEncoder(ScreenCastManager screenCastManager) {
        mScreenCastMgr = screenCastManager;
    }

    @Override
    public void start() {
        if (mEncodeThread != null) {
            mEncodeThread.terminate();
        }
        mEncodeThread = new EncodeThread();
        mEncodeThread.setName("ScreenCast-MJPEG-Thread");
        mEncodeThread.start();
    }

    @Override
    public void stop() {
        if (mEncodeThread != null) {
            mEncodeThread.terminate();
            mEncodeThread = null;
        }
    }

    /**
     * MJPEG のエンコード処理を行うスレッド.
     */
    private class EncodeThread extends Thread {
        private ImageReader mImageReader;
        private ImageScreenCast mScreenCast;

        private void terminate() {
            if (mImageReader != null) {
                try {
                    mImageReader.close();
                } catch (Exception e) {
                    // ignore.
                }
            }

            interrupt();

            try {
                join(200);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        @Override
        public void run() {
            int w = getMJPEGQuality().getWidth();
            int h = getMJPEGQuality().getHeight();
            long frameInterval = 1000L / (long) getMJPEGQuality().getFrameRate();

            try {
                mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 4);
                mScreenCast = mScreenCastMgr.createScreenCast(mImageReader, w, h);
                mScreenCast.startCast();

                while (!interrupted()) {
                    long start = System.currentTimeMillis();

                    Bitmap bitmap = mScreenCast.getScreenshot();
                    if (bitmap == null) {
                        continue;
                    }

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, getMJPEGQuality().getQuality(), out);
                    postJPEG(out.toByteArray());

                    long interval = frameInterval - (System.currentTimeMillis() - start);
                    if (interval > 0) {
                        Thread.sleep(interval);
                    }
                }
            } catch (OutOfMemoryError e) {
                if (DEBUG) {
                    Log.w(TAG, "", e);
                }
            } catch (Exception e) {
                // ignore.
            } finally {
                if (mScreenCast != null) {
                    mScreenCast.stopCast();
                }
            }
        }
    }
}
