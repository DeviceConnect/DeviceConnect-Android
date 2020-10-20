package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;


import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

/** Shooting Mode Change Task. */
class ShootingChangeTask implements DownloadThetaDataTask.ThetaDownloadListener {
    /** Theta Device Exception. */
    private int mException;
    /** Theta Shooting mode index. */
    private ThetaDevice.ShootingMode mMode;
    private ShootingContract.Presenter mPresenter;
    private ShootingContract.View mView;

    /**
     * Constructor.
     * @param mode Theta Device's Shooting Mode
     */
    ShootingChangeTask(final ShootingContract.Presenter presenter,
                              final ShootingContract.View view,
                              final ThetaDevice.ShootingMode mode) {
        mMode = mode;
        mPresenter = presenter;
        mView = view;
        mView.startProgressDialog(R.string.switching);
    }

    @Override
    public void doInBackground() {
        if (!mView.existThetaDevice()) {
            return;
        }
        int count = 0;
        do {
            mException = mView.changeMode(mMode);
            if (mException == -1) {
                break;
            } else {
                count++;
            }
        } while(count < 10); // Retry
    }

    @Override
    public void onPostExecute() {
        mView.stopProgressDialog();
        if (mException != -1 && mView.isThetaM15()) {
            mView.showDialog(R.string.theta_error_failed_change_mode);
        } else if (mException != -1 && mView.isLiveStreaming()) {
            mView.showDialog(R.string.theta_error_change_mode);
        } else {
            if (mView.isLiveStreaming() && mMode == ThetaDevice.ShootingMode.IMAGE) {
                mView.startLiveStreaming();
            }
        }
        mPresenter.stopTask();
    }
}
