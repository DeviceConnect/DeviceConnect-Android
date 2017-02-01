/*
 AnimationUtil.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public final class AnimationUtil {

    private AnimationUtil() {
    }

    public static void animateAlpha(final View target, final Animator.AnimatorListener listener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "alpha", 1f, 0f);
        objectAnimator.setDuration(500);
        objectAnimator.start();
        if (listener != null) {
            objectAnimator.addListener(listener);
        }
    }

    public static void animateAlpha2(final View target, final Animator.AnimatorListener listener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "alpha", 0f, 1f);
        objectAnimator.setDuration(500);
        objectAnimator.start();
        if (listener != null) {
            objectAnimator.addListener(listener);
        }
    }

    public static abstract class AnimationAdapter implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(final Animator animation) {
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
        }

        @Override
        public void onAnimationCancel(final Animator animation) {
        }

        @Override
        public void onAnimationRepeat(final Animator animation) {
        }
    }
}
