/*
 HitoeServiceDiscoveryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.profile;

import android.content.Intent;

import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;

/**
 * Implement ServiceDiscoveryProfile.
 * @author NTT DOCOMO, INC.
 */
public class HitoeServiceDiscoveryProfile extends ServiceDiscoveryProfile {

//    private final HandlerThread mWorkerThread;

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public HitoeServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
//        mWorkerThread = new HandlerThread(getClass().getSimpleName() + "_" + this.hashCode());
//        mWorkerThread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
//        mWorkerThread.quit();
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        return true;
//        if (!BleUtils.isBLESupported(getContext())) {
//            mLogger.warning("BLE not supported.");
//            List<Bundle> services = new ArrayList<>();
//            setResult(response, DConnectMessage.RESULT_OK);
//            setServices(response, services);
//            return true;
//        }
//
//        final Runnable perform = new Runnable() {
//            @Override
//            public void run() {
//                List<Bundle> services = new ArrayList<>();
//                List<HeartRateDevice> devices = getManager().getConnectedDevices();
//                synchronized (devices) {
//                    for (HeartRateDevice device : devices) {
//                        Bundle service = new Bundle();
//                        service.putString(PARAM_ID, device.getId());
//                        service.putString(PARAM_NAME, device.getName());
//                        service.putString(PARAM_TYPE, NetworkType.BLE.getValue());
//                        service.putBoolean(PARAM_ONLINE, true);
//                        service.putString(PARAM_CONFIG, "");
//                        setScopes(service, getProfileProvider());
//                        services.add(service);
//                    }
//                }
//                setResult(response, DConnectMessage.RESULT_OK);
//                setServices(response, services);
//            }
//        };
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            perform.run();
//            return true;
//        } else {
//            if (BleUtils.isBLEPermission(getContext())) {
//                perform.run();
//                return true;
//            } else {
//                PermissionUtility.requestPermissions(getContext(), new Handler(mWorkerThread.getLooper()),
//                        BleUtils.BLE_PERMISSIONS,
//                        new PermissionUtility.PermissionRequestCallback() {
//                            @Override
//                            public void onSuccess() {
//                                perform.run();
//                                sendResponses(response);
//                            }
//
//                            @Override
//                            public void onFail(@NonNull String deniedPermission) {
//                                MessageUtils.setIllegalServerStateError(response,
//                                        "Bluetooth LE scan requires permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION.");
//                                sendResponses(response);
//                            }
//                        });
//                return false;
//            }
//        }
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                                           final String serviceId, final String sessionKey) {
        return super.onPutOnServiceChange(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                              final String serviceId, final String sessionKey) {
        return super.onDeleteOnServiceChange(request, response, serviceId, sessionKey);
    }

    private void sendResponses(final Intent response) {
        DConnectMessageService s = (DConnectMessageService) getContext();
        s.sendResponse(response);
    }
}
