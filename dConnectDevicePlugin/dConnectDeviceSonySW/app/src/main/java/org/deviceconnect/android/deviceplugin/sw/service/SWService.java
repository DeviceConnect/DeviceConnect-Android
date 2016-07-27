package org.deviceconnect.android.deviceplugin.sw.service;


import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.registration.HostApplicationInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationAdapter;

import org.deviceconnect.android.deviceplugin.sw.profile.SWCanvasProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWNotificationProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWTouchProfile;
import org.deviceconnect.android.deviceplugin.sw.profile.SWVibrationProfile;
import org.deviceconnect.android.service.DConnectService;

import java.util.Locale;

public abstract class SWService extends DConnectService {

    private final WatchType mType;

    protected SWService(final BluetoothDevice device, final WatchType type) {
        super(createServiceId(device));
        mType = type;

        setName(device.getName());
        setNetworkType(NetworkType.BLUETOOTH);

        addProfile(new SWDeviceOrientationProfile());
        addProfile(new SWNotificationProfile());
        addProfile(new SWVibrationProfile(type));
        addProfile(new SWCanvasProfile());
        addProfile(new SWTouchProfile());
    }

    private String createConnectionStateQuery() {
        HostApplicationInfo hostAppInfo =
            RegistrationAdapter.getHostApplication(getContext(), getHostPackageName());

        return Registration.DeviceColumns.HOST_APPLICATION_ID
            + " = "
            + hostAppInfo.getId()
            + " AND "
            + Registration.DeviceColumns.ACCESSORY_CONNECTED
            + " = 1";
    }

    @Override
    public boolean isOnline() {
        return isConnectedToSmartConnect();
    }

    private boolean isConnectedToSmartConnect() {
        Cursor cursor = null;
        try {
            String selection = createConnectionStateQuery();
            cursor = getContext().getContentResolver().query(Registration.Device.URI, null, selection, null, null);
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

    public static String createServiceId(final BluetoothDevice device) {
        String address = device.getAddress();
        return address.replace(":", "").toLowerCase(Locale.ENGLISH);
    }

    public abstract String getHostPackageName();

    public void sendRequest(final Intent request) {
        request.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, getContext().getPackageName());
        request.setPackage(getHostPackageName());
        getContext().sendBroadcast(request, Registration.HOSTAPP_PERMISSION);
    }

    public WatchType getWatchType() {
        return mType;
    }

    public enum WatchType {
        MN2,
        SW2
    }
}
