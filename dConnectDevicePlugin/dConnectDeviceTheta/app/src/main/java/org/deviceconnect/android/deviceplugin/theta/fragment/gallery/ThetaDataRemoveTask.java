package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;


import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

import java.util.List;

/**
 * Remove Theta data.
 */
class ThetaDataRemoveTask implements DownloadThetaDataTask.ThetaDownloadListener {

    /**
     * Remove Theta data.
     */
    private ThetaObject mRemoveObject;

    /**
     * isSuccess.
     */
    private boolean mIsSuccess;
    private GalleryContract.View mView;
    ThetaDataRemoveTask(final GalleryContract.View view, final ThetaObject removeObject) {
        mView = view;
        mRemoveObject = removeObject;
        mIsSuccess = true;
    }

    @Override
    public synchronized void doInBackground() {
        try {
            mRemoveObject.remove();
        } catch (ThetaDeviceException e) {
            mIsSuccess = false;
        }
    }

    @Override
    public synchronized void onPostExecute() {
        if (mIsSuccess) {
            mView.showDialog(R.string.theta_remove);
        } else {
            mView.showDialog(R.string.theta_error_failed_delete);
        }

        List<ThetaObject> removedList = mView.removeObj(mRemoveObject);
        mView.notifyGalleryListChanged(removedList);
        mView.updateStatusView(removedList.size());
    }
}
