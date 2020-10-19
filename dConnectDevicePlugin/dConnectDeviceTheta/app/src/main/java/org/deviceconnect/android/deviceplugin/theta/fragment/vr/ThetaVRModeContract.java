package org.deviceconnect.android.deviceplugin.theta.fragment.vr;

import android.view.View;

import androidx.collection.LruCache;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;

interface ThetaVRModeContract {
    interface Presenter {
        void saveScreenShot();
        void startDownloadTask();
        void stopDownloadTask();
    }
    interface View {
        void showReconnectionDialog();
        void showDisconnectionDialog();
        void showOutOfMemoryErrorDialog();
        void showProgressDialog();
        void dismissProgressDialog();
        void enableView();
        void succeedSaveDialog();
        void failSaveDialog();
        boolean checkStorageSize();
        byte[] takeSnapshot();
        ThetaDeviceManager getThetaDeviceManager();
        LruCache<String, byte[]> getCache();
        void startSphereView(byte[] sphericalBinary);
    }
}
