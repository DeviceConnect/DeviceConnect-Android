package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.KeyboardCode;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

public class HOGPKeyboardProfile extends DConnectProfile {

    public HOGPKeyboardProfile() {

        // POST /keyboard
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                String modifier = (String) request.getExtras().get("modifier");
                Integer keyCode = parseInteger(request, "keyCode");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else {
                    sendKeyboard(modifier(modifier), keyCode.byteValue());
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/ascii
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "ascii";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String serviceId = (String) request.getExtras().get("serviceId");
                final String string = (String) request.getExtras().get("string");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (string == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "string is null.");
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < string.length(); i++) {
                                String a = String.valueOf(string.charAt(i));
                                byte modifier = KeyboardCode.modifier(a);
                                byte keyCode = KeyboardCode.keyCode(a);
                                sendKeyboard(modifier, keyCode);
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "keyboard";
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
     * キーボードイベントを送信します.
     * @param modifier モディファイアーキー
     * @param keyCode キーコード
     */
    private void sendKeyboard(final byte modifier, final byte keyCode) {
        HOGPServer server = getHOGPServer();
        server.sendKeyDown(modifier, keyCode);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.sendKeyUp();
    }

    /**
     * モディファイアーキーを取得します.
     * @param modifier APIの引数で指定されたモディファイアーキー
     * @return 変換されたモディファイアーキー
     */
    private byte modifier(final String modifier) {
        if (modifier == null) {
            return 0;
        }

        byte m = 0;
        String[] split = modifier.split(",");
        for (String s : split) {
            if ("ctrl".equals(s)) {
                m |= 1;
            } else if ("shift".equals(s)) {
                m |= 2;
            } else if ("alt".equals(s)) {
                m |= 4;
            } else if ("gui".equals(s)) {
                m |= 8;
            }
        }
        return m;
    }
}