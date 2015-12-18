/*
SonyCameraZoomProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;

import android.content.Intent;

/**
 * Sony Camera 用 カメラプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraZoomProfile extends CameraProfile {
    @Override
    protected boolean onPutActZoom(final Intent request, final Intent response, final String serviceId,
            final String direction, final String movement) {
        return ((SonyCameraDeviceService) getContext())
                .onPutActZoom(request, response, serviceId, direction, movement);
    }

    @Override
    protected boolean onGetZoomDiameter(final Intent request, final Intent response, final String serviceId) {
        return ((SonyCameraDeviceService) getContext()).onGetZoomDiameter(request, response, serviceId);
    }
}
