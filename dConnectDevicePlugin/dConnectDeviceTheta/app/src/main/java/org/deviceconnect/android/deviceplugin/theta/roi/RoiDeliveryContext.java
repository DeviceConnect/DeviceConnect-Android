/*
 RoiDeliveryContext.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.roi;


import android.net.Uri;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewOffscreenRenderer;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * ROI Image Delivery Context.
 *
 * @author NTT DOCOMO, INC.
 */
public class RoiDeliveryContext implements SphericalViewOffscreenRenderer.Listener {

    private static final long EXPIRE_INTERVAL = 10 * 1000;

    private final SphericalViewApi mViewApi;

    private final OmnidirectionalImage mSource;

    private Timer mExpireTimer;

    private final SphericalViewOffscreenRenderer mRenderer = new SphericalViewOffscreenRenderer();

    private SphericalViewParam mCurrentParam;

    private String mUri;

    private String mSegment;

    private OnChangeListener mListener;

    private Logger mLogger = Logger.getLogger("theta.dplugin");

    /**
     * Constructor.
     *
     * @param viewApi an instance of {@link SphericalViewApi}
     * @param source an instance of {@link OmnidirectionalImage} to create ROI image
     */
    public RoiDeliveryContext(final SphericalViewApi viewApi,
                              final OmnidirectionalImage source) {
        mViewApi = viewApi;
        mSource = source;

        mCurrentParam = new SphericalViewParam();
        mCurrentParam.setWidth(600);
        mCurrentParam.setHeight(400);
        mCurrentParam.setFOV(90.0);
    }

    @Override
    public void onRender(final byte[] jpeg) {
        if (mListener != null) {
            mListener.onUpdate(this, jpeg);
        }
    }

    public void setUri(final String uriString) {
        mUri = uriString;
        mSegment = Uri.parse(uriString).getLastPathSegment();
    }

    public String getUri() {
        return mUri;
    }

    public String getSegment() {
        return mSegment;
    }

    public void start() {
        mViewApi.startImageView(mSource.getData(), mCurrentParam, mRenderer);
        mRenderer.setListener(this);
        mRenderer.start();
    }

    public void destroy() {
        mViewApi.stop();
        mRenderer.destroy();
    }

    public void setParameter(final SphericalViewParam param) {
        mViewApi.updateImageView(param);
    }

    public void startExpireTimer() {
        if (mExpireTimer != null) {
            return;
        }
        long now = System.currentTimeMillis();
        Date expireTime = new Date(now + EXPIRE_INTERVAL);
        mExpireTimer = new Timer();
        mExpireTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mExpireTimer.cancel();
                mExpireTimer = null;
                if (mListener != null) {
                    mListener.onExpire(RoiDeliveryContext.this);
                }
            }
        }, expireTime);
    }

    public void stopExpireTimer() {
        if (mExpireTimer != null) {
            mExpireTimer.cancel();
            mExpireTimer = null;
        }
    }

    public void restartExpireTimer() {
        stopExpireTimer();
        startExpireTimer();
    }

    public void setOnChangeListener(final OnChangeListener listener) {
        mListener = listener;
    }

    public interface OnChangeListener {

        void onUpdate(RoiDeliveryContext roiContext, byte[] roi);

        void onExpire(RoiDeliveryContext roiContext);

    }

}
