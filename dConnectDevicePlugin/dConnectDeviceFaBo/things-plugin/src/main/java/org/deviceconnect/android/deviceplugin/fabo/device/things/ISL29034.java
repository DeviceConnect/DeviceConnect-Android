package org.deviceconnect.android.deviceplugin.fabo.device.things;

import com.google.android.things.pio.I2cDevice;

import org.deviceconnect.android.deviceplugin.fabo.device.IISL29034;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ISL29034を操作するクラス.
 * <p>
 * Brick #217
 * </p>
 */
class ISL29034 extends BaseI2C implements IISL29034 {

    // ISL29034 I2C Slave Address
    private static final byte SLAVE_ADDRESS = 0x44;

    // ISL29034 Device ID(xx101xxx)
    private static final int DEVICE_ID = 0x28;

    // Register Addresses
    private static final int REG_CMD1 = 0x00;
    private static final int REG_CMD2 = 0x01;
    private static final int REG_DATA_L = 0x02;
    private static final int REG_DATA_H = 0x03;
    private static final int REG_ID = 0x0F;

    // Operation Mode
    private static final int OP_PWR_DOWN = 0x00; // Power-down the device(Default)
    private static final int OP_ALS_CONT = 0xA0; // Measures ALS continuously

    // FULL SCALE LUX RANGE
    private static final int FS_0 = 0x00; // 1,000(Default)
    private static final int FS_1 = 0x01; // 4,000
    private static final int FS_2 = 0x02; // 16,000
    private static final int FS_3 = 0x03; // 64,000

    // ADC RESOLUTION
    private static final int RES_16 = 0x00; // 16bit(Default)
    private static final int RES_12 = 0x04; // 12bit
    private static final int RES_8 = 0x08; // 8bit
    private static final int RES_4 = 0x0C; // 4bit

    private static final int ID_MASK = 0x38; // ISL29034 Device ID Mask(00111000)

    /**
     * ISL29034に設定する範囲.
     */
    private int mRange = FS_0;

    /**
     * ISL29034に設定する解像度.
     */
    private int mResolution = RES_4;

    /**
     * I2Cデバイス.
     */
    private I2cDevice mI2cDevice;

    /**
     * 管理用スレッド.
     */
    private WatchThread mWatchThread;

    /**
     * コンストラクタ.
     * @param control FaBoコントローラ
     */
    ISL29034(final FaBoThingsDeviceControl control) {
        mI2cDevice = control.getI2cDevice(SLAVE_ADDRESS);
    }

    @Override
    public void read(final OnAmbientLightListener listener) {
        if (!checkDevice()) {
            listener.onError("ISL29034 is not connect.");
        } else {
            try {
                setISL29034();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                listener.onData(readADC());
            } catch (IOException e) {
                listener.onError(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void startRead(final OnAmbientLightListener listener) {
        if (!checkDevice()) {
            listener.onError("ISL29034 is not connect.");
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
    public synchronized void stopRead(final OnAmbientLightListener listener) {
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
     * 接続されているデバイスがISL29034か確認を行う.
     * @return ISL29034ならtrue、それ以外ならfalse
     */
    private boolean checkDevice() {
        if (mI2cDevice == null) {
            return false;
        } else {
            try {
                byte deviceId = mI2cDevice.readRegByte(REG_ID);
                return ((deviceId & ID_MASK) == DEVICE_ID);
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * 範囲の値を取得します.
     * @return 範囲
     */
    private int getRange() {
        switch (mRange) {
            case FS_0:
                return 1000;
            case FS_1:
                return 4000;
            case FS_2:
                return 16000;
            case FS_3:
                return 64000;
            default:
                return -1;
        }
    }

    /**
     * 解像度の値を取得します.
     * @return 解像度
     */
    private int getResolution() {
        switch (mResolution) {
            case RES_16:
                return 65535;
            case RES_12:
                return 4095;
            case RES_8:
                return 255;
            case RES_4:
                return 15;
            default:
                return -1;
        }
    }

    /**
     * ISL29034の設定を行います.
     */
    private void setISL29034() throws IOException {
        setOperation(OP_ALS_CONT);
        setCMD2(FS_3, RES_16);
    }

    /**
     * Set Operation Mode.
     * @param config Operation Mode DEFAULT:Power-down the device
     */
    private void setOperation(int config) throws IOException {
        mI2cDevice.writeRegByte(REG_CMD1, (byte) (config & 0xFF));
    }

    /**
     * CMD2にrangeとresolutionを設定します.
     * @param range luxの範囲
     * @param resolution 解像度
     */
    private void setCMD2(final int range, final int resolution) throws IOException {
        mRange = range;
        mResolution = resolution;
        byte value = mI2cDevice.readRegByte(REG_CMD2);

        value &= 0xFC;
        value |= mRange;

        value &= 0xF3;
        value |= mResolution;

        mI2cDevice.writeRegByte(REG_CMD2, value);
    }

    /**
     * Analog to Digital Converterの値を読み込みます.
     */
    private float readADC() throws IOException {
        byte[] buffer = new byte[2];
        mI2cDevice.readRegBuffer(REG_DATA_L, buffer, 2);
        return convert(decodeShort(buffer, 0));
    }

    /**
     * Analog to Digital Converterの値をluxに変換します.
     * @param adc Analog to Digital Converterの値
     * @return lux
     */
    private float convert(final int adc) {
        int range = getRange();
        int count = getResolution();
        return (range / (float) count) * adc;
    }

    /**
     * 管理用スレッド.
     */
    private class WatchThread extends Thread {
        /**
         * 停止フラグ.
         */
        private boolean mStopFlag;

        /**
         * リスナー.
         */
        private List<OnAmbientLightListener> mListeners = new CopyOnWriteArrayList<>();

        /**
         * リスナーを追加します.
         * @param listener 追加するリスナー
         */
        void addListener(final OnAmbientLightListener listener) {
            mListeners.add(listener);
            listener.onStarted();
        }

        /**
         * リスナーを削除します.
         * @param listener 削除するリスナー
         */
        void removeListener(final OnAmbientLightListener listener) {
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
                setISL29034();

                while (!mStopFlag) {
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double lux = readADC();
                    for (OnAmbientLightListener l : mListeners) {
                        l.onData(lux);
                    }
                }
            } catch (IOException e) {
                for (OnAmbientLightListener l : mListeners) {
                    l.onError(e.getMessage());
                }
                mListeners.clear();
            }
        }
    }
}
