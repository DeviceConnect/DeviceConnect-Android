package org.deviceconnect.android.deviceplugin.theta.fragment.shooting;


import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

class ThetaShootingPresenter implements ShootingContract.Presenter {

    /** Theta Connect Tasker.*/
    private DownloadThetaDataTask mShootingTasker;

    private ShootingContract.View mView;

    ThetaShootingPresenter(final ShootingContract.View view) {
        mView = view;
    }


    @Override
    public void startGetShootingModeTask() {
        if (mShootingTasker == null) {
            mShootingTasker = new DownloadThetaDataTask();
            ShootingModeGetTask shootingGetTask = new ShootingModeGetTask(this, mView);
            mShootingTasker.execute(shootingGetTask);
        }
    }

    @Override
    public void startShootingChangeTask(final ThetaDevice.ShootingMode mode) {
        if (mShootingTasker == null) {
            mShootingTasker = new DownloadThetaDataTask();
            ShootingChangeTask shooting = new ShootingChangeTask(this, mView, mode);
            mShootingTasker.execute(shooting);
        }
    }

    @Override
    public void startRecordingVideoTask() {
        if (mShootingTasker == null) {
            mShootingTasker = new DownloadThetaDataTask();
            RecordingVideoTask recording = new RecordingVideoTask(this, mView);
            mShootingTasker.execute(recording);
        }
    }

    @Override
    public void startShootingTask() {
        if (mShootingTasker == null) {
            mShootingTasker = new DownloadThetaDataTask();
            ShootingTask shooting = new ShootingTask(this, mView);
            mShootingTasker.execute(shooting);
        }
    }

    @Override
    public void stopTask() {
        if (mShootingTasker != null) {
            mShootingTasker.cancel(true);
            mShootingTasker = null;
        }
    }
}
