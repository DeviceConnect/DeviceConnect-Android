package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.ILIDARLiteV3;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;

class LIDARLiteV3 extends BaseI2C implements ILIDARLiteV3 {

    // I2C LIDARLite v3 Slave Address
    private static final byte SLAVE_ADDRESS = 0x62;

    private List<OnLIDARLiteListenerImpl> mOnLIDARLiteListeners = new CopyOnWriteArrayList<>();

    @Override
    public void read(final OnLIDARLiteListener listener) {
        setI2CConfig();
        setLIDARLiteConfig(1);

        OnLIDARLiteListenerImpl impl = new OnLIDARLiteListenerImpl();
        impl.setListener(listener);
        impl.setOnceRead(true);
        impl.start();
        mOnLIDARLiteListeners.add(impl);
    }

    @Override
    public void startRead(final OnLIDARLiteListener listener) {
        setI2CConfig();
        setLIDARLiteConfig(1);

        OnLIDARLiteListenerImpl impl = new OnLIDARLiteListenerImpl();
        impl.setListener(listener);
        impl.setOnceRead(false);
        impl.start();
        mOnLIDARLiteListeners.add(impl);
    }

    @Override
    public void stopRead(final OnLIDARLiteListener listener) {
        OnLIDARLiteListenerImpl impl = get(listener);
        if (impl != null) {
            impl.stop();
        }
    }

    @Override
    byte getAddress() {
        return SLAVE_ADDRESS;
    }

    @Override
    void onReadData(byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset++]);
        switch (register) {
            case 0x8F:
                int distance = FirmataUtil.decodeUShort2(data, offset);
                for (OnLIDARLiteListener listener : mOnLIDARLiteListeners) {
                    listener.onData(distance);
                }

                if (!mOnLIDARLiteListeners.isEmpty()) {
                    // LIDARLiteは連続で読み込みができないので、
                    // ここで、再度readを呼び出します。
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(33);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            startRead();
                        }
                    }).start();
                }
                break;
        }
    }

    /**
     * 指定されたリスナーをもつOnLIDARLiteListenerImplを取得します.
     * @param listener リスナー
     * @return OnLIDARLiteListenerImplのインスタンス
     */
    private OnLIDARLiteListenerImpl get(final OnLIDARLiteListener listener) {
        for (OnLIDARLiteListenerImpl impl : mOnLIDARLiteListeners) {
            if (impl.mListener == listener) {
                return impl;
            }
        }
        return null;
    }

    /**
     * LIDARLite v3の設定を行います.
     * @param configuration 設定番号
     */
    private synchronized void setLIDARLiteConfig(final int configuration) {
        switch (configuration) {
            case 0: // Default mode, balanced performance
                write(SLAVE_ADDRESS, 0x02, 0x80); // Default
                write(SLAVE_ADDRESS, 0x04, 0x08); // Default
                write(SLAVE_ADDRESS, 0x1C, 0x00); // Default
                break;

            case 1: // Short range, high speed
                write(SLAVE_ADDRESS, 0x02, 0x1d);
                write(SLAVE_ADDRESS, 0x04, 0x08); // Default
                write(SLAVE_ADDRESS, 0x1c, 0x00); // Default
                break;

            case 2: // Default range, higher speed short range
                write(SLAVE_ADDRESS, 0x02, 0x80); // Default
                write(SLAVE_ADDRESS, 0x04, 0x00);
                write(SLAVE_ADDRESS, 0x1c, 0x00); // Default
                break;

            case 3: // Maximum range
                write(SLAVE_ADDRESS, 0x02, 0xff);
                write(SLAVE_ADDRESS, 0x04, 0x08); // Default
                write(SLAVE_ADDRESS, 0x1c, 0x00); // Default
                break;

            case 4: // High sensitivity detection, high erroneous measurements
                write(SLAVE_ADDRESS, 0x02, 0x80); // Default
                write(SLAVE_ADDRESS, 0x04, 0x08); // Default
                write(SLAVE_ADDRESS, 0x1c, 0x80);
                break;

            case 5: // Low sensitivity detection, low erroneous measurements
                write(SLAVE_ADDRESS, 0x02, 0x80); // Default
                write(SLAVE_ADDRESS, 0x04, 0x08); // Default
                write(SLAVE_ADDRESS, 0x1c, 0xb0);
                break;
        }
    }

    /**
     * センサーの値の読み込みを開始します.
     */
    private synchronized void startRead() {
        write(SLAVE_ADDRESS, 0x00, 0x04);
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        read(SLAVE_ADDRESS, 0x8F, 2);
    }

    private class OnLIDARLiteListenerImpl implements OnLIDARLiteListener {

        /**
         * タイムアウトを監視するタイマー.
         */
        private Timer mTimer;

        /**
         * 通知を行うリスナー.
         */
        private OnLIDARLiteListener mListener;

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
        void setListener(final OnLIDARLiteListener listener) {
            mListener = listener;
        }

        /**
         * 一度だけ読み込みを行います.
         * @param onceRead trueの場合は1だけ読み込み、falseの場合は連続で読み込む
         */
        void setOnceRead(final boolean onceRead) {
            mOnceRead = onceRead;
        }

        /**
         * 停止フラグを設定します.
         */
        void stop() {
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
            mOnLIDARLiteListeners.remove(this);
        }

        @Override
        public void onStarted() {
        }

        @Override
        public void onData(final int distance) {
            if (!mStartFlag) {
                mStartFlag = true;
                cancelTimer();
                mListener.onStarted();
            }
            mListener.onData(distance);

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
