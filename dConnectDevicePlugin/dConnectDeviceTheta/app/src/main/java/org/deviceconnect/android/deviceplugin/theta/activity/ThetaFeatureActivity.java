/*
 ThetaFeatureActivity.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaShootingModeFragment;
import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaVRModeFragment;

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
        startApp(mMode, dataId);
    }

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

    @Override
    public void onResume() {
        super.onResume();
        if (mMode == MODE_SHOOTING) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        } else if (mMode == MODE_VR) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * Move Page.
     * @param pageId pageId
     * @param dataId Theta Data Id
     */
    public void startApp(final int pageId, final int dataId) {
        if (pageId == MODE_SHOOTING) {
            ThetaShootingModeFragment f = new ThetaShootingModeFragment();
            moveFragment(f);
        } else if (pageId == MODE_VR) {
            ThetaVRModeFragment f = ThetaVRModeFragment.newInstance();
            Bundle args = new Bundle();
            args.putInt(FEATURE_DATA, dataId);
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
