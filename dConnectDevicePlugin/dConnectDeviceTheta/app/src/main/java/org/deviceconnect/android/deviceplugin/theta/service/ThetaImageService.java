package org.deviceconnect.android.deviceplugin.theta.service;


import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaOmnidirectionalImageProfile;
import org.deviceconnect.android.service.DConnectService;

public class ThetaImageService extends DConnectService {

    /**
     * The service ID of ROI Image Service.
     */
    public static final String SERVICE_ID = "roi";

    /**
     * The name of ROI Image Service.
     */
    public static final String SERVICE_NAME = "ROI Image Service";

    public ThetaImageService(final HeadTracker headTracker) {
        super(SERVICE_ID);
        setOnline(true);
        setName(SERVICE_NAME);
        addProfile(new ThetaOmnidirectionalImageProfile(headTracker));
    }

}
