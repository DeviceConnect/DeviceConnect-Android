package org.deviceconnect.android.deviceplugin.irkit.service;


import org.deviceconnect.android.deviceplugin.irkit.data.IRKitDBHelper;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualDeviceData;
import org.deviceconnect.android.deviceplugin.irkit.data.VirtualProfileData;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitLightProfile;
import org.deviceconnect.android.deviceplugin.irkit.profile.IRKitTVProfile;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;

import java.util.List;

public class VirtualService extends DConnectService {

    private final IRKitDBHelper mDBHelper;

    private final DConnectServiceProvider mServiceProvider;

    public VirtualService(final VirtualDeviceData device, final IRKitDBHelper dbHelper,
                          final DConnectServiceProvider provider) {
        super(device.getServiceId());
        mDBHelper = dbHelper;
        mServiceProvider = provider;

        setName(device.getDeviceName());
        setNetworkType(NetworkType.WIFI);

        List<VirtualProfileData> profiles =
            mDBHelper.getVirtualProfiles(device.getServiceId(), null);
        for (VirtualProfileData profile : profiles) {
            if (LightProfile.PROFILE_NAME.equalsIgnoreCase(profile.getProfile())) {
                addProfile(new IRKitLightProfile());
            } else if (IRKitTVProfile.PROFILE_NAME.equalsIgnoreCase(profile.getProfile())) {
                addProfile(new IRKitTVProfile());
            }
        }
    }

    @Override
    public boolean isOnline() {
        if (!isIRExist()) {
            return false;
        }
        boolean isOnline = false;
        for (DConnectService irKit : mServiceProvider.getServiceList()) {
            if (irKit instanceof IRKitService
                && this.getId().startsWith(irKit.getId())) {
                isOnline = irKit.isOnline();
                break;
            }
        }
        return isOnline;
    }

    /**
     * 一つでも赤外線が登録されているかをチェックする.
     * @return true:登録されている, false:登録されていない
     */
    private boolean isIRExist() {
        List<VirtualProfileData> requests = mDBHelper.getVirtualProfiles(getId(), null);
        for (VirtualProfileData request : requests) {
            if (request.getIr() != null && request.getIr().contains("{\"format\":\"raw\",")) {
                return true;
            }
        }
        return false;
    }

}
