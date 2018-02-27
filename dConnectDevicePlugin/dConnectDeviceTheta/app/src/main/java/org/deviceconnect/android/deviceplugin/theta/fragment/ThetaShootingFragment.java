/*
 ThetaShootingModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

/**
 * Shooting in Theta.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaShootingFragment extends Fragment implements ThetaDeviceEventListener {
    /** SphericalView Max fov.*/
    private static final int MAX_FOV = 90;

    /** SphericalView Min fov.*/
    private static final int MIN_FOV = 45;


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

    /** Theta Device. */
    private ThetaDevice mDevice;

    /** Theta Connect Tasker.*/
    private DownloadThetaDataTask mShootingTasker;
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
    private RecordingState mIsRecording;

    /** Now Shooting Mode. */
    private ThetaDevice.ShootingMode mNowShootingMode;
    /** Scale Gesture.*/
    private ScaleGestureDetector mScaleDetector;
    /** Scale factor. */
    private float mScaleFactor = 90.0f;

    /** SphericalViewApi. */
    private SphericalViewApi mApi;


    /** Recording State. */
    private enum RecordingState {
        /** Recording. */
        RECORDING,
        /** Recording stop. */
        STOP,
        /** Recording cancel. */
        CANCEL
    };


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
                            mIsRecording = RecordingState.CANCEL;
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
            if (mShootingTasker != null) {
                return;
            }
            compoundButton.setEnabled(false);
            mShootingTasker = new DownloadThetaDataTask();
            mIsRecording = RecordingState.RECORDING;
            if (mIsRecording != RecordingState.CANCEL && !isRecordered) {
                mIsRecording = RecordingState.STOP;
            }

            RecordingVideoTask recording = new RecordingVideoTask();
            mShootingTasker.execute(recording);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mShootingButton.setEnabled(true);
                }
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
        if (mDevice == null) {
            ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                    getString(R.string.theta_error_disconnect_dialog_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getActivity().finish();
                        }
                    });

            return rootView;
        }
        mShootingTime = (TextView) rootView.findViewById(R.id.shooting_time);
        mLiveView = (SphericalImageLiveView) rootView.findViewById(R.id.shooting_preview);
        mApi = app.getSphericalViewApi();
        mLiveView.setViewApi(mApi);
        mLiveView.setDeviceManager(deviceMgr);
        initShootingLayouts(rootView);
        mShootingMode = (Spinner) rootView.findViewById(R.id.theta_shooting_mode);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                                            R.layout.theta_shooting_mode_adapter,
                                                            getResources().getStringArray(R.array.theta_shooting_mode));

        mShootingMode.setAdapter(adapter);
        setRetainInstance(true);
        if (mShootingTasker == null) {
            mShootingTasker = new DownloadThetaDataTask();
            ShootingModeGetTask shootingGetTask = new ShootingModeGetTask();
            mShootingTasker.execute(shootingGetTask);
        }
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
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.finish();
                }
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

        if (mShootingTasker != null) {
            mShootingTasker.cancel(true);
            mShootingTasker = null;
        }

        if (mNowShootingMode == ThetaDevice.ShootingMode.VIDEO
                && mIsRecording == RecordingState.RECORDING) {
            mShootingTasker = new DownloadThetaDataTask();
            mIsRecording = RecordingState.STOP;
            RecordingVideoTask stoping = new RecordingVideoTask();
            mShootingTasker.execute(stoping);
            mShootingButton.setOnCheckedChangeListener(null);
            mShootingButton.setChecked(false);
            mShootingButton.setOnCheckedChangeListener(mRecordingListener);
        }
        if (mLiveView != null) {
            mLiveView.onPause();
        }
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
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
                if (mDevice != null && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)) {
                    if (mNowShootingMode != ThetaDevice.ShootingMode.IMAGE) {
                        mNowShootingMode = ThetaDevice.ShootingMode.IMAGE;
                        mShootingTasker = new DownloadThetaDataTask();
                        ShootingChangeTask shooting = new ShootingChangeTask(mNowShootingMode);
                        mShootingTasker.execute(shooting);
                    }
                    mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.GONE);
                    mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.VISIBLE);
                    mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                } else {
                    if (mNowShootingMode != ThetaDevice.ShootingMode.IMAGE) {
                        ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                                getString(R.string.theta_error_failed_change_mode), null);
                    }
                    mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.VISIBLE);
                    mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.GONE);
                    mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                }
                break;
            case SPINNER_MODE_MOVIE:
            default:
                if (mShootingMode.getSelectedItemPosition() == SPINNER_MODE_PICTURE
                        && mDevice != null
                        && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)) {
                    mLiveView.stop();
                }
                if (mNowShootingMode != ThetaDevice.ShootingMode.VIDEO
                        && mDevice != null
                        && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)) {
                    mShootingTasker = new DownloadThetaDataTask();
                    mNowShootingMode = ThetaDevice.ShootingMode.VIDEO;
                    ShootingChangeTask shooting = new ShootingChangeTask(mNowShootingMode);
                    mShootingTasker.execute(shooting);
                } else if (mNowShootingMode != ThetaDevice.ShootingMode.VIDEO
                        && mDevice != null
                        && mDevice.getModel() == ThetaDeviceModel.THETA_M15) {
                        ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                                getString(R.string.theta_error_failed_change_mode), null);
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
                mShootingButtons[i] = (Button) rootView.findViewById(identifier);
                mShootingButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        view.setEnabled(false);
                        mShootingTasker = new DownloadThetaDataTask();
                        ShootingTask shooting = new ShootingTask();
                        mShootingTasker.execute(shooting);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                view.setEnabled(true);
                            }
                        }, 500);
                    }
                });
            }
        }
        mShootingTime = (TextView) rootView.findViewById(R.id.shooting_time);
    }

    /** Shooting Picture Task. */
    private class ShootingTask implements DownloadThetaDataTask.ThetaDownloadListener {

        /** Theta Exception. */
        private int mException;

        /**
         * Constructor.
         */
        ShootingTask() {
            mException = -1;
            if (mProgress == null) {
                mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.shooting));
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }
        }
        @Override
        public void doInBackground() {
            if (mDevice == null) {
                return;
            }
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
                mException = e.getReason();
            }
        }

        @Override
        public void onPostExecute() {
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }
            if (mDevice == null) {
                return;
            }
            if (mException == ThetaDeviceException.NOT_FOUND_THETA) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_disconnect_dialog_message),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        });
            } else if (mException != -1 && mDevice.getModel() == ThetaDeviceModel.THETA_M15){
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_failed_change_mode), null);
            } else if (mException != -1 && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_shooting), null);
            } else {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_shooting), null);

            }
            if (mShootingTasker != null) {
                mShootingTasker.cancel(true);
                mShootingTasker = null;
            }
        }
    }

    /** Recording Video. */
    private class RecordingVideoTask implements DownloadThetaDataTask.ThetaDownloadListener {


        /** Theta Device Exception. */
        private int mException;

        /**
         * Constructor.
         */
        RecordingVideoTask() {
            mException = -1;
            String message = getString(R.string.recording);
            if (mIsRecording != RecordingState.RECORDING) {
                message = getString(R.string.stoping);
            }
            if (mProgress == null) {
                mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), message);
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }
        }

        @Override
        public void doInBackground() {
            if (mDevice == null) {
                return;
            }
            try {
                if (mIsRecording == RecordingState.RECORDING) {
                    mDevice.startVideoRecording();
                } else {
                    mDevice.stopVideoRecording();
                }
                mException = -1;
            } catch (ThetaDeviceException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                mException = e.getReason();
            }
        }

        @Override
        public void onPostExecute() {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }
            if (mDevice == null) {
                return;
            }
            mRecordTime = 0;
            mShootingTime.setText("00:00");
            mRecorder.removeCallbacks(mUpdater);

            if (mIsRecording == RecordingState.RECORDING
                    && mException != -1
                    && mDevice.getModel() == ThetaDeviceModel.THETA_M15) {
                ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_failed_change_mode),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mShootingButton.setOnCheckedChangeListener(null);
                                mShootingButton.setChecked(false);
                                mShootingButton.setOnCheckedChangeListener(mRecordingListener);
                            }
                        });
            } else if (mIsRecording == RecordingState.RECORDING
                    && mException != -1
                    && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)) {
                ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_record_start), null);
            } else if (mIsRecording != RecordingState.RECORDING && mException != -1) {
                ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_record_stop), null);
            } else if (mIsRecording == RecordingState.RECORDING && mException == -1) {
                mRecorder.postDelayed(mUpdater, 1000);
            } else if (mIsRecording == RecordingState.STOP && mException == -1) {
                ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_shooting), null);
            } else {
                ThetaDialogFragment.showAlert(activity, getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_limit_shooting_time), null);
            }
            if (mShootingTasker != null) {
                mShootingTasker.cancel(true);
                mShootingTasker = null;
            }
        }

    }

    /** Shooting Mode Change Task. */
    private class ShootingChangeTask implements DownloadThetaDataTask.ThetaDownloadListener {
        /** Theta Device Exception. */
        private int mException;
        /** Theta Shooting mode index. */
        private ThetaDevice.ShootingMode mMode;
        /**
         * Constructor.
         * @param mode Theta Device's Shooting Mode
         */
        ShootingChangeTask(final ThetaDevice.ShootingMode mode) {
            mMode = mode;
            if (mProgress == null) {
                mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.switching));
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }
        }

        @Override
        public void doInBackground() {
            if (mDevice == null) {
                return;
            }
            int count = 0;
            do {
                try {
                    mDevice.changeShootingMode(mMode);
                    mException = -1;
                    break;
                } catch (ThetaDeviceException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    mException = e.getReason();
                    count++;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            } while(count < 10); // Retry
        }

        @Override
        public void onPostExecute() {
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }
            if (mException != -1 && mDevice.getModel() == ThetaDeviceModel.THETA_M15) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_failed_change_mode), null);
            } else if (mException != -1 && (mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_change_mode), null);
            } else {
                if ((mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)
                        && mMode == ThetaDevice.ShootingMode.IMAGE) {
                    try {
                        if (!mApi.isRunning()) {
                            mLiveView.startLivePreview();
                        } else {
                            mLiveView.stop();
                            mLiveView.startLivePreview();
                        }
                    } catch (ThetaDeviceException e) {
                        e.printStackTrace();
                        if (e.getReason() == ThetaDeviceException.NOT_FOUND_THETA) {
                            ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                                    getString(R.string.theta_error_disconnect_dialog_message),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            getActivity().finish();
                                        }
                                    });
                            return;
                        }
                    }
                }
            }
            if (mShootingTasker != null) {
                mShootingTasker.cancel(true);
                mShootingTasker = null;
            }
        }
    }

    /** Get Shooting Mode Task. */
    private class ShootingModeGetTask implements DownloadThetaDataTask.ThetaDownloadListener {
        /**
         * Constructor.
         */
        ShootingModeGetTask() {
            mNowShootingMode = ThetaDevice.ShootingMode.UNKNOWN;
            mShootingMode.setOnItemSelectedListener(null);
        }

        @Override
        public void doInBackground() {
            if (mDevice == null) {
                return;
            }
            try {
                mNowShootingMode = mDevice.getShootingMode();
            } catch (ThetaDeviceException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                mNowShootingMode = ThetaDevice.ShootingMode.UNKNOWN;
            }
        }

        @Override
        public void onPostExecute() {
            if (mDevice == null) {
                return;
            }

            if (mNowShootingMode == ThetaDevice.ShootingMode.UNKNOWN) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_get_mode), null);
            } else if (mNowShootingMode == ThetaDevice.ShootingMode.LIVE_STREAMING) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_usb_live_streaming), null);
            } else if (mNowShootingMode == ThetaDevice.ShootingMode.VIDEO) {
                enableShootingMode(SPINNER_MODE_MOVIE);
                mShootingMode.setSelection(SPINNER_MODE_MOVIE);
            } else if ((mDevice.getModel() == ThetaDeviceModel.THETA_S || mDevice.getModel() == ThetaDeviceModel.THETA_V)){
                enableShootingMode(SPINNER_MODE_PICTURE);
                mShootingMode.setSelection(SPINNER_MODE_PICTURE);
                try{
                    if (!mApi.isRunning()) {
                        mLiveView.startLivePreview();
                    } else {
                        mLiveView.stop();
                        mLiveView.startLivePreview();
                    }

                } catch (ThetaDeviceException e) {
                    if (e.getReason() == ThetaDeviceException.NOT_FOUND_THETA) {
                        ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                                getString(R.string.theta_error_disconnect_dialog_message),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getActivity().finish();
                                    }
                                });
                    }
                }

            } else {
                enableShootingMode(SPINNER_MODE_PICTURE);
                mShootingMode.setSelection(SPINNER_MODE_PICTURE);
            }


            mShootingMode.setOnItemSelectedListener(mModeListener);

            if (mShootingTasker != null) {
                mShootingTasker.cancel(true);
                mShootingTasker = null;
            }
        }
    }
}
