package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IISL29034;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;

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
     * 通知するリスナーのリスト.
     */
    private final List<OnAmbientLightListener> mListeners = new CopyOnWriteArrayList<>();

    /**
     * 現在のステート.
     * <p>
     * 現在、取得しているレジスタの番号になります。
     * </p>
     */
    private int mState = -1;

    /**
     * タイムアウトを監視するタイマー.
     */
    private Timer mTimer;

    @Override
    public void read(final OnAmbientLightListener listener) {
        mListeners.add(listener);

        if (mState != -1) {
            return;
        }
        mState = REG_ID;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onError("timeout");
            }
        }, 1000);

        setI2CConfig();
        read(SLAVE_ADDRESS, REG_ID, 1);
    }

    @Override
    public void startRead(final OnAmbientLightListener listener) {

    }

    @Override
    public void stopRead(final OnAmbientLightListener listener) {

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
    private void setISL29034() {
        setOperation(OP_ALS_CONT);
        setCMD2(FS_3, RES_16);
    }

    /**
     * Set Operation Mode.
     * @param config Operation Mode DEFAULT:Power-down the device
     */
    private void setOperation(int config) {
        write(SLAVE_ADDRESS, REG_CMD1, config);
    }

    /**
     * CMD2にrangeとresolutionを設定します.
     * @param range luxの範囲
     * @param resolution 解像度
     */
    private void setCMD2(final int range, final int resolution) {
        mRange = range;
        mResolution = resolution;
        mState = REG_CMD2;
        read(SLAVE_ADDRESS, REG_CMD2, 1);
    }

    /**
     * CMD2の設定をISL29034に送信します.
     * @param value ISL29034の設定
     */
    private void writeCMD2(byte value) {
        value &= 0xFC;
        value |= mRange;

        value &= 0xF3;
        value |= mResolution;

        write(SLAVE_ADDRESS, REG_CMD2, value);
    }

    /**
     * Analog to Digital Converterの値を読み込みます.
     */
    private void readADC() {
        mState = REG_DATA_L;
        read(SLAVE_ADDRESS, REG_DATA_L, 2);
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
     * Luxの値を通知します.
     * @param adc Analog to Digital Converterの値
     */
    private void notifyLux(final int adc) {
        float lux = convert(adc);
        for (OnAmbientLightListener listener : mListeners) {
            listener.onData(lux);
        }
        mListeners.clear();
        mState = -1;
        cancelTimer();
    }

    /**
     * エラーが発生したことを通知します.
     * @param message エラーメッセージ
     */
    private void onError(final String message) {
        for (OnAmbientLightListener listener : mListeners) {
            listener.onError(message);
        }
        mListeners.clear();
        mState = -1;
        cancelTimer();
    }

    /**
     * タイマーをキャンセルします.
     */
    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    byte getAddress() {
        return SLAVE_ADDRESS;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset++]);
        switch (register) {
            case REG_ID:
                int deviceId = decodeByte(data[offset++], data[offset]);
                if ((deviceId & ID_MASK) == DEVICE_ID) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setISL29034();
                        }
                    }).start();
                } else {
                    onError("ISL29034 is not connect.");
                }
                break;

            case REG_CMD2:
                final int cmd = decodeByte(data[offset++], data[offset]);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        writeCMD2((byte) cmd);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        readADC();
                    }
                }).start();
                break;

            case REG_DATA_L:
                notifyLux(FirmataUtil.decodeShort(data, offset));
                break;
        }
    }
}
