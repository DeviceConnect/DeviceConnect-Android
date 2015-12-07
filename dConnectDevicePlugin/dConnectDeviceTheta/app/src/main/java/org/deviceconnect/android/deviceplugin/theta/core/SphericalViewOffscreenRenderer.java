package org.deviceconnect.android.deviceplugin.theta.core;


import android.graphics.Bitmap;

import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;

import java.io.ByteArrayOutputStream;

public class SphericalViewOffscreenRenderer extends SphericalViewRenderer {

    private PixelBuffer mPixelBuffer;

    private Thread mThread;

    private boolean mIsRequestedToStop;

    private boolean mIsChangedImageSize = true;

    private Listener mListener;

    public void destroy() {
        stop();
    }

    @Override
    public void setScreenSettings(final int width, final int height, final boolean isStereo) {
        super.setScreenSettings(width, height, isStereo);
        mIsChangedImageSize = true;
    }

    private void prepareBuffer() {
        if (mPixelBuffer == null) {
            mPixelBuffer = new PixelBuffer(mScreenWidth, mScreenHeight, mIsStereo);
            mPixelBuffer.setRenderer(this);
        }
    }

    private void disposeBuffer() {
        if (mPixelBuffer != null) {
            mPixelBuffer.destroy();
            mPixelBuffer = null;
        }
    }

    private void readBuffer() {
        Bitmap b = mPixelBuffer.convertToBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] jpeg = baos.toByteArray();

        if (mListener != null) {
            mListener.onRender(jpeg);
        }
    }

    private void draw() {
        mPixelBuffer.render();
    }

    public synchronized boolean start() {
        if (mThread != null) {
            return false;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!mIsRequestedToStop) {
                        if (mIsChangedImageSize) {
                            disposeBuffer();
                            prepareBuffer();
                            mTextureUpdate = true;
                            mIsChangedImageSize = false;
                        }
                        draw();
                        readBuffer();

                        Thread.sleep(250);
                    }
                } catch (InterruptedException e) {
                    // Nothing to do.
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    mIsRequestedToStop = false;
                    mThread = null;

                    disposeBuffer();
                }
            }
        });
        mThread.start();

        return true;
    }

    public synchronized boolean stop() {
        if (mThread == null) {
            return false;
        }
        mIsRequestedToStop = true;
        return true;
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    public interface Listener {

        void onRender(byte[] jpeg);

    }

}
