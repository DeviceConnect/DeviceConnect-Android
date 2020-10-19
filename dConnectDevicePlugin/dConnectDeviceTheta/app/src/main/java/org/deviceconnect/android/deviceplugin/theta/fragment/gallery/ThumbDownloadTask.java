package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;
import org.deviceconnect.android.deviceplugin.theta.view.ThetaLoadingProgressView;

/**
 * Donwload of thumb.
 */
class ThumbDownloadTask implements DownloadThetaDataTask.ThetaDownloadListener {

    /** THETA Object. */
    private final ThetaObject mObj;

    /** View holder. */
    private final ThetaGalleryAdapter.GalleryViewHolder mHolder;

    /** Tag of thumbnail view. */
    private final String mTag;

    /** Thumbnail. */
    private byte[] mThumbnail;

    private GalleryContract.View mView;
    /**
     * Constructor.
     * @param obj THETA Object
     * @param holder view holder
     */
    ThumbDownloadTask(final GalleryContract.View view,
                      final ThetaObject obj,
                      final ThetaGalleryAdapter.GalleryViewHolder holder) {
        mView = view;
        mObj = obj;
        mHolder = holder;
        mTag = holder.mThumbnail.getTag().toString();
    }

    @Override
    public synchronized void doInBackground() {
        mThumbnail = mView.getThumbnail(mObj.getFileName());
        if (mThumbnail == null) {
            try {
                Thread.sleep(100);
                cacheThumbnail(mObj);
            } catch (ThetaDeviceException e) {
                if (BuildConfig.DEBUG) {
                    Log.e("theta.app", "error", e);
                }
            } catch (InterruptedException e) {
                // Nothing to do.
            }
        }
    }

    private void cacheThumbnail(final ThetaObject obj) throws ThetaDeviceException {
        obj.fetch(ThetaObject.DataType.THUMBNAIL);
        mThumbnail = obj.getThumbnailData();
        obj.clear(ThetaObject.DataType.THUMBNAIL);
    }

    @Override
    public synchronized void onPostExecute() {
        ImageView thumbView = mHolder.mThumbnail;
        ThetaLoadingProgressView loadingView = mHolder.mLoading;
        if (!mTag.equals(thumbView.getTag())) {
            return;
        }
        if (mThumbnail != null) {
            mView.putThumbnail(mObj.getFileName(), mThumbnail);
            thumbView = mHolder.mThumbnail;
            loadingView = mHolder.mLoading;
            if (mTag.equals(thumbView.getTag())) {
                Bitmap data = BitmapFactory.decodeByteArray(mThumbnail, 0, mThumbnail.length);

                thumbView.setImageBitmap(data);
                loadingView.setVisibility(View.GONE);
            }
        }
        loadingView.setVisibility(View.GONE);
        mView.updateStatusView();

    }
}
