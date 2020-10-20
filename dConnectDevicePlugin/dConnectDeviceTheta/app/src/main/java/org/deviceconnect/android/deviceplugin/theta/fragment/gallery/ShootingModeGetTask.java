package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

/** Get Shooting Mode Task. */
class ShootingModeGetTask implements DownloadThetaDataTask.ThetaDownloadListener {

    /**
     * Shooting mode.
     */
    private ThetaDevice.ShootingMode mNowShootingMode;
    private GalleryContract.View mView;
    /**
     * Constructor.
     */
    ShootingModeGetTask(final GalleryContract.View view) {
        mView = view;
        mNowShootingMode = ThetaDevice.ShootingMode.UNKNOWN;
    }

    @Override
    public void doInBackground() {
        if (mView.startProgressDialog(R.string.loading)) {
            return;
        }
        if (!mView.existThetaDevice()) {
            return;
        }
        mNowShootingMode = mView.getShootingMode();
    }

    @Override
    public void onPostExecute() {
        if (mNowShootingMode == ThetaDevice.ShootingMode.LIVE_STREAMING) {
            mView.startProgressDialogForReconnect(R.string.theta_error_usb_live_streaming);
        } else {
            mView.loadThetaData();
        }
    }
}