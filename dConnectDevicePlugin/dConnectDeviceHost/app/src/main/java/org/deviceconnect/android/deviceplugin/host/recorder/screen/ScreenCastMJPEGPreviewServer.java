package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MixedReplaceMediaServer;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;


@TargetApi(21)
class ScreenCastMJPEGPreviewServer implements PreviewServer {

    private static final String MIME_TYPE = "video/x-mjpeg";

    private final Logger mLogger = Logger.getLogger("host.dplugin");

    private final AbstractPreviewServerProvider mServerProvider;

    private final Object mLockObj = new Object();

    private final Context mContext;

    private final ScreenCastManager mScreenCastMgr;

    private final ScreenCaster mPreview;

    private MixedReplaceMediaServer mServer;

    private final MixedReplaceMediaServer.Callback mMediaServerCallback = new MixedReplaceMediaServer.Callback() {
        @Override
        public boolean onAccept() {
            synchronized (mLockObj) {
                if (!mPreview.isStarted()) {
                    mPreview.start();
                }
            }
            return true;
        }
    };

    ScreenCastMJPEGPreviewServer(final Context context,
                                 final AbstractPreviewServerProvider serverProvider,
                                 final ScreenCastManager screenCastMgr) {
        mContext = context;
        mServerProvider = serverProvider;
        mScreenCastMgr = screenCastMgr;
        mPreview = new ScreenCaster();
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            final String uri;
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                mServer.setCallback(mMediaServerCallback);
                uri = mServer.start();
            } else {
                uri = mServer.getUrl();
            }
            callback.onStart(uri);
        }
    }

    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            mPreview.stop();
            mServerProvider.hideNotification();
        }
    }

    private class ScreenCaster {

        private boolean mIsStarted;

        private ImageScreenCast mScreenCast;

        private ImageReader mImageReader;

        private Thread mStreamingThread;

        private BroadcastReceiver mConfigChangeReceiver;

        boolean isStarted() {
            return mIsStarted;
        }

        synchronized void start() {
            if (!isStarted()) {
                registerConfigChangeReceiver();

                HostDeviceRecorder.PictureSize size = getRotatedSize();
                int w = size.getWidth();
                int h = size.getHeight();
                mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 4);
                mScreenCast = mScreenCastMgr.createScreenCast(mImageReader, size);
                mScreenCast.startCast();

                mStreamingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mServer != null) {
                            mLogger.info("Server URL: " + mServer.getUrl());
                        }
                        try {
                            while (mIsStarted) {
                                long start = System.currentTimeMillis();

                                Bitmap bitmap = mScreenCast.getScreenshot();
                                if (bitmap == null) {
                                    continue;
                                }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] media = baos.toByteArray();
                                mServer.offerMedia(media);

                                long end = System.currentTimeMillis();
                                double fps = mServerProvider.getMaxFrameRate();
                                long frameInterval = 1000L / (long) fps;
                                long interval = frameInterval - (end - start);
                                if (interval > 0) {
                                    Thread.sleep(interval);
                                }
                            }
                        } catch (InterruptedException e) {
                            // NOP.
                        } catch (Throwable e) {
                            e.printStackTrace();
                            mLogger.warning("MediaProjection is broken." + e.getMessage());
                            stopWebServer();
                        }
                    }
                });
                mStreamingThread.start();

                mIsStarted = true;
            }
        }

        private int getRotation() {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            return display.getRotation();
        }

        private HostDeviceRecorder.PictureSize getRotatedSize() {
            HostDeviceRecorder.PictureSize size = mServerProvider.getPreviewSize();
            int w;
            int h;
            switch (getRotation()) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    w = size.getWidth();
                    h = size.getHeight();
                    break;
                default:
                    w = size.getHeight();
                    h = size.getWidth();
                    break;
            }
            return new HostDeviceRecorder.PictureSize(w, h);
        }

        synchronized void stop() {
            if (isStarted()) {
                mStreamingThread.interrupt();
                mStreamingThread = null;
                mImageReader.close();
                mImageReader = null;
                mScreenCast.stopCast();
                mScreenCast = null;
                unregisterConfigChangeReceiver();
                mIsStarted = false;
            }
        }

        private synchronized void restart() {
            stop();
            start();
        }

        private void registerConfigChangeReceiver() {
            mConfigChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    restart();
                }
            };
            IntentFilter filter = new IntentFilter(
                    "android.intent.action.CONFIGURATION_CHANGED");
            mContext.registerReceiver(mConfigChangeReceiver, filter);
        }

        private void unregisterConfigChangeReceiver() {
            if (mConfigChangeReceiver != null) {
                mContext.unregisterReceiver(mConfigChangeReceiver);
                mConfigChangeReceiver = null;
            }
        }
    }
}
