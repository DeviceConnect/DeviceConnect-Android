/*
 KadecotTemperatureProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.profile;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Temperature Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotTemperatureProfile extends DConnectProfile {

    public KadecotTemperatureProfile() {

        // GET /gotapi/temperature/
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                // WARNING: レスポンスの定義が不正です.
                return true;
            }
        });

        // PUT /gotapi/temperature/
        addApi(new PutApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                Integer temperature = parseInteger(request, "temperature");
                Integer type = parseInteger(request, "type");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                // WARNING: レスポンスの定義が不正です.
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "temperature";
    }
}