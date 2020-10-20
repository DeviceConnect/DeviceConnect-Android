package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;


import android.graphics.Bitmap;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;


public class DefaultProjector extends AbstractProjector {

    private PixelBuffer mPixelBuffer;

    private Thread mThread;

    private boolean mIsRequestedToStop;

    private byte[] mImageCache;

    @Override
    public void setRenderer(final SphericalViewRenderer renderer) {
        super.setRenderer(renderer);
        mRenderer.setSurfaceListener(this);
    }

    @Override
    public boolean start() {
        if (mThread != null) {
            return false;
        }
        if (mScreen == null) {
            return false;
        }

        mThread = new Thread(() -> {
            try {
                mScreen.onStart(DefaultProjector.this);

                while(!mIsRequestedToStop) {
                    long start = System.currentTimeMillis();

                    if (mIsChangedImageSize) {
                        disposeBuffer();
                        prepareBuffer();
                        mRenderer.requestToUpdateTexture();
                        mIsChangedImageSize = false;
                    }
                    draw();
                    readBuffer();

                    long end = System.currentTimeMillis();
                    long interval = 100 - (end - start);
                    if (interval > 0) {
                        Thread.sleep(interval);
                    }
                }
            } catch (InterruptedException e) {
                // Nothing to do.
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                mIsRequestedToStop = false;
                mThread = null;

                disposeBuffer();

                mScreen.onStop(DefaultProjector.this);
            }
        });
        mThread.start();
        return true;
    }

    @Override
    public boolean stop() {
        if (mThread == null) {
            return false;
        }
        mIsRequestedToStop = true;
        return true;
    }

    @Override
    public byte[] getImageCache() {
        return mImageCache;
    }

    private void prepareBuffer() {
        if (mPixelBuffer == null) {
            int width = mRenderer.getScreenWidth();
            int height = mRenderer.getScreenHeight();
            boolean isStereo = mRenderer.isStereo();
            mPixelBuffer = new PixelBuffer(width, height, isStereo);
            mPixelBuffer.setRenderer(mRenderer);
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
        reverse(b);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] jpeg = baos.toByteArray();
        mImageCache = jpeg;
        mScreen.onProjected(this, jpeg);
    }

    private void reverse(final Bitmap b) {
        int width = b.getWidth();
        int height = b.getHeight();
        IntBuffer buf = IntBuffer.allocate(width * height);
        IntBuffer tmp = IntBuffer.allocate(width * height);
        b.copyPixelsToBuffer(buf);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tmp.put((height - i - 1) * width + j, buf.get(i * width + j));
            }
        }
        b.copyPixelsFromBuffer(tmp);
        buf.clear();
    }

    protected void draw() {
        mPixelBuffer.render();
    }

}
