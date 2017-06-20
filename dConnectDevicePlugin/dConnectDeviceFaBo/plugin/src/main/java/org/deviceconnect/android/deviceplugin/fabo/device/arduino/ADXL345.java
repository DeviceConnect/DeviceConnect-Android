package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IADXL345;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeShort;

class ADXL345 extends BaseI2C implements IADXL345 {

    /**
     * ADXL345のアドレス.
     */
    private static final byte ADXL345_DEVICE_ADDR = 0x53;

    /**
     * ADXLのレジスタ.
     */
    private static final int REGISTER = 0x32;

    /**
     * 13bitの分解能.
     */
    private static final double RESOLUTION = (16 + 16) / Math.pow(2, 13);

    /**
     * 取得した値を通知するリスナ.
     */
    private OnADXL345Listener mOnADXL345Listener;

    @Override
    public void start() {
        setI2CConfig();
        setADXL345();
        startRead(ADXL345_DEVICE_ADDR, REGISTER, 6);
    }

    @Override
    public void stop() {
        stopRead(ADXL345_DEVICE_ADDR, REGISTER);
    }

    @Override
    public void setOnADXL345Listener(final OnADXL345Listener listener) {
        mOnADXL345Listener = listener;
    }

    // BaseI2C interface

    @Override
    byte getAddress() {
        return ADXL345_DEVICE_ADDR;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 1;
        int address = decodeByte(data[offset++], data[offset++]);
        int register = decodeByte(data[offset++], data[offset++]);
        if (address == ADXL345_DEVICE_ADDR && register == REGISTER) {
            int ax = decodeShort(data, offset);
            offset += 4;
            int ay = decodeShort(data, offset);
            offset += 4;
            int az = decodeShort(data, offset);

            double gx = convertResolution(ax);
            double gy = convertResolution(ay);
            double gz = convertResolution(az);

            if (mOnADXL345Listener != null) {
                mOnADXL345Listener.onData(gx, gy, gz);
            }
        }
    }

    /**
     * ADXL345の初期化を行います.
     */
    private void setADXL345() {
        write(ADXL345_DEVICE_ADDR, 0x31, 0x0B);
        write(ADXL345_DEVICE_ADDR, 0x2D, 0x08);
    }

    /**
     * 加速度センサーの値を重力加速度に変換します.
     * @param data 加速度センサーからの値
     * @return 重力加速度
     */
    private double convertResolution(final int data) {
        return data * RESOLUTION;
    }
}
