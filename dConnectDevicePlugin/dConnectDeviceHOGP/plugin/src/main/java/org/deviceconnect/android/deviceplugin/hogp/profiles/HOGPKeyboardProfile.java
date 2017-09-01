/*
 HOGPKeyboardProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPSetting;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.KeyboardCode;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

/**
 * Keyboardプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPKeyboardProfile extends DConnectProfile {

    /**
     * コンストラクタ.
     */
    public HOGPKeyboardProfile() {
        // POST /keyboard
        addApi(new PostApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                Byte modifier = modifier((String) request.getExtras().get("modifier"));
                byte[] keyCode = getKeyCodes(request, "keyCode");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else if (keyCode == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "keyCode is invalid.");
                } else if (modifier == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "modifier is invalid.");
                } else {
                    sendKeyboard(modifier, keyCode);
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
                final String string = (String) request.getExtras().get("string");

                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else if (string == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "string is null.");
                } else if (string.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "string is empty.");
                } else if (string.matches("^.*[^\\p{ASCII}].*")) {
                    MessageUtils.setInvalidRequestParameterError(response, "Not ascii character contains.");
                } else {
                    for (int i = 0; i < string.length(); i++) {
                        String a = String.valueOf(string.charAt(i));
                        byte modifier = KeyboardCode.modifier(a);
                        byte keyCode = KeyboardCode.keyCode(a);
                        sendKeyboard(modifier, keyCode);
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/del
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "del";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_DEL);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/downArrow
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "downArrow";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_DOWN_ARROW);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/enter
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "enter";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_ENTER);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/esc
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "esc";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_ESC);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/leftArrow
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "leftArrow";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_LEFT_ARROW);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/rightArrow
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "rightArrow";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_RIGHT_ARROW);
                    setResult(response, DConnectMessage.RESULT_OK);
                }
                return true;
            }
        });

        // POST /keyboard/upArrow
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "upArrow";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                HOGPServer server = getHOGPServer();
                if (server == null) {
                    MessageUtils.setIllegalDeviceStateError(response, "HOGP server is not running.");
                } else if (!getSetting().isEnabledKeyboard()) {
                    MessageUtils.setNotSupportProfileError(response, "Keyboard is not supported.");
                } else {
                    sendKeyboard((byte) 0, KeyboardCode.KEY_UP_ARROW);
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
     * リクエストからキーコードの配列を取得します.
     * @param request リクエスト
     * @param name パラメータ名
     * @return キーコードの配列
     */
    private byte[] getKeyCodes(final Intent request, final String name) {
        String value = (String) request.getExtras().get(name);
        if (value == null) {
            return null;
        }
        try {
            String[] array = value.split(",");
            if (array.length > 6) {
                return null;
            }
            byte[] keyCodes = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                keyCodes[i] = getKeyCode(array[i].trim());
            }
            return keyCodes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文字列からキーコードを取得します.
     * @param value キーコード
     * @return キーコード
     */
    private Byte getKeyCode(final String value) {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("value is invalid.");
        }
        if (value.startsWith("0x")) {
            return Byte.decode(value);
        }
        return  Byte.parseByte(value);
    }

    /**
     * キーボードイベントを送信します.
     * @param modifier モディファイアーキー
     * @param keyCode キーコード
     */
    private void sendKeyboard(final byte modifier, final byte keyCode) {
        HOGPServer server = getHOGPServer();
        server.sendKeyDown(getDevice(), modifier, keyCode);
        server.sendKeyUp(getDevice());
    }

    /**
     * キーボードイベントを送信します.
     * @param modifier モディファイアーキー
     * @param keyCode キーコード
     */
    private void sendKeyboard(final byte modifier, final byte[] keyCode) {
        HOGPServer server = getHOGPServer();
        server.sendKeyDown(getDevice(), modifier, keyCode);
        server.sendKeyUp(getDevice());
    }

    /**
     * モディファイアーキーを取得します.
     * @param modifier APIの引数で指定されたモディファイアーキー
     * @return 変換されたモディファイアーキー
     */
    private Byte modifier(final String modifier) {
        if (modifier == null) {
            return KeyboardCode.MODIFIER_KEY_NONE;
        }

        byte m = KeyboardCode.MODIFIER_KEY_NONE;
        String[] split = modifier.split(",");
        for (String s : split) {
            if ("ctrl".equals(s)) {
                m |= KeyboardCode.MODIFIER_KEY_CTRL;
            } else if ("shift".equals(s)) {
                m |= KeyboardCode.MODIFIER_KEY_SHIFT;
            } else if ("alt".equals(s)) {
                m |= KeyboardCode.MODIFIER_KEY_ALT;
            } else if ("gui".equals(s)) {
                m |= KeyboardCode.MODIFIER_KEY_GUI;
            } else {
                return null;
            }
        }
        return m;
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
}