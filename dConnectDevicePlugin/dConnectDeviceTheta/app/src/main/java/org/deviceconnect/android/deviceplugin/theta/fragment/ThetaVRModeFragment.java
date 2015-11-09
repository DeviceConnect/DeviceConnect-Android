/*
 ThetaVRModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalImageView;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    /** Thread Manager. */
    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();

    /** VR Change toggle button's listener.*/
    private CompoundButton.OnCheckedChangeListener mVRChangeToggleListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public synchronized void onCheckedChanged(final CompoundButton compoundButton, final boolean isStereo) {
            mExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mSphereView != null) {
                                    mSphereView.setStereo(isStereo);
                                    mIsStereo = isStereo;
                                    for (int i = 0; i < mVRModeChangeButton.length; i++) {
                                        mVRModeChangeButton[i].setChecked(isStereo);
                                    }
                                    enableView();
                                }
                            }
                        });
                    }
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    };

    /** ScreenShot shooting button's listener.*/
    private View.OnClickListener mShootingListener = new View.OnClickListener() {
        @Override
        public synchronized void onClick(final View view) {
            mExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Shooting ScreenShot.
                            }
                        });
                    }
                }
            }, 50, TimeUnit.MILLISECONDS);        }
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
        byte[] data = getAssetsData("r.JPG");
        if (data == null) {
            ThetaDialogFragment.showAlert(getActivity(), "Assets", "No Image.");
        } else {
            mSphereView.start(data);
        }
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
     * Get Assets Data.
     * @param name File Name
     */
    private byte[] getAssetsData(final String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = null;
        int len;
        byte[] buf = new byte[4096];
        try {
            in = getResources().getAssets().open(name);
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return out.toByteArray();
    }

    /**
     * enable/disable VR buttons.
     */
    private void enableView() {
        switch(((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (mIsStereo) {
                    mRightLayout.setVisibility(View.VISIBLE);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    mRightLayout.setVisibility(View.GONE);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
                break;
            case Surface.ROTATION_180:
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
