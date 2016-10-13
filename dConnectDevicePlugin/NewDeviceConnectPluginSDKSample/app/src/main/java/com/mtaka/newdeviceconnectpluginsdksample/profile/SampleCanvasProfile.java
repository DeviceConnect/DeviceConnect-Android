package com.mtaka.newdeviceconnectpluginsdksample.profile;


import android.content.Intent;

import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.DConnectServiceEndPoint;

public class SampleCanvasProfile extends CanvasProfile {

    public static final Api DRAW_IMAGE_API = new DrawImage() {

        @Override
        public String[] getSupportedRequestParamNames() {
            return new String[] { "x", "y" };
        }

        @Override
        protected boolean onRequest(DConnectServiceEndPoint service, Intent request, Intent response, String serviceId, Integer x, Integer y) {
            return super.onRequest(service, request, response, serviceId, x, y);
        }
    };

    public static final Api DELETE_IMAGE_API = new DeleteImage() {

        @Override
        protected boolean onRequest(DConnectServiceEndPoint service, Intent request, Intent response, String serviceId) {
            return super.onRequest(service, request, response, serviceId);
        }
    };

    @Override
    protected boolean onPostDrawImage(Intent request, Intent response,
                                      String serviceId, Integer x, Integer y) {
        // XXXX 既存のAPI実装
        return true;
    }

    @Override
    protected boolean onDeleteDrawImage(Intent request, Intent response, String serviceId) {
        // XXXX 既存のAPI実装
        return true;
    }


}
