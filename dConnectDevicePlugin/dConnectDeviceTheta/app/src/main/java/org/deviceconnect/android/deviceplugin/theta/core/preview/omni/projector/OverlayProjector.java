package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;

public class OverlayProjector extends AbstractProjector {

    private static final long MAX_INTERVAL = 100;

    private final Context mContext;
    private final WindowManager mWinMgr;
    private final Handler mHandler;

    private Thread mThread;
    private boolean mIsRequestedToStop;

    private OverlayView mPreview;
    private boolean mIsAttachedView;
    private EventListener mEventListener;
    private byte[] mImageCache;

    private final BroadcastReceiver mOrientReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (mPreview == null) {
                return;
            }
            String action = intent.getAction();
            if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                updatePosition(mPreview);
            }
        }
    };

    public OverlayProjector(final Context context) {
        mContext = context;
        mWinMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public synchronized boolean start() {
        if (isShow()) {
            return false;
        }
        mHandler.post(() -> {
            int w = mRenderer.getScreenWidth();
            int h = mRenderer.getScreenHeight();
            show(w, h, mRenderer.isStereo());
        });

        if (mScreen != null) {
            startProjectionThread();
        }

        return true;
    }

    private void startProjectionThread() {
        mThread = new Thread(() -> {
            try {
                mScreen.onStart(OverlayProjector.this);

                while (!mIsRequestedToStop) {
                    long start = System.currentTimeMillis();

                    byte[] frame = mRenderer.takeSnapshot();
                    mImageCache = frame;
                    mScreen.onProjected(OverlayProjector.this, frame);

                    long end = System.currentTimeMillis();
                    long interval = MAX_INTERVAL - (end - start);
                    if (interval > 0) {
                        Thread.sleep(interval);
                    }
                }
            } catch (InterruptedException e) {
                // Nothing to do.
            } finally {
                mIsRequestedToStop = false;
                mThread = null;

                mScreen.onStop(OverlayProjector.this);
            }
        });
        mThread.start();
    }

    @Override
    public synchronized boolean stop() {
        if (!isShow()) {
            return false;
        }
        mHandler.post(() -> {
            hide();
        });
        if (mThread != null) {
            mIsRequestedToStop = true;
        }
        return true;
    }

    @Override
    public void setParameter(final SphericalViewParam param) {
        updateViewSize(param.getWidth(), param.getHeight(), param.isStereo());
        super.setParameter(param);
    }

    @Override
    public byte[] getImageCache() {
        return mImageCache;
    }

    private boolean isShow() {
        return mIsAttachedView;
    }

    private void show(final int width , final int height, final boolean isStereo) {
        Point size = getDisplaySize();
        int x = size.x / 2;
        int y = size.y / 2;
        show(x, y, width, height, isStereo);
    }

    private void show(final int x, final int y, final int width , final int height,
                      final boolean isStereo) {
        mPreview = new OverlayView(mContext);
        mPreview.setRenderer(getRenderer());
        mPreview.setOnClickListener(new View.OnClickListener() {

            private int i = 0;

            @Override
            public void onClick(final View v) {
                i++;
                Handler handler = new Handler();
                Runnable r = () -> {
                    i = 0;
                };

                if (i == 1) {
                    handler.postDelayed(r, 300);
                } else if (i == 2) {
                    i = 0;
                    if (mEventListener != null) {
                        mEventListener.onClick();
                    }
                }
            }
        });
        mPreview.setOnLongClickListener((v) -> {
            if (mEventListener != null) {
                mEventListener.onClose();
            }
            hide();
            return true;
        });

        final int w = isStereo ? width * 2 : width;
        int type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        final WindowManager.LayoutParams l = new WindowManager.LayoutParams(
            w,
            height,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
        l.x = (int) (x * getScaledDensity());
        l.y = (int) (y * getScaledDensity());
        mWinMgr.addView(mPreview, l);
        mIsAttachedView = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        mContext.registerReceiver(mOrientReceiver, filter);

    }

    private void hide() {
        if (mPreview != null) {
            mWinMgr.removeView(mPreview);
            mPreview = null;
            mContext.unregisterReceiver(mOrientReceiver);
        }
        mIsAttachedView = false;
    }

    private void updateViewSize(final int width, final int height, final boolean isStereo) {
        final int w = isStereo ? width * 2 : width;
        mHandler.post(() -> {
            int type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            final WindowManager.LayoutParams l = new WindowManager.LayoutParams(
                w,
                height,
                    type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
            Point size = getDisplaySize();
            l.x = (int) ((size.x / 2) * getScaledDensity());
            l.y = (int) ((size.y / 2) * getScaledDensity());
            mWinMgr.updateViewLayout(mPreview, l);
        });
    }

    private float getScaledDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        mWinMgr.getDefaultDisplay().getMetrics(metrics);
        return metrics.scaledDensity;
    }

    private Point getDisplaySize() {
        Display display = mWinMgr.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void updatePosition(final View view) {
        if (view == null) {
            return;
        }
        WindowManager.LayoutParams lp =
            (WindowManager.LayoutParams) view.getLayoutParams();
        Point size = getDisplaySize();
        lp.x = (int) ((size.x / 2) * getScaledDensity());
        lp.y = (int) ((size.y / 2) * getScaledDensity());
        mWinMgr.updateViewLayout(view, lp);
    }

    public void setEventListener(final EventListener l) {
        mEventListener = l;
    }

    public interface EventListener {

        void onClose();

        void onClick();
    }
}
