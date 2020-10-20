package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

class ThetaGalleryPresenter implements GalleryContract.Presenter {
    /**
     * Download Task.
     */
    private DownloadThetaDataTask mDownloadTask;

    private GalleryContract.View mView;

    ThetaGalleryPresenter(final GalleryContract.View view) {
        mView = view;
    }


    @Override
    public synchronized void startShootingModeGetTask() {
        if (mDownloadTask == null) {
            ShootingModeGetTask mode = new ShootingModeGetTask(mView);
            mDownloadTask = new DownloadThetaDataTask();
            mDownloadTask.execute(mode);
        }
    }

    @Override
    public synchronized void startThetaDataRemoveTask(final ThetaObject removeObject) {
        if (mDownloadTask == null) {
            ThetaDataRemoveTask removeTask = new ThetaDataRemoveTask(mView, removeObject);
            mDownloadTask = new DownloadThetaDataTask();
            mDownloadTask.execute(removeTask);
        }
    }

    @Override
    public synchronized  void startThumbDownloadTask(final ThetaObject obj,
                                       final ThetaGalleryAdapter.GalleryViewHolder holder) {
        ThumbDownloadTask thumbTask = new ThumbDownloadTask(mView, obj, holder);
        DownloadThetaDataTask downloadTask = new DownloadThetaDataTask();
        downloadTask.execute(thumbTask);
    }

    @Override
    public synchronized  void startThetaInfoGetTask() {
        if (mDownloadTask == null) {
            ThetaInfoGetTask info = new ThetaInfoGetTask(mView);
            mDownloadTask = new DownloadThetaDataTask();
            mDownloadTask.execute(info);
        }
    }

    @Override
    public synchronized  void stopTask() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
    }
}
