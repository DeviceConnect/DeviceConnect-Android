package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IADXL345;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeShort;

/**
 * ADXL345を操作するクラス.
 */
class ADXL345 extends BaseI2C implements IADXL345 {

    /**
     * ADXL345のアドレス.
     */
    private static final byte ADXL345_DEVICE_ADDR = 0x53;

    /**
     * ADXL345のデバイスID取得レジスタ.
     */
    private static final int REGISTER_RA_DEVID = 0x0;

    /**
     * ADXL345のパワー設定レジスタ.
     */
    private static final int REGISTER_RA_POWER_CTL = 0x2D;

    /**
     * ADXL345のデータフォーマット設定レジスタ.
     */
    private static final int REGISTER_RA_DATA_FORMAT = 0x31;

    /**
     * ADXL345のデータ取得レジスタ.
     */
    private static final int REGISTER_RA_DATAX0 = 0x32;

    /**
     * ADXL345のデバイスID.
     */
    private static final int DEVICE_ID = 0xE5;

    /**
     * 13bitの分解能.
     */
    private static final double RESOLUTION = (16 + 16) / Math.pow(2, 13);

    /**
     * 取得した値を通知するリスナ.
     */
    private final List<OnADXL345ListenerImpl> mOnADXL345Listeners = new CopyOnWriteArrayList<>();

    /**
     * ADXL実行カウント.
     */
    private int mRunningCount = 0;

    @Override
    public synchronized void read(final OnADXL345Listener listener) {
        OnADXL345ListenerImpl impl = new OnADXL345ListenerImpl();
        impl.setOnADXL345Listener(listener);
        impl.setOnceRead(true);
        impl.startADXL345();
        mOnADXL345Listeners.add(impl);
    }

    @Override
    public synchronized void startRead(final OnADXL345Listener listener) {
        OnADXL345ListenerImpl impl = new OnADXL345ListenerImpl();
        impl.setOnADXL345Listener(listener);
        impl.setOnceRead(false);
        impl.startADXL345();
        mOnADXL345Listeners.add(impl);
    }

    @Override
    public synchronized void stopRead(final OnADXL345Listener listener) {
        OnADXL345ListenerImpl impl = get(listener);
        if (impl != null) {
            mOnADXL345Listeners.remove(impl);
        }
        stopRead();
    }

    // BaseI2C interface

    @Override
    byte getAddress() {
        return ADXL345_DEVICE_ADDR;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset++]);
        if (register == REGISTER_RA_DEVID) {
            int deviceId = decodeByte(data[offset++], data[offset]);
            if (deviceId == DEVICE_ID) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setADXL345();
                        startRead(ADXL345_DEVICE_ADDR, REGISTER_RA_DATAX0, 6);
                    }
                }).start();
            } else {
                for (OnADXL345Listener listener : mOnADXL345Listeners) {
                    listener.onError("ADXL345 is not connect.");
                }
            }
        } else if (register == REGISTER_RA_DATAX0) {
            int ax = decodeShort(data, offset);
            offset += 4;
            int ay = decodeShort(data, offset);
            offset += 4;
            int az = decodeShort(data, offset);

            double gx = convertResolution(ax);
            double gy = convertResolution(ay);
            double gz = convertResolution(az);

            for (OnADXL345Listener listener : mOnADXL345Listeners) {
                listener.onData(gx, gy, gz);
            }
        }
    }

    /**
     * ADXL345の初期化を行います.
     */
    private void setADXL345() {
        write(ADXL345_DEVICE_ADDR, REGISTER_RA_DATA_FORMAT, 0x0B);
        write(ADXL345_DEVICE_ADDR, REGISTER_RA_POWER_CTL, 0x08);
    }

    /**
     * センサーの値の読み込みを開始します.
     */
    private void startRead() {
        mRunningCount++;

        if (mRunningCount == 1) {
            setI2CConfig();
            read(ADXL345_DEVICE_ADDR, REGISTER_RA_DEVID, 1);
        }
    }

    /**
     * センサーの値の読み込みを停止します.
     */
    private void stopRead() {
        if (mRunningCount > 0) {
            mRunningCount--;

            if (mRunningCount == 0) {
                stopRead(ADXL345_DEVICE_ADDR, REGISTER_RA_DATAX0);
            }
        }
    }

    /**
     * 指定されたリスナーをもつOnADXL345ListenerImplを取得します.
     * @param listener リスナー
     * @return OnADXL345ListenerImplのインスタンス
     */
    private OnADXL345ListenerImpl get(final OnADXL345Listener listener) {
        for (OnADXL345ListenerImpl impl : mOnADXL345Listeners) {
            if (impl.mListener == listener) {
                return impl;
            }
        }
        return null;
    }

    /**
     * 加速度センサーの値を重力加速度に変換します.
     * @param data 加速度センサーからの値
     * @return 重力加速度
     */
    private double convertResolution(final int data) {
        return data * RESOLUTION;
    }

    /**
     * タイマー付きでADXL345のセンサー値を開始します.
     */
    private class OnADXL345ListenerImpl implements OnADXL345Listener {
        /**
         * タイムアウトを監視するタイマー.
         */
        private Timer mTimer;

        /**
         * 通知を行うリスナー.
         */
        private OnADXL345Listener mListener;

        /**
         * 一度だけ読み込む場合.
         */
        private boolean mOnceRead;

        /**
         * 開始フラグ.
         */
        private boolean mStartFlag;

        /**
         * ADXL345のセンサー開始とタイムアウト用のタイマーの開始を行います.
         */
        void startADXL345() {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onError("timeout");
                }
            }, 1000);
            startRead();
        }

        void setOnADXL345Listener(final OnADXL345Listener listener) {
            mListener = listener;
        }

        /**
         * 一度だけ読み込みを行います.
         * @param onceRead
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
            mOnADXL345Listeners.remove(this);
        }

        @Override
        public void onStarted() {
        }

        @Override
        public void onData(final double x, final double y, final double z) {
            if (!mStartFlag) {
                mStartFlag = true;
                cancelTimer();
                mListener.onStarted();
            }
            mListener.onData(x, y, z);

            if (mOnceRead) {
                onFinish();
            }
        }

        @Override
        public void onError(final String message) {
            mListener.onError(message);
            onFinish();
        }
    }
}
