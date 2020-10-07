package org.deviceconnect.android.deviceplugin.uvc.recorder.preview;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.libmedia.streaming.rtsp.RtspServer;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.RtspSession;
import org.deviceconnect.android.libmedia.streaming.rtsp.session.video.H264VideoStream;
import org.deviceconnect.android.libmedia.streaming.video.CanvasVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

import java.io.IOException;

import javax.net.ssl.SSLContext;

public class RTSPPreviewServer implements PreviewServer {

    private static final String SERVER_NAME = "UVC Plugin RTSP Server";

    private RtspServer mServer;
    private UVCDeviceManager mDeviceMgr;
    private UVCDevice mDevice;
    private int mPort;

    public RTSPPreviewServer(final UVCDeviceManager mgr, final UVCDevice device, final int port) {
        mDeviceMgr = mgr;
        mDevice = device;
        mPort = port;
    }

    @Override
    public String getUrl() {
        return "rtsp://localhost:" + mPort;
    }

    @Override
    public String getMimeType() {
        return "video/x-rtp";
    }

    @Override
    public boolean isStarted() {
        return mServer != null;
    }

    @Override
    public boolean usesSSLContext() {
        return false;
    }

    @Override
    public void setSSLContext(SSLContext sslContext) {

    }

    @Override
    public SSLContext getSSLContext() {
        return null;
    }

    @Override
    public void start(final OnWebServerStartCallback callback) {
        if (mServer == null) {
            mServer = new RtspServer();
            mServer.setServerName(SERVER_NAME);
            mServer.setServerPort(mPort);
            mServer.setCallback(mCallback);

            try {
                mServer.start();
            } catch (IOException e) {
                callback.onFail();
                return;
            }
        }

        callback.onStart(getUrl());
    }

    @Override
    public void stop() {
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }

    private final RtspServer.Callback mCallback = new RtspServer.Callback() {
        @Override
        public void createSession(RtspSession session) {
            UVCStream uvcStream = new UVCStream();

            VideoQuality videoQuality = uvcStream.getVideoEncoder().getVideoQuality();
            videoQuality.setVideoWidth(mDevice.getPreviewWidth());
            videoQuality.setVideoHeight(mDevice.getPreviewHeight());
            videoQuality.setBitRate(1024 * 1024);
            videoQuality.setFrameRate((int) mDevice.getFrameRate());
            videoQuality.setIFrameInterval(2);

            session.setVideoMediaStream(uvcStream);
        }

        @Override
        public void releaseSession(RtspSession session) {

        }
    };

    private class UVCStream extends H264VideoStream {
        private UVCEncoder mUVCEncoder;

        UVCStream() {
            mUVCEncoder = new UVCEncoder();
        }

        @Override
        public VideoEncoder getVideoEncoder() {
            return mUVCEncoder;
        }
    }

    private class UVCEncoder extends CanvasVideoEncoder implements UVCDeviceManager.PreviewListener {
        private Bitmap mBitmap;
        private Paint mPaint = new Paint();

        void setBitmap(Bitmap bitmap) {
            synchronized (this) {
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                mBitmap = bitmap;
            }
        }

        @Override
        protected void onStartSurfaceDrawing() {
            super.onStartSurfaceDrawing();

            if (!mDevice.startPreview()) {
                // TODO
            }
            mDeviceMgr.addPreviewListener(this);
        }

        @Override
        protected void onStopSurfaceDrawing() {
            mDevice.stopPreview();
            mDeviceMgr.removePreviewListener(this);

            super.onStopSurfaceDrawing();
        }

        @Override
        public void draw(Canvas canvas, int width, int height) {
            synchronized (this) {
                if (mBitmap != null && !mBitmap.isRecycled()) {
                    canvas.drawBitmap(mBitmap, 0, 0, mPaint);
                }
            }
        }

        @Override
        public void onFrame(UVCDevice device, byte[] frame, int frameFormat, int width, int height) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(frame, 0, frame.length);
            if (bitmap != null) {
                setBitmap(bitmap);
            }
        }
    }
}
