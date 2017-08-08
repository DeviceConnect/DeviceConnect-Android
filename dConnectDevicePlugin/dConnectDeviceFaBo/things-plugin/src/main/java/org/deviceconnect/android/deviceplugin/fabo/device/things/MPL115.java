package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IMPL115;

import java.io.IOException;

/**
 * MPL115を操作するクラス.
 * <p>
 * Brick #204
 * </p>
 */
class MPL115 extends BaseI2C implements IMPL115 {

    // MPL115A2 I2C slave address
    private static final byte SLAVE_ADDRESS = 0x60;

    // MPL115A2 Register Address
    private static final int PADC_MSB = 0x00;
    private static final int PADC_LSB = 0x01;
    private static final int TADC_MSB = 0x02;
    private static final int TACD_LSB = 0x03;
    private static final int A0_MSB = 0x04;
    private static final int A0_LSB = 0x05;
    private static final int B1_MSB = 0x06;
    private static final int B1_LSB = 0x07;
    private static final int B2_MSB = 0x08;
    private static final int B2_LSB = 0x09;
    private static final int C12_MSB = 0x0A;
    private static final int C12_LSB = 0x0B;
    private static final int CONVERT = 0x12;

    /**
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;
    private float mA0;
    private float mB1;
    private float mB2;
    private float mC12;

    MPL115(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(SLAVE_ADDRESS);
    }

    @Override
    public void readAtmosphericPressure(final OnAtmosphericPressureListener listener) {
        if (!checkDevice()) {
            listener.onError("MPL115 is not connected.");
        } else {
            try {
                readCoef();

                mI2cDevice.writeRegByte(CONVERT, (byte) 0x01);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[4];
                mI2cDevice.readRegBuffer(PADC_MSB, buffer, 4);

                int padc = (((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF)) >> 6;
                int tadc = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) >> 6;

                double pcomp = mA0 + (mB1 + mC12 * tadc) * padc + mB2 * tadc;
                double hpa = pcomp * ((1150.0 - 500.0) / 1023.0) + 500.0;
                double temp = 25.0 - (tadc - 512.0) / 5.35;

                listener.onData(hpa, temp);
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    /**
     * 接続されているデバイスがMPL115か確認を行う.
     * @return MPL115ならtrue、それ以外ならfalse
     */
    private boolean checkDevice() {
        return mI2cDevice != null;
    }

    /**
     * MPL115に設定されている値を取得します.
     * @throws IOException 設定の取得に失敗した場合に発生
     */
    private void readCoef() throws IOException {
        byte[] data = new byte[8];
        mI2cDevice.readRegBuffer(A0_MSB, data, 8);

        mA0 = dataConv(data[1], data[0]) / (float) (1 << 3);
        mB1 = dataConv(data[3], data[2]) / (float) (1 << 13);
        mB2 = dataConv(data[5], data[4]) / (float) (1 << 14);
        mC12 = dataConv(data[7], data[6]) / (float) (1 << 24);
    }
}
