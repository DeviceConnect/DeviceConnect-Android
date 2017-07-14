package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IADXL345;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ADXL345を操作するクラス.
 * <p>
 * Brick #201
 * </p>
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
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;

    /**
     * 加速度の値を保持するバッファ.
     */
    private byte[] mBuffer = new byte[6];

    /**
     * 加速度センサーの値を監視するスレッド.
     */
    private WatchThread mWatchThread;

    /**
     * コンストラクタ.
     * @param control コントローラ
     */
    ADXL345(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(ADXL345_DEVICE_ADDR);
    }

    @Override
    public void read(final OnADXL345Listener listener) {
        try {
            if (!checkDevice()) {
                listener.onError("ADXL345 is not connect.");
            } else {
                startADXL345();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                readADXL345(mBuffer, mBuffer.length);

                double x = convertResolution(decodeShort(mBuffer, 0));
                double y = convertResolution(decodeShort(mBuffer, 2));
                double z = convertResolution(decodeShort(mBuffer, 4));

                listener.onData(x, y, z);
            }
        } catch (IOException e) {
            listener.onError(e.getMessage());
        }
    }

    @Override
    public synchronized void startRead(final OnADXL345Listener listener) {
        if (!checkDevice()) {
            listener.onError("ADXL345 is not connect.");
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
    public synchronized void stopRead(final OnADXL345Listener listener) {
        if (mWatchThread != null) {
            mWatchThread.removeListener(listener);
            if (mWatchThread.isEmptyListener()) {
                mWatchThread.stopWatch();
                mWatchThread = null;
            }
        }
    }

    @Override
    synchronized void destroy() {
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
            try {
                byte deviceId = mI2cDevice.readRegByte(REGISTER_RA_DEVID);
                return (deviceId & 0xFF) == DEVICE_ID;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * ADXL345の加速度センサー開始要求を送信します.
     * @throws IOException 送信に失敗した場合に発生
     */
    private void startADXL345() throws IOException {
        mI2cDevice.writeRegByte(REGISTER_RA_DATA_FORMAT, (byte) 0x0B);
        mI2cDevice.writeRegByte(REGISTER_RA_POWER_CTL, (byte) 0x08);
    }

    /**
     * ADXL345から加速度センサーの値を取得します.
     * @param buffer バッファ
     * @param length バッファサイズ
     * @throws IOException 読み込みに失敗した場合に発生
     */
    private void readADXL345(final byte[] buffer, final int length) throws IOException {
        mI2cDevice.readRegBuffer(REGISTER_RA_DATAX0, buffer, length);
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
     * 加速度センサーの値を取得するスレッド.
     */
    private class WatchThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * リスナー.
         */
        private List<OnADXL345Listener> mListeners = new CopyOnWriteArrayList<>();

        /**
         * リスナーを追加します.
         * @param listener 追加するリスナー
         */
        void addListener(final OnADXL345Listener listener) {
            mListeners.add(listener);
            listener.onStarted();
        }

        /**
         * リスナーを削除します.
         * @param listener 削除するリスナー
         */
        void removeListener(final OnADXL345Listener listener) {
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
            final byte[] buffer = new byte[6];

            try {
                startADXL345();

                while (!mStopFlag) {
                    readADXL345(buffer, buffer.length);

                    double x = convertResolution(decodeShort(buffer, 0));
                    double y = convertResolution(decodeShort(buffer, 2));
                    double z = convertResolution(decodeShort(buffer, 4));

                    for (OnADXL345Listener l : mListeners) {
                        l.onData(x, y, z);
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                for (OnADXL345Listener l : mListeners) {
                    l.onError(e.getMessage());
                }
                mListeners.clear();
            }
        }
    }
}
