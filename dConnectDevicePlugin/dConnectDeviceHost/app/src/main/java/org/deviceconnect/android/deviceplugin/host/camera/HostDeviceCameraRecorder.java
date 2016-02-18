package org.deviceconnect.android.deviceplugin.host.camera;


import android.content.Context;
import android.hardware.Camera;

import org.deviceconnect.android.deviceplugin.host.HostDevicePreviewServer;
import org.deviceconnect.android.provider.FileManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public abstract class HostDeviceCameraRecorder implements HostDevicePreviewServer {

    protected final Context mContext;

    protected final CameraOverlay mCameraOverlay;

    protected final String mId;

    protected final String mName;

    protected final int mCameraId;

    protected boolean mIsInitialized;

    protected PictureSize mPictureSize;

    protected PictureSize mPreviewSize;

    protected final List<PictureSize> mSupportedPictureSizes = new ArrayList<PictureSize>();

    protected final List<PictureSize> mSupportedPreviewSizes = new ArrayList<PictureSize>();

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    public HostDeviceCameraRecorder(final Context context, final String id, final String name,
                                    final int cameraId, final FileManager fileMgr) {
        mContext = context;
        mId = id;
        mName = name;
        mCameraId = cameraId;

        mCameraOverlay = new CameraOverlay(context, cameraId);
        mCameraOverlay.setFileManager(fileMgr);
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

    @Override
    public synchronized void initialize() {
        if (mIsInitialized) {
            return;
        }
        Camera camera = Camera.open(mCameraId);
        Camera.Parameters params = camera.getParameters();
        Camera.Size picture = params.getPictureSize();
        setPictureSize(new PictureSize(picture.width, picture.height));
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            mSupportedPictureSizes.add(new PictureSize(size.width, size.height));
        }
        Camera.Size preview = params.getPreviewSize();
        setPreviewSize(new PictureSize(preview.width, preview.height));
        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            mSupportedPreviewSizes.add(new PictureSize(size.width, size.height));
        }
        camera.release();
        mIsInitialized = true;
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
                    mCameraOverlay.setPreviewSize(mPreviewSize);
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
        return mPreviewSize;
    }

    @Override
    public void setPreviewSize(final PictureSize size) {
        mPreviewSize = size;
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
        return mPictureSize;
    }

    @Override
    public void setPictureSize(final PictureSize size) {
        mPictureSize = size;
    }

}
