/*
 SwitchBotButtonProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.deviceplugin.switchbot.profiles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class SwitchBotButtonProfile extends DConnectProfile {

    private static final String TAG = "SwitchBotButtonProfile";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public SwitchBotButtonProfile(final Context context, final SwitchBotDevice switchBotDevice) {
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

                        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
                        if (bluetoothManager != null) {
                            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                            if (bluetoothAdapter != null) {
                                if (!bluetoothAdapter.isEnabled()) {
                                    MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                                    return true;
                                }
                            } else {
                                MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                                return true;
                            }
                        } else {
                            MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                            return true;
                        }

                        if (switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                            MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_mode_unmatched));
                            return true;
                        }

                        // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                        switchBotDevice.connect(new SwitchBotDevice.ConnectCallback() {
                            @Override
                            public void onSuccess() {
                                if (switchBotDevice.down()) {
                                    setResult(response, DConnectMessage.RESULT_OK);
                                } else {
                                    MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_device_busy));
                                }
                                sendResponse(response);
                            }

                            @Override
                            public void onFailure() {
                                MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_connect_failure));
                                sendResponse(response);
                            }
                        });
                    }
                    return false;
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

                        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
                        if (bluetoothManager != null) {
                            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                            if (bluetoothAdapter != null) {
                                if (!bluetoothAdapter.isEnabled()) {
                                    MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                                    return true;
                                }
                            } else {
                                MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                                return true;
                            }
                        } else {
                            MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                            return true;
                        }

                        if (switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                            MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_mode_unmatched));
                            return true;
                        }

                        // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                        switchBotDevice.connect(new SwitchBotDevice.ConnectCallback() {
                            @Override
                            public void onSuccess() {
                                if (switchBotDevice.press()) {
                                    setResult(response, DConnectMessage.RESULT_OK);
                                } else {
                                    MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_device_busy));
                                }
                                sendResponse(response);
                            }

                            @Override
                            public void onFailure() {
                                MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_connect_failure));
                                sendResponse(response);
                            }
                        });
                    }
                    return false;
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

                        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
                        if (bluetoothManager != null) {
                            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                            if (bluetoothAdapter != null) {
                                if (!bluetoothAdapter.isEnabled()) {
                                    MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                                    return true;
                                }
                            } else {
                                MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                                return true;
                            }
                        } else {
                            MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_bluetooth_not_available));
                            return true;
                        }

                        if (switchBotDevice.getDeviceMode() == SwitchBotDevice.Mode.SWITCH) {
                            MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_mode_unmatched));
                            return true;
                        }

                        // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                        switchBotDevice.connect(new SwitchBotDevice.ConnectCallback() {
                            @Override
                            public void onSuccess() {
                                if (switchBotDevice.up()) {
                                    setResult(response, DConnectMessage.RESULT_OK);
                                } else {
                                    MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_device_busy));
                                }
                                sendResponse(response);
                            }

                            @Override
                            public void onFailure() {
                                MessageUtils.setIllegalDeviceStateError(response, context.getString(R.string.error_response_connect_failure));
                                sendResponse(response);
                            }
                        });
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public String getProfileName() {
        return "button";
    }
}