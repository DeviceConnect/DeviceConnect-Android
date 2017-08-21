/*
 HOGPMouseProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Mouseプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPMouseProfile extends DConnectProfile {

    public HOGPMouseProfile() {

        // POST /mouse/absolute
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "absolute";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                Float x = parseFloat(request, "x");
                Float y = parseFloat(request, "y");
                Float wheel = parseFloat(request, "wheel");
                Boolean rightButton = parseBoolean(request, "rightButton");
                Boolean leftButton = parseBoolean(request, "leftButton");
                Boolean middleButton = parseBoolean(request, "middleButton");

                // TODO ここでAPIを実装してください. 以下はサンプルのレスポンス作成処理です.
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // POST /mouse/relative
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "relative";
            }
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                Float x = parseFloat(request, "x");
                Float y = parseFloat(request, "y");
                Float wheel = parseFloat(request, "wheel");
                Boolean rightButton = parseBoolean(request, "rightButton");
                Boolean leftButton = parseBoolean(request, "leftButton");
                Boolean middleButton = parseBoolean(request, "middleButton");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else {
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

                    int dx = (int) (x * 127);
                    int dy = (int) (y * 127);
                    int dw = (int) (wheel * 127);

                    server.movePointer(dx, dy, dw, leftButton, rightButton, middleButton);
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
                String serviceId = (String) request.getExtras().get("serviceId");
                String button = (String) request.getExtras().get("button");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else {
                    boolean leftButton = "left".equals(button);
                    boolean rightButton = "right".equals(button);
                    boolean middleButton = "middle".equals(button);
                    server.movePointer(0, 0, 0, leftButton, rightButton, middleButton);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    server.movePointer(0, 0, 0, false, false, false);
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
                String serviceId = (String) request.getExtras().get("serviceId");
                String button = (String) request.getExtras().get("button");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else {
                    boolean leftButton = "left".equals(button);
                    boolean rightButton = "right".equals(button);
                    boolean middleButton = "middle".equals(button);
                    server.movePointer(0, 0, 0, leftButton, rightButton, middleButton);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    server.movePointer(0, 0, 0, false, false, false);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    server.movePointer(0, 0, 0, leftButton, rightButton, middleButton);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    server.movePointer(0, 0, 0, false, false, false);

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
     * HOGPサーバを取得します.
     * @return HOGPサーバ
     */
    private HOGPServer getHOGPServer() {
        HOGPMessageService service = (HOGPMessageService) getContext();
        return (HOGPServer) service.getHOGPServer();
    }
}