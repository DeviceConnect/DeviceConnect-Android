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

public class MidiKeyEventProfile extends BaseMidiProfile {

    public MidiKeyEventProfile() {

        // GET /gotapi/keyEvent/onDown
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                Bundle keyevent = new Bundle();
                keyevent.putInt("id", 0);
                keyevent.putString("config", "test");
                root.putBundle("keyevent", keyevent);
                response.putExtras(root);
                return true;
            }
        });

        // PUT /gotapi/keyEvent/onDown
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onDown";
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

        // DELETE /gotapi/keyEvent/onDown
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onDown";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                EventError error = EventManager.INSTANCE.removeEvent(request);
                switch (error) {
                    case NONE:
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

        // GET /gotapi/keyEvent/onKeyChange
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                Bundle keyevent = new Bundle();
                keyevent.putString("state", "test");
                keyevent.putInt("id", 0);
                keyevent.putString("config", "test");
                root.putBundle("keyevent", keyevent);
                response.putExtras(root);
                return true;
            }
        });

        // PUT /gotapi/keyEvent/onKeyChange
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
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

        // DELETE /gotapi/keyEvent/onKeyChange
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onKeyChange";
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

        // GET /gotapi/keyEvent/onUp
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onUp";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                Bundle root = response.getExtras();
                Bundle keyevent = new Bundle();
                keyevent.putInt("id", 0);
                keyevent.putString("config", "test");
                root.putBundle("keyevent", keyevent);
                response.putExtras(root);
                return true;
            }
        });

        // PUT /gotapi/keyEvent/onUp
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onUp";
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

        // DELETE /gotapi/keyEvent/onUp
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onUp";
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
        return "keyEvent";
    }
}