/*
 ThetaSplashActivity
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.theta.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * The Splash window of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaSplashActivity extends Activity {

    /**
     * Handler.
     */
    private Handler handler;
    /**
     * Runnable.
     */
    private final Runnable run = new Runnable() {
        @Override
        public void run() {
            openThetaSettingOrGallery();
        }
    };


    /**
     * Minimum time to display the splash.
     * msec
     */
    private static final int MIN_TIME_TO_SHOW_SPLASH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theta_splashtop);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacks(run);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler = new Handler();
        handler.postDelayed(run, MIN_TIME_TO_SHOW_SPLASH);
    }
    /**
     * Is Connected Theta?
     */
    private void openThetaSettingOrGallery() {
        Intent intent = new Intent();
        intent.setClass(this, ThetaDeviceActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * disable the splash screen the back key
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}
