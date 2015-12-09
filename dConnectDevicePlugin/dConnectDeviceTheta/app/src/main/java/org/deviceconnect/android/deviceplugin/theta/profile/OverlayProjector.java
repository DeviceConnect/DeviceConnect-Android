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

class OverlayProjector extends DefaultProjector {

    private final Context mContext;
    private final WindowManager mWinMgr;
    private final Handler mHandler;

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
                show();
            }
        });
        return super.start();
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
        return super.stop();
    }

    @Override
    protected void draw() {
        // NOTE:
        //     Nothing to do here.
        //     The rendering will be executed in OverlayView class.
    }

    private boolean isShow() {
        return mIsAttachedView;
    }

    private void show() {
        Point size = getDisplaySize();

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
            size.x, //pt,
            size.y, //pt,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
        l.x = -size.x / 2;
        l.y = -size.y / 2;
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
//        if (mZoomInBtn != null) {
//            mWinMgr.removeView(mZoomInBtn);
//            mZoomInBtn = null;
//        }
//        if (mZoomOutBtn != null) {
//            mWinMgr.removeView(mZoomOutBtn);
//            mZoomOutBtn = null;
//        }
        mIsAttachedView = false;
        mContext.unregisterReceiver(mOrientReceiver);
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
