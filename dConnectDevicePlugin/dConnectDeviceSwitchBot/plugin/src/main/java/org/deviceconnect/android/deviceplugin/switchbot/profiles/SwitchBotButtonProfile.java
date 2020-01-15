package org.deviceconnect.android.deviceplugin.switchbot.profiles;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class SwitchBotButtonProfile extends DConnectProfile {

    private SwitchBotDevice switchBotDevice;

    public SwitchBotButtonProfile(final SwitchBotDevice switchBotDevice) {

        this.switchBotDevice = switchBotDevice;

        // POST /gotapi/button/down
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "down";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                if(switchBotDevice != null && switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                    MessageUtils.setIllegalServerStateError(response, "target device mode mismatch");
                    return true;
                }

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // POST /gotapi/button/push
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "push";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                if(switchBotDevice != null && switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                    MessageUtils.setIllegalServerStateError(response, "target device mode mismatch");
                    return true;
                }

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // POST /gotapi/button/up
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "up";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "button";
    }
}