
/*
 HitoeService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe;

import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeDevice;
import org.deviceconnect.android.deviceplugin.hitoe.data.HitoeManager;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeBatteryProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeDeviceOrientationProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeECGProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeHealthProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoePoseEstimationProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeStressEstimationProfile;
import org.deviceconnect.android.deviceplugin.hitoe.profile.HitoeWalkStateProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * Implement Hitoe's information.
 * @author NTT DOCOMO, INC.
 */
public class HitoeService extends DConnectService {
    /**
     * コンストラクタ.
     *
     * @param id サービスID
     */
    private final HitoeDevice mEntity;

    /**
     * Construcotr.
     * @param manager HitoeManager
     * @param entity HitoeDevice
     */
    public HitoeService(final HitoeManager manager, final HitoeDevice entity) {
        super(entity.getId());
        setName(entity.getName());
        setNetworkType(NetworkType.BLE);
        setConfig("");
        setOnline(entity.isRegisterFlag());
        addProfile(new HitoeHealthProfile(manager));
        addProfile(new HitoeDeviceOrientationProfile(manager));
        addProfile(new HitoeBatteryProfile());
        addProfile(new HitoeECGProfile(manager));
        addProfile(new HitoeStressEstimationProfile(manager));
        addProfile(new HitoePoseEstimationProfile(manager));
        addProfile(new HitoeWalkStateProfile(manager));
        mEntity = entity;
    }
    @Override
    public boolean isOnline() {
        return mEntity.isRegisterFlag();
    }

}
