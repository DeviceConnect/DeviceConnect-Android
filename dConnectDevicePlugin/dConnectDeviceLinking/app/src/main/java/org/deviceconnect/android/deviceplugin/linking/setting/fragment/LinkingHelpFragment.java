/*
 LinkingHelpFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.setting.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.linking.lib.R;

import java.util.ArrayList;
import java.util.List;

public class LinkingHelpFragment extends Fragment {

    private static final String EXTRA_RES_ID = "resId";

    private boolean mDestroy;

    public static LinkingHelpFragment newInstance(final int resId) {
        LinkingHelpFragment fragment = new LinkingHelpFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_RES_ID, resId);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        int resId = getArguments().getInt(EXTRA_RES_ID);

        View root = inflater.inflate(resId, container, false);
        View v = root.findViewById(R.id.fragment_help_balloon1);
        if (v != null) {
            createAnimation(v);
        }
        View v2 = root.findViewById(R.id.fragment_help_balloon2);
        if (v2 != null) {
            createAnimation(v2);
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        mDestroy = true;
        super.onDestroyView();
    }

    private void createAnimation(final View v) {
        float size = 12.0f * getResources().getDisplayMetrics().density;
        long time = 1000;

        List<Animator> animatorList = new ArrayList<>();

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "translationY", -size, 0);
        fadeIn.setDuration(time);
        animatorList.add(fadeIn);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "translationY", 0, -size);
        fadeOut.setDuration(time);
        animatorList.add(fadeOut);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animatorList);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mDestroy) {
                    animation.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }
}
