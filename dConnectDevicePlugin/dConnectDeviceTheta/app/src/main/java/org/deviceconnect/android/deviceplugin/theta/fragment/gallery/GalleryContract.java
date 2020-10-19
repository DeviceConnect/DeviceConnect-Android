package org.deviceconnect.android.deviceplugin.theta.fragment.gallery;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;

import java.util.List;

interface GalleryContract {
    /** Gallery Mode: App. */
    int GALLERY_MODE_APP = 0;

    /** Gallery Mode: Theta. */
    int GALLERY_MODE_THETA = 1;

    /** Gallery Command: data import. */
    int DIALOG_COMMAND_IMPORT = 0;

    /** Gallery Command: data delete. */
    int DIALOG_COMMAND_DELETE = 1;


    /** Gallery Mode Enable background. */
    int MODE_ENABLE_BACKGROUND = R.drawable.button_blue;

    /** Gallery Mode Disable background. */
    int MODE_DISABLE_BACKGROUND = R.drawable.button_white;

    /** Gallery Mode Enable text color. */
    int MODE_ENABLE_TEXT_COLOR = R.color.tab_text;

    /** Gallery Mode Disable text color. */
    int MODE_DISABLE_TEXT_COLOR = R.color.action_bar_background;

    interface Presenter {
        void startShootingModeGetTask();
        void startThetaDataRemoveTask(final ThetaObject removeObject);
        void startThumbDownloadTask(final ThetaObject obj,
                                    final ThetaGalleryAdapter.GalleryViewHolder holder);
        void startThetaInfoGetTask();
        void stopTask();
    }
    interface View {
        void loadThetaData();
        boolean startProgressDialog(int message);
        void startProgressDialogForReconnect(int message);
        void stopProgressDialog();
        void showReconnectionDialog();
        void showDialog(int message);
        void showSettingsActivity();
        void updateStatusView(int resultCount);
        void updateStatusView();
        void updateThetaObjectList(List<ThetaObject> obj);
        List<ThetaObject> removeObj(ThetaObject obj);
        void notifyGalleryListChanged(List<ThetaObject> obj);
        boolean existThetaDevice();
        ThetaDevice.ShootingMode getShootingMode();
        List<ThetaObject> getThetaObjects() throws ThetaDeviceException;
        byte[] getThumbnail(final String fileName);
        void putThumbnail(final String fileName, final byte[] data);
    }
}
