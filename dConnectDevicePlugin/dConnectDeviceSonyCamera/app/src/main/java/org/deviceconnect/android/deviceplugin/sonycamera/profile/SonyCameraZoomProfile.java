/*
SonyCameraZoomProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

/**
 * Sony Camera 用 カメラプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraZoomProfile extends CameraProfile {

    private final DConnectApi mPutZoomApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ZOOM;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext())
                .onPutActZoom(request, response, getServiceID(request), getDirection(request),
                    getMovement(request));
        }
    };

    private final DConnectApi mGetZoomApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ZOOM;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return ((SonyCameraDeviceService) getContext()).onGetZoomDiameter(request, response,
                getServiceID(request));
        }
    };

    public SonyCameraZoomProfile() {
        addApi(mPutZoomApi);
        addApi(mGetZoomApi);
    }
}
