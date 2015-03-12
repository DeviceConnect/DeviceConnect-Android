/*
 SWServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.sonyericsson.extras.liveware.aef.registration.Registration.Device;
import com.sonyericsson.extras.liveware.aef.registration.Registration.DeviceColumns;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationAdapter;

/**
 * SonySWデバイスプラグインの{@link ServiceDiscoveryProfile}実装.
 * @author NTT DOCOMO, INC.
 */
public class SWServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public SWServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
            if (bondedDevices != null) {
                for (BluetoothDevice device : bondedDevices) {
                    String deviceName = device.getName();
                    if (deviceName == null) {
                        continue;
                    }
                    if (deviceName.startsWith(SWConstants.DEVICE_NAME_PREFIX)) {
                        long hostAppId = RegistrationAdapter.
                                getHostApplication(getContext(), SWUtil.toHostAppPackageName(deviceName)).getId(); 
                        if (isConnected(hostAppId)) {
                            Bundle bundle = SWUtil.toBundle(device);
                            setScopes(bundle, getProfileProvider());
                            services.add(bundle);
                        }
                    }
                }
            }
        }
        setResult(response, DConnectMessage.RESULT_OK);
        setServices(response, services);
        return true;
    }

    /**
     * SonyWatchがオンラインかどうかの判定をする.
     * 
     * @param hostAppId ホストアプリケーションID
     * @return オンラインであればtrue、そうでない場合はfalse
     */
    private boolean isConnected(final long hostAppId) {
        Cursor cursor = null;
        try {
            String selection = DeviceColumns.HOST_APPLICATION_ID + " = " + hostAppId + " AND "
                    + DeviceColumns.ACCESSORY_CONNECTED + " = 1";
            cursor = getContext().getContentResolver().query(Device.URI, null, selection, null, null);
            if (cursor != null) {
                return (cursor.getCount() > 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

}
