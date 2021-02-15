/*
 UsbSerialPort.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libusb;

import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.libuvc.BuildConfig;

import java.io.IOException;

/**
 * USBデバイスのシリアルポート.
 *
 * @author NTT DOCOMO, INC.
 */
public class UsbSerialPort {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "USB";

    /**
     * USB管理するクラス.
     */
    private final UsbManager mUsbManager;

    /**
     * USBデバイス.
     */
    private final UsbDevice mUsbDevice;

    /**
     * USBデバイスとのコネクション.
     */
    private UsbDeviceConnection mConnection;

    /**
     * タイムアウト.
     */
    private int mTimeout = 1000;

    /**
     * デバイスID.
     */
    private final int mDeviceId;

    /**
     * ベンダーID.
     */
    private final int mVendorId;

    /**
     * プロダクトID.
     */
    private final int mProductId;

    /**
     * デバイス名.
     */
    private String mDeviceName;

    /**
     * シリアルナンバー.
     */
    private String mSerialNumber;

    /**
     * 製造業者名.
     */
    private String mManufacturerName;

    /**
     * バージョン.
     */
    private String mVersion;

    /**
     * コンストラクタ.
     *
     * @param manager USB管理クラス
     * @param device  USBデバイス
     */
    UsbSerialPort(final UsbManager manager, final UsbDevice device) {
        if (manager == null || device == null) {
            throw new IllegalArgumentException("manager or device is null.");
        }
        mUsbManager = manager;
        mUsbDevice = device;

        mDeviceId = mUsbDevice.getDeviceId();
        mVendorId = mUsbDevice.getVendorId();
        mProductId = mUsbDevice.getProductId();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDeviceName = mUsbDevice.getProductName();
            mSerialNumber = mUsbDevice.getSerialNumber();
            mManufacturerName = mUsbDevice.getManufacturerName();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mVersion = mUsbDevice.getVersion();
        }
    }

    /**
     * USBデバイスを取得します.
     *
     * @return USBデバイス
     */
    public UsbDevice getUsbDevice() {
        return mUsbDevice;
    }

    /**
     * デバイス名を取得します.
     *
     * Android OS が LOLLIPOP 以下の場合は null を返却します。
     * USB からデバイス名が取得できなかった場合は null を返却します。
     *
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * バージョンを取得します.
     *
     * Android OS が M 未満の場合は null を返却します。
     * USB からバージョンが取得できなかった場合は null を返却します。
     *
     * @return バージョン
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * 製造業者名を取得します.
     *
     * Android OS が LOLLIPOP 以下の場合は null を返却します。
     * USB から製造業者名が取得できなかった場合は null を返却します。
     *
     * @return 製造業者名
     */
    public String getManufacturerName() {
        return mManufacturerName;
    }

    /**
     * シリアルナンバーを取得します.
     *
     * Android OS が LOLLIPOP 以下の場合は null を返却します。
     * USB からシリアルナンバーが取得できなかった場合は null を返却します。
     *
     * @return シリアルナンバー
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * ベンダー ID を取得します.
     *
     * @return ベンダー ID
     */
    public int getVendorId() {
        return mVendorId;
    }

    /**
     * プロダクト ID を取得します.
     *
     * @return プロダクト ID
     */
    public int getProductId() {
        return mProductId;
    }

    /**
     * デバイス ID を取得します.
     *
     * @return デバイス ID
     */
    public int getDeviceId() {
        return mDeviceId;
    }

    /**
     * USBデバイスに接続します.
     * <p>
     * 既に接続されている場合には、呼び出されても特に処理は行いません。
     * </p>
     * @throws IOException 接続に失敗した場合に発生
     */
    public synchronized void open() throws IOException {
        if (mConnection != null) {
            return;
        }

        mConnection = mUsbManager.openDevice(getUsbDevice());
        if (mConnection == null) {
            throw new IOException("Failed to connect device. device=" + mUsbDevice.getDeviceName());
        }
    }

    /**
     * 接続したUSBデバイスのファイルディスクリプタを取得します.
     *
     * @return ファイルディスクリプタ
     */
    public synchronized int getFileDescriptor() {
        checkConnected();
        return mConnection.getFileDescriptor();
    }

    /**
     * 接続したUSBデバイスのディスクリプタを取得します.
     *
     * @return ディスクリプタの配列
     */
    public byte[] getRawDescriptors() {
        checkConnected();
        return mConnection.getRawDescriptors();
    }

    /**
     * USBデバイスの接続を切断します.
     */
    public synchronized void close() {
        if (mConnection != null) {
            try {
                mConnection.close();
                mConnection = null;
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    /**
     * USBデバイスとの接続状態を取得します.
     *
     * @return 接続中の場合はtrue、それ以外はfalse
     */
    public synchronized boolean isConnected() {
        return mConnection != null;
    }

    /**
     * デバイスディスクリプタを読み込みます.
     *
     * @return デバイスディスクリプタ
     * @throws IOException 読み込みに失敗した場合に発生
     */
    public byte[] readDeviceDescriptor() throws IOException {
        return readDescriptor(UsbConstants.USB_DIR_IN, (1 << 8), 18);
    }

    /**
     * 指定されたIDのサブクラスのディスクリプタを取得します.
     *
     * @param value ID
     * @param length サイズ
     * @return ディスクリプタの配列
     * @throws IOException 読み込みに失敗した場合
     */
    public byte[] readSubClassDescriptor(final int value, final int length) throws IOException {
        checkConnected();

        int requestType = UsbConstants.USB_DIR_IN | UsbConstants.USB_INTERFACE_SUBCLASS_BOOT;
        return readDescriptor(requestType, value, length);
    }

    /**
     * コンフィグレーションディスクリプタを読み込みます.
     *
     * @return コンフィグレーションディスクリプタ
     * @throws IOException 読み込みに失敗した場合に発生
     */
    public byte[] readConfiguration() throws IOException {
        checkConnected();

        int requestType = UsbConstants.USB_DIR_IN;
        int request = 8; // GET_CONFIGURATION
        int value = 0;
        int index = 0;
        int length = 1;
        byte[] buffer = new byte[length];

        int result = mConnection.controlTransfer(requestType, request, value, index, buffer, length, mTimeout);
        if (result >= 0) {
            int idx = buffer[0] & 0xff;
            return getConfigurationDescriptor(idx);
        } else {
            throw new IOException("Failed to read a Configuration.");
        }
    }

    private byte[] getConfigurationDescriptor(final int index) throws IOException {
        int requestType = UsbConstants.USB_DIR_IN;
        byte[] buf = readDescriptor(requestType, (2 << 8) | index, 9);
        int totalLength = (buf[3] & 0xff) << 8 | (buf[2] & 0xff);
        buf = readDescriptor(requestType, (2 << 8) | index, totalLength);
        return buf;
    }

    private byte[] readDescriptor(final int requestType, final int value, final int length) throws IOException {
        checkConnected();

        int request = 6; // GET_DESCRIPTOR
        int index = 0;
        byte[] buf = new byte[length];
        int result = mConnection.controlTransfer(requestType, request, value, index, buf, length, mTimeout);
        if (result < 0) {
            throw new IOException("Failed to read a descriptor.");
        }
        return buf;
    }

    /**
     * 指定されたパケットデータをbulk転送で送信します.
     *
     * @param endpoint 送信先のエンドポイント
     * @param packet 送信するパケットデータ
     * @return 送信できたバイト数、負の値の場合には送信失敗
     */
    public int bulkTransfer(final UsbEndpoint endpoint, final byte[] packet) {
        checkConnected();

        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is null.");
        }

        if (packet == null) {
            throw new IllegalArgumentException("packet is null.");
        }

        int offset = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            while (offset < packet.length) {
                int len = mConnection.bulkTransfer(endpoint, packet, offset, packet.length - offset, mTimeout);
                if (len < 0) {
                    return -1;
                }
                offset += len;
            }
        } else {
            while (offset < packet.length) {
                // TODO コピーが遅いので最適化すること
                byte[] data = new byte[packet.length - offset];
                System.arraycopy(packet, offset, data, 0, packet.length - offset);
                int len = mConnection.bulkTransfer(endpoint, data, packet.length - offset, mTimeout);
                if (len < 0) {
                    return -1;
                }
                offset += len;
            }
        }
        return offset;
    }

    /**
     * 指定されたインターフェースクラスのエンドポイントを取得します.
     * <p>
     * エンドポイントが見つからない場合にはnullを返却します。
     * </p>
     * @param interfaceClass インターフェースクラス
     * @return エンドポイント
     */
    public UsbEndpoint findEndpoint(final int interfaceClass) {
        for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {
            UsbInterface usbInterface = mUsbDevice.getInterface(i);
            int code = usbInterface.getInterfaceClass();
            if (code == interfaceClass) {
                if (usbInterface.getEndpointCount() > 0) {
                    return usbInterface.getEndpoint(0);
                }
            }
        }
        return null;
    }

    /**
     * 全てのインターフェースを登録します.
     */
    public void claimAllInterfaces() {
        checkConnected();

        for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {
            if (!claimInterface(i)) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to claim interface.");
                }
            }
        }
    }

    /**
     * 全てのインターフェースを解除します.
     */
    public void releaseAllInterfaces() {
        checkConnected();

        for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {
            if (!releaseInterface(i)) {
                if (DEBUG) {
                    Log.w(TAG, "Failed to release interface.");
                }
            }
        }
    }

    /**
     * インターフェースの登録を行います.
     *
     * @param interfaceIndex 登録するインターフェースのインデックス
     * @return 登録に成功した場合はtrue、それ以外はfalse
     */
    public boolean claimInterface(final int interfaceIndex) {
        checkConnected();

        UsbInterface usbInterface = mUsbDevice.getInterface(interfaceIndex);
        return mConnection.claimInterface(usbInterface, true);
    }

    /**
     * インターフェースの解除を行います.
     *
     * @param interfaceIndex 解除するインターフェースのインデックス
     * @return 解除に成功した場合はtrue、それ以外はfalse
     */
    public boolean releaseInterface(final int interfaceIndex) {
        checkConnected();

        UsbInterface usbInterface = mUsbDevice.getInterface(interfaceIndex);
        return mConnection.releaseInterface(usbInterface);
    }

    /**
     * コンフィグレーション数を取得します.
     *
     * @return コンフィグレーション数
     */
    public int getConfigurationCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mUsbDevice.getConfigurationCount();
        }
        return 0;
    }

    /**
     * 指定されたconfigIdと同じIDを持つUsbConfigurationを設定します.
     *
     * @param configId コンフィグレーションID
     * @return 設定に成功した場合はtrue、それ以外はfalse
     */
    public boolean setConfiguration(final int configId) {
        checkConnected();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < mUsbDevice.getConfigurationCount(); i++) {
                UsbConfiguration config = mUsbDevice.getConfiguration(i);
                if (config.getId() == configId) {
                    return mConnection.setConfiguration(config);
                }
            }
        }
        return false;
    }

    /**
     * 接続状態を確認します.
     *
     * @throws IllegalStateException 接続されていない場合に発生
     */
    private void checkConnected() {
        if (mConnection == null) {
            throw new IllegalStateException("This port is not connected yet.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsbSerialPort that = (UsbSerialPort) o;

        return mUsbDevice.equals(that.mUsbDevice);
    }

    @Override
    public int hashCode() {
        return mUsbDevice.hashCode();
    }
}
