package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IHTS221;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;

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

    private List<OnHumidityCallback> mOnHumidityCallbacks = new ArrayList<>();
    private List<OnTemperatureCallback> mOnTemperatureCallbacks = new ArrayList<>();

    private Humidity mHumidity = new Humidity();
    private Temperature mTemperature = new Temperature();

    @Override
    public synchronized void readHumidity(final OnHumidityCallback callback) {
        mOnHumidityCallbacks.add(callback);

        if (mHumidity.isMeasuring()) {
            return;
        }
        mHumidity.start();
    }

    @Override
    public synchronized void readTemperature(final OnTemperatureCallback callback) {
        mOnTemperatureCallbacks.add(callback);

        if (mTemperature.isMeasuring()) {
            return;
        }
        mTemperature.start();
    }

    @Override
    byte getAddress() {
        return HTS221_SLAVE_ADDRESS;
    }

    @Override
    synchronized void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset]);
        switch (register) {
            case REGISTER_DEVICE_REG:
                mHumidity.onReadData(data, register);
                mTemperature.onReadData(data, register);
                break;

            // humidity

            case REGISTER_H0_RH_X2:
            case REGISTER_H1_RH_X2:
            case REGISTER_H0_T0_OUT_L:
            case REGISTER_H0_T0_OUT_H:
            case REGISTER_H1_T0_OUT_L:
            case REGISTER_H1_T0_OUT_H:
            case REGISTER_HUMIDITY_OUT_L:
            case REGISTER_HUMIDITY_OUT_H:
                mHumidity.onReadData(data, register);
                break;

            // temperature

            case REGISTER_T1_T0_MSB:
            case REGISTER_T0_DEGC_X8:
            case REGISTER_T1_DEGC_X8:
            case REGISTER_T0_OUT_L:
            case REGISTER_T0_OUT_H:
            case REGISTER_T1_OUT_L:
            case REGISTER_T1_OUT_H:
            case REGISTER_TEMP_OUT_L:
            case REGISTER_TEMP_OUT_H:
                mTemperature.onReadData(data, register);
                break;

            case REGISTER_STATUS_REG:
                break;
        }
    }

    private void powerOn() {
        write(HTS221_SLAVE_ADDRESS, REGISTER_CTRL_REG1, PD | ODR_1HZ);
    }

    private void configDevice() {
        write(HTS221_SLAVE_ADDRESS, REGISTER_AV_CONF, AVGH_32 | AVGT_16);
    }

    private int dataConv(int data1, int data2) {
        int value = (data1 & 0xff) | ((data2 & 0xff) << 8);
        if ((value & 0x8000) != 0) {
            value -= (1 << 16);
        }
        return value;
    }

    private abstract class HTSS221State {

        /**
         * 現在のステート.
         * <p>
         * 現在、取得しているレジスタの番号になります。
         * </p>
         */
        int mState = -1;

        /**
         * タイムアウトを監視するタイマー.
         */
        private Timer mTimer;

        /**
         * 湿度の計測中フラグ.
         * @return 計測中の場合にはtrue、それ以外はfalse
         */
        boolean isMeasuring() {
            return mState != -1;
        }

        /**
         * 湿度の計測を開始します.
         */
        void start() {
            setI2CConfig();

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onError("timeout");
                }
            }, 1000);

            next(REGISTER_DEVICE_REG);
        }

        /**
         * 次のレジスタの値を取得します.
         * @param register 次に取得するレジスタ
         */
        void next(final int register) {
            mState = register;
            read(HTS221_SLAVE_ADDRESS, register, 1);
        }

        /**
         * エラーが発生した場合の処理を行います.
         * @param message エラーメッセージ
         */
        void onError(final String message) {
            onFinish();
        }

        /**
         * 後始末処理を行います.
         */
        void onFinish() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mState = -1;
        }

        /**
         * HTS221から送られてきたデータを解析して、次のステップに進みます.
         * @param data 送られてきたデータ
         * @param register レジスタ番号
         */
        abstract void onReadData(final byte[] data, final int register);
    }


    /**
     * HTS221から湿度を取得するためのステートを管理するクラス.
     */
    private class Humidity extends HTSS221State {

        private int h0_rh_x2;
        private int h1_rh_x2;
        private int h0_t0_l;
        private int h0_t0_h;
        private int h1_t0_l;
        private int h1_t0_h;

        private int humidity_out_l;
        private int humidity_out_h;

        /**
         * HTS221から送られてきたデータを湿度に変換します.
         * @return 変換した湿度
         */
        private double convertHumidity() {
            int h_out = dataConv(humidity_out_l, humidity_out_h);

            double t_H0_rH = h0_rh_x2 / 2.0;
            double t_H1_rH = h1_rh_x2 / 2.0;

            int h0_t0_out = dataConv(h0_t0_l, h0_t0_h);
            int h1_t0_out = dataConv(h1_t0_l, h1_t0_h);

            return t_H0_rH + (t_H1_rH - t_H0_rH) * (h_out - h0_t0_out) / (h1_t0_out - h0_t0_out);
        }

        /**
         * 取得した湿度の値を配信します.
         */
        private void onReadHumidity() {
            double humidity = convertHumidity();
            for (OnHumidityCallback callback : mOnHumidityCallbacks) {
                callback.onHumidity(humidity);
            }
            mOnHumidityCallbacks.clear();
            onFinish();
        }

        @Override
        void onError(final String message) {
            for (OnHumidityCallback callback : mOnHumidityCallbacks) {
                callback.onError(message);
            }
            mOnHumidityCallbacks.clear();
            super.onError(message);
        }

        @Override
        void onReadData(final byte[] data, final int register) {

            if (mState != register) {
                // レジスタが一致しない場合には、不正なデータなので無視
                return;
            }

            int offset = 5;
            switch(register) {
                case REGISTER_DEVICE_REG:
                    int deviceId = decodeByte(data[offset++], data[offset]);
                    if (deviceId == DEVICE_ID) {
                        powerOn();
                        configDevice();
                        next(REGISTER_H0_RH_X2);
                    } else {
                        onError("HTS221 is not connect.");
                    }
                    break;

                case REGISTER_H0_RH_X2:
                    h0_rh_x2 = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_H1_RH_X2);
                    break;

                case REGISTER_H1_RH_X2:
                    h1_rh_x2 = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_H0_T0_OUT_L);
                    break;

                case REGISTER_H0_T0_OUT_L:
                    h0_t0_l = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_H0_T0_OUT_H);
                    break;

                case REGISTER_H0_T0_OUT_H:
                    h0_t0_h = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_H1_T0_OUT_L);
                    break;

                case REGISTER_H1_T0_OUT_L:
                    h1_t0_l = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_H1_T0_OUT_H);
                    break;

                case REGISTER_H1_T0_OUT_H:
                    h1_t0_h = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_HUMIDITY_OUT_L);
                    break;

                case REGISTER_HUMIDITY_OUT_L:
                    humidity_out_l = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_HUMIDITY_OUT_H);
                    break;

                case REGISTER_HUMIDITY_OUT_H:
                    humidity_out_h = decodeByte(data[offset++], data[offset]);
                    onReadHumidity();
                    break;
            }
        }
    }

    /**
     * HTS221から温度を取得するためのステートを管理するクラス.
     */
    private class Temperature extends HTSS221State {

        private int t1_t0_msb;

        private int t0_degc_x8;
        private int t1_degc_x8;

        private int t0_l;
        private int t0_h;

        private int t1_l;
        private int t1_h;

        private int temp_out_l;
        private int temp_out_h;

        /**
         * HTS221から送られてきたデータを温度に変換します.
         * @return 変換した温度
         */
        private double convertTemperature() {
            int t_out = dataConv(temp_out_l, temp_out_h);

            double t_T0_degC = t0_degc_x8 / 8.0;
            double t_T1_degC = t1_degc_x8 / 8.0;

            int t0_out = dataConv(t0_l, t0_h);
            int t1_out = dataConv(t1_l, t1_h);

            return t_T0_degC + (t_T1_degC - t_T0_degC) * (t_out - t0_out) / (t1_out - t0_out);
        }

        /**
         * 計測した結果を配信します.
         */
        private void onReadTemperature() {
            double temperature = convertTemperature();
            for (OnTemperatureCallback callback : mOnTemperatureCallbacks) {
                callback.onTemperature(temperature);
            }
            mOnTemperatureCallbacks.clear();
            onFinish();
        }

        @Override
        void onError(final String message) {
            for (OnTemperatureCallback callback : mOnTemperatureCallbacks) {
                callback.onError(message);
            }
            mOnTemperatureCallbacks.clear();
            super.onError(message);
        }

        @Override
        void onReadData(final byte[] data, final int register) {

            if (mState != register) {
                // レジスタが一致しない場合には、不正なデータなので無視
                return;
            }

            int offset = 5;
            switch (register) {
                case REGISTER_DEVICE_REG:
                    int deviceId = decodeByte(data[offset++], data[offset]);
                    if (deviceId == DEVICE_ID) {
                        powerOn();
                        configDevice();
                        next(REGISTER_T1_T0_MSB);
                    } else {
                        onError("HTS221 is not connect.");
                    }
                    break;

                case REGISTER_T1_T0_MSB:
                    t1_t0_msb = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_T0_DEGC_X8);
                    break;

                case REGISTER_T0_DEGC_X8:
                    int a = decodeByte(data[offset++], data[offset]);
                    t0_degc_x8 = ((t1_t0_msb & 0x3) << 8) | a;
                    next(REGISTER_T1_DEGC_X8);
                    break;

                case REGISTER_T1_DEGC_X8:
                    int b = decodeByte(data[offset++], data[offset]);
                    t1_degc_x8 = ((t1_t0_msb & 0xC) << 6) | b;
                    next(REGISTER_T0_OUT_L);
                    break;

                case REGISTER_T0_OUT_L:
                    t0_l = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_T0_OUT_H);
                    break;

                case REGISTER_T0_OUT_H:
                    t0_h = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_T1_OUT_L);
                    break;

                case REGISTER_T1_OUT_L:
                    t1_l = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_T1_OUT_H);
                    break;

                case REGISTER_T1_OUT_H:
                    t1_h = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_TEMP_OUT_L);
                    break;

                case REGISTER_TEMP_OUT_L:
                    temp_out_l = decodeByte(data[offset++], data[offset]);
                    next(REGISTER_TEMP_OUT_H);
                    break;

                case REGISTER_TEMP_OUT_H:
                    temp_out_h = decodeByte(data[offset++], data[offset]);
                    onReadTemperature();
                    break;
            }
        }
    }
}
