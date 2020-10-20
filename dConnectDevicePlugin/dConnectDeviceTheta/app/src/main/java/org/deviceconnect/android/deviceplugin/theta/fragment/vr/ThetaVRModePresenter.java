package org.deviceconnect.android.deviceplugin.theta.fragment.vr;

import android.content.Context;

import androidx.collection.LruCache;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.data.ThetaObjectStorage;
import org.deviceconnect.android.deviceplugin.theta.utils.DownloadThetaDataTask;
import org.deviceconnect.android.deviceplugin.theta.utils.MediaSharing;
import org.deviceconnect.android.provider.FileManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

class ThetaVRModePresenter implements ThetaVRModeContract.Presenter {

    /** Task. */
    private DownloadThetaDataTask mDownloadTask;

    private Context mContext;
    /** Default VR. */
    private int mDefaultId;

    /** Is Storage. */
    private boolean mIsStorage;

    private final MediaSharing mMediaSharing = MediaSharing.getInstance();
    private ThetaObjectStorage mStorage;

    /**
     * Logger.
     */
    private final Logger mLogger = Logger.getLogger("theta.sampleapp");

    private ThetaVRModeContract.View mView;
    public ThetaVRModePresenter(final Context context,
                                final ThetaVRModeContract.View view,
                                final int defaultId,
                                final boolean isStorage) {
        mContext = context;
        mView = view;
        mDefaultId = defaultId;
        mIsStorage = isStorage;
        mStorage = new ThetaObjectStorage(context);
    }
    /**
     * Save ScreenShot.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void saveScreenShot() {
        final FileManager fileManager = new FileManager(mContext);
        fileManager.checkWritePermission(new FileManager.CheckPermissionCallback() {
            @Override
            public void onSuccess() {
                if (!mView.checkStorageSize()) {
                    return;
                }

                String cacheDirName = "screenshots";
                File cacheDir = new File(fileManager.getBasePath(), cacheDirName);
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                Date date = new Date();
                SimpleDateFormat fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
                final String fileName = "theta_vr_screenshot_" + fileDate.format(date) + ".jpg";

                try {
                    fileManager.saveFile(cacheDirName + "/" + fileName, mView.takeSnapshot());

                    mMediaSharing.sharePhoto(mContext, new File(cacheDir, fileName));

                    mView.succeedSaveDialog();
                } catch (IOException e) {
                    mLogger.severe("Failed to save screenshot: " + e.getMessage());
                    mView.failSaveDialog();
                } finally {
                    mView.dismissProgressDialog();
                }

            }

            @Override
            public void onFail() {
               mView.dismissProgressDialog();
               mView.failSaveDialog();
            }
        });

    }

    @Override
    public void startDownloadTask() {
        if (mDownloadTask == null) {
            mDownloadTask = new DownloadThetaDataTask();
            ThetaMainData main = new ThetaMainData();
            mDownloadTask.execute(main);
        }
    }
    @Override
    public void stopDownloadTask() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
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
        private LruCache<String, byte[]> mDataCache;
        /** Communication error. */
        private int mError = -1;
        @Override
        public synchronized void doInBackground() {
            ThetaDeviceManager deviceMgr = mView.getThetaDeviceManager();
            mDataCache = mView.getCache();
            mDevice = deviceMgr.getConnectedDevice();
            if (!mIsStorage && mDevice != null) {
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
            } else if (mIsStorage) {
                try {
                    List<ThetaObject> list = mStorage.geThetaObjectCaches(null);
                    mObj = list.get(mDefaultId);
                    mSphericalBinary = mDataCache.get("Android_" + mObj.getFileName());
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
            mView.dismissProgressDialog();

            if (mSphericalBinary == null) {
                if (mError == ThetaDeviceException.OUT_OF_MEMORY) {
                    mView.showOutOfMemoryErrorDialog();
                } else if (mError > 0) {
                    // THETA device is found, but communication error occurred.
                    mView.showReconnectionDialog();
                } else {
                    // THETA device is not found.
                    mView.showDisconnectionDialog();
                }
            } else {
                if (mObj != null) {
                    String device = "Android";
                    if (mDevice != null) {
                        device = mDevice.getName();
                    }
                    mDataCache.put(device + "_" + mObj.getFileName(),
                            mSphericalBinary);
                }
                mView.startSphereView(mSphericalBinary);
            }

        }
    }
}
