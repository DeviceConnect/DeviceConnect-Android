/*
 HOGPMouseProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPSetting;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

/**
 * Mouseプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPMouseProfile extends DConnectProfile {

    public HOGPMouseProfile() {

        // GET /mouse
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (getSetting().getMouseMode() == HOGPServer.MouseMode.NONE) {
                    MessageUtils.setNotSupportAttributeError(response, "Mouse is not supported.");
                } else {
                    response.putExtra("mouse", createMouseInfo());
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /mouse
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Float x = parseFloat(request, "x");
                Float y = parseFloat(request, "y");
                Float wheel = parseFloat(request, "wheel");
                Boolean rightButton = parseBoolean(request, "rightButton");
                Boolean leftButton = parseBoolean(request, "leftButton");
                Boolean middleButton = parseBoolean(request, "middleButton");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (getSetting().getMouseMode() == HOGPServer.MouseMode.NONE) {
                    MessageUtils.setNotSupportAttributeError(response, "Mouse is not supported.");
                } else {
                    if (x == null) {
                        x = 0.0f;
                    }

                    if (y == null) {
                        y = 0.0f;
                    }

                    if (wheel == null) {
                        wheel = 0.0f;
                    }

                    if (rightButton == null) {
                        rightButton = false;
                    }

                    if (leftButton == null) {
                        leftButton = false;
                    }

                    if (middleButton == null) {
                        middleButton = false;
                    }

                    server.movePointer(getDevice(), x, y, wheel, leftButton, rightButton, middleButton);

                    response.putExtra("mouse", createMouseInfo());
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /mouse/click
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "click";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String button = (String) request.getExtras().get("button");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (getSetting().getMouseMode() == HOGPServer.MouseMode.NONE) {
                    MessageUtils.setNotSupportAttributeError(response, "Mouse is not supported.");
                } else if (!checkButton(button)) {
                    MessageUtils.setNotSupportAttributeError(response, "button is invalid.");
                } else {
                    BluetoothDevice device = getDevice();
                    boolean leftButton = "left".equals(button);
                    boolean rightButton = "right".equals(button);
                    boolean middleButton = "middle".equals(button);
                    server.movePointer(device, 0, 0, 0, leftButton, rightButton, middleButton);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    server.movePointer(device, 0, 0, 0, false, false, false);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /mouse/doubleClick
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "doubleClick";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String button = (String) request.getExtras().get("button");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (getSetting().getMouseMode() == HOGPServer.MouseMode.NONE) {
                    MessageUtils.setNotSupportAttributeError(response, "Mouse is not supported.");
                } else if (!checkButton(button)) {
                    MessageUtils.setNotSupportAttributeError(response, "button is invalid.");
                } else {
                    BluetoothDevice device = getDevice();
                    boolean leftButton = "left".equals(button);
                    boolean rightButton = "right".equals(button);
                    boolean middleButton = "middle".equals(button);
                    server.movePointer(device, 0, 0, 0, leftButton, rightButton, middleButton);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    server.movePointer(device, 0, 0, 0, false, false, false);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    server.movePointer(device, 0, 0, 0, leftButton, rightButton, middleButton);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    server.movePointer(device, 0, 0, 0, false, false, false);

                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });
    }

    @Override
    public String getProfileName() {
        return "mouse";
    }

    /**
     * マウスの情報を格納したBundleを取得します.
     * @return マウスの情報を格納したBundle
     */
    private Bundle createMouseInfo() {
        Bundle mouse = new Bundle();
        switch (getSetting().getMouseMode()) {
            case ABSOLUTE:
                mouse.putString("type", "absolute");
                break;
            case RELATIVE:
                mouse.putString("type", "relative");
                break;
        }
        return mouse;
    }

    /**
     * HOGPSettingのインスタンスを取得します.
     * @return インスタンス
     */
    private HOGPSetting getSetting() {
        HOGPMessageService service = (HOGPMessageService) getContext();
        return service.getHOGPSetting();
    }

    /**
     * HOGPサーバを取得します.
     * @return HOGPサーバ
     */
    private HOGPServer getHOGPServer() {
        HOGPMessageService service = (HOGPMessageService) getContext();
        return (HOGPServer) service.getHOGPServer();
    }

    /**
     * BluetoothDeviceを取得します.
     * <p>
     * BluetoothDeviceが取得できない場合はnullを返却します。
     * </p>
     * @return BluetoothDevice
     */
    private BluetoothDevice getDevice() {
        DConnectService service = getService();
        if (service instanceof HOGPService) {
            return ((HOGPService) service).getDevice();
        }
        return null;
    }

    /**
     * 指定されたbuttonがleft,right,middleに当てはまるか確認します.
     * @param button ボタン
     * @return 当てはまる場合はtrue、それ以外はfalse
     */
    private boolean checkButton(final String button) {
        boolean leftButton = "left".equals(button);
        boolean rightButton = "right".equals(button);
        boolean middleButton = "middle".equals(button);
        return leftButton | rightButton | middleButton;
    }
}