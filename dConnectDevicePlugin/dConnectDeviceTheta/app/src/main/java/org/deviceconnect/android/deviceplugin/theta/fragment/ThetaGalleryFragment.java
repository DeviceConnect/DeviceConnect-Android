/*
 ThetaGalleryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaFeatureActivity;
import org.deviceconnect.android.deviceplugin.theta.view.ThetaLoadingProgressView;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
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
public class ThetaGalleryFragment extends Fragment implements ThetaDeviceEventListener {

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

    /**
     * Update Menu item.
     */
    private MenuItem mUpdateItem;

    /**
     * Theta Device.
     */
    private ThetaDevice mDevice;

    /**
     * Thumbnail cache.
     */
    private LruCache<String, byte[]> mThumbnailCache;

    /**
     * Singleton.
     */
    public static ThetaGalleryFragment newInstance(final ThetaDeviceManager deviceMgr) {
        ThetaGalleryFragment fragment = new ThetaGalleryFragment();
        deviceMgr.registerDeviceEventListener(fragment);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        ThetaDeviceManager deviceMgr = app.getDeviceManager();
        mDevice= deviceMgr.getConnectedDevice();

        mGalleryAdapter = new ThetaGalleryAdapter(getActivity(), new ArrayList<ThetaObject>());

        if (mDevice == null) {
            showSettingsActivity();
            return;
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        menu.clear();
        // Add Menu Button
        mUpdateItem = menu.add(R.string.theta_update);
        if (mDevice != null) {
            mUpdateItem.setVisible(true);
        } else {
            mUpdateItem.setVisible(false);
        }
        mUpdateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mUpdateItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                if (item.getTitle().equals(mUpdateItem.getTitle())) {
                    ShootingModeGetTask mode = new ShootingModeGetTask();
                    mDownloadTask = new DownloadThetaDataTask();
                    mDownloadTask.execute(mode);
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.theta_gallery, container, false);
        getActivity().getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        mThumbnailCache = app.getCache();
        int color = R.color.action_bar_background;
        Drawable backgroundDrawable = getActivity().getApplicationContext().getResources().getDrawable(color);
        getActivity().getActionBar().setBackgroundDrawable(backgroundDrawable);
        mRecconectLayout = (RelativeLayout) mRootView.findViewById(R.id.theta_reconnect_layout);
        mRootView.findViewById(R.id.theta_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsActivity();
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
        initListView(mRootView);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgress = null;
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        ThetaDeviceManager deviceMgr = app.getDeviceManager();
        mDevice = deviceMgr.getConnectedDevice();
        enableReconnectView();

        if (mUpdateList.size() > 0) {
            mStatusView.setVisibility(View.GONE);
        } else {
            mStatusView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
    }

    // TODO As when offline also use only VR mode

    @Override
    public void onConnected(final ThetaDevice device) {
        mDevice = device;
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mProgress == null) {
                            mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.loading));
                            mProgress.show(getActivity().getFragmentManager(),
                                    "fragment_dialog");
                        }
                        enableReconnectView();
                    } catch (IllegalStateException e) {  //Check background/foreground
                        return;
                    }
                }
            });
        }
    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        mDevice = null;
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mGalleryAdapter != null) {
                        mGalleryAdapter.clear();
                        mGalleryAdapter.notifyDataSetChanged();
                    }
                    mUpdateList.clear();
                    enableReconnectView();
                }
            });
        }
    }

    /** Enabled Reconnect View.*/
    private void enableReconnectView() {
        if (mDevice != null) {
            mRecconectLayout.setVisibility(View.GONE);
            if (mUpdateItem != null) {
                mUpdateItem.setVisible(true);
            }
            String ssId = mDevice.getName();
            getActivity().getActionBar().setTitle(ssId);
            if (mRecconectLayout.isEnabled()
                    && mUpdateList.size() == 0) {
                if (mDownloadTask != null) {
                    mDownloadTask.cancel(true);
                    mDownloadTask = null;
                }
                ShootingModeGetTask mode = new ShootingModeGetTask();
                mDownloadTask = new DownloadThetaDataTask();
                mDownloadTask.execute(mode);
            }
        } else {
            mRecconectLayout.setVisibility(View.VISIBLE);
            getActivity().getActionBar().setTitle(getString(R.string.app_name));

            if (mUpdateItem != null) {
                mUpdateItem.setVisible(false);
            }
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
                            getString(R.string.theta_ssid_prefix),
                            getString(R.string.theta_error_unsupported_movie), null);
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
                        getString(R.string.theta_ssid_prefix), typeString, getString(R.string.ok), new DialogInterface.OnClickListener() {
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
        ThetaInfoTask info = new ThetaInfoTask();
        mDownloadTask = new DownloadThetaDataTask();
        mDownloadTask.execute(info);
    }

    private void showReconnectionDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showReconnectionDialog(activity,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int i) {
                            dialog.dismiss();
                            showSettingsActivity();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int i) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    private void showSettingsActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Toast.makeText(activity, R.string.camera_must_connect, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(activity, ThetaDeviceSettingsActivity.class);
        startActivity(intent);
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
            String dateString = data.getCreationTime();
            holder.mDate.setText(dateString);
            if (data.isImage()) {
                holder.mType.setImageResource(R.drawable.theta_data_img);

                ThetaThumbTask thumbTask = new ThetaThumbTask(data, holder);
                DownloadThetaDataTask downloader = new DownloadThetaDataTask();
                downloader.execute(thumbTask);
            } else {
                holder.mType.setImageResource(R.drawable.theta_data_mv);
                holder.mLoading.setVisibility(View.GONE);
            }

            return cv;
        }
    }

    /**
     * Gallery View Holder.
     */
    private static class GalleryViewHolder {

        ImageView mThumbnail;

        ImageView mType;

        TextView mDate;

        ThetaLoadingProgressView mLoading;

        GalleryViewHolder(final View view) {
            mThumbnail = (ImageView) view.findViewById(R.id.theta_thumb_data);
            mType = (ImageView) view.findViewById(R.id.data_type);
            mDate =  (TextView) view.findViewById(R.id.data_date);
            mLoading = (ThetaLoadingProgressView) view.findViewById(R.id.theta_thumb_progress);
        }

    }


    /**
     * Download of info.
     */
    private class ThetaInfoTask implements DownloadThetaDataTask.ThetaDownloadListener {

        private int mError = -1;

        private List<ThetaObject> mResult;

        @Override
        public synchronized void doInBackground() {
            final Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (mProgress == null) {
                mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.loading));
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }

            if (mDevice != null) {
                try {
                    mResult = mDevice.fetchAllObjectList();
                } catch (ThetaDeviceException e) {
                    mError = e.getReason();
                }
            }
        }

        @Override
        public synchronized void onPostExecute() {
            if (mResult == null) {
                showSettingsActivity();
                return;
            }
            mUpdateList = mResult;

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
            try {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            } catch (IllegalStateException e) {

            }

            if (mError > 0) {
                showReconnectionDialog();
            }
        }
    }

    /**
     * Donwload of thumb.
     */
    private class ThetaThumbTask implements DownloadThetaDataTask.ThetaDownloadListener {

        /** THETA Object. */
        private final ThetaObject mObj;

        /** View holder. */
        private final GalleryViewHolder mHolder;

        /** Tag of thumbnail view. */
        private final String mTag;

        /** Thumbnail. */
        private byte[] mThumbnail;

        /**
         * Constructor.
         * @param obj THETA Object
         * @param holder view holder
         */
        ThetaThumbTask(final ThetaObject obj, final GalleryViewHolder holder) {
            mObj = obj;
            mHolder = holder;
            mTag = holder.mThumbnail.getTag().toString();
        }

        @Override
        public synchronized void doInBackground() {
            mThumbnail = mThumbnailCache.get(mObj.getFileName());
            if (mThumbnail == null) {
                try {
                    Thread.sleep(100);
                    cacheThumbnail(mObj);
                } catch (ThetaDeviceException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e("AAA", "error", e);
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
                mThumbnailCache.put(mObj.getFileName(), mThumbnail);
                thumbView = mHolder.mThumbnail;
                loadingView = mHolder.mLoading;
                if (mTag.equals(thumbView.getTag())) {
                    Bitmap data = BitmapFactory.decodeByteArray(mThumbnail, 0, mThumbnail.length);

                    thumbView.setImageBitmap(data);
                    loadingView.setVisibility(View.GONE);
                }
            }
            loadingView.setVisibility(View.GONE);
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
                mIsSuccess = false;
            }
        }

        @Override
        public synchronized void onPostExecute() {
            if (!mIsSuccess) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_failed_delete), null);

            } else {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_remove), null);

            }

            if (mGalleryAdapter != null) {
                mGalleryAdapter.clear();
                mGalleryAdapter.addAll(mUpdateList);
                mGalleryAdapter.notifyDataSetChanged();
            }
        }
    }

    /** Get Shooting Mode Task. */
    private class ShootingModeGetTask implements DownloadThetaDataTask.ThetaDownloadListener {

        /**
         * Shooting mode.
         */
        private ThetaDevice.ShootingMode mNowShootingMode;

        /**
         * Constructor.
         */
        ShootingModeGetTask() {
            mNowShootingMode = ThetaDevice.ShootingMode.UNKNOWN;
            if (mProgress == null) {
                try {
                    mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.loading));
                    mProgress.show(getActivity().getFragmentManager(),
                            "fragment_dialog");
                } catch (IllegalStateException e) {  //Check background/foreground
                    return;
                }
            }
        }

        @Override
        public void doInBackground() {
            if (mDevice == null) {
                return;
            }
            try {
                mNowShootingMode = mDevice.getShootingMode();
            } catch (ThetaDeviceException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                mNowShootingMode = ThetaDevice.ShootingMode.UNKNOWN;
            }
        }

        @Override
        public void onPostExecute() {
            if (mNowShootingMode == ThetaDevice.ShootingMode.LIVE_STREAMING) {
                if (mProgress != null) {
                    try {
                        mProgress.dismiss();
                        mProgress = null;
                    } catch (IllegalStateException e) {  //Check background/foreground
                        return;
                    }
                }
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_usb_live_streaming), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showReconnectionDialog();
                            }
                        });
            } else {
                loadThetaData();
            }
        }
    }
}