package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IHTS221;

import java.io.IOException;

/**
 * HTS221を操作するクラス.
 * <p>
 * Brick #208
 * </p>
 */
class HTS221 extends BaseI2C implements IHTS221 {

    /**
     * HTS221のアドレス.
     */
    private static final byte HTS221_SLAVE_ADDRESS = 0x5F;

    /**
     * HTS221のデバイスID.
     */
    private static final int DEVICE_ID = 0xBC;

    // AV_CONF:AVGH
    // Averaged humidity samples configuration
    // AVGH_4   : 0b00000000
    private static final byte AVGH_4 = 0x00;
    // AVGH_8   : 0b00000001
    private static final byte AVGH_8 = 0x01;
    // AVGH_16  : 0b00000010
    private static final byte AVGH_16 = 0x02;
    // AVGH_32  : 0b00000011 // defalut
    private static final byte AVGH_32 = 0x03;
    // AVGH_64  : 0b00000100
    private static final byte AVGH_64 = 0x04;
    // AVGH_128 : 0b00000101
    private static final byte AVGH_128 = 0x05;
    // AVGH_256 : 0b00000110
    private static final byte AVGH_256 = 0x06;
    // AVGH_512 : 0b00000111
    private static final byte AVGH_512 = 0x07;

    // AV_CONF:AVGT
    // Averaged temperature samples configuration
    // AVGT_2   : 0b00000000
    private static final byte AVGT_2 = 0x00;
    // AVGT_4   : 0b00001000
    private static final byte AVGT_4 = 0x08;
    // AVGT_8   : 0b00010000
    private static final byte AVGT_8 = 0x10;
    // AVGT_16  : 0b00011000 // defalut
    private static final byte AVGT_16 = 0x18;
    // AVGT_32  : 0b00100000
    private static final byte AVGT_32 = 0x20;
    // AVGT_64  : 0b00101000
    private static final byte AVGT_64 = 0x28;
    // AVGT_128 : 0b00110000
    private static final byte AVGT_128 = 0x30;
    // AVGT_256 : 0b00111000
    private static final byte AVGT_256 = 0x38;

    // CTRL_REG1
    // Power Down control : 0b10000000
    private static final byte PD = (byte) 0x80;
    // Block Data Update control : 0b00000100
    private static final byte BDU = 0x04;
    // Output Data Rate : One Shot : 0b00000000
    private static final byte ODR_ONE = 0x00;
    // Output Data Rate : 1Hz : 0b00000001
    private static final byte ODR_1HZ = 0x01;
    // Output Data Rate : 7Hz : 0b00000010
    private static final byte ODR_7HZ = 0x02;
    // Output Data Rate : 12.5Hz : 0b00000011
    private static final byte ODR_125HZ = 0x03;

    // CTRL_REG2
    // Reboot memory content : 0b10000000
    private static final byte BOOT = (byte) 0x80;
    // Heater : 0b00000010
    private static final byte HEATER = 0x02;
    // One shot enable : 0b00000001
    private static final byte ONE_SHOT = 0x01;

    // CTRL_REG3
    // DRDY pin is no connect in FaBo Brick
    private static final byte CTRL_REG3_DEFAULT = 0x00;

    // REGISTER_STATUS_REG
    // Humidity Data Available
    private static final byte H_DA = 0x02;
    // Temperature Data Available
    private static final byte T_DA = 0x01;

    private static final int REGISTER_DEVICE_REG = 0x0F;
    private static final int REGISTER_AV_CONF = 0x10;
    private static final int REGISTER_CTRL_REG1 = 0x20;
    private static final int REGISTER_CTRL_REG2 = 0x21;
    private static final int REGISTER_CTRL_REG3 = 0x22;
    private static final int REGISTER_STATUS_REG = 0x27;
    private static final int REGISTER_HUMIDITY_OUT_L = 0x28;
    private static final int REGISTER_HUMIDITY_OUT_H = 0x29;
    private static final int REGISTER_TEMP_OUT_L = 0x2A;
    private static final int REGISTER_TEMP_OUT_H = 0x2B;
    private static final int REGISTER_H0_RH_X2 = 0x30;
    private static final int REGISTER_H1_RH_X2 = 0x31;
    private static final int REGISTER_T0_DEGC_X8 = 0x32;
    private static final int REGISTER_T1_DEGC_X8 = 0x33;
    private static final int REGISTER_T1_T0_MSB = 0x35;
    private static final int REGISTER_H0_T0_OUT_L = 0x36;
    private static final int REGISTER_H0_T0_OUT_H = 0x37;
    private static final int REGISTER_H1_T0_OUT_L = 0x3A;
    private static final int REGISTER_H1_T0_OUT_H = 0x3B;
    private static final int REGISTER_T0_OUT_L = 0x3C;
    private static final int REGISTER_T0_OUT_H = 0x3D;
    private static final int REGISTER_T1_OUT_L = 0x3E;
    private static final int REGISTER_T1_OUT_H = 0x3F;

