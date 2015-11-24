package org.deviceconnect.android.deviceplugin.theta.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SphericalImageLiveView extends SphericalImageView {

    private ThetaDeviceManager mDeviceMgr;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean mIsStarted;

    public SphericalImageLiveView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDeviceManager(final ThetaDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
    }

    public synchronized void stopLivePreview() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;
    }

    public synchronized void startLivePreview() throws ThetaDeviceException {
        if (mIsStarted) {
            return;
        }
        mIsStarted = true;

        if (mDeviceMgr == null) {
            throw new IllegalStateException("Device Manager is not set.");
        }
        final ThetaDevice device = mDeviceMgr.getConnectedDevice();
        if (device == null) {
            throw new ThetaDeviceException(ThetaDeviceException.NOT_FOUND_THETA);
        }
        if (device.getModel() != ThetaDeviceModel.THETA_S) {
            throw new ThetaDeviceException(ThetaDeviceException.NOT_SUPPORTED_FEATURE);
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                MotionJpegInputStream mjpeg = null;
                try {
                    is = device.getLivePreview();
                    mjpeg = new MotionJpegInputStream(is);
                    byte[] frame;

                    while (mIsStarted && (frame = mjpeg.readFrame()) != null) {
                        Bitmap texture = BitmapFactory.decodeByteArray(frame, 0, frame.length);
                        getRenderer().setTexture(texture);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ThetaDeviceException e) {
                    e.printStackTrace();
                } finally {
                    mIsStarted = false;
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (mjpeg != null) {
                            mjpeg.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void destroy() {
        stopLivePreview();
        mExecutor.shutdownNow();
    }

}
