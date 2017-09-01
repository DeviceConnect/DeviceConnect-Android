/*
 HOGPServer.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.server;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.COLLECTION;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.END_COLLECTION;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.INPUT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.LOGICAL_MAXIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.LOGICAL_MINIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.LSB;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.MSB;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.OUTPUT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.PHYSICAL_MAXIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.PHYSICAL_MINIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.REPORT_COUNT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.REPORT_ID;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.REPORT_SIZE;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.UNIT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE_MAXIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE_MINIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE_PAGE;

/**
 * HOGPサーバ.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPServer extends AbstractHOGPServer {

    /**
     * マウスモード.
     */
    public enum MouseMode {
        /**
         * マウス無し.
         */
        NONE(0),

        /**
         * マウスのRelative入力モード.
         */
        RELATIVE(1),

        /**
         * マウスのAbsolute入力モード.
         */
        ABSOLUTE(2);

        /**
         * モードの値.
         */
        private int mValue;

        /**
         * コンストラクタ.
         * @param value モードの値
         */
        MouseMode(final int value) {
            mValue = value;
        }

        /**
         * 指定された値に対応するモードを取得します.
         * @param value 値
         * @return マウスモード
         */
        public static MouseMode valueOf(final int value) {
            for (MouseMode mode : values()) {
                if (mode.getValue() == value) {
                    return mode;
                }
            }
            return null;
        }

        /**
         * モードの値を取得します.
         * @return モードの値
         */
        public int getValue() {
            return mValue;
        }
    }

    /**
     * マウスのサイズ.
     */
    public static final int ABSOLUTE_MOUSE_SIZE = 32767;


    /**
     * マウスのレポートID.
     */
    private static byte REPORT_ID_MOUSE = 0x01;

    /**
     * ジョイスティックのレポートID.
     */
    private static byte REPORT_ID_JOYSTICK = 0x02;

    /**
     * キーボードのレポートID.
     */
    private static byte REPORT_ID_KEYBOARD = 0x03;

    /**
     * レポートの先頭につけるヘッダ.
     */
    private static final byte[] REPORT_MAP_HEADER = {
            USAGE_PAGE(1),      0x01,             // Generic Desktop
    };

    /**
     * Relativeマウス用のレポート定義マップ.
     */
    private static final byte[] REPORT_MAP_RELATIVE_MOUSE = {
            USAGE(1),           0x02,             // Mouse
            COLLECTION(1),      0x01,             // Application
            REPORT_ID(1),       REPORT_ID_MOUSE,  //  Report Id
            USAGE(1),           0x01,             //  Pointer
            COLLECTION(1),      0x00,             //  Physical
            USAGE_PAGE(1),      0x09,             //   Buttons
            USAGE_MINIMUM(1),   0x01,
            USAGE_MAXIMUM(1),   0x03,
            LOGICAL_MINIMUM(1), 0x00,             //   0
            LOGICAL_MAXIMUM(1), 0x01,             //   1
            REPORT_COUNT(1),    0x03,             //   3 bits (Buttons)
            REPORT_SIZE(1),     0x01,
            INPUT(1),           0x02,             //   Data, Variable, Absolute

            REPORT_COUNT(1),    0x01,             //   5 bits (Padding)
            REPORT_SIZE(1),     0x05,
            INPUT(1),           0x01,             //   Constant

            USAGE_PAGE(1),      0x01,             //   Generic Desktop
            USAGE(1),           0x30,             //   X
            USAGE(1),           0x31,             //   Y
            USAGE(1),           0x38,             //   Wheel
            LOGICAL_MINIMUM(1), (byte) 0x81,      //   -127
            LOGICAL_MAXIMUM(1), 0x7F,             //   127
            REPORT_SIZE(1),     0x08,             //   8 bits
            REPORT_COUNT(1),    0x03,             //   3 x 8 bits = 3 bytes
            INPUT(1),           0x06,             //   Data, Variable, Relative
            END_COLLECTION(0),
            END_COLLECTION(0),
    };

    /**
     * Absoluteマウス用のレポート定義マップ.
     */
    private static final byte[] REPORT_MAP_ABSOLUTE_MOUSE = {
            USAGE(1),           0x02,             // Mouse
            COLLECTION(1),      0x01,             // Application
            REPORT_ID(1),       REPORT_ID_MOUSE,  //  Report Id
            USAGE(1),           0x01,             //  Pointer
            COLLECTION(1),      0x00,             //  Physical
            USAGE_PAGE(1),      0x09,             //   Buttons
            USAGE_MINIMUM(1),   0x01,
            USAGE_MAXIMUM(1),   0x03,
            LOGICAL_MINIMUM(1), 0x00,             //   0
            LOGICAL_MAXIMUM(1), 0x01,             //   1
            REPORT_COUNT(1),    0x03,             //   3 bits (Buttons)
            REPORT_SIZE(1),     0x01,
            INPUT(1),           0x02,             //   Data, Variable, Absolute

            REPORT_COUNT(1),    0x01,             //   5 bits (Padding)
            REPORT_SIZE(1),     0x05,
            INPUT(1),           0x01,             //   Constant

            USAGE_PAGE(1),      0x01,             //   Generic Desktop
            USAGE(1),           0x30,             //    X
            USAGE(1),           0x31,             //    Y
            LOGICAL_MINIMUM(1), 0x00,             //    0
            LOGICAL_MAXIMUM(2), (byte)0xFF, 0x7F, //    32767
            REPORT_SIZE(1),     0x10,
            REPORT_COUNT(1),    0x02,
            INPUT(1),           0x02,             //    Data, Variable, Absolute

            USAGE_PAGE(1),      0x01,             //   Generic Desktop
            USAGE(1),           0x38,             //    scroll
            LOGICAL_MINIMUM(1), (byte)0x81,       //    -127
            LOGICAL_MAXIMUM(1), 0x7f,             //     127
            REPORT_SIZE(1),     0x08,
            REPORT_COUNT(1),    0x01,
            INPUT(1),           0x06,             //     Data, Variable, Relative

            END_COLLECTION(0),
            END_COLLECTION(0)
    };

    /**
     * ジョイスティック用のレポート定義マップ.
     */
    private static final byte[] REPORT_MAP_JOYSTICK = {
            USAGE(1),           0x04,             // Joystick
            COLLECTION(1),      0x01,             // Application
            COLLECTION(1),      0x00,             //  Physical
            REPORT_ID(1),       REPORT_ID_JOYSTICK,
            USAGE_PAGE(1),      0x09,             //   Buttons
            USAGE_MINIMUM(1),   0x01,
            USAGE_MAXIMUM(1),   0x03,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x01,
            REPORT_COUNT(1),    0x03,              //   3 bits (Buttons)
            REPORT_SIZE(1),     0x01,
            INPUT(1),           0x02,              //   Data, Variable, Absolute

            REPORT_COUNT(1),    0x01,              //   5 bits (Padding)
            REPORT_SIZE(1),     0x05,
            INPUT(1),           0x01,              //   Constant

            USAGE_PAGE(1),      0x01,              //   Generic Desktop
            UNIT(1),            0x14,              //   Unit (Degrees)
            USAGE(1),           0x30,              //   X
            USAGE(1),           0x31,              //   Y
            USAGE(1),           0x32,              //   Z
            USAGE(1),           0x33,              //   Rx
            USAGE(1),           0x34,              //   Ry
            USAGE(1),           0x35,              //   Rz
            LOGICAL_MINIMUM(1), (byte) 0x81,       //   -127
            LOGICAL_MAXIMUM(1), 0x7F,              //   127
            PHYSICAL_MINIMUM(1),(byte) 0x81,       //   -127
            PHYSICAL_MAXIMUM(1),0x7F,              //   127
            REPORT_SIZE(1),     0x08,              //   8 bits
            REPORT_COUNT(1),    0x04,              //   4 x 8 bits = 4 bytes
            INPUT(1),           0x02,

            END_COLLECTION(0),
            END_COLLECTION(0),
    };

    /**
     * キーボード用のレポート定義マップ.
     */
    private static final byte[] REPORT_MAP_KEYBOARD = {
            USAGE(1),           0x06,                 // Keyboard
            COLLECTION(1),      0x01,                 // Application
            USAGE_PAGE(1),      0x07,                 // keyboard/Keypad
            REPORT_ID(1),       REPORT_ID_KEYBOARD,   //   Report Id
            USAGE_MINIMUM(1),   (byte) 0xE0,
            USAGE_MAXIMUM(1),   (byte) 0xE7,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x01,
            REPORT_SIZE(1),     0x01,                 //   1 byte (Modifier)
            REPORT_COUNT(1),    0x08,
            INPUT(1),           0x02,                 //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position

            REPORT_COUNT(1),    0x01,                 //   1 byte (Reserved)
            REPORT_SIZE(1),     0x08,
            INPUT(1),           0x01,                 //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position

            REPORT_COUNT(1),    0x05,                 //   5 bits (Num lock, Caps lock, Scroll lock, Compose, Kana)
            REPORT_SIZE(1),     0x01,
            USAGE_PAGE(1),      0x08,                 //   LEDs
            USAGE_MINIMUM(1),   0x01,                 //   Num Lock
            USAGE_MAXIMUM(1),   0x05,                 //   Kana
            OUTPUT(1),          0x02,                 //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile

            REPORT_COUNT(1),    0x01,                 //   3 bits (Padding)
            REPORT_SIZE(1),     0x03,
            OUTPUT(1),          0x01,                 //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile

            REPORT_COUNT(1),    0x06,                 //   6 bytes (Keys)
            REPORT_SIZE(1),     0x08,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), (byte) 0xFF,          //   255 keys
            USAGE_PAGE(1),      0x07,                 //   keyboard/Keypad
            USAGE_MINIMUM(1),   0x00,
            USAGE_MAXIMUM(1),   0x65,
            INPUT(1),           0x00,                 //   Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position

            END_COLLECTION(0),
    };

    /**
     * 空のレポート.
     */
    private static final byte[] EMPTY_REPORT = new byte[9];

    /**
     * 前回送ったマウス用レポート.
     */
    private byte[] mLastMouseReport = new byte[5];

    // 初期化
    static {
        EMPTY_REPORT[0] = REPORT_ID_KEYBOARD;
    }

    /**
     * マウスモード.
     */
    private MouseMode mMouseMode = MouseMode.NONE;

    /**
     * キーボードフラグ.
     */
    private boolean mKeyboard;

    /**
     * ジョイスティックフラグ.
     */
    private boolean mJoystick;

    /**
     * コンストラクタ.
     * @param context このクラスが属するコンテキスト
     * @param mode マウスモード
     * @param keyboard キーボードの有効・無効
     * @param joystick ジョイスティックの有効・無効
     */
    public HOGPServer(final Context context, final MouseMode mode, final boolean keyboard, final boolean joystick) {
        super(context);

        mMouseMode = mode;
        mKeyboard = keyboard;
        mJoystick = joystick;
    }

    @Override
    byte[] getReportMap() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(REPORT_MAP_HEADER);

            switch (mMouseMode) {
                case RELATIVE:
                    out.write(REPORT_MAP_RELATIVE_MOUSE);
                    break;
                case ABSOLUTE:
                    out.write(REPORT_MAP_ABSOLUTE_MOUSE);
                    break;
            }

            if (mJoystick) {
                out.write(REPORT_MAP_JOYSTICK);
            }

            if (mKeyboard) {
                out.write(REPORT_MAP_KEYBOARD);
            }

            return out.toByteArray();
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e("HOGP", "Failed to write report map.", e);
            }
            return null;
        }
    }

    @Override
    void onOutputReport(final byte[] outputReport) {
        if (BuildConfig.DEBUG) {
            String t = "";
            for (byte report : outputReport) {
                t += String.format("%02x", report);
            }
            Log.i("HOGP", t);
        }
    }

    /**
     * マウスの入力モードを取得します.
     * @return マウスの入力モード
     */
    public MouseMode getMouseMode() {
        return mMouseMode;
    }

    /**
     * 指定された配列が0で埋め尽くされているか確認します.
     * @param array 配列
     * @return 全ての要素が0の場合はtrue、それ以外はfalse
     */
    private boolean isZero(final byte[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mouseのレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param dx X座標の移動量 (-1 〜 1)
     * @param dy Y座標の移動量 (-1 〜 1)
     * @param wheel wheelの移動量 (-1 〜 1)
     * @param leftButton 左ボタンが押下されている場合はtrue
     * @param rightButton 右ボタンが押下されている場合はtrue
     * @param middleButton 真ん中ボタンが押下されている場合はtrue
     */
    public void movePointer(final BluetoothDevice device, float dx, float dy, float wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {
        switch (mMouseMode) {
            case RELATIVE: {
                int x = (int) (127 * dx);
                int y = (int) (127 * dy);
                int w = (int) (127 * wheel);
                moveRelativePointer(device, x, y, w, leftButton, rightButton, middleButton);
            }   break;
            case ABSOLUTE: {
                int x = (int) (ABSOLUTE_MOUSE_SIZE * dx);
                int y = (int) (ABSOLUTE_MOUSE_SIZE * dy);
                int w = (int) (127 * wheel);
                moveAbsolutePointer(device, x, y, w, leftButton, rightButton, middleButton);
            }   break;
        }
    }

    /**
     * Mouseのレポートを送信します.
     * <p>
     * 接続されている全デバイスにレポートを送信します。
     * </p>
     * @param dx X座標の移動量 (-127 〜 127)
     * @param dy Y座標の移動量 (-127 〜 127)
     * @param wheel wheelの移動量 (-127 〜 127)
     * @param leftButton 左ボタンが押下されている場合はtrue
     * @param rightButton 右ボタンが押下されている場合はtrue
     * @param middleButton 真ん中ボタンが押下されている場合はtrue
     */
    public void movePointer(int dx, int dy, int wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {
        movePointer(null, dx, dy, wheel, leftButton, rightButton, middleButton);
    }

    /**
     * Mouseのレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param dx X座標の移動量 (-127 〜 127)
     * @param dy Y座標の移動量 (-127 〜 127)
     * @param wheel wheelの移動量 (-127 〜 127)
     * @param leftButton 左ボタンが押下されている場合はtrue
     * @param rightButton 右ボタンが押下されている場合はtrue
     * @param middleButton 真ん中ボタンが押下されている場合はtrue
     */
    public void movePointer(final BluetoothDevice device, int dx, int dy, int wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {
        switch (mMouseMode) {
            case RELATIVE:
                moveRelativePointer(device, dx, dy, wheel, leftButton, rightButton, middleButton);
                break;
            case ABSOLUTE:
                moveAbsolutePointer(device, dx, dy, wheel, leftButton, rightButton, middleButton);
                break;
        }
    }

    /**
     * Relative入力モードのMouseレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param dx X座標の移動量 (-127 〜 127)
     * @param dy Y座標の移動量 (-127 〜 127)
     * @param wheel wheelの移動量 (-127 〜 127)
     * @param leftButton 左ボタンが押下されている場合はtrue
     * @param rightButton 右ボタンが押下されている場合はtrue
     * @param middleButton 真ん中ボタンが押下されている場合はtrue
     */
    private void moveRelativePointer(final BluetoothDevice device, int dx, int dy, int wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {
        if (dx > 127) dx = 127;
        if (dx < -127) dx = -127;
        if (dy > 127) dy = 127;
        if (dy < -127) dy = -127;
        if (wheel > 127) wheel = 127;
        if (wheel < -127) wheel = -127;

        byte button = 0;
        if (leftButton) {
            button |= 1;
        }
        if (rightButton) {
            button |= 2;
        }
        if (middleButton) {
            button |= 4;
        }

        final byte[] report = new byte[5];
        report[0] = REPORT_ID_MOUSE;
        report[1] = (byte) (button & 7);
        report[2] = (byte) dx;
        report[3] = (byte) dy;
        report[4] = (byte) wheel;

        if (isZero(mLastMouseReport) && isZero(report)) {
            return;
        }

        addInputReport(device, report);

        mLastMouseReport[0] = report[0];
        mLastMouseReport[1] = report[1];
        mLastMouseReport[2] = report[2];
        mLastMouseReport[3] = report[3];
        mLastMouseReport[4] = report[4];
    }

    /**
     * Absolute入力モードのMouseレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param dx X座標の移動量 (-127 〜 127)
     * @param dy Y座標の移動量 (-127 〜 127)
     * @param wheel wheelの移動量 (-127 〜 127)
     * @param leftButton 左ボタンが押下されている場合はtrue
     * @param rightButton 右ボタンが押下されている場合はtrue
     * @param middleButton 真ん中ボタンが押下されている場合はtrue
     */
    private void moveAbsolutePointer(final BluetoothDevice device, int dx, int dy, int wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {
        if (dx > ABSOLUTE_MOUSE_SIZE) dx = ABSOLUTE_MOUSE_SIZE;
        if (dx < 0) dx = 0;
        if (dy > ABSOLUTE_MOUSE_SIZE) dy = ABSOLUTE_MOUSE_SIZE;
        if (dy < 0) dy = 0;
        if (wheel > 127) wheel = 127;
        if (wheel < -127) wheel = -127;

        byte button = 0;
        if (leftButton) {
            button |= 1;
        }
        if (rightButton) {
            button |= 2;
        }
        if (middleButton) {
            button |= 4;
        }

        final byte[] report = new byte[7];
        report[0] = REPORT_ID_MOUSE;
        report[1] = (byte) (button & 7);
        report[2] = LSB(dx);
        report[3] = MSB(dx);
        report[4] = LSB(dy);
        report[5] = MSB(dy);
        report[6] = (byte) wheel;

        addInputReport(device, report);
    }


    /**
     * キーダウンのレポートを送信します.
     *
     * @param modifier モディファイアキー
     * @param keyCode キーコード
     */
    public void sendKeyDown(final byte modifier, final byte keyCode) {
        sendKeyDown(null, modifier, keyCode);
    }

    /**
     * キーダウンのレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param modifier モディファイアキー
     * @param keyCode キーコード
     */
    public void sendKeyDown(final BluetoothDevice device, final byte modifier, final byte keyCode) {
        final byte[] report = new byte[9];
        report[0] = REPORT_ID_KEYBOARD;
        report[1] = modifier;
        report[2] = 0x00; // Reserved
        report[3] = keyCode;
        addInputReport(device, report);
    }

    /**
     * キーダウンのレポートを送信します.
     *
     * @param modifier モディファイアキー
     * @param keyCode キーコード
     */
    public void sendKeyDown(final byte modifier, final byte[] keyCode) {
        sendKeyDown(null, modifier, keyCode);
    }

    /**
     * キーダウンのレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param modifier モディファイアキー
     * @param keyCode キーコード
     */
    public void sendKeyDown(final BluetoothDevice device, final byte modifier, final byte[] keyCode) {
        final byte[] report = new byte[9];
        report[0] = REPORT_ID_KEYBOARD;
        report[1] = modifier;
        report[2] = 0x00; // Reserved
        for (int i = 0; i  < keyCode.length && 3 + i < report.length; i++) {
            report[3 + i] = keyCode[i];
        }
        addInputReport(device, report);
    }

    /**
     * キーアップのレポートを送信します.
     */
    public void sendKeyUp() {
        sendKeyUp(null);
    }

    /**
     * キーアップのレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     */
    public void sendKeyUp(final BluetoothDevice device) {
        addInputReport(device, EMPTY_REPORT);
    }

    /**
     * ジョイスティックのレポートを送信します.
     * @param dx x軸への移動量
     * @param dy y軸への移動量
     * @param dz z軸への移動量
     * @param rx x軸の回転量
     * @param ry y軸の回転量
     * @param rz z軸の回転量
     */
    public void sendJoystick(int dx, int dy, int dz, int rx, int ry, int rz) {
        sendJoystick(null, dx, dy, dz, rx, ry, rz);
    }

    /**
     * ジョイスティックのレポートを送信します.
     * <p>
     *     deviceがnullの場合には、全てのデバイスに対して送信します.
     * </p>
     * @param device 送信するBluetoothDevice
     * @param dx x軸への移動量
     * @param dy y軸への移動量
     * @param dz z軸への移動量
     * @param rx x軸の回転量
     * @param ry y軸の回転量
     * @param rz z軸の回転量
     */
    public void sendJoystick(final BluetoothDevice device, int dx, int dy, int dz, int rx, int ry, int rz) {
        if (dx > 127) dx = 127;
        if (dx < -127) dx = -127;
        if (dy > 127) dy = 127;
        if (dy < -127) dy = -127;
        if (dz > 127) dz = 127;
        if (dz < -127) dz = -127;

        final byte[] report = new byte[8];
        report[0] = REPORT_ID_JOYSTICK;
        report[1] = 0x00;
        report[2] = (byte) dx;
        report[3] = (byte) dy;
        report[4] = (byte) dz;
        report[5] = (byte) rx;
        report[6] = (byte) ry;
        report[7] = (byte) rz;

        addInputReport(device, report);
    }
}
