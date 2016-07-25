/*
 HvcServiceInformationProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;

import java.util.LinkedList;
import java.util.List;

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

    private final DConnectApi mServiceInformationApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            appendServiceInformation(response);

            // Proprietary extensions for HumanDetect.

            Bundle camera = new Bundle();
            camera.putInt(PARAM_WIDTH, HvcConstants.HVC_C_CAMERA_WIDTH);
            camera.putInt(PARAM_HEIGHT, HvcConstants.HVC_C_CAMERA_HEIGHT);

            List<Bundle> cameraArray = new LinkedList<Bundle>();
            cameraArray.add(camera);

            Bundle humandetect = new Bundle();
            humandetect.putParcelableArray(PARAM_CAMERA, cameraArray.toArray(new Bundle[cameraArray.size()]));
            response.putExtra(PARAM_HUMANDETECT, humandetect);

            return true;
        }
    };

    /**
     * Constructor.
     */
    public HvcServiceInformationProfile() {
        addApi(mServiceInformationApi);
    }
}
