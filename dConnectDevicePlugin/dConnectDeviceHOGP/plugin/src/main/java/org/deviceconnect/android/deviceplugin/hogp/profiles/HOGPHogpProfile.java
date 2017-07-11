package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class HOGPHogpProfile extends DConnectProfile {

    public HOGPHogpProfile() {

        // POST /hogp/mouse
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "mouse";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                Integer x = parseInteger(request, "x");
                Integer y = parseInteger(request, "y");
                Integer wheel = parseInteger(request, "wheel");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "hogp";
    }
}