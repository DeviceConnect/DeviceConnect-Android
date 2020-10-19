package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

/** Shooting Picture Task. */
class ShootingTask implements DownloadThetaDataTask.ThetaDownloadListener {

    /** Theta Exception. */
    private int mException;
    private ShootingContract.Presenter mPresenter;
    private ShootingContract.View mView;

    /**
     * Constructor.
     */
    ShootingTask(final ShootingContract.Presenter presenter,
                 final ShootingContract.View view) {
        mException = -1;
        mPresenter = presenter;
        mView = view;
        mView.startProgressDialog(R.string.shooting);
    }
    @Override
    public void doInBackground() {
        if (!mView.existThetaDevice()) {
            return;
        }
        mException = mView.takePicture();
    }

    @Override
    public void onPostExecute() {
        mView.stopProgressDialog();
        if (!mView.existThetaDevice()) {
            return;
        }
        if (mException == ThetaDeviceException.NOT_FOUND_THETA) {
            mView.showDisconnectDialog();
        } else if (mException != -1 && mView.isThetaM15()){
            mView.showDialog(R.string.theta_error_failed_change_mode);
        } else if (mException != -1 && mView.isLiveStreaming()) {
            mView.showDialog(R.string.theta_error_shooting);
        } else {
            mView.showDialog(R.string.theta_shooting);
        }
        mPresenter.stopTask();
    }
}