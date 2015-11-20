/*
 ThetaVRModeFragment
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaDeviceSettingsActivity;
import org.deviceconnect.android.deviceplugin.theta.activity.ThetaFeatureActivity;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalImageView;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewApi;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Fragment to display the VR mode of THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaVRModeFragment extends Fragment {

    /** THETA Device Plug-in apk limit size. */
    private static final int LIMIT_APK_SIZE = 100;

    /** SphericalView Max fov.*/
    private static final int MAX_FOV = 90;

    /** SphericalView Min fov.*/
    private static final int MIN_FOV = 45;

    /** VR Mode Left Layout. */
    private RelativeLayout mLeftLayout;

    /** VR Mode Right Layout.*/
    private RelativeLayout mRightLayout;

    /** VR Mode change button left and right.*/
    private ToggleButton[] mVRModeChangeButton = new ToggleButton[2];

    /** shooting button left and right. */
    private Button[] mShootingButton = new Button[2];

    /** SphericalView. */
    private SphericalImageView mSphereView;
    /** SphericalViewApi. */
    private SphericalViewApi mApi;

    /** Stereo Flag. */
    private boolean mIsStereo = false;
    /** Thread Manager. */
    private ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    /** Progress. */
    private ThetaDialogFragment mProgress;
    /** Task. */
    private DownloadThetaDataTask mDownloadTask;

    /** Default VR. */
    private int mDefaultId;

    /** Scale Gesture.*/
    private ScaleGestureDetector mScaleDetector;
    /** Scale factor. */
    private float mScaleFactor = 90.0f;

    private LruCache<String, byte[]> mDataCache;
    /**
     * Singleton.
     */
    public static ThetaVRModeFragment newInstance() {
        ThetaVRModeFragment fragment = new ThetaVRModeFragment();
        return fragment;
    }

    /** VR Change toggle button's listener.*/
    private CompoundButton.OnCheckedChangeListener mVRChangeToggleListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public synchronized void onCheckedChanged(final CompoundButton compoundButton, final boolean isStereo) {
            mExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mIsStereo != isStereo) {
                                    for (int i = 0; i < mVRModeChangeButton.length; i++) {
                                        mVRModeChangeButton[i].setChecked(isStereo);
                                    }
                                    mIsStereo = isStereo;
                                    enableView();
                                }
                                if (mSphereView != null) {
                                    mSphereView.setStereo(mIsStereo);
                                }
                            }
                        });
                    }
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    };

    /** ScreenShot shooting button's listener.*/
    private View.OnClickListener mShootingListener = new View.OnClickListener() {
        @Override
        public synchronized void onClick(final View view) {
            if (mProgress != null) {
                mProgress.dismiss();
            }
            mProgress = ThetaDialogFragment.newInstance("THETA", "保存中");
            mProgress.show(getActivity().getFragmentManager(),
                    "fragment_dialog");
            mExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    saveScreenShot();
                }
            }, 50, TimeUnit.MILLISECONDS);
        }
    };

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            mDefaultId = args.getInt(ThetaFeatureActivity.FEATURE_DATA, -1);
        }
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        setRetainInstance(true);
        ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
        mDataCache = app.getCache();
        View rootView = inflater.inflate(R.layout.theta_vr_mode, null);
        mLeftLayout = (RelativeLayout) rootView.findViewById(R.id.left_ui);
        mRightLayout = (RelativeLayout) rootView.findViewById(R.id.right_ui);
        mSphereView = (SphericalImageView) rootView.findViewById(R.id.vr_view);

        mApi = app.getSphericalViewApi();
        mSphereView.setViewApi(mApi);

        mSphereView.setOnTouchListener(new View.OnTouchListener() {

            private boolean mIsEnabledLongTouch = true;

            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mIsEnabledLongTouch = true;
                    return true;
                }
                if (motionEvent.getPointerCount() == 1) {
                    if (mIsEnabledLongTouch && motionEvent.getEventTime() - motionEvent.getDownTime() >= 300) {
                        mSphereView.resetCameraDirection();
                    }
                } else {
                    mIsEnabledLongTouch = false;
                    mScaleDetector.onTouchEvent(motionEvent);
                }
                return true;
            }
        });
        init3DButtons(rootView);
        enableView();
        mScaleDetector = new ScaleGestureDetector(getActivity(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(final ScaleGestureDetector detector) {
                        mScaleFactor /= detector.getScaleFactor();
                        double scale =  mScaleFactor;
                        if (scale > MAX_FOV) {
                            scale = MAX_FOV;
                            mScaleFactor = MAX_FOV;
                        }
                        if (scale < MIN_FOV) {
                            scale = MIN_FOV;
                            mScaleFactor = MIN_FOV;
                        }
                        mSphereView.setFOV(scale);

                        return true;
                    }
                });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDownloadTask == null) {
            if (mProgress == null) {
                mProgress = ThetaDialogFragment.newInstance("THETA", "読み込み中...");
                mProgress.show(getActivity().getFragmentManager(),
                        "fragment_dialog");
            }

            mDownloadTask = new DownloadThetaDataTask();
            ThetaMainData main = new ThetaMainData();
            mDownloadTask.execute(main);
        }
    }

    @Override
    public void onPause() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
        if (mSphereView != null) {
            mSphereView.stop();
            mSphereView.onPause();
        }
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        enableView();
    }

    private void showReconnectionDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showReconnectionDialog(activity,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int i) {
                            dialog.dismiss();
                            activity.finish();
                            showSettingsActivity();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int i) {
                            dialog.dismiss();
                            activity.finish();
                        }
                    });
        }
    }

    private void showDisconnectionDialog() {
        final Activity activity = getActivity();
        if (activity != null) {
            ThetaDialogFragment.showDisconnectionDialog(activity,
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
     * enable/disable VR buttons.
     */
    private void enableView() {
        if (mIsStereo) {
            mRightLayout.setVisibility(View.VISIBLE);
        } else {
            mRightLayout.setVisibility(View.GONE);
        }
    }


    /**
     * Init VR Buttons.
     * @param rootView Root XML Layout
     */
    private void init3DButtons(final View rootView) {
        for (int i = 0; i < mVRModeChangeButton.length; i++) {
            int identifier = getResources().getIdentifier("change_vr_mode_" + i, "id", getActivity().getPackageName());
            mVRModeChangeButton[i] = (ToggleButton) rootView.findViewById(identifier);
            mVRModeChangeButton[i].setOnCheckedChangeListener(mVRChangeToggleListener);
            identifier = getResources().getIdentifier("theta_shutter_" + i, "id", getActivity().getPackageName());
            mShootingButton[i] = (Button) rootView.findViewById(identifier);
            mShootingButton[i].setOnClickListener(mShootingListener);
        }
    }

    /**
     * Save ScreenShot.
     */
    private void saveScreenShot() {
        FileManager fileManager = new FileManager(getActivity());
        fileManager.checkWritePermission(new FileManager.CheckPermissionCallback() {
            @Override
            public void onSuccess() {
                Activity activity = getActivity();
                if (activity != null && !hasEnoughStorageSize(LIMIT_APK_SIZE)) {
                    if (mProgress != null) {
                        mProgress.dismiss();
                        mProgress = null;
                    }
                    // Check Android Storage Limit
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ThetaDialogFragment.showAlert(getActivity(),
                                getResources().getString(R.string.theta_ssid_prefix),
                                getResources().getString(R.string.theta_error_shortage_by_android), null);
                        }
                    });
                    return;
                }

                String root = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
                File dir = new File(root);
                if (!dir.exists()) {
                    dir.mkdir();
                }

                Date date = new Date();
                SimpleDateFormat fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
                final String fileName = "theta_vr_screenshot_" + fileDate.format(date) + ".jpg";
                final String filePath = root + fileName;

                try {
                    saveFile(filePath, mSphereView.takeSnapshot());
                    if (BuildConfig.DEBUG) {
                        Log.d("AAA", "absolute path:" + filePath);
                    }
                    ContentValues values = new ContentValues();
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    values.put(MediaStore.Images.Media.TITLE, fileName);
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ThetaDialogFragment.showAlert(getActivity(),
                                        getResources().getString(R.string.theta_ssid_prefix),
                                        getResources().getString(R.string.theta_save_screenshot), null);
                            }
                        });
                    }
                } catch (IOException e) {
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                failSaveDialog();
                            }
                        });
                    }
                } finally {
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgress != null) {
                                    mProgress.dismiss();
                                }
                            }
                        });
                    }
                }

            }

            @Override
            public void onFail() {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        if (mProgress != null) {
                            mProgress.dismiss();
                        }
                        failSaveDialog();
                        }
                    });
                }
            }
        });

    }

    /**
     * Save File.
     * @param filename absolute path
     * @param data binary
     * @throws IOException Failed Save
     */
    private void saveFile(final String filename, final byte[] data) throws IOException {
        Uri u = Uri.parse("file://" + filename);
        ContentResolver contentResolver = getActivity().getContentResolver();
        OutputStream out = null;
        try {
            out = contentResolver.openOutputStream(u, "w");
            out.write(data);
            out.flush();
        } catch (Exception e) {
            throw new IOException("Failed to save a file." + filename);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * ScreenShot failed.
     */
    private void failSaveDialog() {
        ThetaDialogFragment.showAlert(getActivity(),
                getResources().getString(R.string.theta_ssid_prefix),
                getResources().getString(R.string.theta_error_failed_save_file), null);
    }

    /**
     * Check Android Storage size.
     * @param minSize Storage size(MB)
     * @return Return a false if true, otherwise there is a minimum required value or more free
     */
    private boolean hasEnoughStorageSize(final int minSize) {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        float total = 1.0f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            total = stat.getTotalBytes();
        } else {
            total = (float) stat.getBlockSize() * stat.getAvailableBlocks();
        }
        int v = (int) (total / (1024.f * 1024.f));
        if(BuildConfig.DEBUG) {
            if(v < minSize) {
                Log.e("AAA", "hasEnoughStorageSize is less than " + minSize + ", rest size =" + v);
            }
        }
        return v >= minSize;
    }
    /**
     * Donwload of data.
     */
    private class ThetaMainData implements DownloadThetaDataTask.ThetaDownloadListener {

        /** Theta Device. */
        private ThetaDevice mDevice;
        /** Theta Object. */
        private ThetaObject mObj;
        /** SphericalView byte. */
        private byte[] mSphericalBinary;
        /** Communication error. */
        private int mError = -1;
        @Override
        public synchronized void doInBackground() {
            ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
            ThetaDeviceManager deviceMgr = app.getDeviceManager();
            mDevice = deviceMgr.getConnectedDevice();
            if (mDevice != null) {
                try {
                    List<ThetaObject> list = mDevice.fetchAllObjectList();
                    mObj = list.get(mDefaultId);
                    mSphericalBinary = mDataCache.get(mDevice.getName() + "_" + mObj.getFileName());
                    if (mSphericalBinary == null) {
                        mObj.fetch(ThetaObject.DataType.MAIN);
                        mSphericalBinary = mObj.getMainData();
                        mObj.clear(ThetaObject.DataType.MAIN);
                    }
                } catch (ThetaDeviceException e) {
                    e.printStackTrace();
                    mError = e.getReason();
                    mSphericalBinary = null;
                } catch (OutOfMemoryError e) {
                    mError = ThetaDeviceException.OUT_OF_MEMORY;
                }
            } else {
                mSphericalBinary = null;
            }
        }

        @Override
        public synchronized void onPostExecute() {
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }

            if (mSphericalBinary == null) {
                if (mError == ThetaDeviceException.OUT_OF_MEMORY) {
                    ThetaDialogFragment.showAlert(getActivity(), "THETA",
                            getString(R.string.theta_error_memory_warning),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getActivity().finish();
                                }
                            });
                } else if (mError > 0) {
                    // THETA device is found, but communication error occurred.
                    showReconnectionDialog();
                } else {
                    // THETA device is not found.
                    showDisconnectionDialog();
                }
            } else {
                if (mDevice != null && mObj != null) {
                    mDataCache.put(mDevice.getName() + "_" + mObj.getFileName(),
                            mSphericalBinary);
                }
                if (mSphereView != null) {
                    try {
                        mSphereView.onResume();
                        mSphereView.start(mSphericalBinary);
                    } catch (OutOfMemoryError e) {
                        ThetaDialogFragment.showAlert(getActivity(), "THETA",
                                getString(R.string.theta_error_memory_warning),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getActivity().finish();
                                    }
                                });

                    }
                }
            }

        }
    }
}
