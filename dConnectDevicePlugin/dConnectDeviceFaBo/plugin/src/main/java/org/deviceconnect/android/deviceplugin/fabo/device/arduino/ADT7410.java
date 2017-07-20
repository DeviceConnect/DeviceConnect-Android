package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IADT7410;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private static final int BIT16_OP_MODE_1FAULT = 0x00;

    /**
     * ADXL実行カウント.
     */
    private int mRunningCount = 0;

    /**
     * 取得した値を通知するリスナ.
     */
    private final List<OnADT7410ListenerImpl> mOnADT7410Listeners = new CopyOnWriteArrayList<>();

    @Override
    public void read(final OnADT7410Listener listener) {
        OnADT7410ListenerImpl impl = new OnADT7410ListenerImpl();
        impl.setListener(listener);
        impl.setOnceRead(true);
        impl.start();
        mOnADT7410Listeners.add(impl);
    }

    @Override
    public void startRead(final OnADT7410Listener listener) {
        OnADT7410ListenerImpl impl = new OnADT7410ListenerImpl();
        impl.setListener(listener);
        impl.setOnceRead(false);
        impl.start();
        mOnADT7410Listeners.add(impl);
    }

    @Override
    public void stopRead(final OnADT7410Listener listener) {
        OnADT7410ListenerImpl impl = get(listener);
        if (impl != null) {
            mOnADT7410Listeners.remove(impl);
        }
        stopRead();
    }

    // BaseI2C interface

    @Override
    byte getAddress() {
        return ADT7410_DEVICE_ADDR;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset++]);
        switch (register) {
            case DEVICE_REG:
                int deviceId = decodeByte(data[offset++], data[offset]);
                if ((deviceId & DEVICE_ID) != 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setADT7410();
                            startRead(ADT7410_DEVICE_ADDR, REGISTER_CONFIG, 2);
                        }
                    }).start();
                } else {
                    for (OnADT7410Listener listener : mOnADT7410Listeners) {
                        listener.onError("ADT7410 is not connect.");
                    }
                }
                break;

            case REGISTER_CONFIG:
                double temp = convertTemperature(FirmataUtil.decodeUShort2(data, offset));
                for (OnADT7410Listener listener : mOnADT7410Listeners) {
                    listener.onData(temp);
                }
                break;
        }
    }

    /**
     * ADT7410の初期化を行います.
     */
    private void setADT7410() {
        write(ADT7410_DEVICE_ADDR, REGISTER_CONFIG, BIT16_RESOLUTION);
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

    /**
     * センサーの値の読み込みを開始します.
     */
    private void startRead() {
        mRunningCount++;

        if (mRunningCount == 1) {
            setI2CConfig();
            read(ADT7410_DEVICE_ADDR, DEVICE_REG, 1);
        }
    }

    /**
     * センサーの値の読み込みを停止します.
     */
    private void stopRead() {
        if (mRunningCount > 0) {
            mRunningCount--;

            if (mRunningCount == 0) {
                stopRead(ADT7410_DEVICE_ADDR, REGISTER_CONFIG);
            }
        }
    }

    /**
     * 指定されたリスナーをもつOnADXL345ListenerImplを取得します.
     * @param listener リスナー
     * @return OnADXL345ListenerImplのインスタンス
     */
    private OnADT7410ListenerImpl get(final OnADT7410Listener listener) {
        for (OnADT7410ListenerImpl impl : mOnADT7410Listeners) {
            if (impl.mListener == listener) {
                return impl;
            }
        }
        return null;
    }

    private class OnADT7410ListenerImpl implements OnADT7410Listener {

        /**
         * タイムアウトを監視するタイマー.
         */
        private Timer mTimer;

        /**
         * 通知を行うリスナー.
         */
        private OnADT7410Listener mListener;

        /**
         * 一度だけ読み込む場合.
         */
        private boolean mOnceRead;

        /**
         * 開始フラグ.
         */
        private boolean mStartFlag;

        /**
         * センサー開始とタイムアウト用のタイマーの開始を行います.
         */
        void start() {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onError("timeout");
                }
            }, 1000);
            startRead();
        }

        /**
         * リスナーを設定します.
         * @param listener リスナー
         */
        void setListener(final OnADT7410Listener listener) {
            mListener = listener;
        }

        /**
         * 一度だけ読み込みを行います.
         * @param onceRead フラグ
         */
        void setOnceRead(final boolean onceRead) {
            mOnceRead = onceRead;
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
            stopRead();
            mOnADT7410Listeners.remove(this);
        }

        @Override
        public void onStarted() {
        }

        @Override
        public void onData(double temperature) {
            if (!mStartFlag) {
                mStartFlag = true;
                cancelTimer();
                mListener.onStarted();
            }
            mListener.onData(temperature);

            if (mOnceRead) {
                onFinish();
            }
        }

        @Override
        public void onError(String message) {
            mListener.onError(message);
            onFinish();
        }
    }
}
