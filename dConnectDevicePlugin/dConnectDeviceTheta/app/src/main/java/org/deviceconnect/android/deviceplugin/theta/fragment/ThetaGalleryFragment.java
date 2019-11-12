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
import android.os.Handler;

import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaFeatureActivity;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.data.ThetaObjectStorage;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;
import org.deviceconnect.android.deviceplugin.theta.view.ThetaLoadingProgressView;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * THETA Device's Gallery Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaGalleryFragment extends Fragment implements ThetaDeviceEventListener {

    /** Gallery Mode: App. */
    private static final int GALLERY_MODE_APP = 0;

    /** Gallery Mode: Theta. */
    private static final int GALLERY_MODE_THETA = 1;

    /** Gallery Command: data import. */
    private static final int DIALOG_COMMAND_IMPORT = 0;

    /** Gallery Command: data delete. */
    private static final int DIALOG_COMMAND_DELETE = 1;


    /** Gallery Mode Enable background. */
    private static final int MODE_ENABLE_BACKGROUND = R.drawable.button_blue;

    /** Gallery Mode Disable background. */
    private static final int MODE_DISABLE_BACKGROUND = R.drawable.button_white;

    /** Gallery Mode Enable text color. */
    private static final int MODE_ENABLE_TEXT_COLOR = R.color.tab_text;

    /** Gallery Mode Disable text color. */
    private static final int MODE_DISABLE_TEXT_COLOR = R.color.action_bar_background;

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

    /** Move Shooting Fragment. */
    private Button mShootingButton;

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
    private List<ThetaObject> mUpdateThetaList = new ArrayList<>();
    /**
     * Theta's data in App's External Storage.
     */
    private List<ThetaObject> mUpdateAppList = new ArrayList<>();

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

    /** App/theta gallery mode flag. true:app false:theta*/
    private boolean mIsGalleryMode = true;

    /** App/theta  gallery mode change buttons.*/
    private Button[] mGalleryModeButtons = new Button[2];

    /** Control Storage of App*/
    private ThetaObjectStorage mStorage;

    /** Storage Listener. */
    private ThetaObjectStorage.Listener mStorageListener = new ThetaObjectStorage.Listener() {

        @Override
        public void onCompleted(final ThetaObjectStorage.DBMode mode, final long result) {
            mUpdateAppList = mStorage.geThetaObjectCaches(null);
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgress != null) {
                            mProgress.dismiss();
                            mProgress = null;
                        }
                        if (mode == ThetaObjectStorage.DBMode.Add) {
                            if (result > 0) {
                                ThetaDialogFragment.showAlert(getActivity(),
                                        getString(R.string.theta_ssid_prefix),
                                        getString(R.string.theta_data_import), null);
                            } else {
                                ThetaDialogFragment.showAlert(getActivity(),
                                        getString(R.string.theta_ssid_prefix),
                                        getString(R.string.theta_error_import), null);
                            }
                        }
                        List<ThetaObject> updateList = mUpdateAppList;
                        if (!mIsGalleryMode) {
                            updateList = mUpdateThetaList;
                        }
                        if (mGalleryAdapter != null) {
                            mGalleryAdapter.clear();
                            mGalleryAdapter.addAll(updateList);
                            mGalleryAdapter.notifyDataSetChanged();
                        }

                    }
                });
            }
        }
    };


    /** Gallery Mode Change Listener. */
    private View.OnClickListener mGalleryModeChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mGalleryModeButtons[GALLERY_MODE_APP].setEnabled(false);
            mGalleryModeButtons[GALLERY_MODE_THETA].setEnabled(false);
            if (mIsGalleryMode) {
                mIsGalleryMode = false;
            } else {
                mIsGalleryMode = true;
            }
            enableGalleryModeButtons();
            List<ThetaObject> updateList = mUpdateAppList;
            if (mDevice != null && !mIsGalleryMode) {
                updateList = mUpdateThetaList;
            } else {
                enableOfflineView();
            }
            if (updateList.size() == 0) {
                enableReconnectView();
            }
            if (mGalleryAdapter != null) {
                mGalleryAdapter.clear();
                mGalleryAdapter.addAll(updateList);
                mGalleryAdapter.notifyDataSetChanged();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGalleryModeButtons[GALLERY_MODE_APP].setEnabled(true);
                    mGalleryModeButtons[GALLERY_MODE_THETA].setEnabled(true);
                }
            }, 500);
        }
    };




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
        initThetaObjectManagers();
        mGalleryAdapter = new ThetaGalleryAdapter(getActivity(), new ArrayList<ThetaObject>());
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
        Drawable backgroundDrawable = getResources().getDrawable(color);
        getActivity().getActionBar().setBackgroundDrawable(backgroundDrawable);
        mRecconectLayout = (RelativeLayout) mRootView.findViewById(R.id.theta_reconnect_layout);
        mRootView.findViewById(R.id.theta_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsActivity();
            }
        });
        mShootingButton = (Button) mRootView.findViewById(R.id.theta_shutter);
        mShootingButton.setOnClickListener(new View.OnClickListener() {
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
        initGalleryModeButtons(mRootView);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        enableGalleryModeButtons();
        mProgress = null;
        enableReconnectView();
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

    @Override
    public void onConnected(final ThetaDevice device) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!mIsGalleryMode && mProgress == null) {
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
                    if (!mIsGalleryMode && mGalleryAdapter != null) {
                        mGalleryAdapter.clear();
                        mGalleryAdapter.notifyDataSetChanged();
                    }
                    mUpdateThetaList.clear();
                    enableReconnectView();
                }
            });
        }
    }


    /** Enable App mode. State of App mode of view. */
    private void enableOfflineView() {
        mRecconectLayout.setVisibility(View.GONE);
        getActivity().getActionBar().setTitle(getString(R.string.app_name));

        if (mDevice == null) {
            mShootingButton.setVisibility(View.GONE);
            getActivity().getActionBar().setTitle(getString(R.string.app_name));
            if (mUpdateItem != null) {
                mUpdateItem.setVisible(false);
            }
            if (!mIsGalleryMode && mGalleryAdapter != null) {
                mUpdateThetaList.clear();
                mGalleryAdapter.clear();
                mGalleryAdapter.notifyDataSetChanged();
                mRecconectLayout.setVisibility(View.VISIBLE);
            }
        } else {
            getActivity().getActionBar().setTitle(mDevice.getName());
        }
    }
    /** init Managers. */
    private void initThetaObjectManagers() {
        if (getActivity() == null) {
            return;
        }
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        ThetaDeviceManager deviceMgr = app.getDeviceManager();
        mStorage = new ThetaObjectStorage(getContext());
        mStorage.setListener(mStorageListener);

        mDevice = deviceMgr.getConnectedDevice();
    }

    /** init  gallery mode change buttons. */
    private void initGalleryModeButtons(final View rootView) {
        mGalleryModeButtons[GALLERY_MODE_APP] = (Button) rootView.findViewById(R.id.change_list_app);
        mGalleryModeButtons[GALLERY_MODE_APP].setOnClickListener(mGalleryModeChangeListener);
        mGalleryModeButtons[GALLERY_MODE_THETA] = (Button) rootView.findViewById(R.id.change_list_theta);
        mGalleryModeButtons[GALLERY_MODE_THETA].setOnClickListener(mGalleryModeChangeListener);
    }

    /** Enabled gallery mode buttons. */
    private void enableGalleryModeButtons() {
        if (mIsGalleryMode) {
            mGalleryModeButtons[GALLERY_MODE_APP].setBackgroundResource(MODE_ENABLE_BACKGROUND);
            mGalleryModeButtons[GALLERY_MODE_APP].setTextColor(ContextCompat.getColor(getActivity(), MODE_ENABLE_TEXT_COLOR));
            mGalleryModeButtons[GALLERY_MODE_THETA].setBackgroundResource(MODE_DISABLE_BACKGROUND);
            mGalleryModeButtons[GALLERY_MODE_THETA].setTextColor(ContextCompat.getColor(getActivity(), MODE_DISABLE_TEXT_COLOR));
        } else {
            mGalleryModeButtons[GALLERY_MODE_APP].setBackgroundResource(MODE_DISABLE_BACKGROUND);
            mGalleryModeButtons[GALLERY_MODE_APP].setTextColor(ContextCompat.getColor(getActivity(), MODE_DISABLE_TEXT_COLOR));
            mGalleryModeButtons[GALLERY_MODE_THETA].setBackgroundResource(MODE_ENABLE_BACKGROUND);
            mGalleryModeButtons[GALLERY_MODE_THETA].setTextColor(ContextCompat.getColor(getActivity(), MODE_ENABLE_TEXT_COLOR));
        }
    }


    /** Enabled Reconnect View.*/
    private void enableReconnectView() {
        initThetaObjectManagers();
        mShootingButton.setVisibility(View.VISIBLE);

        if (mDevice != null && !mIsGalleryMode) {
            mShootingButton.setEnabled(true);
            mRecconectLayout.setVisibility(View.GONE);
            if (mUpdateItem != null) {
                mUpdateItem.setVisible(true);
            }
            if (mDevice != null) {
                String ssId = mDevice.getName();
                getActivity().getActionBar().setTitle(ssId);
            }
            if ((mRecconectLayout.isEnabled()
                    && !mIsGalleryMode && mUpdateThetaList.size() == 0)) {
                if (mDownloadTask != null) {
                    mDownloadTask.cancel(true);
                    mDownloadTask = null;
                }
                ShootingModeGetTask mode = new ShootingModeGetTask();
                mDownloadTask = new DownloadThetaDataTask();
                mDownloadTask.execute(mode);
            }
        } else if (mDevice == null && !mIsGalleryMode) {
            if (mGalleryAdapter != null) {
                mUpdateThetaList.clear();
                mGalleryAdapter.clear();
                mGalleryAdapter.notifyDataSetChanged();
            }
            mShootingButton.setEnabled(false);
            mRecconectLayout.setVisibility(View.VISIBLE);
            getActivity().getActionBar().setTitle(getString(R.string.app_name));

            if (mUpdateItem != null) {
                mUpdateItem.setVisible(false);
            }
        } else {
            enableOfflineView();
            if (mDownloadTask != null) {
                mDownloadTask.cancel(true);
                mDownloadTask = null;
            }
            if (mIsGalleryMode && mUpdateAppList.size() == 0) {
                ShootingModeGetTask mode = new ShootingModeGetTask();
                mDownloadTask = new DownloadThetaDataTask();
                mDownloadTask.execute(mode);
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
                if ((mIsGalleryMode && !mUpdateAppList.get(position).isImage())
                        || (!mIsGalleryMode && !mUpdateThetaList.get(position).isImage())) {
                    ThetaDialogFragment.showAlert(getActivity(),
                            getString(R.string.theta_ssid_prefix),
                            getString(R.string.theta_error_unsupported_movie), null);
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                        ThetaFeatureActivity.MODE_VR);

                int index = -1;
                if (mUpdateThetaList.size() > 0) {
                    index = mStorage.getThetaObjectCachesIndex(mUpdateThetaList.get(position).getFileName());
                }
                if (!mIsGalleryMode
                        && index != -1) {
                    intent.putExtra(ThetaFeatureActivity.FEATURE_IS_STORAGE,
                            !mIsGalleryMode);
                    intent.putExtra(ThetaFeatureActivity.FEATURE_DATA,
                            index);
                } else {
                    intent.putExtra(ThetaFeatureActivity.FEATURE_IS_STORAGE,
                            mIsGalleryMode);
                    intent.putExtra(ThetaFeatureActivity.FEATURE_DATA,
                            position);
                }
                intent.setClass(getActivity(), ThetaFeatureActivity.class);
                startActivity(intent);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView,
                                           final View view, final int position,
                                           final long id) {
                if (!mIsGalleryMode
                        && mUpdateThetaList.get(position).isImage()
                        && !existThetaData(mUpdateThetaList.get(position))) {
                    ThetaDialogFragment.showSelectCommandDialog(getActivity(),
                            getResources().getStringArray(R.array.theta_gallery_command),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, final int pos) {
                                    FileManager fileManager = new FileManager(getActivity());
                                    fileManager.checkWritePermission(new FileManager.CheckPermissionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            if (pos == DIALOG_COMMAND_IMPORT) {
                                                Activity activity = getActivity();
                                                if (activity != null && !ThetaObjectStorage.hasEnoughStorageSize()) {
                                                    // Check Android Storage Limit
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ThetaDialogFragment.showAlert(getActivity(),
                                                                    getResources().getString(R.string.theta_ssid_prefix),
                                                                    getResources().getString(R.string.theta_error_import_shortage_by_android), null);
                                                        }
                                                    });
                                                    return;
                                                }

                                                exeImportData(position);
                                            } else {
                                                showRemoveConfirmDialog(position);
                                            }
                                        }

                                        @Override
                                        public void onFail() {
                                            Activity activity = getActivity();
                                            if (activity != null) {
                                                activity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ThetaDialogFragment.showAlert(getActivity(),
                                                                getResources().getString(R.string.theta_ssid_prefix),
                                                                getResources().getString(R.string.theta_error_failed_save_file), null);
                                                    }
                                                });
                                            }

                                        }
                                    });
                                }
                            });
                } else {
                    FileManager fileManager = new FileManager(getActivity());
                    fileManager.checkWritePermission(new FileManager.CheckPermissionCallback() {
                        @Override
                        public void onSuccess() {
                            showRemoveConfirmDialog(position);
                        }

                        @Override
                        public void onFail() {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ThetaDialogFragment.showAlert(getActivity(),
                                                getResources().getString(R.string.theta_ssid_prefix),
                                                getResources().getString(R.string.theta_error_failed_save_file), null);
                                    }
                                });
                            }

                        }
                    });

                }
                return true;
            }
        });
    }

    /** already exist?.*/
    private boolean existThetaData(final ThetaObject searchObj) {
        List<ThetaObject> obj = mStorage.geThetaObjectCaches(searchObj.getFileName());
        return (obj.size() > 0);
    }



    /** Import data of Theta to storage of App. */
    private void exeImportData(final int position) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mProgress == null) {
                            mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix),
                                    getString(R.string.saving));
                            mProgress.show(getActivity().getFragmentManager(),
                                    "fragment_dialog");
                        }
                    } catch (IllegalStateException e) {  //background
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ThetaObject importObj = mUpdateThetaList.get(position);
                mStorage.addThetaObjectCache(importObj);
            }
        }).start();
    }


    /* Show Remove Theta Object Confirm Dialog. */
    private void showRemoveConfirmDialog(final int position) {
        String typeString = getString(R.string.theta_remove_data);
        ThetaObject removeObject = null;
        if (mIsGalleryMode) {
            removeObject = mUpdateAppList.get(position);
        } else {
            removeObject = mUpdateThetaList.get(position);
        }
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

                        RemoveThetaData removeObj = null;
                        if (mDownloadTask != null) {
                            mDownloadTask.cancel(true);
                            mDownloadTask = null;
                        }
                        if (mIsGalleryMode) {
                            removeObj = new RemoveThetaData(mUpdateAppList.remove(position));
                        } else {
                            removeObj = new RemoveThetaData(mUpdateThetaList.remove(position));
                        }
                        mDownloadTask = new DownloadThetaDataTask();
                        mDownloadTask.execute(removeObj);
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

    /** Show Reconnection Dialog. */
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

    /** Show Settings Activity. */
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
         * Constructor.
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

        private List<ThetaObject> mResult = new ArrayList<ThetaObject>();

        @Override
        public void doInBackground() {
            final Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (mProgress == null) {
                try {
                    mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(R.string.loading));
                    mProgress.show(getActivity().getFragmentManager(),
                            "fragment_dialog");
                } catch (IllegalStateException e) {
                    return;
                }
            }

            try {
                if (mIsGalleryMode) {
                    mResult = mStorage.geThetaObjectCaches(null);
                } else if (mDevice != null) {
                    mResult = mDevice.fetchAllObjectList();
                }
            } catch (ThetaDeviceException e) {
                e.printStackTrace();
                mError = e.getReason();
            }
        }

        @Override
        public void onPostExecute() {
            if (!mIsGalleryMode && mResult == null) {
                showSettingsActivity();
                return;
            }
            if (mResult.size() > 0) {
                mStatusView.setVisibility(View.GONE);
            } else {
                mStatusView.setVisibility(View.VISIBLE);
            }

            if (mIsGalleryMode) {
                mUpdateAppList = mResult;
            } else {
                mUpdateThetaList = mResult;
            }
            if (mGalleryAdapter != null) {
                mGalleryAdapter.clear();
                mGalleryAdapter.addAll(mResult);
                mGalleryAdapter.notifyDataSetChanged();
            }
            try {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            } catch (IllegalStateException e) {  //Check background/foreground
                return;
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
            if ((mUpdateThetaList.size() > 0 && !mIsGalleryMode)
                    || (mUpdateAppList.size() > 0 && mIsGalleryMode)) {
                mStatusView.setVisibility(View.GONE);
            } else {
                mStatusView.setVisibility(View.VISIBLE);
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
                mIsSuccess = false;
            }
        }

        @Override
        public synchronized void onPostExecute() {
            if (mIsSuccess) {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_remove), null);
            } else {
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(R.string.theta_error_failed_delete), null);
            }

            List<ThetaObject> removedList = mUpdateAppList;
            if (!mIsGalleryMode) {
                removedList = mUpdateThetaList;
            }
            if (mGalleryAdapter != null) {
                mGalleryAdapter.clear();
                mGalleryAdapter.addAll(removedList);
                mGalleryAdapter.notifyDataSetChanged();
            }
            if (removedList.size() > 0) {
                mStatusView.setVisibility(View.GONE);
            } else {
                mStatusView.setVisibility(View.VISIBLE);
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
                        ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                                getString(R.string.theta_error_usb_live_streaming), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        showReconnectionDialog();
                                    }
                                });
                    } catch (IllegalStateException e) {  //Check background/foreground
                        return;
                    }
                }
            } else {
                loadThetaData();
            }
        }
    }
}