/*
 ThetaShootingModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.theta.R;

/**
 * Shooting in Theta.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaShootingModeFragment extends Fragment {

    /** THETA m15's picture shooting mode. */
    private static final int MODE_M15_SHOOTING = 0;

    /** THETA S's picture shooting mode. */
    private static final int MODE_S_SHOOTING = 1;

    /** THETA movie shooting mode. */
    private static final int MODE_MOVIE_SHOOTING = 2;

    /** Spinner THETA picture mode. */
    private static final int SPINNER_MODE_PICTURE = 0;

    /** Spinner THETA movie mode. */
    private static final int SPINNER_MODE_MOVIE = 1;

    /** Shooting Layouts.*/
    private FrameLayout[] mShootingLayouts = new FrameLayout[3];

    /** Shooting Buttons. */
    private Button[] mShootingButtons = new Button[3];
    /** Shooting Button for Movie. */
    private ToggleButton mShootingButton;

    /** Movie Shooting time. */
    private TextView mShootingTime;

    /** Shooting mode spinner. */
    private Spinner mShootingMode;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.theta_shooting_mode, null);
        initShootingLayouts(rootView);
        Spinner shootingMode = (Spinner) rootView.findViewById(R.id.theta_shooting_mode);
        shootingMode.setSelection(0);
        enableShootingMode(0);
        shootingMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                enableShootingMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        setRetainInstance(true);

        return rootView;
    }

    /**
     * Enable Shooting mode.
     * @param mode mode
     */
    private void enableShootingMode(final int mode) {
        switch (mode) {
            case SPINNER_MODE_PICTURE:
                // TODO Add m15 or S, now m15
                mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.VISIBLE);
                mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.GONE);
                mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                break;
            case SPINNER_MODE_MOVIE:
            default:
                mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.GONE);
                mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.GONE);
                mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.VISIBLE);
        }
    }

    /**
     * Init Shooting Layouts.
     * @param rootView Root View
     */
    private void initShootingLayouts(final View rootView) {
        for (int i = 0; i < mShootingLayouts.length; i++) {
            int identifier = getResources().getIdentifier("theta_shooting_layout_" + i,
                                    "id", getActivity().getPackageName());
            mShootingLayouts[i] = (FrameLayout) rootView.findViewById(identifier);
            identifier = getResources().getIdentifier("theta_shutter_" + i,
                    "id", getActivity().getPackageName());
            if (i == MODE_MOVIE_SHOOTING) {
                mShootingButton = (ToggleButton) rootView.findViewById(identifier);
            } else {
                mShootingButtons[i] = (Button) rootView.findViewById(identifier);
            }
        }
        mShootingTime = (TextView) rootView.findViewById(R.id.shooting_time);
    }
}
