package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IVCNL4010;

import java.io.IOException;

/**
 * VCNL4010を操作するクラス.
 * <p>
 * Brick #205
 * </p>
 */
class VCNL4010 extends BaseI2C implements IVCNL4010 {
    // I2C VCNL4010 Slave Address
    private static final byte SLAVE_ADDRESS = 0x13;

    // Product,Revision ID Value
    private static final int DEVICE_ID = 0x21;

    // Register Addresses
    private static final int REG_CMD = 0x80;
    private static final int DEVICE_REG = 0x81;
    private static final int REG_PROX_RATE = 0x82;
    private static final int REG_LED_CRNT = 0x83;
    private static final int REG_AMBI_PARM = 0x84;
    private static final int REG_AMBI_DATA_H = 0x85;
    private static final int REG_AMBI_DATA_L = 0x86;
    private static final int REG_PROX_DATA_H = 0x87;
    private static final int REG_PROX_DATA_L = 0x88;
    private static final int REG_INT_CTRL = 0x89;
    private static final int REG_INT_LOW_H = 0x8A;
    private static final int REG_INT_LOW_L = 0x8B;
    private static final int REG_INT_HIGH_H = 0x8C;
    private static final int REG_INT_HIGH_L= 0x8D;
    private static final int REG_INT_STAT = 0x8E;
    private static final int REG_PROX_ADJ = 0x8F;

    // Commands
    private static final int CMD_SELFTIMED_EN = 0x01;
    private static final int CMD_PROX_EN = 0x02;
    private static final int CMD_ALS_EN = 0x04;
    private static final int CMD_PROX_OD = 0x08;
    private static final int CMD_ALS_OD = 0x10;
    private static final int CMD_PROX_DRDY = 0x20;
    private static final int CMD_ALS_DRDY = 0x40;

    // Proximity Measurement Rate
    private static final int PROX_RATE_1 = 0x00;
    private static final int PROX_RATE_3 = 0x01;
    private static final int PROX_RATE_7 = 0x02;
    private static final int PROX_RATE_16 = 0x03;
    private static final int PROX_RATE_31 = 0x04;
    private static final int PROX_RATE_62 = 0x05;
    private static final int PROX_RATE_125 = 0x06;
    private static final int PROX_RATE_250 = 0x07;

    // Ambient Light Parameter
    private static final int AMBI_CONT_CONV_MODE = 0x80;
    private static final int AMBI_RATE_1 = 0x00;
    private static final int AMBI_RATE_2 = 0x10;
    private static final int AMBI_RATE_3 = 0x20;
    private static final int AMBI_RATE_4 = 0x30;
    private static final int AMBI_RATE_5 = 0x40;
    private static final int AMBI_RATE_6 = 0x50;
    private static final int AMBI_RATE_8 = 0x60;
    private static final int AMBI_RATE_10 = 0x70;
    private static final int AMBI_AUTO_OFFSET = 0x08;
    private static final int AMBI_AVE_NUM_1 = 0x00;
    private static final int AMBI_AVE_NUM_2 = 0x01;
    private static final int AMBI_AVE_NUM_4 = 0x02;
    private static final int AMBI_AVE_NUM_8 = 0x03;
    private static final int AMBI_AVE_NUM_16 = 0x04;
    private static final int AMBI_AVE_NUM_32 = 0x05;
    private static final int AMBI_AVE_NUM_64 = 0x06;
    private static final int AMBI_AVE_NUM_128 = 0x07;

    /**
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;

    /**
     * 一時的にデータを格納するバッファ.
     */
    private byte[] mBuffer = new byte[2];

    /**
     * Proximityの値を監視するスレッド.
     */
    private ProximityWatchTread mProximityWatchTread;

    /**
     * AmbientLightの値を監視するスレッド.
     */
    private AmbientLightWatchThread mAmbientLightWatchThread;

    /**
     * コンストラクタ.
     * @param control コントローラ
     */
    VCNL4010(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(SLAVE_ADDRESS);
    }

