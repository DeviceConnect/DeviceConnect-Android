/*
 HeartRateServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.sphero.util.BleUtils;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.service.DConnectServiceProvider;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class SpheroServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    private final HandlerThread mWorkerThread;

    private final DConnectApi mServiceDiscoveryApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final Runnable perform = new Runnable() {
                @Override
                public void run() {
                    appendServiceList(response);
                }
            };

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                perform.run();
                return true;
            } else {
                if (BleUtils.isBLEPermission(getContext())) {
                    perform.run();
                    return true;
                } else {
                    PermissionUtility.requestPermissions(getContext(), new Handler(mWorkerThread.getLooper()),
                        BleUtils.BLE_PERMISSIONS,
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                perform.run();
                                sendResponses(response);
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                MessageUtils.setIllegalServerStateError(response,
                                    "Bluetooth LE scan requires permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION.");
                                sendResponses(response);
                            }
                        });
                    return false;
                }
            }
        }
    };

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public SpheroServiceDiscoveryProfile(final DConnectServiceProvider provider) {
        super(provider);
        mWorkerThread = new HandlerThread(getClass().getSimpleName() + "_" + this.hashCode());
        mWorkerThread.start();

        addApi(mServiceDiscoveryApi);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mWorkerThread.quit();
    }

    private void sendResponses(final Intent response) {
        DConnectMessageService s = (DConnectMessageService) getContext();
        s.sendResponse(response);
    }
}
