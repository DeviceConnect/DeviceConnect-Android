/*
 ThetaDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSystemProfile;
import org.deviceconnect.android.deviceplugin.theta.service.ThetaService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService
    implements ThetaDeviceEventListener {

    private static final String TYPE_NONE = "none";
    private ThetaDeviceManager mDeviceMgr;
    private HeadTracker mHeadTracker;
    private ThetaDeviceClient mClient;
    private FileManager mFileMgr;
    private ThetaMediaStreamRecordingProfile mThetaMediaStreamRecording;

    @Override
    public void onCreate() {
        super.onCreate();

        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        mDeviceMgr = app.getDeviceManager();
        mDeviceMgr.registerDeviceEventListener(this);
        mDeviceMgr.checkConnectedDevice();
        mHeadTracker = app.getHeadTracker();
        mClient = new ThetaDeviceClient(mDeviceMgr);
        mFileMgr = new FileManager(this);

        EventManager.INSTANCE.setController(new MemoryCacheController());
    }

    @Override
    public void onDestroy() {
        mDeviceMgr.unregisterDeviceEventListener(this);
        try {
            PtpipInitiator.close();
        } catch (ThetaException e) {
            // Nothing to do.
        }
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ThetaSystemProfile();
    }

    @Override
    public void onConnected(final ThetaDevice device) {
        DConnectService service = getServiceProvider().getService(device.getId());
        if (service == null) {
            service = new ThetaService(device, mClient, mFileMgr, mHeadTracker);
            getServiceProvider().addService(service);
            mThetaMediaStreamRecording = (ThetaMediaStreamRecordingProfile)service.getProfile(ThetaMediaStreamRecordingProfile.PROFILE_NAME);
        }
        service.setOnline(true);
    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        if (getServiceProvider().hasService(device.getId())) {
            DConnectService service = getServiceProvider().getService(device.getId());
            service.setOnline(false);
        }
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO: Managerアンインストール検知時の処理要追加。
    }

    @Override
    protected void onManagerTerminated() {
        // TODO: Manager正常終了通知受信時の処理要追加。
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // TODO: ManagerのEvent送信経路切断通知受信時の処理要追加。
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // TODO: Device Plug-inへのReset要求受信時の処理要追加。
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /** 全イベント削除. */
        EventManager.INSTANCE.removeAll();

        /** 記録処理・プレビュー停止 */

        if (mThetaMediaStreamRecording != null) {
            mThetaMediaStreamRecording.forcedStopRecording();
        }
    }

}
