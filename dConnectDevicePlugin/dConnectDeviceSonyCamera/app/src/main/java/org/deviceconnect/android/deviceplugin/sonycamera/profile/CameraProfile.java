/*
CameraProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;

/**
 * カメラプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class CameraProfile extends DConnectProfile {

    /** プロファイル名. */
    public static final String PROFILE_NAME = "camera";

    /**
     * 属性: {@value} .
     */
    public static final String ATTRIBUTE_ZOOM = "zoom";
    /**
     * パラメータ:{@value} .
     */
    public static final String PARAM_ZOOM_POSITION = "zoomPosition";

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    //---------GET------------

    /** 
     * Movementパラメータのゲッター.
     * @param request リクエスト
     * @return movement
     */
    public static String getMovement(final Intent request) {
        return request.getStringExtra("movement");
    }

    /**
     *  Directionパラメータのゲッター.
     *  @param request リクエスト
     *  @return direction
     */
    public static String getDirection(final Intent request) {
        return request.getStringExtra("direction");
    }
}
