package org.deviceconnect.android.deviceplugin.hogp.server;

import android.content.Context;

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

public class MouseHOGPServer extends HOGPServer {
    /**
     * Characteristic Data(Report Map)
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

    public MouseHOGPServer(final Context context) {
        super(context);
    }

    @Override
    byte[] getReportMap() {
        return REPORT_MAP;
    }


    @Override
    void onOutputReport(final byte[] outputReport) {
    }

    private final byte[] mLastSent = new byte[12];

    /**
     * Move the mouse pointer
     *
     * @param dx delta X (-127 .. +127)
     * @param dy delta Y (-127 .. +127)
     * @param wheel wheel (-127 .. +127)
     * @param leftButton true : sendButtons down
     * @param rightButton true : sendButtons down
     * @param middleButton true : sendButtons down
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

        if (mLastSent[0] == 0 && mLastSent[1] == 0 && mLastSent[2] == 0 && mLastSent[3] == 0 &&
                report[0] == 0 && report[1] == 0 && report[2] == 0 && report[3] == 0) {
            return;
        }
        mLastSent[0] = report[0];
        mLastSent[1] = report[1];
        mLastSent[2] = report[2];
        mLastSent[3] = report[3];
        addInputReport(report);
    }

    private static final byte[] EMPTY_REPORT = new byte[12];

    private static final int KEY_PACKET_MODIFIER_KEY_INDEX = 4;
    private static final int KEY_PACKET_KEY_INDEX = 6;

    /**
     * Send Key Down Event
     * @param modifier modifier key
     * @param keyCode key code
     */
    public void sendKeyDown(final byte modifier, final byte keyCode) {
        final byte[] report = new byte[12];
        report[KEY_PACKET_MODIFIER_KEY_INDEX] = modifier;
        report[KEY_PACKET_KEY_INDEX] = keyCode;
        addInputReport(report);
    }

    /**
     * Send Key Up Event
     */
    public void sendKeyUp() {
        addInputReport(EMPTY_REPORT);
    }

}
