/*
 ThetaFeatureActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ThetaShootingFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.vr.ThetaVRModeFragment;

/**
 * Activity for the transition from the gallery to the function screen of THETA.
 * 
 * @author NTT DOCOMO, INC.
 */
public class ThetaFeatureActivity extends FragmentActivity {

    /**
     * Feature Mode.
     */
    public static final String FEATURE_MODE = "org.deviceconnect.android.theta.feature.MODE";

    /**
     * Theta Picture data.
     */
    public static final String FEATURE_DATA = "org.deviceconnect.android.theta.feature.DATA";
    /**
     * Theta Storage Flag.<br>
     * true:storage<br>
     * false:theta<br>
     */
    public static final String FEATURE_IS_STORAGE = "org.deviceconnect.android.theta.feature.STORAGE";

    /**
     * Mode VR.
     */
    public static final int MODE_VR = 0;

    /**
     * Mode Shooting.
     */
    public static final int MODE_SHOOTING = 1;

    /** mode. */
    private int mMode = -1;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMode = getIntent().getIntExtra(FEATURE_MODE, -1);
        int dataId = getIntent().getIntExtra(FEATURE_DATA, -1);
        boolean isStorage = getIntent().getBooleanExtra(FEATURE_IS_STORAGE, false);
        startApp(mMode, dataId, isStorage);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    break;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Move Page.
     * @param pageId pageId
     * @param dataId Theta Data Id
     * @param isStorage true:storage false:theta
     */
    public void startApp(final int pageId, final int dataId, final boolean isStorage) {
        if (pageId == MODE_SHOOTING) {
            ThetaShootingFragment f = new ThetaShootingFragment();
            moveFragment(f);
        } else if (pageId == MODE_VR) {
            ThetaVRModeFragment f = ThetaVRModeFragment.newInstance();
            Bundle args = new Bundle();
            args.putInt(FEATURE_DATA, dataId);
            args.putBoolean(FEATURE_IS_STORAGE, isStorage);
            f.setArguments(args);
            moveFragment(f);
        }
    }


    /**
     * Fragment の遷移.
     * @param f Fragment
     */
    private void moveFragment(final Fragment f) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setTransition(FragmentTransaction.TRANSIT_NONE);
        t.replace(android.R.id.content, f);
        t.addToBackStack(null);
        t.commit();

    }

}
