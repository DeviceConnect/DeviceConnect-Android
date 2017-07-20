package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

final class FirmataUtil {

    private FirmataUtil() {
    }

    /**
     * 送られてきたデータをbyteに変換して取得します.
     * <p>
     * 2byteを1byteにします。
     * </p>
     * @param buffer データ
     * @return byteデータ
     */
    static int decodeByte(final byte[] buffer) {
        return decodeByte(buffer, 0);
    }

    /**
     * 送られてきたデータをbyteに変換して取得します.
     * <p>
     * 2byteを1byteにします。
     * </p>
     * @param buffer データ
     * @param startIndex オフセット
     * @return byteデータ
     */
    static int decodeByte(final byte[] buffer, final int startIndex) {
        int offset = startIndex;
        return decodeByte(buffer[offset++], buffer[offset]);
    }

    /**
     * 下位ビットと上位ビットを合わせてbyteデータに変換します.
     * @param lsb 下位ビット
     * @param msb 上位ビット
     * @return byteデータ
     */
    static int decodeByte(final int lsb, final int msb) {
        return (msb << 7) + lsb;
    }

    /**
     * 送られてきたデータを文字列に変換して取得します.
     * @param buffer 送られてきたデータ
     * @param startIndex 開始位置
     * @param endIndex 修了位置
     * @return 文字列
     */
    static String decodeString(final byte[] buffer, final int startIndex, final int endIndex) {
        StringBuilder sb = new StringBuilder();
        int offset = startIndex;
        int length = (endIndex - startIndex + 1) / 2;
        for (int i = 0; i < length; i++) {
            sb.append((char) decodeByte(buffer[offset++], buffer[offset++]));
        }
        return sb.toString();
    }

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
        int offset = startIndex;
        byte lsb = (byte) (decodeByte(buffer[offset++], buffer[offset++]) & 0xFF);
        byte msb = (byte) (decodeByte(buffer[offset++], buffer[offset]) & 0xFF);
        return ((msb << 8) | (lsb & 0xFF));
    }

    /**
     * 送られてきたデータをshortに変換して取得します.
     * <p>
     * 4byteをshortにします。
     * </p>
     * <p>
     * ビックエンディアン.
     * </p>
     * @param buffer 送られてきたデータ
     * @param startIndex 開始位置
     * @return shortデータ
     */
    static int decodeShort2(final byte[] buffer, final int startIndex) {
        int offset = startIndex;
        byte lsb = (byte) (decodeByte(buffer[offset++], buffer[offset++]) & 0xFF);
        byte msb = (byte) (decodeByte(buffer[offset++], buffer[offset]) & 0xFF);
        return (lsb << 8 | (msb & 0xFF));
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
        int offset = startIndex;
        byte lsb = (byte) (decodeByte(buffer[offset++], buffer[offset++]) & 0xFF);
        byte msb = (byte) (decodeByte(buffer[offset++], buffer[offset]) & 0xFF);
        return ((lsb & 0xFF) << 8 | (msb & 0xFF));
    }

    static int dataConv(final int data1, final int data2) {
        int value = data1 | (data2 << 8);
        if ((value & (1 << 16 - 1)) != 0) {
            value -= (1 << 16);
        }
        return value;
    }
}
