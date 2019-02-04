/*
 DConnectAvailabilityProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.profile;

import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AvailabilityProfileConstants;

/**
 * Availability Profile.
 * 
 * @author NTT DOCOMO, Inc.
 */
public class DConnectAvailabilityProfile extends DConnectProfile implements AvailabilityProfileConstants {

    /**
     * Device Connect 設定を保存するクラス.
     */
    private DConnectSettings mSettings;

    /**
     * コンストラクタ.
     */
    public DConnectAvailabilityProfile(final DConnectSettings settings) {
        mSettings = settings;
        addApi(mGetRequest);
    }

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * GET /gotapi/availability.
     */
    private final DConnectApi mGetRequest = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (mSettings.isAvailabilityVisibleName()) {
                setName(response, mSettings.getManagerName());
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    /**
     * レスポンスに名前を追加します.
     *
     * @param response 名前を追加するレスポンス
     * @param name 名前
     */
    private static void setName(final Intent response, final String name) {
        response.putExtra(PARAM_NAME, name);
    }
}
