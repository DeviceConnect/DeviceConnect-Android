package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IADT7410;

import java.io.IOException;

/**
 * ADT7410を操作するクラス.
 * <p>
 * Brick #207
 * </p>
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
     * ADT7410のデバイスID取得レジスタ.
     */
    private static final int DEVICE_REG = 0x0B;

    /**
     * ADT7410のデバイスID.
     */
    private static final int DEVICE_ID = 0x0C;

    /**
     * 16bitの分解度を定義.
     */
    private static final int BIT16_RESOLUTION = 0x80;

    /**
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;

    /**
     * 温度の値を保持するバッファ.
     */
    private byte[] mBuffer = new byte[2];

    /**
     * コンストラクタ.
     * @param control コントローラ
     */
    ADT7410(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(ADT7410_DEVICE_ADDR);
    }

    @Override
    public void read(final OnADT7410Listener listener) {
        if (!checkDevice()) {
            listener.onError("ADT7410 is not connect.");
        } else {
            try {
                setADT7410();
                readADT7410(mBuffer, mBuffer.length);
                listener.onData(convertTemperature(decodeUShort2(mBuffer, 0)));
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    @Override
    public void startRead(final OnADT7410Listener listener) {
    }

    @Override
    public void stopRead(final OnADT7410Listener listener) {
    }

    /**
     * 接続されているデバイスがADT7410か確認を行う.
     * @return ADT7410ならtrue、それ以外ならfalse
     */
    private boolean checkDevice() {
        if (mI2cDevice == null) {
            return false;
        } else {
            try {
                byte deviceId = mI2cDevice.readRegByte(DEVICE_REG);
                return (deviceId & DEVICE_ID) != 0;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * ADT7410の初期化を行います.
     */
    private void setADT7410() throws IOException {
        mI2cDevice.writeRegByte(REGISTER_CONFIG, (byte) BIT16_RESOLUTION);
    }

    /**
     * ADT7410から温度を読み込みます.
     * @param buffer バッファ
     * @param length サイズ
     * @throws IOException 読み込みに失敗した場合に発生
     */
    private void readADT7410(final byte[] buffer, final int length) throws IOException {
        mI2cDevice.readRegBuffer(REGISTER_CONFIG, buffer, length);
    }

    /**
     * センサーの値を摂氏に変換します.
     * @param value センサーの値
     * @return 摂氏
     */
    private double convertTemperature(int value) {
        if ((value & 0x8000) != 0) {
            value = value - 65536;
        }
        return value / 128.0;
    }
}
