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
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.theta.BuildConfig;
import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceApplication;
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
    /** SphericalView byte. */
    private byte[] mSphericalBinary;
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
        View rootView = inflater.inflate(R.layout.theta_vr_mode, null);
        mLeftLayout = (RelativeLayout) rootView.findViewById(R.id.left_ui);
        mRightLayout = (RelativeLayout) rootView.findViewById(R.id.right_ui);
        mSphereView = (SphericalImageView) rootView.findViewById(R.id.vr_view);
        mApi = new SphericalViewApi(getActivity());
        mSphereView.setViewApi(mApi);
        mSphereView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSphereView.resetCameraDirection();
            }
        });

        init3DButtons(rootView);
        enableView();
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
                String root = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
                File dir = new File(root);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                Date date = new Date();
                SimpleDateFormat fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
                final String fileName = "theta_vr_screenshot_" + fileDate.format(date) + ".jpg";
                final String filePath = root + fileName;
                Activity activity = getActivity();
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
                                        getResources().getString(R.string.theta_save_screenshot));
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
                getResources().getString(R.string.theta_error_failed_save_file));
    }


    /**
     * Donwload of data.
     */
    private class ThetaMainData implements DownloadThetaDataTask.ThetaDownloadListener {

        @Override
        public synchronized void doInBackground() {
            ThetaDeviceApplication app = (ThetaDeviceApplication) getActivity().getApplication();
            ThetaDeviceManager deviceMgr = app.getDeviceManager();
            ThetaDevice device = deviceMgr.getConnectedDevice();
            if (device != null) {
                try {
                    List<ThetaObject> list = device.fetchAllObjectList();
                    list.get(mDefaultId).fetch(ThetaObject.DataType.MAIN);
                    mSphericalBinary = list.get(mDefaultId).getMainData();
                } catch (ThetaDeviceException e) {
                    e.printStackTrace();
                    mSphericalBinary = null;
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
                ThetaDialogFragment.showAlert(getActivity(), "THETA",
                        getString(R.string.theta_error_disconnect_dialog_message));
                getActivity().finish();
            } else {
                if (mSphereView != null) {
                    mSphereView.onResume();
                    mSphereView.start(mSphericalBinary);
                }
            }

        }
    }
}
