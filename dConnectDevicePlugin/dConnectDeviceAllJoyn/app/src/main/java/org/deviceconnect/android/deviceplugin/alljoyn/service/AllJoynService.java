package org.deviceconnect.android.deviceplugin.alljoyn.service;


import org.deviceconnect.android.deviceplugin.alljoyn.AllJoynServiceEntity;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynLightProfile;
import org.deviceconnect.android.service.DConnectService;

public class AllJoynService extends DConnectService {

    private final AllJoynServiceEntity mEntity;

    public AllJoynService(final AllJoynServiceEntity entity) {
        super(entity.appId);
        setName(entity.serviceName);
        addProfile(new AllJoynLightProfile());
        mEntity = entity;
    }

    public AllJoynServiceEntity getEntity() {
        return mEntity;
    }
}
