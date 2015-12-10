package org.deviceconnect.android.deviceplugin.theta.profile;


import android.graphics.Bitmap;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;


class DefaultProjector implements Projector, SphericalViewRenderer.SurfaceListener {

    private PixelBuffer mPixelBuffer;

    private Thread mThread;

    private boolean mIsRequestedToStop;

    private boolean mIsChangedImageSize = true;

    private SphericalViewRenderer mRenderer;

    private ProjectionScreen mScreen;

    @Override
    public void onSurfaceChanged(final int width, final int height, final boolean isStereo) {
        mIsChangedImageSize = true;
    }

    @Override
    public SphericalViewRenderer getRenderer() {
        return mRenderer;
    }

    @Override
    public void setRenderer(final SphericalViewRenderer renderer) {
        mRenderer = renderer;
        mRenderer.setSurfaceListener(this);
    }

    @Override
    public void setScreen(final ProjectionScreen screen) {
        mScreen = screen;
    }

    @Override
    public boolean start() {
        if (mThread != null) {
            return false;
        }
        if (mScreen == null) {
            return false;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
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
