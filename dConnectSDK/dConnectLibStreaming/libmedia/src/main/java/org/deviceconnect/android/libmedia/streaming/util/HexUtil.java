package org.deviceconnect.android.libmedia.streaming.util;

public final class HexUtil {

    private HexUtil() {
    }

    public static String hexToString(byte[] data) {
        return hexToString(data, 0, data.length);
    }

    public static String hexToString(byte[] data, int length) {
        return hexToString(data, 0, length);
    }

    public static String hexToString(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < data.length; i++) {
            sb.append(String.format("%02X", (data[i] & 0xFF)));
        }
        return sb.toString();
    }
}
