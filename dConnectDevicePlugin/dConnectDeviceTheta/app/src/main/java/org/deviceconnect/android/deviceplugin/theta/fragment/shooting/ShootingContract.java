package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;

interface ShootingContract {
    /** SphericalView Max fov.*/
    int MAX_FOV = 90;

    /** SphericalView Min fov.*/
    int MIN_FOV = 45;


    /** THETA m15's picture shooting mode. */
    int MODE_M15_SHOOTING = 0;

    /** THETA S's picture shooting mode. */
    int MODE_S_SHOOTING = 1;

    /** THETA movie shooting mode. */
    int MODE_MOVIE_SHOOTING = 2;

    /** Spinner THETA picture mode. */
    int SPINNER_MODE_PICTURE = 0;

    /** Spinner THETA movie mode. */
    int SPINNER_MODE_MOVIE = 1;
    /** Recording State. */
    enum RecordingState {
        /** Recording. */
        RECORDING,
        /** Recording stop. */
        STOP,
        /** Recording cancel. */
        CANCEL
    };
    interface Presenter {
        void startGetShootingModeTask();
        void startShootingChangeTask(ThetaDevice.ShootingMode mode);
        void startRecordingVideoTask();
        void startShootingTask();
        void stopTask();
    }
    interface View {
        void startRecordingDialog();
        void startProgressDialog(int message);
        void stopProgressDialog();
        void showDialog(int message);
        void showDisconnectDialog();
        void showFailedChangeRecordingMode();
        ThetaDevice.ShootingMode nowShootingMode();
        void setOnSelectListener();
        void initUpdater();
        void enabledMode(int mode);
        boolean existThetaDevice();
        boolean isLiveStreaming();
        boolean isThetaM15();
        boolean isRecording();

        void startLiveStreaming();
        int takePicture();
        int recording();
        void retryRecording();
        int changeMode(ThetaDevice.ShootingMode mode);
    }
}
