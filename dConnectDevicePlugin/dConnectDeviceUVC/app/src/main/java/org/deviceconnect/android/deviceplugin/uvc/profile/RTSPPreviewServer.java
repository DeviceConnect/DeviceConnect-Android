package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.view.Surface;

import com.serenegiant.usb.UVCCamera;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.rtsp.RtspServerImpl;
import net.majorkernelpanic.streaming.video.SurfaceH264Stream;
import net.majorkernelpanic.streaming.video.VideoQuality;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class RTSPPreviewServer implements PreviewServer,
        UVCDeviceManager.PreviewListener,
        RtspServer.Delegate {

    private static final String SERVER_NAME = "UVC Plugin RTSP Server";

    private final Context mContext;

    private final RtspServer mServer;

    private final UVCDeviceManager mDeviceMgr;

    private final UVCDevice mDevice;

    private SurfaceH264Stream mVideoStream;

    private final Logger mLogger = Logger.getLogger("uvc.plugin");

    private final Object mLock = new Object();

    RTSPPreviewServer(final Context context, final UVCDeviceManager mgr, final UVCDevice device) {
        mContext = context;
        mDeviceMgr = mgr;
        mDevice = device;
        mServer = new RtspServerImpl(SERVER_NAME);
        mServer.setDelegate(this);
    }

    @Override
    public String getUrl() {
        return "rtsp://localhost:" + mServer.getPort();
    }

    @Override
    public String getMimeType() {
        return "video/x-rtp";
    }

    @Override
    public void start(final OnWebServerStartCallback callback) {
        if (mServer.start()) {
            callback.onStart(getUrl());
        } else {
            callback.onFail();
        }
    }

    @Override
    public void stop() {
        mDevice.stopPreview();
        mDeviceMgr.removePreviewListener(this);
        mServer.stop();
    }

    @Override
    public Session generateSession(final String uri, final Socket clientSocket) {
        try {
            if (!mDevice.startPreview()) {
                mLogger.log(Level.SEVERE, "Failed to start preview: Device ID = " + mDevice.getId());
                return null;
            }
            mDeviceMgr.addPreviewListener(this);

            VideoQuality videoQuality = new VideoQuality();
            videoQuality.resX = mDevice.getPreviewWidth();
            videoQuality.resY = mDevice.getPreviewHeight();
            videoQuality.bitrate = 256 * 8 * 1024; //1KB //mServerProvider.getPreviewBitRate();
            videoQuality.framerate = (int) mDevice.getFrameRate();

            synchronized (mLock) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                mVideoStream = new SurfaceH264Stream(prefs, videoQuality);
            }

            SessionBuilder builder = new SessionBuilder();
            builder.setContext(mContext);
            builder.setVideoStream(mVideoStream);
            builder.setVideoQuality(videoQuality);

            Session session = builder.build();
            session.setOrigin(clientSocket.getLocalAddress().getHostAddress());
            if (session.getDestination() == null) {
                session.setDestination(clientSocket.getInetAddress().getHostAddress());
            }
            mLogger.info("Created RTSP session: " + videoQuality.resX + " x " + videoQuality.resY);
            return session;
        } catch (IOException e) {
            mLogger.log(Level.SEVERE, "Failed to generate RTSP session", e);
            return null;
        }
    }

    @Override
    public void onFrame(final UVCDevice device, final byte[] frame, final int frameFormat, final int width, final int height) {
        if (frameFormat != UVCCamera.FRAME_FORMAT_MJPEG) {
            mLogger.warning("onFrame: unsupported frame format: " + frameFormat);
            return;
        }
        //mLogger.info("onFrame: " + width + " x " + height);

        SurfaceH264Stream stream = mVideoStream;
        if (stream != null) {
            Surface surface = stream.getInputSurface();
            Canvas canvas = surface.lockCanvas(null);
            if (canvas == null) {
                mLogger.severe("Failed to lock canvas");
                return;
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(frame, 0, frame.length);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    bitmap.recycle();
                    //mLogger.info("onFrame: draw JPEG frame to canvas: " + width + " x " + height);
                } else {
                    mLogger.warning("onFrame: Failed to decode JPEG to bitmap");
                }
            } finally {
                surface.unlockCanvasAndPost(canvas);
            }
        }
    }
}
