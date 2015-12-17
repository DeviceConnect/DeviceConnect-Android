package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;

class OverlayProjector extends AbstractProjector {

    private static final long MAX_INTERVAL = 100;

    private final Context mContext;
    private final WindowManager mWinMgr;
    private final Handler mHandler;

    private Thread mThread;
    private boolean mIsRequestedToStop;

    private OverlayView mPreview;
    private boolean mIsAttachedView;
    private EventListener mEventListener;

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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int w = mRenderer.getScreenWidth();
                int h = mRenderer.getScreenHeight();
                show(w, h);
            }
        });

        if (mScreen != null) {
            startProjectionThread();
        }

        return true;
    }

    private void startProjectionThread() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mScreen.onStart(OverlayProjector.this);

                    while (!mIsRequestedToStop) {
                        long start = System.currentTimeMillis();

                        byte[] frame = mRenderer.takeSnapshot();
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
            }
        });
        mThread.start();
    }

    @Override
    public synchronized boolean stop() {
        if (!isShow()) {
            return false;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        });
        if (mThread != null) {
            mIsRequestedToStop = true;
        }
        return true;
    }

    @Override
    public void setParameter(final SphericalViewParam param) {
        updateViewSize(param.getWidth(), param.getHeight());
        super.setParameter(param);
    }

    private boolean isShow() {
        return mIsAttachedView;
    }

    private void show() {
        Point size = getDisplaySize();
        int x = -size.x / 2;
        int y = -size.y / 2;
        show(x, y, size.x, size.y);
    }

    private void show(final int width , final int height) {
        Point size = getDisplaySize();
        int x = -size.x / 2;
        int y = -size.y / 2;
        show(x, y, width, height);
    }

    private void show(final int x, final int y, final int width , final int height) {
        mPreview = new OverlayView(mContext);
        mPreview.setRenderer(getRenderer());
        mPreview.setOnClickListener(new View.OnClickListener() {

            private int i = 0;

            @Override
            public void onClick(final View v) {
                i++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        i = 0;
                    }
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
        mPreview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (mEventListener != null) {
                    mEventListener.onClose();
                }
                hide();
                return true;
            }
        });

        final WindowManager.LayoutParams l = new WindowManager.LayoutParams(
            width, //pt,
            height, //pt,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
        l.x = x;
        l.y = y;
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
        }
        mIsAttachedView = false;
        mContext.unregisterReceiver(mOrientReceiver);
    }

    private void updateViewSize(final int width, final int height) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Point size = getDisplaySize();
                int x = -size.x / 2;
                int y = -size.y / 2;

                final WindowManager.LayoutParams l = new WindowManager.LayoutParams(
                    width, //pt,
                    height, //pt,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
                l.x = x;
                l.y = y;
                mWinMgr.updateViewLayout(mPreview, l);
            }
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
        Point size = getDisplaySize();
        WindowManager.LayoutParams lp =
            (WindowManager.LayoutParams) view.getLayoutParams();
        lp.x = -size.x / 2;
        lp.y = -size.y / 2;
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
