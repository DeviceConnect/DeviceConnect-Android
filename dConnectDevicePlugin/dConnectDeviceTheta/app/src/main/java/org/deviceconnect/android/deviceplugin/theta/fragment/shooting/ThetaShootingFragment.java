/*
 ThetaShootingModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalImageLiveView;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;
import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaDialogFragment;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.MAX_FOV;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.MIN_FOV;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.MODE_M15_SHOOTING;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.MODE_MOVIE_SHOOTING;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.MODE_S_SHOOTING;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.SPINNER_MODE_MOVIE;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.SPINNER_MODE_PICTURE;

/**
 * Shooting in Theta.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaShootingFragment extends Fragment implements ThetaDeviceEventListener, ShootingContract.View {
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

    /** Theta Device. */
    private ThetaDevice mDevice;
    /**
     * Progress.
     */
    private ThetaDialogFragment mProgress;
    /** Shooting LiveView. */
    private SphericalImageLiveView mLiveView;

    /** Recorder handler. */
    private final Handler mRecorder = new Handler();

    /** Recorder time. */
    private int mRecordTime;
    /** Is Recording. */
    private ShootingContract.RecordingState mIsRecording;

    /** Now Shooting Mode. */
    private ThetaDevice.ShootingMode mNowShootingMode;
    /** Scale Gesture.*/
    private ScaleGestureDetector mScaleDetector;
    /** Scale factor. */
    private float mScaleFactor = 90.0f;

    /** SphericalViewApi. */
    private SphericalViewApi mApi;

    private ThetaShootingPresenter mPresenter;

    /** Timer Runnable. */
    private Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            mRecordTime++;
            String display = "";
            display = String.format("%02d:%02d", mRecordTime / 60, mRecordTime % 60);
            Activity activity = getActivity();
            final String dispLabel = display;
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mShootingTime.setText(dispLabel);
                        if (mRecordTime >= (mDevice.getMaxVideoLength() / 1000)) {
                            mShootingButton.setChecked(false);
                            mIsRecording = ShootingContract.RecordingState.CANCEL;
                            mRecorder.removeCallbacks(this);
                        } else {
                            mRecorder.removeCallbacks(this);
                            mRecorder.postDelayed(mUpdater, 1000);
                        }
                    }
                });
            }
        }
    };

    /** Item Mode Listener. */
    private AdapterView.OnItemSelectedListener mModeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            enableShootingMode(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    /** Recoridng Listener. */
    private CompoundButton.OnCheckedChangeListener mRecordingListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isRecordered) {
            compoundButton.setEnabled(false);
            mIsRecording = ShootingContract.RecordingState.RECORDING;
            if (mIsRecording != ShootingContract.RecordingState.CANCEL && !isRecordered) {
                mIsRecording = ShootingContract.RecordingState.STOP;
            }

            mPresenter.startRecordingVideoTask();
            new Handler().postDelayed(() -> {
                mShootingButton.setEnabled(true);
            }, 500);
        }
    };
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.theta_shooting_mode, null);
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        ThetaDeviceManager deviceMgr = app.getDeviceManager();
        deviceMgr.registerDeviceEventListener(this);
        mDevice = deviceMgr.getConnectedDevice();
        mPresenter = new ThetaShootingPresenter(this);
        if (mDevice == null) {
            showDisconnectDialog();
            return rootView;
        }
        mShootingTime = rootView.findViewById(R.id.shooting_time);
        mLiveView = rootView.findViewById(R.id.shooting_preview);
        mApi = app.getSphericalViewApi();
        mLiveView.setViewApi(mApi);
        mLiveView.setDeviceManager(deviceMgr);
        initShootingLayouts(rootView);
        mShootingMode = rootView.findViewById(R.id.theta_shooting_mode);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                                            R.layout.theta_shooting_mode_adapter,
                                                            getResources().getStringArray(R.array.theta_shooting_mode));

        mShootingMode.setAdapter(adapter);
        setRetainInstance(true);
        mPresenter.startGetShootingModeTask();
        mNowShootingMode = ThetaDevice.ShootingMode.UNKNOWN;
        mShootingMode.setOnItemSelectedListener(null);
        mLiveView.setOnTouchListener(new View.OnTouchListener() {

            private boolean mIsEnabledLongTouch = true;

            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mIsEnabledLongTouch = true;
                    return true;
                }
                if (motionEvent.getPointerCount() == 1) {
                    if (mIsEnabledLongTouch && motionEvent.getEventTime() - motionEvent.getDownTime() >= 300) {
                        mLiveView.resetCameraDirection();
                    }
                } else {
                    mIsEnabledLongTouch = false;
                    mScaleDetector.onTouchEvent(motionEvent);
                }
                return true;
            }
        });
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
                        mLiveView.setFOV(scale);

                        return true;
                    }
                });
        rotateShootingButton(getActivity().getResources().getConfiguration());
        return rootView;
    }

    @Override
    public void onConnected(ThetaDevice device) {
        mDevice = device;
    }

    @Override
    public void onDisconnected(ThetaDevice device) {
        mDevice = null;
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                    activity.finish();
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rotateShootingButton(newConfig);
    }

    /** Rotate Shooting Button. */
    private void rotateShootingButton(Configuration newConfig) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mShootingButton.getLayoutParams();
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                params.gravity = Gravity.RIGHT | Gravity.CENTER;
                break;
            default:
                params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        }
        if (mShootingMode.getSelectedItemPosition() == SPINNER_MODE_PICTURE) {
            mShootingButtons[MODE_S_SHOOTING].setLayoutParams(params);
        } else if (mShootingMode.getSelectedItemPosition() == SPINNER_MODE_MOVIE) {
            mShootingButton.setLayoutParams(params);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLiveView != null) {
            mLiveView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.stopTask();

        if (mNowShootingMode == ThetaDevice.ShootingMode.VIDEO
                && mIsRecording == ShootingContract.RecordingState.RECORDING) {
            mIsRecording = ShootingContract.RecordingState.STOP;
            mPresenter.startRecordingVideoTask();
            mShootingButton.setOnCheckedChangeListener(null);
            mShootingButton.setChecked(false);
            mShootingButton.setOnCheckedChangeListener(mRecordingListener);
        }
        if (mLiveView != null) {
            mLiveView.onPause();
        }
        stopProgressDialog();
        mRecorder.removeCallbacks(mUpdater);
    }
    

    @Override
    public void onDestroy() {
        if (mLiveView != null) {
            mLiveView.destroy();
        }
        super.onDestroy();
    }

    /**
     * Enable Shooting mode.
     * @param mode mode
     */
    private void enableShootingMode(final int mode) {
        switch (mode) {
            case SPINNER_MODE_PICTURE:
                if (isLiveStreaming()) {
                    if (mNowShootingMode != ThetaDevice.ShootingMode.IMAGE) {
                        mNowShootingMode = ThetaDevice.ShootingMode.IMAGE;
                        mPresenter.startShootingChangeTask(mNowShootingMode);
                    }
                    mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.GONE);
                    mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.VISIBLE);
                    mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                } else {
                    if (mNowShootingMode != ThetaDevice.ShootingMode.IMAGE) {
                        showDialog(R.string.theta_error_failed_change_mode);
                    }
                    mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.VISIBLE);
                    mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.GONE);
                    mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                }
                break;
            case SPINNER_MODE_MOVIE:
            default:
                if (mShootingMode.getSelectedItemPosition() == SPINNER_MODE_PICTURE
                        && isLiveStreaming()) {
                    mLiveView.stop();
                }
                if (mNowShootingMode != ThetaDevice.ShootingMode.VIDEO
                        && isLiveStreaming()) {
                    mNowShootingMode = ThetaDevice.ShootingMode.VIDEO;
                    mPresenter.startShootingChangeTask(mNowShootingMode);
                } else if (mNowShootingMode != ThetaDevice.ShootingMode.VIDEO
                        && mDevice != null
                        && isThetaM15()) {
                        showDialog(R.string.theta_error_failed_change_mode);
                }
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
                mShootingButton.setOnCheckedChangeListener(mRecordingListener);
            } else {
                mShootingButtons[i] = rootView.findViewById(identifier);
                mShootingButtons[i].setOnClickListener((view) -> {
                    view.setEnabled(false);
                    mPresenter.startShootingTask();
                    new Handler().postDelayed(() -> {
                        view.setEnabled(true);
                    }, 500);
                });
            }
        }
        mShootingTime = rootView.findViewById(R.id.shooting_time);
    }

    @Override
    public void startRecordingDialog() {
        int message = R.string.recording;
        if (mIsRecording != ShootingContract.RecordingState.RECORDING) {
            message = R.string.stoping;
        }
        startProgressDialog(message);
    }

    @Override
    public void startProgressDialog(final int message) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (mProgress == null) {
                    mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(message));
                    mProgress.show(getActivity().getFragmentManager(),
                            "fragment_dialog");
                }
            });
        }
    }

    @Override
    public void stopProgressDialog() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void showDialog(final int message) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(message), null);
            });
        }
    }
    @Override
    public void showDisconnectDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_disconnect_dialog_message),
                        (dialogInterface, i) -> {
                            activity.finish();
                        });
            });
        }
    }

    @Override
    public void showFailedChangeRecordingMode() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                    getString(R.string.theta_error_failed_change_mode),
                    (dialogInterface, i) -> {
                        mShootingButton.setOnCheckedChangeListener(null);
                        mShootingButton.setChecked(false);
                        mShootingButton.setOnCheckedChangeListener(mRecordingListener);
                    });
        }
    }

    @Override
    public ThetaDevice.ShootingMode nowShootingMode() {
        if (mDevice == null) {
            return ThetaDevice.ShootingMode.UNKNOWN;
        }
        try {
            return mDevice.getShootingMode();
        } catch (ThetaDeviceException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return  ThetaDevice.ShootingMode.UNKNOWN;
        }
    }

    @Override
    public void setOnSelectListener() {
        mShootingMode.setOnItemSelectedListener(mModeListener);
    }

    @Override
    public void initUpdater() {
        mRecordTime = 0;
        mShootingTime.setText("00:00");
        mRecorder.removeCallbacks(mUpdater);
    }

    @Override
    public void enabledMode(int mode) {
        enableShootingMode(mode);
        mShootingMode.setSelection(mode);
    }

    @Override
    public boolean existThetaDevice() {
        return (mDevice != null);
    }

    @Override
    public boolean isLiveStreaming() {
        return mDevice != null
                && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V);
    }

    @Override
    public boolean isThetaM15() {
        return (mDevice.getModel() == ThetaDeviceModel.THETA_M15);
    }

    @Override
    public boolean isRecording() {
        return (mIsRecording == ShootingContract.RecordingState.RECORDING);
    }

    @Override
    public void startLiveStreaming() {
        try{
            if (!mApi.isRunning()) {
                mLiveView.startLivePreview();
            } else {
                mLiveView.stop();
                mLiveView.startLivePreview();
            }
        } catch (ThetaDeviceException e) {
            if (e.getReason() == ThetaDeviceException.NOT_FOUND_THETA) {
                showDisconnectDialog();
            }
        }
    }

    @Override
    public int takePicture() {
        try {
            if (mDevice.getModel() == ThetaDeviceModel.THETA_M15) {
                mDevice.takePicture();
            } else {
                mLiveView.stop();
                mDevice.takePicture();
                mLiveView.startLivePreview();
            }
        } catch (ThetaDeviceException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return e.getReason();
        }
        return -1;
    }

    @Override
    public int recording() {
        try {
            if (mIsRecording == ShootingContract.RecordingState.RECORDING) {
                mDevice.startVideoRecording();
            } else {
                mDevice.stopVideoRecording();
            }
            return -1;
        } catch (ThetaDeviceException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return e.getReason();
        }
    }

    @Override
    public void retryRecording() {
        mRecorder.postDelayed(mUpdater, 1000);
    }

    @Override
    public int changeMode(ThetaDevice.ShootingMode mode) {
        try {
            mDevice.changeShootingMode(mode);
            return -1;
        } catch (ThetaDeviceException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return e.getReason();
        }
    }
}
