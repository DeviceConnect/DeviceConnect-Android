/*
 HvcServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import java.util.LinkedList;
import java.util.List;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

import android.content.Intent;
import android.os.Bundle;

/**
 * Hvc service information profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HvcServiceInformationProfile extends ServiceInformationProfile {

    /**
     * Parameter: {@value} .
     */
    private static final String PARAM_HUMANDETECT = "humanDetect";
    /**
     * Parameter: {@value} .
     */
    private static final String PARAM_CAMERA = "camera";
    /**
     * Parameter: {@value} .
     */
    private static final String PARAM_WIDTH = "width";
    /**
     * Parameter: {@value} .
     */
    private static final String PARAM_HEIGHT = "height";

    /**
     * Constructor.
     * @param provider provider
     */
    public HvcServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetInformation(final Intent request, final Intent response, final String serviceId) {
        
        boolean result = super.onGetInformation(request, response, serviceId);
        
        // Proprietary extensions for HumanDetect.
        
        Bundle camera = new Bundle();
        camera.putInt(PARAM_WIDTH, HvcConstants.HVC_C_CAMERA_WIDTH);
        camera.putInt(PARAM_HEIGHT, HvcConstants.HVC_C_CAMERA_HEIGHT);
        
        List<Bundle> cameraArray = new LinkedList<Bundle>();
        cameraArray.add(camera);
        
        Bundle humandetect = new Bundle();
        humandetect.putParcelableArray(PARAM_CAMERA, cameraArray.toArray(new Bundle[cameraArray.size()]));
        response.putExtra(PARAM_HUMANDETECT, humandetect);
        
        return result;
    }
}
