package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException.NOT_FOUND_OBJECT;

/**
 * Download of info.
 */
class ThetaInfoGetTask implements DownloadThetaDataTask.ThetaDownloadListener {

    private int mError = -1;

    private List<ThetaObject> mResult = new ArrayList<ThetaObject>();

    private GalleryContract.View mView;
    ThetaInfoGetTask(final GalleryContract.View  view) {
        mView = view;
    }

    @Override
    public void doInBackground() {
        if (mView.startProgressDialog(R.string.loading)) {
            return;
        }
        try {
            mResult = mView.getThetaObjects();
            if (mResult == null) {
                mError = NOT_FOUND_OBJECT;
            }
        } catch (ThetaDeviceException e) {
            mError = e.getReason();
        }
    }

    @Override
    public void onPostExecute() {
        mView.stopProgressDialog();
        if (mResult == null) {
            mView.showSettingsActivity();
            return;
        }
        mView.updateStatusView(mResult.size());
        mView.updateThetaObjectList(mResult);
        mView.notifyGalleryListChanged(mResult);

        if (mError > 0) {
            mView.showReconnectionDialog();
        }
    }
}