    @Override
    public void readProximity(final OnProximityListener listener) {
        if (!checkDevice()) {
            listener.onError("VCNL4010 is not connect.");
        } else {
            try {
                setVCNL4010();

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                listener.onData(readProx());
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    @Override
    public void startProximity(final OnProximityListener listener) {
        if (!checkDevice()) {
            listener.onError("VCNL4010 is not connect.");
        } else {
            if (mProximityWatchTread == null) {
                mProximityWatchTread = new ProximityWatchTread();
                mProximityWatchTread.mListener = listener;
                mProximityWatchTread.start();
            }
        }
    }

    @Override
    public void stopProximity(final OnProximityListener listener) {
        if (mProximityWatchTread != null) {
            mProximityWatchTread.stopWatch();
            mProximityWatchTread = null;
        }
    }

    @Override
    public void readAmbientLight(final OnAmbientLightListener listener) {
        if (!checkDevice()) {
            listener.onError("VCNL4010 is not connect.");
        } else {
            try {
                setVCNL4010();

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                listener.onData(readAmbi());
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    @Override
    public void startAmbientLight(final OnAmbientLightListener listener) {
        if (!checkDevice()) {
            listener.onError("VCNL4010 is not connect.");
        } else {
            if (mAmbientLightWatchThread == null) {
                mAmbientLightWatchThread = new AmbientLightWatchThread();
                mAmbientLightWatchThread.mListener = listener;
                mAmbientLightWatchThread.start();
            }
        }
    }

    @Override
    public void stopAmbientLight(final OnAmbientLightListener listener) {
        if (mAmbientLightWatchThread != null) {
            mAmbientLightWatchThread.stopWatch();
            mAmbientLightWatchThread = null;
        }
    }

    /**
     * 接続されているデバイスがVCNL4010か確認を行う.
     * @return VCNL4010ならtrue、それ以外ならfalse
     */
    private boolean checkDevice() {
        if (mI2cDevice == null) {
            return false;
        } else {
            try {
                byte deviceId = mI2cDevice.readRegByte(DEVICE_REG);
                return (deviceId & 0xFF) == DEVICE_ID;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * VCNL4010の設定を行います.
     */
    private void setVCNL4010() throws IOException {
        setCommand(CMD_SELFTIMED_EN | CMD_PROX_EN | CMD_ALS_EN);
        setProxRate(PROX_RATE_250);
        setLedCurrent(20);
        setAmbiParm(AMBI_RATE_10 | AMBI_AUTO_OFFSET | AMBI_AVE_NUM_128);
    }

    /**
     * コマンドを送信します.
     * @param config 送信するコマンド設定
     */
    private void setCommand(final int config) throws IOException {
        mI2cDevice.writeRegByte(REG_CMD, (byte) config);
    }

    /**
     * Proximityのレートを送信します.
     * @param config　送信するレート
     */
    private void setProxRate(final int config) throws IOException {
        mI2cDevice.writeRegByte(REG_PROX_RATE, (byte) config);
    }

    /**
     * LED Currentの値を送信します.
     * @param config 送信する値
     */
    private void setLedCurrent(final int config) throws IOException {
        mI2cDevice.writeRegByte(REG_LED_CRNT, (byte) config);
    }

    /**
     * Ambient Lightの設定を送信します.
     * @param config 送信する設定
     */
    private void setAmbiParm(final int config) throws IOException {
        mI2cDevice.writeRegByte(REG_AMBI_PARM, (byte) config);
    }

    /**
     * 距離(cm)に変換します.
     * @param proximity センサーからの値
     * @return 距離(cm)
     */
    private double convert(final int proximity) {
        return 0.1 + ((65535 - proximity) / 65535.0) * 2.0;
    }

    /**
     * Proximityの値を読み込みます.
     * @return Proximityの値
     * @throws IOException Proximityの読み込みに失敗した場合に発生
     */
    private synchronized double readProx() throws IOException {
        mI2cDevice.readRegBuffer(REG_PROX_DATA_H, mBuffer, 2);
        return convert(decodeUShort2(mBuffer, 0));
    }

    /**
     * Ambient Lightの値を読み込みます.
     * @return Ambient Lightの値
     * @throws IOException Ambient Lightの読み込みに失敗した場合に発生
     */
    private synchronized double readAmbi() throws IOException {
        mI2cDevice.readRegBuffer(REG_AMBI_DATA_H, mBuffer, 2);
        return decodeUShort2(mBuffer, 0);
    }

    private class ProximityWatchTread extends Thread {
        /**
         * 終了フラグ.
         */
        private boolean mFinishFlag;

        /**
         * リスナー.
         */
        private OnProximityListener mListener;

        @Override
        public void run() {
            try {
                setVCNL4010();

                mListener.onStarted();

                while (!mFinishFlag) {
                    mListener.onData(readProx());
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                mListener.onError(e.getMessage());
            }
        }

        void stopWatch() {
            mFinishFlag = true;
            interrupt();
        }
    }

    private class AmbientLightWatchThread extends Thread {
        /**
         * 終了フラグ.
         */
        private boolean mFinishFlag;

        /**
         * リスナー.
         */
        private OnAmbientLightListener mListener;

        @Override
        public void run() {
            try {
                setVCNL4010();

                mListener.onStarted();

                while (!mFinishFlag) {
                    mListener.onData(readAmbi());
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                mListener.onError(e.getMessage());
            }
        }

        void stopWatch() {
            mFinishFlag = true;
            interrupt();
        }
    }
}
