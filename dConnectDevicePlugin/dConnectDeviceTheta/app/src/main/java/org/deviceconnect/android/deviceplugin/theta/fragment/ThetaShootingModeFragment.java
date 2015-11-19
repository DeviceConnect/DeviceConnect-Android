/*
 ThetaShootingModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

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


    /** Timer Runnable. */
    private Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            String display = "";
            if (mRecordTime < 60) {
                display = String.format("%02d", mRecordTime);
            } else {
                display = String.format("%d:%02d", mRecordTime / 60, mRecordTime % 60);
            }
            Activity activity = getActivity();
            final String dispLabel = display;
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mShootingTime.setText(dispLabel);

                        // TODO THETA's Recording limit.
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.theta_shooting_mode, null);
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        ThetaDeviceManager deviceMgr = app.getDeviceManager();
        mDevice = deviceMgr.getConnectedDevice();
        if (mDevice == null) {
            ThetaDialogFragment.showAlert(getActivity(), "THETA",
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
        initShootingLayouts(rootView);
        mShootingMode = (Spinner) rootView.findViewById(R.id.theta_shooting_mode);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                                            R.layout.theta_shooting_mode_adapter,
                                                            getResources().getStringArray(R.array.theta_shooting_mode));
        mShootingMode.setAdapter(adapter);
        mShootingMode.setSelection(0);
        enableShootingMode(0);
        mShootingMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    @Override
    public void onResume() {
        super.onResume();
        // TODO Recording Mode
        mShootingMode.setVisibility(View.GONE);
        if (mShootingMode.getSelectedItemPosition() == SPINNER_MODE_PICTURE
            && mDevice.getModel().equals("THETA_S")) {
            try{
                mLiveView.startLivePreview();
            } catch (ThetaDeviceException e) {
                if (e.getReason() == ThetaDeviceException.NOT_FOUND_THETA) {
                    ThetaDialogFragment.showAlert(getActivity(), "THETA",
                            getString(R.string.theta_error_disconnect_dialog_message),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().finish();
                                }
                            });
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mShootingMode.getSelectedItemPosition() == SPINNER_MODE_PICTURE
                && mDevice.getModel().equals("THETA_S")) {
            mLiveView.stop();
        }
    }

    /**
     * Enable Shooting mode.
     * @param mode mode
     */
    private void enableShootingMode(final int mode) {
        mLiveView.stopLivePreview();
        switch (mode) {
            case SPINNER_MODE_PICTURE:
                if (mDevice.getModel().equals("THETA_S")) {
                    try {
                        mLiveView.startLivePreview();
                    } catch (ThetaDeviceException e) {
                        e.printStackTrace();
                        if (e.getReason() == ThetaDeviceException.NOT_FOUND_THETA) {
                            ThetaDialogFragment.showAlert(getActivity(), "THETA",
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
                    mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.GONE);
                    mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.VISIBLE);
                    mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                } else {
                    mShootingLayouts[MODE_M15_SHOOTING].setVisibility(View.VISIBLE);
                    mShootingLayouts[MODE_S_SHOOTING].setVisibility(View.GONE);
                    mShootingLayouts[MODE_MOVIE_SHOOTING].setVisibility(View.GONE);
                }
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
                mShootingButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isRecordered) {
                        mShootingTasker = new DownloadThetaDataTask();
                        RecordingVideoTask recording = new RecordingVideoTask(isRecordered);
                        mShootingTasker.execute(recording);
                    }
                });
            } else {
                mShootingButtons[i] = (Button) rootView.findViewById(identifier);
                mShootingButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mShootingTasker != null) {
                            return;
                        }
                        if (mProgress == null) {
                            mProgress = ThetaDialogFragment.newInstance("THETA", getString(R.string.shooting));
                            mProgress.show(getActivity().getFragmentManager(),
                                    "fragment_dialog");
                        }

                        mShootingTasker = new DownloadThetaDataTask();
                        ShootingTask shooting = new ShootingTask();
                        mShootingTasker.execute(shooting);
                    }
                });
            }
        }
        mShootingTime = (TextView) rootView.findViewById(R.id.shooting_time);
        mLiveView = (SphericalImageLiveView) rootView.findViewById(R.id.shooting_preview);
    }

    /** Shooting Picture Task. */
    private class ShootingTask implements DownloadThetaDataTask.ThetaDownloadListener {

        /** Theta Exception. */
        private ThetaDeviceException mException;

        @Override
        public void doInBackground() {
            try {
                mDevice.takePicture();
                mException = null;
            } catch (ThetaDeviceException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                mException = e;
            }
        }

        @Override
        public void onPostExecute() {
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }
            if (mException != null) {
                // TODO Error Reason
                if (mException.getReason() == ThetaDeviceException.NOT_FOUND_THETA) {
                    ThetaDialogFragment.showAlert(getActivity(), "THETA",
                            getString(R.string.theta_error_disconnect_dialog_message),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().finish();
                                }
                            });
                } else {
                    ThetaDialogFragment.showAlert(getActivity(), "THETA",
                            getString(R.string.theta_error_shooting), null);
                }
            } else {
                ThetaDialogFragment.showAlert(getActivity(), "THETA",
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

        /** Is Recording. */
        private boolean mIsRecording;

        /** Theta Device Exception. */
        private ThetaDeviceException mException;

        /**
         * Constructor.
         * @param isRecording true:Recording false:Stop
         */
        RecordingVideoTask(final boolean isRecording) {
            mIsRecording = isRecording;
            mException = null;
        }

        @Override
        public void doInBackground() {
            try {
                if (mIsRecording) {
                    mDevice.startVideoRecording();
                } else {
                    mDevice.stopVideoRecording();
                }
            } catch (ThetaDeviceException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                mException = e;
            }
        }

        @Override
        public void onPostExecute() {
            mRecordTime = 0;
            mShootingTime.setText("00:00");
            mRecorder.removeCallbacks(mUpdater);

            if (mIsRecording && mException != null) {

                ThetaDialogFragment.showAlert(getActivity(), "THETA",
                        getString(R.string.theta_error_record_start),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        });
                return;
            } else if (!mIsRecording && mException != null) {
                ThetaDialogFragment.showAlert(getActivity(), "THETA",
                        getString(R.string.theta_error_record_start),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        });
                return;
            }
            if (mIsRecording) {
                mRecorder.postAtTime(mUpdater, 1000);
            } else {
                ThetaDialogFragment.showAlert(getActivity(), "THETA",
                        getString(R.string.theta_shooting), null);

            }
        }
    }
}
