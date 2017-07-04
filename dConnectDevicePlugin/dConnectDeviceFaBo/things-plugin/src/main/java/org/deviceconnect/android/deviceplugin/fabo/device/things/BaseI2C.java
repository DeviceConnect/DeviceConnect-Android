package org.deviceconnect.android.deviceplugin.fabo.device.things;

abstract class BaseI2C {

    void destroy() {}

    /**
     * 送られてきたデータをshortに変換して取得します.
     * <p>
     * 4byteをshortにします。
     * </p>
     * <p>
     * リトルエンディアン.
     * </p>
     * @param buffer 送られてきたデータ
     * @param startIndex 開始位置
     * @return shortデータ
     */
    static int decodeShort(final byte[] buffer, final int startIndex) {
        byte lsb = (byte) (buffer[startIndex] & 0xFF);
        byte msb = (byte) (buffer[startIndex + 1] & 0xFF);
        return ((msb << 8) | (lsb & 0xFF));
    }

    /**
     * 送られてきたデータをshortに変換して取得します.
     * <p>
     * 4byteをunsigned shortにします。
     * </p>
     * <p>
     * ビックエンディアン.
     * </p>
     * @param buffer 送られてきたデータ
     * @param startIndex 開始位置
     * @return shortデータ
     */
    static int decodeUShort2(final byte[] buffer, final int startIndex) {
        byte lsb = (byte) (buffer[startIndex] & 0xFF);
        byte msb = (byte) (buffer[startIndex + 1] & 0xFF);
        return ((lsb & 0xFF) << 8 | (msb & 0xFF));
    }

    static int dataConv(int data1, int data2) {
        int value = (data1 & 0xFF) | ((data2 & 0xFF) << 8);
        if ((value & 0x8000) != 0) {
            value -= (1 << 16);
        }
        return value;
    }
}
