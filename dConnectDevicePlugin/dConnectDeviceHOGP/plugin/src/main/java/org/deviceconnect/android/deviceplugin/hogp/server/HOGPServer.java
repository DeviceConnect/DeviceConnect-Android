package org.deviceconnect.android.deviceplugin.hogp.server;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;

import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.COLLECTION;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.END_COLLECTION;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.INPUT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.LOGICAL_MAXIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.LOGICAL_MINIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.OUTPUT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.REPORT_COUNT;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.REPORT_SIZE;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE_MAXIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE_MINIMUM;
import static org.deviceconnect.android.deviceplugin.hogp.util.HIDUtils.USAGE_PAGE;

public class HOGPServer extends AbstractHOGPServer {
    /**
     * マウスとキーボード用のレポート定義マップ.
     */
    private static final byte[] REPORT_MAP = {
            USAGE_PAGE(1),      0x01,         // Generic Desktop
            USAGE(1),           0x02,         // Mouse
            COLLECTION(1),      0x01,         // Application
            USAGE(1),           0x01,         //  Pointer
            COLLECTION(1),      0x00,         //  Physical
            USAGE_PAGE(1),      0x09,         //   Buttons
            USAGE_MINIMUM(1),   0x01,
            USAGE_MAXIMUM(1),   0x03,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x01,
            REPORT_COUNT(1),    0x03,         //   3 bits (Buttons)
            REPORT_SIZE(1),     0x01,
            INPUT(1),           0x02,         //   Data, Variable, Absolute

            REPORT_COUNT(1),    0x01,         //   5 bits (Padding)
            REPORT_SIZE(1),     0x05,
            INPUT(1),           0x01,         //   Constant

            USAGE_PAGE(1),      0x01,         //   Generic Desktop
            USAGE(1),           0x30,         //   X
            USAGE(1),           0x31,         //   Y
            USAGE(1),           0x38,         //   Wheel
            LOGICAL_MINIMUM(1), (byte) 0x81,  //   -127
            LOGICAL_MAXIMUM(1), 0x7f,         //   127
            REPORT_SIZE(1),     0x08,         //   Three bytes
            REPORT_COUNT(1),    0x03,
            INPUT(1),           0x06,         //   Data, Variable, Relative
            END_COLLECTION(0),
            END_COLLECTION(0),


            COLLECTION(1),      0x01,       // Application
            USAGE_PAGE(1),      0x07,       //   keyboard/Keypad
            USAGE_MINIMUM(1), (byte) 0xE0,
            USAGE_MAXIMUM(1), (byte) 0xE7,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x01,
            REPORT_SIZE(1),     0x01,       //   1 byte (Modifier)
            REPORT_COUNT(1),    0x08,
            INPUT(1),           0x02,       //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position

            REPORT_COUNT(1),    0x01,       //   1 byte (Reserved)
            REPORT_SIZE(1),     0x08,
            INPUT(1),           0x01,       //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position

            REPORT_COUNT(1),    0x05,       //   5 bits (Num lock, Caps lock, Scroll lock, Compose, Kana)
            REPORT_SIZE(1),     0x01,
            USAGE_PAGE(1),      0x08,       //   LEDs
            USAGE_MINIMUM(1),   0x01,       //   Num Lock
            USAGE_MAXIMUM(1),   0x05,       //   Kana
            OUTPUT(1),          0x02,       //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile

            REPORT_COUNT(1),    0x01,       //   3 bits (Padding)
            REPORT_SIZE(1),     0x03,
            OUTPUT(1),          0x01,       //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile

            REPORT_COUNT(1),    0x06,       //   6 bytes (Keys)
            REPORT_SIZE(1),     0x08,
            LOGICAL_MINIMUM(1), 0x00,
            LOGICAL_MAXIMUM(1), 0x65,       //   101 keys
            USAGE_PAGE(1),      0x07,       //   keyboard/Keypad
            USAGE_MINIMUM(1),   0x00,
            USAGE_MAXIMUM(1),   0x65,
            INPUT(1),           0x00,       //   Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position

            END_COLLECTION(0),
    };

    /**
     * キーボード用レポートのModifierのインデックス.
     */
    private static final int KEY_PACKET_MODIFIER_KEY_INDEX = 4;

    /**
     * キーボード用レポートのKeyCodeのインデックス.
     */
    private static final int KEY_PACKET_KEY_INDEX = 6;

    /**
     * 空のレポート.
     */
    private static final byte[] EMPTY_REPORT = new byte[12];

    /**
     * 前回送ったマウス用レポート.
     */
    private byte[] mLastMouseReport = new byte[12];

    /**
     * コンストラクタ.
     * @param context このクラスが属するコンテキスト
     */
    public HOGPServer(final Context context) {
        super(context);
    }

    @Override
    byte[] getReportMap() {
        return REPORT_MAP;
    }


    @Override
    void onOutputReport(final byte[] outputReport) {
        if (BuildConfig.DEBUG) {
            String t = "";
            for (int i = 0; i < outputReport.length; i++) {
                t += String.format("%02x", outputReport[i]);
            }
            Log.i("HOGP", t);
        }
    }

    /**
     * 指定された配列が0で埋め尽くされているか確認します.
     * @param array 配列
     * @return 全ての要素が0の場合はtrue、それ以外はfalse
     */
    private boolean isZero(final byte[] array) {
        for (int a : array) {
            if (a != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mouseのレポートを送信します.
     *
     * @param dx X座標の移動量 (-127 〜 127)
     * @param dy Y座標の移動量 (-127 〜 127)
     * @param wheel wheelの移動量 (-127 〜 127)
     * @param leftButton 左ボタンが押下されている場合はtrue
     * @param rightButton 右ボタンが押下されている場合はtrue
     * @param middleButton 真ん中ボタンが押下されている場合はtrue
     */
    public void movePointer(int dx, int dy, int wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {
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

        final byte[] report = new byte[12];
        report[0] = (byte) (button & 7);
        report[1] = (byte) dx;
        report[2] = (byte) dy;
        report[3] = (byte) wheel;

        if (isZero(mLastMouseReport) && isZero(report)) {
            return;
        }

        addInputReport(report);

        mLastMouseReport[0] = report[0];
        mLastMouseReport[1] = report[1];
        mLastMouseReport[2] = report[2];
        mLastMouseReport[3] = report[3];
    }

    /**
     * キーダウンのレポートを送信します.
     *
     * @param modifier モディファイアキー
     * @param keyCode キーコード
     */
    public void sendKeyDown(final byte modifier, final byte keyCode) {
        final byte[] report = new byte[12];
        report[KEY_PACKET_MODIFIER_KEY_INDEX] = modifier;
        report[KEY_PACKET_KEY_INDEX] = keyCode;
        addInputReport(report);
    }

    /**
     * キーアップのレポートを送信します.
     */
    public void sendKeyUp() {
        addInputReport(EMPTY_REPORT);
    }
}
