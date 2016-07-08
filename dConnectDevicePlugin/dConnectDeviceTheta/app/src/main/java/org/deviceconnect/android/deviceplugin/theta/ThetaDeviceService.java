/*
 ThetaDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.content.Intent;
import android.os.Bundle;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaBatteryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaFileProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaOmnidirectionalImageProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSystemProfile;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.OmnidirectionalImageProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService {

    private static final String TYPE_NONE = "none";
    private ThetaDeviceManager mDeviceMgr;
    private ThetaDeviceClient mClient;
    private ThetaMediaStreamRecordingProfile mThetaMediaStreamRecording;

    @Override
    public void onCreate() {
        super.onCreate();

        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        mDeviceMgr = app.getDeviceManager();
        mClient = new ThetaDeviceClient(mDeviceMgr);

        EventManager.INSTANCE.setController(new MemoryCacheController());

        FileManager fileMgr = new FileManager(this);
        mThetaMediaStreamRecording = new ThetaMediaStreamRecordingProfile(mClient, fileMgr);
        addProfile(new ThetaBatteryProfile(mClient));
        addProfile(new ThetaFileProfile(mClient, fileMgr));
        addProfile(mThetaMediaStreamRecording);
        addProfile(new ThetaOmnidirectionalImageProfile(app.getHeadTracker()));
    }

    @Override
    public void onDestroy() {
        try {
            PtpipInitiator.close();
        } catch (ThetaException e) {
            // Nothing to do.
        }
        super.onDestroy();
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
        mThetaMediaStreamRecording.forcedStopRecording();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ThetaSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) {
        };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new ThetaServiceDiscoveryProfile(this);
    }

    public boolean searchDevice(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();
        ThetaDevice device = mDeviceMgr.getConnectedDevice();
        if (device != null) {
            Bundle service = new Bundle();
            ServiceDiscoveryProfile.setId(service, device.getId());
            ServiceDiscoveryProfile.setName(service, device.getName());
            ServiceDiscoveryProfile.setType(service, ServiceDiscoveryProfile.NetworkType.WIFI);
            ServiceDiscoveryProfile.setOnline(service, true);
            ServiceDiscoveryProfile.setScopes(service, this);
            services.add(service);
        }

        Bundle service = new Bundle();
        ServiceDiscoveryProfile.setId(service,
            ThetaOmnidirectionalImageProfile.SERVICE_ID);
        ServiceDiscoveryProfile.setName(service,
            ThetaOmnidirectionalImageProfile.SERVICE_NAME);
        ServiceDiscoveryProfile.setType(service, TYPE_NONE);
        ServiceDiscoveryProfile.setOnline(service, true);
        service.putStringArray(ServiceDiscoveryProfile.PARAM_SCOPES,
            new String[]{OmnidirectionalImageProfile.PROFILE_NAME});
        services.add(service);

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES, services.toArray(new Bundle[services.size()]));
        return true;
    }

}
