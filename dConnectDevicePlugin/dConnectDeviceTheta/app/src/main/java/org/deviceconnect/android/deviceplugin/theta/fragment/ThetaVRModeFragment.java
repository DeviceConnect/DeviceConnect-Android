/*
 ThetaVRModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalImageView;

import java.io.ByteArrayOutputStream;

/**
 * Fragment to display the VR mode of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaVRModeFragment extends Fragment {

    /** VR Mode Left Layout. */
    private RelativeLayout mLeftLayout;

    /** VR Mode Right Layout.*/
    private RelativeLayout mRightLayout;

    /** VR Mode change button left and right.*/
    private ToggleButton[] mVRModeChangeButton = new ToggleButton[2];

    /** shooting button left and right. */
    private Button[] mShootingButton = new Button[2];

    /** SphericalView. */
    private SphericalImageView mSphereView;

    /** Stereo Flag. */
    private boolean mIsStereo = false;

    /** VR Change toggle button's listener.*/
    private CompoundButton.OnCheckedChangeListener mVRChangeToggleListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isStereo) {
            if (mSphereView != null) {
                mSphereView.setStereo(isStereo);
                mIsStereo = isStereo;
                for (int i = 0; i < mVRModeChangeButton.length; i++) {
                    mVRModeChangeButton[i].setChecked(isStereo);
                }
                enableView();
            }
        }
    };

    /** ScreenShot shooting button's listener.*/
    private View.OnClickListener mShootingListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            // TODO Shooting ScreenShot.
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.theta_vr_mode, null);
        mLeftLayout = (RelativeLayout) rootView.findViewById(R.id.left_ui);
        mRightLayout = (RelativeLayout) rootView.findViewById(R.id.right_ui);
        mSphereView = (SphericalImageView) rootView.findViewById(R.id.vr_view);
        SphericalViewApi api = new SphericalViewApi(getActivity());
        mSphereView.setViewApi(api);
        // TODO Read Theta's file.
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.r);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        mSphereView.start(bytes);
        init3DButtons(rootView);
        enableView();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSphereView != null) {
            mSphereView.stop();
        }
    }

    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableView();
    }

    /**
     * enable/disable VR buttons.
     */
    private void enableView() {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        switch(config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (mIsStereo) {
                    mRightLayout.setVisibility(View.VISIBLE);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                }
            case Configuration.ORIENTATION_PORTRAIT:
            default :
                if (mIsStereo) {
                    mRightLayout.setVisibility(View.VISIBLE);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    mRightLayout.setVisibility(View.GONE);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
        }
    }

    /**
     * Init VR Buttons.
     * @param rootView Root XML Layout
     */
    private void init3DButtons(final View rootView) {
        for (int i = 0; i < mVRModeChangeButton.length; i++) {
            int identifier = getResources().getIdentifier("change_vr_mode_" + i, "id", getActivity().getPackageName());
            mVRModeChangeButton[i] = (ToggleButton) rootView.findViewById(identifier);
            mVRModeChangeButton[i].setOnCheckedChangeListener(mVRChangeToggleListener);
            identifier = getResources().getIdentifier("theta_shutter_" + i, "id", getActivity().getPackageName());
            mShootingButton[i] = (Button) rootView.findViewById(identifier);
            mShootingButton[i].setOnClickListener(mShootingListener);
        }
    }
}
