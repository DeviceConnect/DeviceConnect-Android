package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.view.ThetaLoadingProgressView;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ThetaGalleryAdapter.
 */
class ThetaGalleryAdapter extends ArrayAdapter<ThetaObject> {
    /**
     * LayoutInflater.
     */
    private LayoutInflater mInflater;
    private GalleryContract.Presenter mPresenter;
    /**
     * Constructor.
     *
     * @param context Context.
     * @param objects ThetaGalleryList.
     */
    public ThetaGalleryAdapter(final Context context, final List<ThetaObject> objects, final GalleryContract.Presenter presenter) {
        super(context, 0, objects);
        mPresenter = presenter;
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View cv = convertView;
        GalleryViewHolder holder;
        if (cv == null) {
            cv = mInflater.inflate(R.layout.theta_gallery_adapter, parent, false);
            holder = new GalleryViewHolder(cv);
            cv.setTag(holder);
        } else {
            holder = (GalleryViewHolder) cv.getTag();
        }

        ThetaObject data = getItem(position);
        holder.mThumbnail.setImageResource(R.drawable.theta_gallery_thumb);
        holder.mThumbnail.setTag(data.getFileName());
        holder.mLoading.setVisibility(View.VISIBLE);
        Date date = RFC3339DateUtils.toDate(data.getCreationTime());
        String dateString = null;
        if (date != null) {
            dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
        } else {
            dateString = data.getCreationTime();
        }
        holder.mDate.setText(dateString);
        if (data.isImage()) {
            holder.mType.setImageResource(R.drawable.theta_data_img);
            mPresenter.stopTask();
            mPresenter.startThumbDownloadTask(data, holder);
        } else {
            holder.mType.setImageResource(R.drawable.theta_data_mv);
            holder.mLoading.setVisibility(View.GONE);
        }

        return cv;
    }
    /**
     * Gallery View Holder.
     */
    static class GalleryViewHolder {

        ImageView mThumbnail;

        ImageView mType;

        TextView mDate;

        ThetaLoadingProgressView mLoading;

        GalleryViewHolder(final View view) {
            mThumbnail = view.findViewById(R.id.theta_thumb_data);
            mType = view.findViewById(R.id.data_type);
            mDate = view.findViewById(R.id.data_date);
            mLoading = view.findViewById(R.id.theta_thumb_progress);
        }

    }
}

