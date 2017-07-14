package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class HOGPHogpProfile extends DConnectProfile {

    public HOGPHogpProfile() {

        // POST /hogp/keyboard
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "keyboard";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                Integer modifier = parseInteger(request, "modifier");
                Integer keyCode = parseInteger(request, "keyCode");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

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
                Integer rightButton = parseInteger(request, "rightButton");
                Integer leftButton = parseInteger(request, "leftButton");
                Integer middleButton = parseInteger(request, "middleButton");

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