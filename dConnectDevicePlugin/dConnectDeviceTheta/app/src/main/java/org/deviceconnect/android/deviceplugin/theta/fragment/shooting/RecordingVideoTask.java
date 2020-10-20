package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

/** Recording Video. */
class RecordingVideoTask implements DownloadThetaDataTask.ThetaDownloadListener {


    /** Theta Device Exception. */
    private int mException;
    private ShootingContract.Presenter mPresenter;
    private ShootingContract.View mView;

    /**
     * Constructor.
     */
    RecordingVideoTask(final ShootingContract.Presenter presenter,
                              final ShootingContract.View view) {
        mException = -1;
        mPresenter = presenter;
        mView = view;
        mView.startRecordingDialog();
    }

    @Override
    public void doInBackground() {
        if (!mView.existThetaDevice()) {
            return;
        }
        mException = mView.recording();
    }

    @Override
    public void onPostExecute() {
        mView.stopProgressDialog();
        if (!mView.existThetaDevice()) {
            return;
        }
        mView.initUpdater();

        if (mView.isRecording()) {
            if (mException != -1 && mView.isThetaM15()) {
                mView.showFailedChangeRecordingMode();
            } else if (mException != -1 && mView.isLiveStreaming()) {
                mView.showDialog(R.string.theta_error_record_start);
            } else if (mException != -1) {
                mView.showDialog(R.string.theta_error_record_stop);
            } else {
                mView.retryRecording();
            }
        } else {
            if (!mView.isRecording() && mException == -1) {
                mView.showDialog(R.string.theta_shooting);
            } else {
                mView.showDialog(R.string.theta_error_limit_shooting_time);
            }
        }
        mPresenter.stopTask();
    }

}
