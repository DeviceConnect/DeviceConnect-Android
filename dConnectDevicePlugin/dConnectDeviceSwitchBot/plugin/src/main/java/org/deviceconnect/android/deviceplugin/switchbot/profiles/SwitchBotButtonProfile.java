package org.deviceconnect.android.deviceplugin.switchbot.profiles;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class SwitchBotButtonProfile extends DConnectProfile {

    private static final String TAG = "SwitchBotButtonProfile";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public SwitchBotButtonProfile(SwitchBotDevice switchBotDevice) {
        if (DEBUG) {
            Log.d(TAG, "SwitchBotButtonProfile()");
        }
        if (switchBotDevice != null) {
            // POST /gotapi/button/down
            addApi(new PostApi() {
                @Override
                public String getAttribute() {
                    return "down";
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    Bundle extras = request.getExtras();
                    if (extras != null) {
                        String serviceId = (String) request.getExtras().get("serviceId");
                        if (DEBUG) {
                            Log.d(TAG, "serviceId : " + serviceId);
                        }

                        if (switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                            MessageUtils.setIllegalServerStateError(response, "target device mode mismatch");
                            return true;
                        }

                        // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                        if (switchBotDevice.isConnected()) {
                            if (switchBotDevice.down()) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                MessageUtils.setIllegalServerStateError(response, "target device busy");
                            }
                        } else {
                            switchBotDevice.connect();
                            MessageUtils.setIllegalServerStateError(response, "target device not online(reconnecting)");
                        }
                    }
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
                    Bundle extras = request.getExtras();
                    if (extras != null) {
                        String serviceId = (String) request.getExtras().get("serviceId");
                        if (DEBUG) {
                            Log.d(TAG, "serviceId : " + serviceId);
                        }
                        if (switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                            MessageUtils.setIllegalServerStateError(response, "target device mode mismatch");
                            return true;
                        }

                        // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                        if (switchBotDevice.isConnected()) {
                            if (switchBotDevice.press()) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                MessageUtils.setIllegalServerStateError(response, "target device busy");
                            }
                        } else {
                            switchBotDevice.connect();
                            MessageUtils.setIllegalServerStateError(response, "target device not online(reconnecting)");
                        }
                    }
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
                    Bundle extras = request.getExtras();
                    if (extras != null) {
                        String serviceId = (String) request.getExtras().get("serviceId");
                        if (DEBUG) {
                            Log.d(TAG, "serviceId : " + serviceId);
                        }

                        if (switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                            MessageUtils.setIllegalServerStateError(response, "target device mode mismatch");
                            return true;
                        }

                        // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                        if (switchBotDevice.isConnected()) {
                            if (switchBotDevice.up()) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                MessageUtils.setIllegalServerStateError(response, "target device busy");
                            }
                        } else {
                            switchBotDevice.connect();
                            MessageUtils.setIllegalServerStateError(response, "target device not online(reconnecting)");
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public String getProfileName() {
        return "button";
    }
}