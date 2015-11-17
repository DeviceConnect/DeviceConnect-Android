package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaFeatureActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.view.ThetaLoadingProgressView;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;

import java.util.ArrayList;
import java.util.List;

/**
 * THETA Device's Gallery Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaGalleryFragment extends Fragment {

    /**
     * Item per page.
     */
    private static final int PER_PAGE = 20;

    /**
     * Theta's Gallery.
     */
    private ThetaGalleryAdapter mGalleryAdapter;

    /**
     * Theta disconnect warning view.
     */
    private RelativeLayout mRecconectLayout;

    /**
     * Theta status TextView.
     */
    private TextView mStatusView;

    /**
     * Theta's Loading View.
     */
    private LinearLayout mLoadingView;
    /**
     * Root View.
     */
    private View mRootView;

    /**
     * Download Task.
     */
    private DownloadThetaDataTask mDownloadTask;
    /**
     * Progress.
     */
    private ThetaDialogFragment mProgress;

    /**
     * Theta's data.
     */
    private List<ThetaObject> mUpdateList = new ArrayList<ThetaObject>();

    /** Theta Device.*/
    private ThetaDevice mDevice;

    /**
     * Gallery State.
     */
    enum GalleryState {

        /**
         * First load data.
         */
        INIT,

        /**
         * Update load data.
         */
        UPDATE,

        /**
         * Update Thumbnail.
         */
        THUMBNAIL

    }

    /**
     * Singleton.
     */
    public static ThetaGalleryFragment newInstance() {
        return new ThetaGalleryFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mGalleryAdapter = new ThetaGalleryAdapter(getActivity(), new ArrayList<ThetaObject>());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.theta_gallery, container, false);
        getActivity().getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
        int color = R.color.action_bar_background;
        Drawable backgroundDrawable = getActivity().getApplicationContext().getResources().getDrawable(color);
        getActivity().getActionBar().setBackgroundDrawable(backgroundDrawable);
        mLoadingView = (LinearLayout) mRootView.findViewById(R.id.theta_gallery_progress);
        mRecconectLayout = (RelativeLayout) mRootView.findViewById(R.id.theta_reconnect_layout);
        mRootView.findViewById(R.id.theta_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ThetaDeviceSettingsActivity.class);
                startActivity(intent);
            }
        });
        mRootView.findViewById(R.id.theta_shutter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                        ThetaFeatureActivity.MODE_SHOOTING);
                intent.setClass(getActivity(), ThetaFeatureActivity.class);
                startActivity(intent);
            }
        });
        mStatusView = (TextView) mRootView.findViewById(R.id.theta_no_data);
        mStatusView.setVisibility(View.VISIBLE);
        initListView(mRootView);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadThetaData();
    }

        @Override
    public void onPause() {
        super.onPause();
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }

    /**
     * ListView Initialize.
     *
     * @param rootView Root View
     */
    private void initListView(final View rootView) {
        AbsListView list = (AbsListView) rootView.findViewById(R.id.theta_list);
        list.setAdapter(mGalleryAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView,
                                    final View view,
                                    final int position,
                                    final long id) {
                if (!mUpdateList.get(position).isImage()) {
                    ThetaDialogFragment.showAlert(getActivity(),
                            "THETA",
                            getString(R.string.theta_error_unsupported_movie));
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                        ThetaFeatureActivity.MODE_VR);
                intent.putExtra(ThetaFeatureActivity.FEATURE_DATA,
                        position);
                intent.setClass(getActivity(), ThetaFeatureActivity.class);
                startActivity(intent);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView,
                                           final View view, final int position,
                                           final long id) {
                String typeString = getString(R.string.theta_remove_data);
                ThetaObject removeObject = mUpdateList.get(position);
                String[] mode = getResources().getStringArray(R.array.theta_shooting_mode);
                String type = mode[0];
                if (!removeObject.isImage()) {
                    type = mode[1];
                }
                typeString = typeString.replace("$NAME$", type);
                ThetaDialogFragment.showConfirmAlert(getActivity(),
                        "THETA", typeString, getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                RemoveThetaData removeObj = new RemoveThetaData(mUpdateList.remove(position));
                                new DownloadThetaDataTask().execute(removeObj);
                            }
                        });
                return true;
            }
        });
    }

    /**
     * Load Theta Datas.
     */
    private void loadThetaData() {
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        ThetaDeviceManager deviceMgr = app.getDeviceManager();
        mDevice = deviceMgr.getConnectedDevice();
        if (mDevice != null) {
            String ssId = mDevice.getName();
            getActivity().getActionBar().setTitle(ssId);
            if (mProgress == null) {
                mProgress = ThetaDialogFragment.newInstance("THETA", "読み込み中...");
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }
            ThetaInfoTask info = new ThetaInfoTask();
            mDownloadTask = new DownloadThetaDataTask();
            mDownloadTask.execute(info);
        } else {
            Toast.makeText(getActivity(), R.string.camera_must_connect, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setClass(getActivity(), ThetaDeviceSettingsActivity.class);
            startActivity(intent);
        }
    }


    /**
     * ThetaGalleryAdapter.
     */
    private class ThetaGalleryAdapter extends ArrayAdapter<ThetaObject> {
        /**
         * LayoutInflater.
         */
        private LayoutInflater mInflater;

        /**
         * コンストラクタ.
         *
         * @param context Context.
         * @param objects ThetaGalleryList.
         */
        public ThetaGalleryAdapter(final Context context, final List<ThetaObject> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View cv = convertView;
            if (cv == null) {
                cv = mInflater.inflate(R.layout.theta_gallery_adapter, parent, false);
            } else {
                cv = convertView;
            }
            ImageView thumb = (ImageView) cv.findViewById(R.id.theta_thumb_data);
            ImageView type = (ImageView) cv.findViewById(R.id.data_type);
            TextView date = (TextView) cv.findViewById(R.id.data_date);
            ThetaLoadingProgressView progress = (ThetaLoadingProgressView)
                    cv.findViewById(R.id.theta_thumb_progress);

            ThetaObject data = getItem(position);
            String dateString = data.getCreationTime();
            date.setText(dateString);
            if (data.isImage()) {
                type.setImageResource(R.drawable.theta_data_img);
                if (data.isFetched(ThetaObject.DataType.THUMBNAIL)) {
                    progress.setVisibility(View.GONE);
                    Bitmap b = BitmapFactory.decodeByteArray(data.getThumbnailData(), 0, data.getThumbnailData().length);
                    thumb.setImageBitmap(b);
                }
            } else {
                thumb.setImageResource(R.drawable.theta_gallery_thumb);
                type.setImageResource(R.drawable.theta_data_mv);
                progress.setVisibility(View.GONE);
            }

            return cv;
        }
    }


    /**
     * Download of info.
     */
    private class ThetaInfoTask implements DownloadThetaDataTask.ThetaDownloadListener {

        @Override
        public synchronized void doInBackground() {
            try {  //Case m15
                mUpdateList = mDevice.fetchAllObjectList();
            } catch (ThetaDeviceException ex) {
                return;
            }

        }

        @Override
        public synchronized void onPostExecute() {
            if (mUpdateList.size() > 0) {
                mStatusView.setVisibility(View.GONE);
            } else {
                mStatusView.setVisibility(View.VISIBLE);
            }
            if (mGalleryAdapter != null) {
                mGalleryAdapter.clear();
                mGalleryAdapter.addAll(mUpdateList);
                mGalleryAdapter.notifyDataSetChanged();
            }
            if (mProgress != null) {
                mProgress.dismiss();
            }
            mLoadingView.setVisibility(View.GONE);
            ThetaThumbTask thumbTask = new ThetaThumbTask();
            DownloadThetaDataTask downloader = new DownloadThetaDataTask();
            downloader.execute(thumbTask);
        }
    }

    /**
     * Donwload of thumb.
     */
    private class ThetaThumbTask implements DownloadThetaDataTask.ThetaDownloadListener {

        @Override
        public synchronized void doInBackground() {
            for (ThetaObject object : mUpdateList) {
                try {
                    if (!object.isFetched(ThetaObject.DataType.THUMBNAIL)) {
                        object.fetch(ThetaObject.DataType.THUMBNAIL);
                        try {
                            Thread.sleep(100);  // Delay
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ThetaDeviceException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e("AAA", "error", e);
                    }
                }
            }
        }

        @Override
        public synchronized void onPostExecute() {
            if (mGalleryAdapter != null) {
                mGalleryAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Remove Theta data.
     */
    private class RemoveThetaData implements DownloadThetaDataTask.ThetaDownloadListener {

        /**
         * Remove Theta data.
         */
        private ThetaObject mRemoveObject;

        /**
         * isSuccess.
         */
        private boolean mIsSuccess;

        RemoveThetaData(final ThetaObject removeObject) {
            mRemoveObject = removeObject;
            mIsSuccess = true;
        }

        @Override
        public synchronized void doInBackground() {
            try {
                mRemoveObject.remove();
            } catch (ThetaDeviceException e) {
                e.printStackTrace();
                mIsSuccess = false;
            }
        }

        @Override
        public synchronized void onPostExecute() {
            if (!mIsSuccess) {
                ThetaDialogFragment.showAlert(getActivity(), "THETA",
                        getString(R.string.theta_error_failed_delete));

            } else {
                ThetaDialogFragment.showAlert(getActivity(), "THETA",
                        getString(R.string.theta_remove));

            }

            if (mGalleryAdapter != null) {
                mGalleryAdapter.clear();
                mGalleryAdapter.addAll(mUpdateList);
                mGalleryAdapter.notifyDataSetChanged();
            }
        }
    }
}