package org.deviceconnect.android.deviceplugin.hogp.util;

public final class HIDUtils {
    private static final int INPUT_CONSTANT = 1;
    private static final int INPUT_VARIABLE = (1 << 1);
    private static final int INPUT_RELATIVE = (1 << 2);
    private static final int INPUT_WRAP = (1 << 3);
    private static final int INPUT_NON_LINEAR = (1 << 4);
    private static final int INPUT_NO_PREFERRED = (1 << 5);
    private static final int INPUT_NULL_STATE = (1 << 6);
    private static final int INPUT_BUFFERED_BYTES = (1 << 8);

    private HIDUtils() {
    }

//    Input 1000 00 nn
//    Bit 0 {Data (0) | Constant (1)}
//    Bit 1 {Array (0) | Variable (1)}
//    Bit 2 {Absolute (0) | Relative (1)}
//    Bit 3 {No Wrap (0) | Wrap (1)}
//    Bit 4 {Linear (0) | Non Linear (1)}
//    Bit 5 {Preferred State (0) | No Preferred (1)}
//    Bit 6 {No Null position (0) | Null state(1)}
//    Bit 7 Reserved (0)
//    Bit 8 {Bit Field (0) | Buffered Bytes (1)}
    public static byte input(boolean constant, boolean variable, boolean relative, boolean wrap,
                             boolean nonLinear, boolean noPreferred, boolean nullState, boolean bufferedBytes) {
        byte result = 0;

        if (constant) {
            result |= INPUT_CONSTANT;
        }

        if (variable) {
            result |= INPUT_VARIABLE;
        }

        if (relative) {
            result |= INPUT_RELATIVE;
        }

        if (wrap) {
            result |= INPUT_WRAP;
        }

        if (nonLinear) {
            result |= INPUT_NON_LINEAR;
        }

        if (noPreferred) {
            result |= INPUT_NO_PREFERRED;
        }

        if (nullState) {
            result |= INPUT_NULL_STATE;
        }

        if (bufferedBytes) {
            result |= INPUT_BUFFERED_BYTES;
        }

        return result;
    }

    /**
     * Main items
     */
    public static byte INPUT(final int size) {
        return (byte) (0x80 | size);
    }

    public static byte OUTPUT(final int size) {
        return (byte) (0x90 | size);
    }

    public static byte COLLECTION(final int size) {
        return (byte) (0xA0 | size);
    }

    public static byte FEATURE(final int size) {
        return (byte) (0xB0 | size);
    }

    public static byte END_COLLECTION(final int size) {
        return (byte) (0xC0 | size);
    }

    /**
     * Global items
     */
    public static byte USAGE_PAGE(final int size) {
        return (byte) (0x04 | size);
    }

    public static byte LOGICAL_MINIMUM(final int size) {
        return (byte) (0x14 | size);
    }

    public static byte LOGICAL_MAXIMUM(final int size) {
        return (byte) (0x24 | size);
    }

    public static byte PHYSICAL_MINIMUM(final int size) {
        return (byte) (0x34 | size);
    }

    public static byte PHYSICAL_MAXIMUM(final int size) {
        return (byte) (0x44 | size);
    }

    public static byte UNIT_EXPONENT(final int size) {
        return (byte) (0x54 | size);
    }

    public static byte UNIT(final int size) {
        return (byte) (0x64 | size);
    }

    public static byte REPORT_SIZE(final int size) {
        return (byte) (0x74 | size);
    }

    public static byte REPORT_ID(final int size) {
        return (byte) (0x84 | size);
    }

    public static byte REPORT_COUNT(final int size) {
        return (byte) (0x94 | size);
    }

    /**
     * Local items
     */
    public static byte USAGE(final int size) {
        return (byte) (0x08 | size);
    }

    public static byte USAGE_MINIMUM(final int size) {
        return (byte) (0x18 | size);
    }

    public static byte USAGE_MAXIMUM(final int size) {
        return (byte) (0x28 | size);
    }

    public static byte LSB(final int value) {
        return (byte) (value & 0xff);
    }

    public static byte MSB(final int value) {
        return (byte) (value >> 8 & 0xff);
    }

}
