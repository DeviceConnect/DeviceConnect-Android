package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IVCNL4010;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;

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

    private static final double THRESHOLD = 1.8;

    private List<OnProximityListener> mOnProximityListeners = new CopyOnWriteArrayList<>();
    private List<OnAmbientLightListener> mOnAmbientLightListeners = new CopyOnWriteArrayList<>();

    private Proximity mProximity = new Proximity();
    private Ambi mAmbi = new Ambi();

    @Override
    public void readProximity(final OnProximityListener listener) {
        mOnProximityListeners.add(new OnProximityListener() {
            @Override
            public void onStarted() {
                listener.onStarted();
            }

            @Override
            public void onData(final boolean proximity) {
                listener.onData(proximity);
                stopProximity(this);
            }

            @Override
            public void onError(final String message) {
                listener.onError(message);
                stopProximity(this);
            }
        });
        if (mProximity.isMeasuring()) {
            return;
        }
        mProximity.start();
    }

    @Override
    public void startProximity(final OnProximityListener listener) {
        if (!mOnProximityListeners.contains(listener)) {
            mOnProximityListeners.add(listener);
        }

        if (mProximity.isMeasuring()) {
            return;
        }
        mProximity.start();
    }

    @Override
    public void stopProximity(final OnProximityListener listener) {
        if (mProximity.isMeasuring()) {
            mProximity.stop();
        }
        mOnProximityListeners.remove(listener);
    }

    @Override
    public void readAmbientLight(final OnAmbientLightListener listener) {
        if (!mOnAmbientLightListeners.contains(listener)) {
            mOnAmbientLightListeners.add(listener);
        }

        if (mAmbi.isMeasuring()) {
            return;
        }
        mAmbi.start();
    }

    @Override
    public void startAmbientLight(final OnAmbientLightListener listener) {
        if (!mOnAmbientLightListeners.contains(listener)) {
            mOnAmbientLightListeners.add(listener);
        }

    }

    @Override
    public void stopAmbientLight(final OnAmbientLightListener listener) {
        if (mAmbi.isMeasuring()) {
            mAmbi.stop();
        }
        mOnAmbientLightListeners.remove(listener);
    }

    // BaseI2C interface

    @Override
    byte getAddress() {
        return SLAVE_ADDRESS;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset]);
        switch (register) {
            case DEVICE_REG:
                mProximity.onReadData(data, register);
                mAmbi.onReadData(data, register);
                break;
            case REG_PROX_DATA_H:
                mProximity.onReadData(data, register);
                break;
            case REG_AMBI_DATA_H:
                mAmbi.onReadData(data, register);
                break;
        }
    }

    /**
     * VCNL4010の設定を行います.
     */
    private void setVCNL4010() {
        setCommand(CMD_SELFTIMED_EN | CMD_PROX_EN | CMD_ALS_EN);
        setProxRate(PROX_RATE_250);
        setLedCurrent(20);
        setAmbiParm(AMBI_RATE_10 | AMBI_AUTO_OFFSET | AMBI_AVE_NUM_128);
    }

    /**
     * コマンドを送信します.
     * @param config 送信するコマンド設定
     */
    private void setCommand(final int config) {
        write(SLAVE_ADDRESS, REG_CMD, config);
    }

    /**
     * Proximityのレートを送信します.
     * @param config　送信するレート
     */
    private void setProxRate(final int config) {
        write(SLAVE_ADDRESS, REG_PROX_RATE, config);
    }

    /**
     * LED Currentの値を送信します.
     * @param config 送信する値
     */
    private void setLedCurrent(final int config) {
        write(SLAVE_ADDRESS, REG_LED_CRNT, config);
    }

    /**
     * Ambient Lightの設定を送信します.
     * @param config 送信する設定
     */
    private void setAmbiParm(final int config) {
        write(SLAVE_ADDRESS, REG_AMBI_PARM, config);
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
     * VCNL4010と通信する場合のステートを管理するクラス.
     */
    private abstract class VCNL4010State {

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
         * 開始フラグ.
         */
        boolean mStartFlag;

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

            mStartFlag = false;

            next(DEVICE_REG);
        }

        /**
         * 次のレジスタの値を取得します.
         * @param register 次に取得するレジスタ
         */
        void next(final int register) {
            mState = register;
            read(SLAVE_ADDRESS, register, 1);
        }

        /**
         * エラーが発生した場合の処理を行います.
         * @param message エラーメッセージ
         */
        void onError(final String message) {
            for (OnProximityListener listener : mOnProximityListeners) {
                listener.onError(message);
            }
            onFinish();
        }

        /**
         * タイマーをキャンセルします.
         */
        void cancelTimer() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }

        /**
         * 後始末処理を行います.
         */
        void onFinish() {
            cancelTimer();
            mState = -1;
        }

        /**
         * 読み込みを停止します.
         */
        abstract void stop();

        /**
         * HTS221から送られてきたデータを解析して、次のステップに進みます.
         * @param data 送られてきたデータ
         * @param register レジスタ番号
         */
        abstract void onReadData(final byte[] data, final int register);
    }

    private class Proximity extends VCNL4010State {

        private double mOldProximity;

        @Override
        void stop() {
            stopRead(SLAVE_ADDRESS, REG_PROX_DATA_H);
            onFinish();
        }

        @Override
        void onReadData(final byte[] data, final int register) {

            if (mState != register) {
                // レジスタが一致しない場合には、不正なデータなので無視
                return;
            }

            int offset = 5;
            switch(register) {
                case DEVICE_REG:
                    int deviceId = decodeByte(data[offset++], data[offset]);
                    if (deviceId == DEVICE_ID) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                setVCNL4010();

                                // 設定が反映されるまで、少し時間がかかるのスリープを入れておく
                                try {
                                    Thread.sleep(33);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                mState = REG_PROX_DATA_H;
                                startRead(SLAVE_ADDRESS, REG_PROX_DATA_H, 2);
                            }
                        }).start();
                    } else {
                        onError("VCNL4010 is not connect.");
                    }
                    break;

                case REG_PROX_DATA_H:
                    if (!mStartFlag) {
                        for (OnProximityListener listener : mOnProximityListeners) {
                            listener.onStarted();
                        }
                        mStartFlag = true;
                        cancelTimer();
                    }

                    double proximity = convert(FirmataUtil.decodeUShort2(data, offset));
                    if (mOldProximity > THRESHOLD && proximity < THRESHOLD) {
                        for (OnProximityListener l : mOnProximityListeners) {
                            l.onData(true);
                        }
                    } else if (mOldProximity < THRESHOLD && proximity > THRESHOLD) {
                        for (OnProximityListener l : mOnProximityListeners) {
                            l.onData(false);
                        }
                    }
                    mOldProximity = proximity;

                    break;
            }
        }
    }

    private class Ambi extends VCNL4010State {
        @Override
        void stop() {
            stopRead(SLAVE_ADDRESS, REG_AMBI_DATA_H);
            onFinish();
        }

        @Override
        void onReadData(final byte[] data, final int register) {

            if (mState != register) {
                // レジスタが一致しない場合には、不正なデータなので無視
                return;
            }

            int offset = 5;
            switch(register) {
                case DEVICE_REG:
                    int deviceId = decodeByte(data[offset++], data[offset]);
                    if (deviceId == DEVICE_ID) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                setVCNL4010();

                                // 設定が反映されるまで、少し時間がかかるのスリープを入れておく
                                try {
                                    Thread.sleep(33);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                mState = REG_AMBI_DATA_H;
                                startRead(SLAVE_ADDRESS, REG_AMBI_DATA_H, 2);
                            }
                        }).start();
                    } else {
                        onError("VCNL4010 is not connect.");
                    }
                    break;

                case REG_AMBI_DATA_H:
                    int lux = FirmataUtil.decodeUShort2(data, offset);
                    break;
            }
        }
    }
}
