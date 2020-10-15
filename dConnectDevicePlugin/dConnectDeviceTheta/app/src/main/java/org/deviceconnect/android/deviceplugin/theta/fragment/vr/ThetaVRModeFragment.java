/*
 ThetaVRModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment.vr;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.collection.LruCache;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaFeatureActivity;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalImageView;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.data.ThetaObjectStorage;
import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaDialogFragment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Fragment to display the VR mode of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaVRModeFragment extends Fragment implements ThetaVRModeContract.View {


    /** SphericalView Max fov.*/
    private static final int MAX_FOV = 90;

    /** SphericalView Min fov.*/
    private static final int MIN_FOV = 45;


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
    /** Progress. */
    private ThetaDialogFragment mProgress;
    /**
     * Logger.
     */
    private final Logger mLogger = Logger.getLogger("theta.sampleapp");

    private ThetaVRModeContract.Presenter mPresenter;


    /** Scale Gesture.*/
    private ScaleGestureDetector mScaleDetector;
    /** Scale factor. */
    private float mScaleFactor = 90.0f;
    /** Default VR. */
    private int mDefaultId;

    /** Is Storage. */
    private boolean mIsStorage;

    /**
     * Singleton.
     */
    public static ThetaVRModeFragment newInstance() {
        ThetaVRModeFragment fragment = new ThetaVRModeFragment();
        return fragment;
    }

    /** VR Change toggle button's listener.*/
    private CompoundButton.OnCheckedChangeListener mVRChangeToggleListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public synchronized void onCheckedChanged(final CompoundButton compoundButton, final boolean isStereo) {
            mExecutorService.schedule(() -> {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                            if (mIsStereo != isStereo) {
                                for (int i = 0; i < mVRModeChangeButton.length; i++) {
                                    mVRModeChangeButton[i].setChecked(isStereo);
                                }
                                mIsStereo = isStereo;
                                enableView();
                            }
                            if (mSphereView != null) {
                                mSphereView.setStereo(mIsStereo);
                            }
                     });
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    };

    /** ScreenShot shooting button's listener.*/
    private View.OnClickListener mShootingListener = (view) -> {
        showProgressDialog();
        mExecutorService.schedule(() -> {
            mPresenter.saveScreenShot();
        }, 50, TimeUnit.MILLISECONDS);
    };



    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            mDefaultId = args.getInt(ThetaFeatureActivity.FEATURE_DATA, -1);
            mIsStorage = args.getBoolean(ThetaFeatureActivity.FEATURE_IS_STORAGE);
        }
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setRetainInstance(true);
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        mPresenter = new ThetaVRModePresenter(getContext(), this, mDefaultId, mIsStorage);
        View rootView = inflater.inflate(R.layout.theta_vr_mode, null);
        mRightLayout = rootView.findViewById(R.id.right_ui);
        mSphereView = rootView.findViewById(R.id.vr_view);
        SphericalViewApi api = app.getSphericalViewApi();
        mSphereView.setViewApi(api);
        mSphereView.setOnTouchListener(new View.OnTouchListener() {

            private boolean mIsEnabledLongTouch = true;

            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mIsEnabledLongTouch = true;
                    return true;
                }
                if (motionEvent.getPointerCount() == 1) {
                    if (mIsEnabledLongTouch && motionEvent.getEventTime() - motionEvent.getDownTime() >= 300) {
                        mSphereView.resetCameraDirection();
                    }
                } else {
                    mIsEnabledLongTouch = false;
                    mScaleDetector.onTouchEvent(motionEvent);
                }
                return true;
            }
        });
        init3DButtons(rootView);
        enableView();
        mScaleDetector = new ScaleGestureDetector(getActivity(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(final ScaleGestureDetector detector) {
                        mScaleFactor /= detector.getScaleFactor();
                        double scale =  mScaleFactor;
                        if (scale > MAX_FOV) {
                            scale = MAX_FOV;
                            mScaleFactor = MAX_FOV;
                        }
                        if (scale < MIN_FOV) {
                            scale = MIN_FOV;
                            mScaleFactor = MIN_FOV;
                        }
                        mSphereView.setFOV(scale);

                        return true;
                    }
                });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mProgress == null) {
            mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.loading));
            mProgress.show(getActivity().getFragmentManager(),
                    "fragment_dialog");
        }

        mPresenter.startDownloadTask();
    }

    @Override
    public void onPause() {
        dismissProgressDialog();
        if (mSphereView != null) {
            mSphereView.stop();
            mSphereView.onPause();
        }
        mPresenter.stopDownloadTask();
        super.onPause();
    }


    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableView();
    }

    @Override
    public void showReconnectionDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showReconnectionDialog(activity,
                    (dialog, i) -> {
                        dialog.dismiss();
                        activity.finish();
                        showSettingsActivity();
                    },
                    (dialog, i) -> {
                        dialog.dismiss();
                        activity.finish();
                    });
        }
    }

    @Override
    public void showDisconnectionDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showDisconnectionDialog(activity,
                (dialog, i) -> {
                    dialog.dismiss();
                    showSettingsActivity();
                },
                (dialog, i) -> {
                    dialog.dismiss();
                });
        }
    }

    @Override
    public void showOutOfMemoryErrorDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                    getString(R.string.theta_error_memory_warning),
                    (dialogInterface, i) -> {
                        getActivity().finish();
                    });
            });
        }

    }


    @Override
    public void showProgressDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
            activity.runOnUiThread(() -> {

                mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.saving));
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            });
        }
    }

    @Override
    public void dismissProgressDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (mProgress != null) {
                    mProgress.dismiss();
                }
            });
        }
    }

    private void showSettingsActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Toast.makeText(activity, R.string.camera_must_connect, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(activity, ThetaDeviceSettingsActivity.class);
        startActivity(intent);
    }

    /**
     * enable/disable VR buttons.
     */
    @Override
    public void enableView() {
        if (mIsStereo) {
            mRightLayout.setVisibility(View.VISIBLE);
        } else {
            mRightLayout.setVisibility(View.GONE);
        }
    }



    /**
     * Init VR Buttons.
     * @param rootView Root XML Layout
     */
    private void init3DButtons(final View rootView) {
        for (int i = 0; i < mVRModeChangeButton.length; i++) {
            int identifier = getResources().getIdentifier("change_vr_mode_" + i, "id", getActivity().getPackageName());
            mVRModeChangeButton[i] = rootView.findViewById(identifier);
            mVRModeChangeButton[i].setOnCheckedChangeListener(mVRChangeToggleListener);
            identifier = getResources().getIdentifier("theta_shutter_" + i, "id", getActivity().getPackageName());
            mShootingButton[i] = rootView.findViewById(identifier);
            mShootingButton[i].setOnClickListener(mShootingListener);
        }
    }



    @Override
    public void succeedSaveDialog() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {

                ThetaDialogFragment.showAlert(getActivity(),
                        getResources().getString(R.string.theta_ssid_prefix),
                        getResources().getString(R.string.theta_save_screenshot), null);
            });
        }
    }
    /**
     * ScreenShot failed.
     */
    @Override
    public void failSaveDialog() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                ThetaDialogFragment.showAlert(getActivity(),
                        getResources().getString(R.string.theta_ssid_prefix),
                        getResources().getString(R.string.theta_error_failed_save_file), null);
            });
        }
    }

    @Override
    public boolean checkStorageSize() {
        Activity activity = getActivity();
        if (activity != null && !ThetaObjectStorage.hasEnoughStorageSize()) {
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }
            // Check Android Storage Limit
            activity.runOnUiThread(() -> {
                ThetaDialogFragment.showAlert(getActivity(),
                        getResources().getString(R.string.theta_ssid_prefix),
                        getResources().getString(R.string.theta_error_shortage_by_android), null);
            });
            return false;
        }
        return true;
    }

    @Override
    public byte[] takeSnapshot() {
        return mSphereView.takeSnapshot();
    }

    @Override
    public ThetaDeviceManager getThetaDeviceManager() {
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        return app.getDeviceManager();
    }

    @Override
    public LruCache<String, byte[]> getCache() {
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        return app.getCache();
    }

    @Override
    public void startSphereView(final byte[] sphericalBinary) {
        if (mSphereView != null) {
            try {
                mSphereView.onResume();
                mSphereView.start(sphericalBinary);
            } catch (OutOfMemoryError e) {
                showOutOfMemoryErrorDialog();
            }
        }
    }
}
