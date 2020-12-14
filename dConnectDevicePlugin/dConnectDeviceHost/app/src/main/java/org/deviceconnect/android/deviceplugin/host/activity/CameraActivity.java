package org.deviceconnect.android.deviceplugin.host.activity;

import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class CameraActivity extends HostDevicePluginBindActivity {
    private HostMediaRecorderManager mMediaRecorderManager;
    private HostMediaRecorder mMediaRecorder;

    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;
    private Surface mSurface;

    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            if (mSurface != null) {
                addSurface(mSurface);
            }
        }

        @Override
        public void onStopped() {
        }

        @Override
        public void onError(Exception e) {
        }

        @Override
        public void onDrawn(EGLSurfaceBase eglSurfaceBase) {
            // ignore.
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurface = holder.getSurface();
                if (mEGLSurfaceDrawingThread != null && mEGLSurfaceDrawingThread.isRunning()) {
                    addSurface(mSurface);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mSurface != null) {
                    mEGLSurfaceDrawingThread.removeEGLSurfaceBase(mSurface);
                    mSurface = null;
                }
            }
        });

        // 明示的に画面を OFF にさせない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void unbindService() {
        if (mEGLSurfaceDrawingThread != null) {
            mEGLSurfaceDrawingThread.removeEGLSurfaceBase(mSurface);
            mEGLSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
            mEGLSurfaceDrawingThread.stop(false);
        }

        super.unbindService();
    }

    @Override
    protected void onBindService() {
        mMediaRecorderManager = getHostDevicePlugin().getHostMediaRecorderManager();
        mMediaRecorder = mMediaRecorderManager.getRecorder(null);

        mEGLSurfaceDrawingThread = mMediaRecorder.getSurfaceDrawingThread();
        mEGLSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
        if (!mEGLSurfaceDrawingThread.isRunning()) {
            mEGLSurfaceDrawingThread.start();
        }
    }

    @Override
    protected void onUnbindService() {
    }

    private void addSurface(Surface surface) {
        if (mEGLSurfaceDrawingThread.findEGLSurfaceBaseByTag(surface) != null) {
            return;
        }
        mEGLSurfaceDrawingThread.addEGLSurfaceBase(surface);
        runOnUiThread(() -> adjustSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions()));
    }

    private void adjustSurfaceView(boolean isSwappedDimensions) {
        runOnUiThread(() -> {
            HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
            Size previewSize = settings.getPreviewSize();

            SurfaceView surfaceView = findViewById(R.id.surface_view);
            View root = findViewById(R.id.root);

            int cameraWidth = isSwappedDimensions ? previewSize.getHeight() : previewSize.getWidth();
            int cameraHeight = isSwappedDimensions ? previewSize.getWidth() : previewSize.getHeight();
            Size viewSize = new Size(root.getWidth(), root.getHeight());
            Size changeSize = calculateViewSize(cameraWidth, cameraHeight, viewSize);

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = changeSize.getWidth();
            layoutParams.height = changeSize.getHeight();
            surfaceView.setLayoutParams(layoutParams);

            surfaceView.getHolder().setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        });
    }

    private Size calculateViewSize(int width, int height, Size viewSize) {
        int h =  (int) (height * (viewSize.getWidth() / (float) width));
        if (viewSize.getHeight() < h) {
            int w = (int) (width * (viewSize.getHeight() / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, viewSize.getHeight());
        }
        return new Size(viewSize.getWidth(), h);
    }
}
