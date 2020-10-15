package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.SPINNER_MODE_MOVIE;
import static org.deviceconnect.android.deviceplugin.theta.fragment.shooting.ShootingContract.SPINNER_MODE_PICTURE;

/** Get Shooting Mode Task. */
class ShootingModeGetTask implements DownloadThetaDataTask.ThetaDownloadListener {
    private ShootingContract.Presenter mPresenter;
    private ShootingContract.View mView;
    /** Now Shooting Mode. */
    private ThetaDevice.ShootingMode mNowShootingMode;

    /**
     * Constructor.
     */
    public ShootingModeGetTask(final ShootingContract.Presenter presenter,
                               final ShootingContract.View view) {
        mPresenter = presenter;
        mView = view;
    }

    @Override
    public void doInBackground() {
        mNowShootingMode = mView.nowShootingMode();
    }

    @Override
    public void onPostExecute() {
        if (!mView.existThetaDevice()) {
            return;
        }

        if (mNowShootingMode == ThetaDevice.ShootingMode.UNKNOWN) {
            mView.showDialog(R.string.theta_error_get_mode);
        } else if (mNowShootingMode == ThetaDevice.ShootingMode.LIVE_STREAMING) {
            mView.showDialog(R.string.theta_error_usb_live_streaming);
        } else if (mNowShootingMode == ThetaDevice.ShootingMode.VIDEO) {
            mView.enabledMode(SPINNER_MODE_MOVIE);
        } else if (mView.isLiveStreaming()){
            mView.enabledMode(SPINNER_MODE_PICTURE);
            mView.startLiveStreaming();
        } else {
            mView.enabledMode(SPINNER_MODE_PICTURE);
        }

        mView.setOnSelectListener();
        mPresenter.stopTask();
    }

}
