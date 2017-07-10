package org.deviceconnect.android.deviceplugin.fabo.device.arduino;


import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.END_SYSEX;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_CONFIG;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_READ;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_READ_CONTINUOUSLY;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_REQUEST;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_STOP_READING;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.I2C_WRITE;
import static org.deviceconnect.android.deviceplugin.fabo.device.arduino.FirmataV32.START_SYSEX;

class BaseI2C {

    /**
     * Usbに接続されたデバイスを管理するクラス.
     */
    private FaBoUsbDeviceControl mFaBoDeviceControl;

    /**
     * FaBoDeviceControlのインスタンスを取得します.
     * @return FaBoDeviceControlのインスタンス
     */
    FaBoUsbDeviceControl getFaBoDeviceControl() {
        return mFaBoDeviceControl;
    }

    /**
     * FaBoDeviceControlのインスタンスを設定します.
     * @param controller FaBoDeviceControlのインスタンス
     */
    void setFaBoDeviceControl(final FaBoUsbDeviceControl controller) {
        mFaBoDeviceControl = controller;
    }

    /**
     * I2Cコンフィグを送信します.
     */
    void setI2CConfig() {
        byte[] config = {
                START_SYSEX,
                I2C_CONFIG,
                (byte) 0x00,
                (byte) 0x00,
                END_SYSEX
        };
        writeI2C(config);
    }

    /**
     * I2Cにデータを送信します.
     * @param data 送信するデータ
     */
    void writeI2C(final byte[] data) {
        getFaBoDeviceControl().writeI2C(data);
    }

    /**
     * 指定されたアドレスにSysex messageを送信します.
     * @param address アドレス
     * @param a 送信するデータ1
     * @param b 送信するデータ2
     */
    void write(final byte address, final int a, final int b) {
        byte[] config = {
                START_SYSEX,
                I2C_REQUEST,
                address,
                I2C_WRITE,
                (byte) (a & 0x7f),
                (byte) ((a >> 7) & 0x7f),
                (byte) (b & 0x7f),
                (byte) ((b >> 7) & 0x7f),
                END_SYSEX
        };
        writeI2C(config);
    }

    /**
     * 指定されたアドレスからデータを読み込みます.
     * @param address アドレス
     * @param register レジスタ
     * @param size 読み込むデータサイズ
     */
    void read(final byte address, final int register, final int size) {
        byte[] command = {
                START_SYSEX,
                I2C_REQUEST,
                address,
                I2C_READ,
                (byte) (register & 0x7f),
                (byte) ((register >> 7) & 0x7f),
                (byte) (size & 0x7f),
                (byte) ((size >> 7) & 0x7f),
                END_SYSEX
        };
        writeI2C(command);
    }

    /**
     * 指定されたアドレスからデータを読み込みを開始します.
     * @param address アドレス
     * @param register レジスタ
     * @param size 読み込むデータサイズ
     */
    void startRead(final byte address, final int register, final int size) {
        byte[] command = {
                START_SYSEX,
                I2C_REQUEST,
                address,
                I2C_READ_CONTINUOUSLY,
                (byte) (register & 0x7f),
                (byte) ((register >> 7) & 0x7f),
                (byte) (size & 0x7f),
                (byte) ((size >> 7) & 0x7f),
                END_SYSEX
        };
        writeI2C(command);
    }

    /**
     * 指定されたアドレスからデータを読み込みを停止します.
     * @param address アドレス
     * @param register レジスタ
     */
    void stopRead(final byte address, final int register) {
        byte[] command = {
                START_SYSEX,
                I2C_REQUEST,
                address,
                I2C_STOP_READING,
                (byte) (register & 0x7f),
                (byte) ((register >> 7) & 0x7f),
                END_SYSEX
        };
        writeI2C(command);
    }

    /**
     * I2Cのアドレスを取得します.
     * @return アドレス
     */
    byte getAddress() {
        return -1;
    }

    /**
     * I2Cから読み込まれたデータ.
     * @param data データ
     */
    void onReadData(final byte[] data) {}
}