    /**
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;

    private int h0_rh_x2;
    private int h1_rh_x2;
    private int h0_t0_out;
    private int h1_t0_out;
    private int t0_degc_x8;
    private int t1_degc_x8;
    private int t0_out;
    private int t1_out;

    HTS221(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(HTS221_SLAVE_ADDRESS);
    }

    @Override
    public synchronized void readHumidity(final OnHumidityCallback callback) {
        if (!checkDevice()) {
            callback.onError("HTS221 is not connect.");
        } else {
            try {
                powerOn();
                configDevice();

                h0_rh_x2 = mI2cDevice.readRegByte(REGISTER_H0_RH_X2) & 0xFF;
                h1_rh_x2 = mI2cDevice.readRegByte(REGISTER_H1_RH_X2) & 0xFF;

                byte h0_t0_l = mI2cDevice.readRegByte(REGISTER_H0_T0_OUT_L);
                byte h0_t0_h = mI2cDevice.readRegByte(REGISTER_H0_T0_OUT_H);
                h0_t0_out = dataConv(h0_t0_l, h0_t0_h);

                byte h1_t0_l = mI2cDevice.readRegByte(REGISTER_H1_T0_OUT_L);
                byte h1_t0_h = mI2cDevice.readRegByte(REGISTER_H1_T0_OUT_H);
                h1_t0_out = dataConv(h1_t0_l, h1_t0_h);

                byte h_out_l = mI2cDevice.readRegByte(REGISTER_HUMIDITY_OUT_L);
                byte h_out_h = mI2cDevice.readRegByte(REGISTER_HUMIDITY_OUT_H);

                int h_out = dataConv(h_out_l, h_out_h);

                int t_H0_rH = h0_rh_x2 / 2;
                int t_H1_rH = h1_rh_x2 / 2;

                double humidity = t_H0_rH + (t_H1_rH - t_H0_rH) * (h_out - h0_t0_out) / (h1_t0_out - h0_t0_out);
                callback.onHumidity(humidity);
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void readTemperature(final OnTemperatureCallback callback) {
        if (!checkDevice()) {
            callback.onError("HTS221 is not connect.");
        } else {
            try {
                powerOn();
                configDevice();

                byte data = mI2cDevice.readRegByte(REGISTER_T1_T0_MSB);

                byte t0_degc_x8 = mI2cDevice.readRegByte(REGISTER_T0_DEGC_X8);
                byte t1_degc_x8 = mI2cDevice.readRegByte(REGISTER_T1_DEGC_X8);
                this.t0_degc_x8 = ((data & 0x3) << 8) | t0_degc_x8;
                this.t1_degc_x8 = ((data & 0xC) << 6) | t1_degc_x8;

                byte t0_l = mI2cDevice.readRegByte(REGISTER_T0_OUT_L);
                byte t0_h = mI2cDevice.readRegByte(REGISTER_T0_OUT_H);
                t0_out = dataConv(t0_l, t0_h);

                byte t1_l = mI2cDevice.readRegByte(REGISTER_T1_OUT_L);
                byte t1_h = mI2cDevice.readRegByte(REGISTER_T1_OUT_H);
                t1_out = dataConv(t1_l, t1_h);

                byte temp_out_l = mI2cDevice.readRegByte(REGISTER_TEMP_OUT_L);
                byte temp_out_h = mI2cDevice.readRegByte(REGISTER_TEMP_OUT_H);
                int t_out = dataConv(temp_out_l, temp_out_h);

                // 1/8にする
                int t_T0_degC = this.t0_degc_x8 / 8;
                int t_T1_degC = this.t1_degc_x8 / 8;

                // 線形補間でもとめる
                double temperature = t_T0_degC + (t_T1_degC - t_T0_degC) * (t_out - t0_out) / (t1_out - t0_out);
                callback.onTemperature(temperature);
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }
    }

    /**
     * HTS221の確認を行います.
     * @return HTS221の場合にはtrue、それ以外はfalse
     */
    private boolean checkDevice() {
        if (mI2cDevice == null) {
            return false;
        } else {
            try {
                byte deviceId = mI2cDevice.readRegByte(REGISTER_DEVICE_REG);
                return (deviceId & 0xFF) == DEVICE_ID;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * HTS221へ起動要求を行います.
     * @throws IOException 起動要求に失敗した場合に発生.
     */
    private void powerOn() throws IOException {
        mI2cDevice.writeRegByte(REGISTER_CTRL_REG1, (byte) (PD | ODR_1HZ));
    }

    /**
     * HTS221へ設定要求を行います.
     * @throws IOException 設定要求に失敗した場合に発生.
     */
    private void configDevice() throws IOException {
        mI2cDevice.writeRegByte(REGISTER_AV_CONF, (byte) (AVGH_32 | AVGT_16));
    }
}
