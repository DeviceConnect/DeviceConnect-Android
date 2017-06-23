package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IADT7410;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;

/**
 * ADT7410を操作するクラス.
 */
class ADT7410 extends BaseI2C implements IADT7410 {

    /**
     * ADT7410のアドレス.
     */
    private static final byte ADT7410_DEVICE_ADDR = 0x48;

    /**
     * ADT7410のコンフィグ用のレジスタ.
     */
    private static final int REGISTER_CONFIG = 0x03;

    /**
     * 16bitの分解度を定義.
     */
    private static final int BIT16_RESOLUTION = 0x80;
    private static final int BIT16_OP_MODE_1FAULT = 0x00;

    /**
     * 動作中フラグ.
     */
    private boolean mRunningFlag;

    /**
     * ADT7410のイベントを通知するリスナー.
     */
    private OnADT7410Listener mOnADT7410Listener;

    @Override
    public synchronized void start() {
        if (mRunningFlag) {
            return;
        }
        mRunningFlag = true;

        setI2CConfig();
        setADT7410();
        startRead(ADT7410_DEVICE_ADDR, REGISTER_CONFIG, 2);
    }

    @Override
    public synchronized void stop() {
        if (mRunningFlag) {
            stopRead(ADT7410_DEVICE_ADDR, REGISTER_CONFIG);
        }
        mRunningFlag = false;
    }

    @Override
    public void setOnADT7410Listener(final OnADT7410Listener listener) {
        mOnADT7410Listener = listener;
    }

    @Override
    byte getAddress() {
        return ADT7410_DEVICE_ADDR;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset++]);
        if (register == REGISTER_CONFIG) {
            int ax = decodeShort2(data, offset);
            if ((ax & 0x8000) != 0) {
                ax = ax - 65536;
            }
            double temp = ax / 128.0;

            if (mOnADT7410Listener != null) {
                mOnADT7410Listener.onData(temp);
            }
        }
    }

    /**
     * ADT7410の初期化を行います.
     */
    private void setADT7410() {
        write(ADT7410_DEVICE_ADDR, REGISTER_CONFIG, BIT16_RESOLUTION);
    }

    /**
     * 送られてきたデータをshortに変換して取得します.
     * <p>
     * 4byteをshortにします。
     * </p>
     * @param buffer 送られてきたデータ
     * @param startIndex 開始位置
     * @return shortデータ
     */
    private int decodeShort2(final byte[] buffer, final int startIndex) {
        int offset = startIndex;
        byte lsb = (byte) (decodeByte(buffer[offset++], buffer[offset++]) & 0xFF);
        byte msb = (byte) (decodeByte(buffer[offset++], buffer[offset]) & 0xFF);
        return (lsb << 8 | (msb & 0xFF));
    }
}
