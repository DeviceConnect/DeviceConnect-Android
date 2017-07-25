package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.ILIDARLiteV3;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class LIDARLiteV3 extends BaseI2C implements ILIDARLiteV3 {
    // ISL29034 I2C Slave Address
    private static final byte SLAVE_ADDRESS = 0x62;

    /**
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;

    /**
     * 読み込み用バッファ.
     */
    private final byte[] mBuffer = new byte[2];

    /**
     * 監視スレッド.
     */
    private WatchThread mWatchThread;

    /**
     * コンストラクタ.
     * @param control コントローラ
     */
    LIDARLiteV3(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(SLAVE_ADDRESS);
    }

    @Override
    public void read(final OnLIDARLiteListener listener) {
        if (!checkDevice()) {
            listener.onError("LIDARLiteV3 is not connect.");
        } else {
            try {
                setLIDARLiteConfig(1);
                listener.onData(readDistance());
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void startRead(final OnLIDARLiteListener listener) {
        if (!checkDevice()) {
            listener.onError("LIDARLiteV3 is not connect.");
        } else {
            if (mWatchThread == null) {
                mWatchThread = new WatchThread();
                mWatchThread.addListener(listener);
                mWatchThread.start();
            } else {
                mWatchThread.addListener(listener);
            }
        }
    }

    @Override
    public synchronized void stopRead(final OnLIDARLiteListener listener) {
        if (mWatchThread != null) {
            mWatchThread.removeListener(listener);
            if (mWatchThread.isEmptyListener()) {
                mWatchThread.stopWatch();
                mWatchThread = null;
            }
        }
    }

    @Override
    void destroy() {
        if (mWatchThread != null) {
            mWatchThread.stopWatch();
            mWatchThread = null;
        }
    }

    /**
     * 接続されているデバイスがADXL345か確認を行う.
     * @return ADXL345ならtrue、それ以外ならfalse
     */
    private boolean checkDevice() {
        if (mI2cDevice == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * LIDARLite v3の設定を行います.
     * @param configuration 設定番号
     * @throws IOException 設定に失敗した場合に発生
     */
    private synchronized void setLIDARLiteConfig(final int configuration) throws IOException {
        switch (configuration) {
            case 0: // Default mode, balanced performance
                mI2cDevice.writeRegByte(0x02, (byte) 0x80); // Default
                mI2cDevice.writeRegByte(0x04, (byte) 0x08); // Default
                mI2cDevice.writeRegByte(0x1C, (byte) 0x00); // Default
                break;

            case 1: // Short range, high speed
                mI2cDevice.writeRegByte(0x02, (byte) 0x1d);
                mI2cDevice.writeRegByte(0x04, (byte) 0x08); // Default
                mI2cDevice.writeRegByte(0x1c, (byte) 0x00); // Default
                break;

            case 2: // Default range, higher speed short range
                mI2cDevice.writeRegByte(0x02, (byte) 0x80); // Default
                mI2cDevice.writeRegByte(0x04, (byte) 0x00);
                mI2cDevice.writeRegByte(0x1c, (byte) 0x00); // Default
                break;

            case 3: // Maximum range
                mI2cDevice.writeRegByte(0x02, (byte) 0xff);
                mI2cDevice.writeRegByte(0x04, (byte) 0x08); // Default
                mI2cDevice.writeRegByte(0x1c, (byte) 0x00); // Default
                break;

            case 4: // High sensitivity detection, high erroneous measurements
                mI2cDevice.writeRegByte(0x02, (byte) 0x80); // Default
                mI2cDevice.writeRegByte(0x04, (byte) 0x08); // Default
                mI2cDevice.writeRegByte(0x1c, (byte) 0x80);
                break;

            case 5: // Low sensitivity detection, low erroneous measurements
                mI2cDevice.writeRegByte(0x02, (byte) 0x80); // Default
                mI2cDevice.writeRegByte(0x04, (byte) 0x08); // Default
                mI2cDevice.writeRegByte(0x1c, (byte) 0xb0);
                break;
        }
    }

    /**
     * 距離センサーの値を取得します.
     * @return 距離
     * @throws IOException 距離センサーの値の読み込みに失敗した場合に発生
     */
    private synchronized int readDistance() throws IOException {
        mI2cDevice.writeRegByte(0x00, (byte) 0x04);
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mI2cDevice.readRegBuffer(0x8F, mBuffer, mBuffer.length);
        return decodeUShort2(mBuffer, 0);
    }

    /**
     * 距離センサーを監視するクラス.
     */
    private class WatchThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * リスナー.
         */
        private List<OnLIDARLiteListener> mListeners = new CopyOnWriteArrayList<>();

        /**
         * リスナーを追加します.
         * @param listener 追加するリスナー
         */
        void addListener(final OnLIDARLiteListener listener) {
            mListeners.add(listener);
            listener.onStarted();
        }

        /**
         * リスナーを削除します.
         * @param listener 削除するリスナー
         */
        void removeListener(final OnLIDARLiteListener listener) {
            mListeners.remove(listener);
        }

        /**
         * 登録されているリスナーが空か確認します.
         * @return 空の場合はtrue、それ以外はfalse
         */
        boolean isEmptyListener() {
            return mListeners.isEmpty();
        }

        /**
         * 監視を停止します.
         */
        void stopWatch() {
            mListeners.clear();
            mStopFlag = true;
            interrupt();
        }

        @Override
        public void run() {
            try {
                setLIDARLiteConfig(1);

                while (!mStopFlag) {
                    int distance = readDistance();

                    for (OnLIDARLiteListener l : mListeners) {
                        l.onData(distance);
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                for (OnLIDARLiteListener l : mListeners) {
                    l.onError(e.getMessage());
                }
                mListeners.clear();
            }
        }
    }
}
