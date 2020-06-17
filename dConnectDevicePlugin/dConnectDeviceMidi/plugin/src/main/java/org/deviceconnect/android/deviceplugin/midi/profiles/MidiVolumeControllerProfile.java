package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

public class MidiVolumeControllerProfile extends BaseMidiProfile {

    public MidiVolumeControllerProfile() {

        // GET /gotapi/volumeController/onVolumeChange
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onVolumeChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                root.putInt("channel", 0);
                root.putFloat("value", 0.0f);
                response.putExtras(root);
                return true;
            }
        });

        // PUT /gotapi/volumeController/onVolumeChange
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onVolumeChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.addEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

        // DELETE /gotapi/volumeController/onVolumeChange
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onVolumeChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
                        setResult(response, DConnectMessage.RESULT_OK);
                        break;
                    case INVALID_PARAMETER:
                        MessageUtils.setInvalidRequestParameterError(response);
                        break;
                    case NOT_FOUND:
                        MessageUtils.setUnknownError(response, "Event is not registered.");
                        break;
                    default:
                        MessageUtils.setUnknownError(response);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "volumeController";
    }
}