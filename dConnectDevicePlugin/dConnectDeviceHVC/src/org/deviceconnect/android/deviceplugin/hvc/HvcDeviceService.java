/*
 HvcDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcHumanDetectProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcSystemProfile;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import android.content.Intent;

/**
 * HVC Device Service.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceService extends DConnectMessageService {

    @Override
    public void onCreate() {

        super.onCreate();

        // LocalOAuthの処理
        LocalOAuth2Main.initialize(getApplicationContext());

        // add supported profiles
        addProfile(new HvcHumanDetectProfile());
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
//        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                    HvcConnectProfile.PROFILE_NAME,
//                    List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
//                    null,
//                    HvcConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
//
//            for (int i = 0; i < events.size(); i++) {
//                Event event = events.get(i);
//                Intent mIntent = EventManager.createEventMessage(event);
//                HvcConnectProfile.setAttribute(mIntent, HvcConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
//                Bundle bluetoothConnecting = new Bundle();
//                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                HvcConnectProfile.setEnable(bluetoothConnecting, mBluetoothAdapter.isEnabled());
//                HvcConnectProfile.setConnectStatus(mIntent, bluetoothConnecting);
//                getContext().sendBroadcast(mIntent);
//            }
//            return START_STICKY;
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HvcSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HvcServiceDiscoveryProfile();
    }
}
