/*
 HostDeviceCameraRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;


import android.content.Context;
import android.hardware.Camera;

import org.deviceconnect.android.deviceplugin.host.HostDevicePreviewServer;
import org.deviceconnect.android.provider.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Host Device Camera Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public abstract class HostDeviceCameraRecorder implements HostDevicePreviewServer {

    private static final int DEFAULT_PREVIEW_WIDTH_THRESHOLD = 640;

    private static final int DEFAULT_PREVIEW_HEIGHT_THRESHOLD = 480;

    protected final Context mContext;

    protected final CameraOverlay mCameraOverlay;

    protected final String mId;

    protected final String mName;

    protected final int mCameraId;

    protected boolean mIsInitialized;

    protected final List<PictureSize> mSupportedPictureSizes = new ArrayList<PictureSize>();

    protected final List<PictureSize> mSupportedPreviewSizes = new ArrayList<PictureSize>();

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    public HostDeviceCameraRecorder(final Context context, final String id, final String name,
                                    final CameraFacing facing, final int cameraId,
                                    final FileManager fileMgr) {
        mContext = context;
        mId = id;
        mName = name;
        mCameraId = cameraId;

        mCameraOverlay = new CameraOverlay(context, cameraId);
        mCameraOverlay.setFileManager(fileMgr);
        if (facing == CameraFacing.FRONT) {
            mCameraOverlay.setFacingDirection(-1);
        }
    }

    protected boolean isShowCamera() {
        return mCameraOverlay != null && mCameraOverlay.isShow();
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getCameraId() {
        return mCameraId;
    }

    protected abstract List<Camera.Size> getSupportedSizes(Camera.Parameters params);

    @Override
    public synchronized void initialize() {
        if (mIsInitialized) {
            return;
        }
        Camera camera = Camera.open(mCameraId);
        Camera.Parameters params = camera.getParameters();
        Camera.Size picture = params.getPictureSize();
        setPictureSize(new PictureSize(picture.width, picture.height));
        for (Camera.Size size : getSupportedSizes(params)) {
            mSupportedPictureSizes.add(new PictureSize(size.width, size.height));
        }
        Camera.Size preview = params.getPreviewSize();
        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            mSupportedPreviewSizes.add(new PictureSize(size.width, size.height));
        }
        PictureSize defaultSize = getDefaultPreviewSize();
        if (defaultSize != null) {
            setPreviewSize(defaultSize);
        } else {
            setPreviewSize(new PictureSize(preview.width, preview.height));
        }

        camera.release();
        mIsInitialized = true;
    }

    private PictureSize getDefaultPreviewSize() {
        if (mSupportedPreviewSizes.size() == 0) {
            return null;
        }
        PictureSize defaultSize = null;
        for (PictureSize size : mSupportedPreviewSizes) {
            if (size.getWidth() == DEFAULT_PREVIEW_WIDTH_THRESHOLD
                && size.getHeight() == DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                defaultSize = size;
            }
        }
        if (defaultSize != null) {
            return defaultSize;
        }
        for (PictureSize size : mSupportedPreviewSizes) {
            if (size.getWidth() * size.getHeight() <=
                DEFAULT_PREVIEW_WIDTH_THRESHOLD * DEFAULT_PREVIEW_HEIGHT_THRESHOLD) {
                defaultSize = size;
            }
        }
        if (defaultSize != null) {
            return defaultSize;
        }
        return mSupportedPreviewSizes.get(0);
    }

    @Override
    public boolean usesCamera() {
        return true;
    }

    /**
     * Start a web server.
     *
     * @param callback a callback to return the result.
     */
    @Override
    public void startWebServer(final OnWebServerStartCallback callback) {
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("HostDevicePlugin Server");
                mServer.setContentType("image/jpg");
                final String ip = mServer.start();

                if (!mCameraOverlay.isShow()) {
                    mCameraOverlay.show(new CameraOverlay.Callback() {
                        @Override
                        public void onSuccess() {
                            mCameraOverlay.setFinishFlag(false);
                            mCameraOverlay.setServer(mServer);
                            callback.onStart(ip);
                        }

                        @Override
                        public void onFail() {
                            callback.onFail();
                        }
                    });
                } else {
                    mCameraOverlay.setFinishFlag(false);
                    mCameraOverlay.setServer(mServer);
                    callback.onStart(ip);
                }
            } else {
                callback.onStart(mServer.getUrl());
            }
        }
    }

    /**
     * Stop a web server.
     */
    @Override
    public void stopWebServer() {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
            if (isShowCamera()) {
                mCameraOverlay.hide();
            }
        }
    }

    @Override
    public List<PictureSize> getSupportedPreviewSizes() {
        return mSupportedPreviewSizes;
    }

    @Override
    public boolean supportsPreviewSize(final int width, final int height) {
        if (mSupportedPreviewSizes != null) {
            for (PictureSize size : mSupportedPreviewSizes) {
                if (width == size.getWidth() && height == size.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public PictureSize getPreviewSize() {
        return mCameraOverlay.getPreviewSize();
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        mCameraOverlay.setPreviewSize(size);
    }

    @Override
    public double getPreviewMaxFrameRate() {
        return mCameraOverlay.getPreviewMaxFrameRate();
    }

    @Override
    public void setPreviewFrameRate(final double max) {
        mCameraOverlay.setPreviewFrameRate(max);
    }

    @Override
    public List<PictureSize> getSupportedPictureSizes() {
        return mSupportedPictureSizes;
    }

    @Override
    public boolean supportsPictureSize(final int width, final int height) {
        if (mSupportedPictureSizes != null) {
            for (PictureSize size : mSupportedPictureSizes) {
                if (width == size.getWidth() && height == size.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public PictureSize getPictureSize() {
        return mCameraOverlay.getPictureSize();
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        mCameraOverlay.setPictureSize(size);
    }

}
