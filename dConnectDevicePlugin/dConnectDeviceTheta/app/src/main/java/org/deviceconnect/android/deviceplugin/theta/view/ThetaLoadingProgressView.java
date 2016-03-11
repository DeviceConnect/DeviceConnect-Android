/*
 ThetaLoadingProgress
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * Theta's loading Progress view.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaLoadingProgressView extends View {
    public ThetaLoadingProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.loading_progress);
        AnimationDrawable anim = (AnimationDrawable) getBackground();
        anim.start();
        invalidate();
    }
}
