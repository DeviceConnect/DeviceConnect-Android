package org.deviceconnect.android.deviceplugin.fabo.device.arduino;

import org.deviceconnect.android.deviceplugin.fabo.device.IMPL115;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataUtil.decodeByte;

class MPL115 extends BaseI2C implements IMPL115 {

    // MPL115A2 I2C slave address
    private static final byte SLAVE_ADDRESS = 0x60;

    // MPL115A2 Register Address
    private static final int PADC_MSB = 0x00;
    private static final int PADC_LSB = 0x01;
    private static final int TADC_MSB = 0x02;
    private static final int TACD_LSB = 0x03;
    private static final int A0_MSB = 0x04;
    private static final int A0_LSB = 0x05;
    private static final int B1_MSB = 0x06;
    private static final int B1_LSB = 0x07;
    private static final int B2_MSB = 0x08;
    private static final int B2_LSB = 0x09;
    private static final int C12_MSB = 0x0A;
    private static final int C12_LSB = 0x0B;
    private static final int CONVERT = 0x12;

    private List<OnAtmosphericPressureListener> mOnAtmosphericPressureListeners = new CopyOnWriteArrayList<>();

    private float mA0;
    private float mB1;
    private float mB2;
    private float mC12;

    private Timer mTimer;

    private boolean mRunningFlag;

    @Override
    public void readAtmosphericPressure(final OnAtmosphericPressureListener listener) {
        mOnAtmosphericPressureListeners.add(listener);

        if (mRunningFlag) {
            return;
        }
        mRunningFlag = true;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onError("timeout");
            }
        }, 1000);

        setI2CConfig();
        readCoef();
    }

    // BaseI2C interface

    @Override
    byte getAddress() {
        return SLAVE_ADDRESS;
    }

    @Override
    void onReadData(final byte[] data) {
        int offset = 3;
        int register = decodeByte(data[offset++], data[offset++]);
        switch (register) {
            case A0_MSB: {
                int[] value = new int[8];
                for (int i = 0; i < value.length; i++) {
                    value[i] = decodeByte(data[offset++], data[offset++]);
                }
                onReadCoef(value);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readData();
                    }
                }).start();
            }   break;

            case PADC_MSB: {
                int[] value = new int[4];
                for (int i = 0; i < value.length; i++) {
                    value[i] = decodeByte(data[offset++], data[offset++]);
                }
                onReadData(value);
            }   break;
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void onError(final String message) {
        cancelTimer();

        for (OnAtmosphericPressureListener listener : mOnAtmosphericPressureListeners) {
            listener.onError(message);
        }
        mRunningFlag = false;
    }

    private void readCoef() {
        read(SLAVE_ADDRESS, A0_MSB, 8);
    }

    private void onReadCoef(final int[] data) {
        mA0 = FirmataUtil.dataConv(data[1], data[0]) / (float) (1 << 3);
        mB1 = FirmataUtil.dataConv(data[3], data[2]) / (float) (1 << 13);
        mB2 = FirmataUtil.dataConv(data[5], data[4]) / (float) (1 << 14);
        mC12 = FirmataUtil.dataConv(data[7], data[6]) / (float) (1 << 24);
    }

    private void readData() {
        write(SLAVE_ADDRESS, CONVERT, 0x01);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        read(SLAVE_ADDRESS, PADC_MSB, 4);
    }

    private void onReadData(final int[] data) {
        int padc = ((data[0] << 8) | data[1]) >> 6;
        int tadc = ((data[2] << 8) | data[3]) >> 6;

        double pcomp = mA0 + (mB1 + mC12 * tadc) * padc + mB2 * tadc;
        double hpa = pcomp * ((1150.0 - 500.0) / 1023.0) + 500.0;
        double temp = 25.0 - (tadc - 512.0) / 5.35;

        for (OnAtmosphericPressureListener listener : mOnAtmosphericPressureListeners) {
            listener.onData(hpa, temp);
        }
        mOnAtmosphericPressureListeners.clear();

        cancelTimer();

        mRunningFlag = false;
    }
}
