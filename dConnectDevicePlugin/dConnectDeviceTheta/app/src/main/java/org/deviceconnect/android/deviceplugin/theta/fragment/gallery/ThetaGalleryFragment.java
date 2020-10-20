/*
 ThetaGalleryFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
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
import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaDialogFragment;
import org.deviceconnect.android.provider.FileManager;
import java.util.ArrayList;
import java.util.List;

import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.DIALOG_COMMAND_IMPORT;
import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.GALLERY_MODE_APP;
import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.GALLERY_MODE_THETA;
import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.MODE_DISABLE_BACKGROUND;
import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.MODE_DISABLE_TEXT_COLOR;
import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.MODE_ENABLE_BACKGROUND;
import static org.deviceconnect.android.deviceplugin.theta.fragment.gallery.GalleryContract.MODE_ENABLE_TEXT_COLOR;

/**
 * THETA Device's Gallery Fragment.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaGalleryFragment extends Fragment implements ThetaDeviceEventListener,  GalleryContract.View {
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

    private ThetaGalleryPresenter mPresenter;
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
                activity.runOnUiThread(() -> {
                    stopProgressDialog();
                    if (mode == ThetaObjectStorage.DBMode.Add) {
                        if (result > 0) {
                            showDialog(R.string.theta_data_import);
                        } else {
                            showDialog(R.string.theta_error_import);
                        }
                    }
                    List<ThetaObject> updateList = mUpdateAppList;
                    if (!mIsGalleryMode) {
                        updateList = mUpdateThetaList;
                    }
                    notifyGalleryListChanged(updateList);
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
            notifyGalleryListChanged(updateList);
            new Handler().postDelayed(() -> {
                mGalleryModeButtons[GALLERY_MODE_APP].setEnabled(true);
                mGalleryModeButtons[GALLERY_MODE_THETA].setEnabled(true);
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
        mPresenter = new ThetaGalleryPresenter(this);
        mGalleryAdapter = new ThetaGalleryAdapter(getActivity(), new ArrayList<>(), mPresenter);
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
        mUpdateItem.setOnMenuItemClickListener((item) -> {
            if (item.getTitle().equals(mUpdateItem.getTitle())) {
                mPresenter.startShootingModeGetTask();
            }
            return true;
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
        mRecconectLayout = mRootView.findViewById(R.id.theta_reconnect_layout);
        mRootView.findViewById(R.id.theta_reconnect).setOnClickListener((view) -> {
            showSettingsActivity();
        });
        mShootingButton = mRootView.findViewById(R.id.theta_shutter);
        mShootingButton.setOnClickListener((view) -> {
            Intent intent = new Intent();
            intent.putExtra(ThetaFeatureActivity.FEATURE_MODE,
                    ThetaFeatureActivity.MODE_SHOOTING);
            intent.setClass(getActivity(), ThetaFeatureActivity.class);
            startActivity(intent);
        });
        mStatusView = mRootView.findViewById(R.id.theta_no_data);
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
        stopProgressDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.stopTask();
    }

    @Override
    public void onConnected(final ThetaDevice device) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                try {
                    if (!mIsGalleryMode) {
                        startProgressDialog(R.string.loading);
                    }
                    enableReconnectView();
                } catch (IllegalStateException e) {  //Check background/foreground
                    return;
                }
            });
        }
    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        mDevice = null;
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (!mIsGalleryMode && mGalleryAdapter != null) {
                    mGalleryAdapter.clear();
                    mGalleryAdapter.notifyDataSetChanged();
                }
                mUpdateThetaList.clear();
                enableReconnectView();
            });
        }
    }

    @Override
    public boolean startProgressDialog(int message) {
        if (mProgress == null) {
            try {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        mProgress = ThetaDialogFragment.newInstance(getString(R.string.theta_ssid_prefix), getString(message));
                        mProgress.show(getActivity().getFragmentManager(),
                                "fragment_dialog");
                    });
                }
            } catch (IllegalStateException e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startProgressDialogForReconnect(int message) {
        if (mProgress != null) {
            try {
                mProgress.dismiss();
                mProgress = null;
                ThetaDialogFragment.showAlert(getActivity(), getString(R.string.theta_ssid_prefix),
                        getString(message), (dialogInterface, i) -> {
                            showReconnectionDialog();
                        });
            } catch (IllegalStateException e) {  //Check background/foreground
                return;
            }
        }
    }

    @Override
    public void stopProgressDialog() {
        if (mProgress != null) {
            try {
                mProgress.dismiss();
                mProgress = null;
            } catch (IllegalStateException e) {
                // nop
            }
        }
    }


    @Override
    public void showDialog(int message) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                ThetaDialogFragment.showAlert(getActivity(),
                        getResources().getString(R.string.theta_ssid_prefix),
                        getResources().getString(message), null);
            });
        }
    }

    /** already exist?.*/
    private boolean existThetaData(final ThetaObject searchObj) {
        List<ThetaObject> obj = mStorage.geThetaObjectCaches(searchObj.getFileName());
        return (obj.size() > 0);
    }



    /** Import data of Theta to storage of App. */
    private void exeImportData(final int position) {
        startProgressDialog(R.string.saving);
        new Thread(() -> {
            ThetaObject importObj = mUpdateThetaList.get(position);
            mStorage.addThetaObjectCache(importObj);
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
        final ThetaObject removeObj = removeObject;
        ThetaDialogFragment.showConfirmAlert(getActivity(),
                getString(R.string.theta_ssid_prefix), typeString, getString(R.string.ok), (dialogInterface, i) -> {
                    mPresenter.stopTask();
                    mPresenter.startThetaDataRemoveTask(removeObj);
                });
    }


    /**
     * Load Theta Datas.
     */
    @Override
    public void loadThetaData() {
        mPresenter.stopTask();

        mPresenter.startThetaInfoGetTask();
    }

    /** Show Reconnection Dialog. */
    @Override
    public void showReconnectionDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showReconnectionDialog(activity,
                    (dialog, i) -> {
                        dialog.dismiss();
                        showSettingsActivity();
                    },
                    (dialog, i) -> {
                        dialog.dismiss();
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
                clearThumbList();
                mRecconectLayout.setVisibility(View.VISIBLE);
            }
        } else {
            getActivity().getActionBar().setTitle(mDevice.getName());
        }
    }

    private void clearThumbList() {
        mUpdateThetaList.clear();
        mGalleryAdapter.clear();
        mGalleryAdapter.notifyDataSetChanged();
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
        mGalleryModeButtons[GALLERY_MODE_APP] = rootView.findViewById(R.id.change_list_app);
        mGalleryModeButtons[GALLERY_MODE_APP].setOnClickListener(mGalleryModeChangeListener);
        mGalleryModeButtons[GALLERY_MODE_THETA] = rootView.findViewById(R.id.change_list_theta);
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
                mPresenter.stopTask();
                mPresenter.startShootingModeGetTask();
            }
        } else if (mDevice == null && !mIsGalleryMode) {
            if (mGalleryAdapter != null) {
                clearThumbList();
            }
            mShootingButton.setEnabled(false);
            mRecconectLayout.setVisibility(View.VISIBLE);
            getActivity().getActionBar().setTitle(getString(R.string.app_name));

            if (mUpdateItem != null) {
                mUpdateItem.setVisible(false);
            }
        } else {
            enableOfflineView();
            mPresenter.stopTask();
            if (mIsGalleryMode && mUpdateAppList.size() == 0) {
                mPresenter.startShootingModeGetTask();
            }
        }

    }

    /**
     * ListView Initialize.
     *
     * @param rootView Root View
     */
    private void initListView(final View rootView) {
        AbsListView list =  rootView.findViewById(R.id.theta_list);
        list.setAdapter(mGalleryAdapter);

        list.setOnItemClickListener((adapterView, view, position, id) -> {
            if ((mIsGalleryMode && !mUpdateAppList.get(position).isImage())
                    || (!mIsGalleryMode && !mUpdateThetaList.get(position).isImage())) {
                showDialog(R.string.theta_error_unsupported_movie);
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
        });
        list.setOnItemLongClickListener((adapterView, view, position, id) -> {
            if (!mIsGalleryMode
                    && mUpdateThetaList.get(position).isImage()
                    && !existThetaData(mUpdateThetaList.get(position))) {
                ThetaDialogFragment.showSelectCommandDialog(getActivity(),
                        getResources().getStringArray(R.array.theta_gallery_command),
                        (dialogInterface, pos) -> {
                            FileManager fileManager = new FileManager(getActivity());
                            fileManager.checkWritePermission(new FileManager.CheckPermissionCallback() {
                                @Override
                                public void onSuccess() {
                                    if (pos == DIALOG_COMMAND_IMPORT) {
                                        if (!ThetaObjectStorage.hasEnoughStorageSize()) {
                                            // Check Android Storage Limit
                                            showDialog(R.string.theta_error_import_shortage_by_android);
                                            return;
                                        }

                                        exeImportData(position);
                                    } else {
                                        showRemoveConfirmDialog(position);
                                    }
                                }

                                @Override
                                public void onFail() {
                                    showDialog(R.string.theta_error_failed_save_file);
                                }
                            });
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
                        showDialog(R.string.theta_error_failed_save_file);

                    }
                });

            }
            return true;
        });
    }


    /** Show Settings Activity. */
    @Override
    public void showSettingsActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Toast.makeText(activity, R.string.camera_must_connect, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(activity, ThetaDeviceSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateStatusView(int resultCount) {
        if (resultCount > 0) {
            mStatusView.setVisibility(View.GONE);
        } else {
            mStatusView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateStatusView() {
        if ((mUpdateThetaList.size() > 0 && !mIsGalleryMode)
                || (mUpdateAppList.size() > 0 && mIsGalleryMode)) {
            mStatusView.setVisibility(View.GONE);
        } else {
            mStatusView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateThetaObjectList(List<ThetaObject> obj) {
        if (mIsGalleryMode) {
            mUpdateAppList = obj;
        } else {
            mUpdateThetaList = obj;
        }
    }

    @Override
    public List<ThetaObject> removeObj(ThetaObject obj) {
        if (!mIsGalleryMode) {
            mUpdateThetaList.remove(obj);
            return mUpdateThetaList;
        } else {
            mUpdateAppList.remove(obj);
            return mUpdateAppList;
        }
    }

    @Override
    public void notifyGalleryListChanged(List<ThetaObject> obj) {
        if (mGalleryAdapter != null) {
            mGalleryAdapter.clear();
            mGalleryAdapter.addAll(obj);
            mGalleryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean existThetaDevice() {
        return (mDevice != null);
    }

    @Override
    public ThetaDevice.ShootingMode getShootingMode() {
        try {
            return mDevice.getShootingMode();
        } catch (ThetaDeviceException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return ThetaDevice.ShootingMode.UNKNOWN;
        }
    }

    @Override
    public List<ThetaObject> getThetaObjects() throws ThetaDeviceException {
        if (mIsGalleryMode) {
            return mStorage.geThetaObjectCaches(null);
        } else if (mDevice != null) {
            return mDevice.fetchAllObjectList();
        } else {
            return null;
        }
    }

    @Override
    public byte[] getThumbnail(String fileName) {
        return mThumbnailCache.get(fileName);
    }

    @Override
    public void putThumbnail(String fileName, byte[] data) {
        mThumbnailCache.put(fileName, data);
    }
}