/*
 UVCCameraManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.os.Build;

import org.deviceconnect.android.libusb.UsbSerialPort;
import org.deviceconnect.android.libusb.UsbSerialPortManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.deviceconnect.android.libusb.UsbSerialPortManager.OnUsbEventListener;

/**
 * UVCカメラを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCCameraManager {
    /**
     * 接続している UVC カメラを Map で保持します.
     */
    private final Map<UsbSerialPort, UVCCamera> mUVCCameras = new HashMap<>();

    /**
     * USB の管理を行うクラス.
     */
    private UsbSerialPortManager mUsbSerialPortManager;

    /**
     * UVC カメラの接続・切断などのイベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * UsbSerialPortManagerのイベントを受け取るリスナー.
     */
    private final OnUsbEventListener mUsbEventListener = new OnUsbEventListener() {
        @Override
        public void onAttached(final UsbSerialPort serialPort) {
            try {
                if (checkCameraPermission()) {
                    checkOpen(serialPort);
                } else {
                    notifyError(new RuntimeException("Camera permission deny."));
                }
            } catch (Exception e) {
                notifyError(e);
            }
        }

        @Override
        public void onDetached(final UsbSerialPort serialPort) {
            try {
                checkClose(serialPort);
            } catch (Exception e) {
                notifyError(e);
            }
        }

        @Override
        public void onError(final Exception e) {
            notifyError(e);
        }

        @Override
        public void onRequestPermission(UsbSerialPortManager.PermissionCallback callback) {
            notifyOnRequestPermission(callback);
        }
    };

    /**
     * コンストラクタ.
     * <p>
     * UsbSerialPortManager を内部で作成して使用します。
     * </p>
     * @param context コンテキスト
     * @throws UnsupportedOperationException USBがサポートされていない場合に発生
     */
    public UVCCameraManager(final Context context) {
        this(new UsbSerialPortManager(context));
    }

    /**
     * コンストラクタ.
     *
     * @param manager USB管理クラス
     * @throws UnsupportedOperationException USBがサポートされていない場合に発生
     */
    public UVCCameraManager(final UsbSerialPortManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager is null.");
        }

        mUsbSerialPortManager = manager;
        mUsbSerialPortManager.addOnUsbEventListener(UsbConstants.USB_CLASS_MISC, mUsbEventListener);
    }

    /**
     * 後始末を行います.
     */
    public void dispose() {
        if (mUsbSerialPortManager != null) {
            mUsbSerialPortManager.removeOnUsbEventListener(UsbConstants.USB_CLASS_MISC, mUsbEventListener);
            mUsbSerialPortManager = null;
        }
    }

    /**
     * UVC カメラの接続・切断などのイベントを通知するリスナーを設定します.
     *
     * @param listener イベントを通知するリスナー
     */
    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * UVC カメラの接続・切断の監視を開始します.
     */
    public void startMonitoring() {
        mUsbSerialPortManager.startUsbMonitoring();
    }

    /**
     * UVC カメラの接続・切断の監視を停止します.
     */
    public void stopMonitoring() {
        mUsbSerialPortManager.stopUsbMonitoring();
    }

    /**
     * 接続されている UVC カメラのリストを取得します.
     *
     * @return UVC カメラのリスト
     */
    public synchronized List<UVCCamera> getUVCCameras() {
        return new ArrayList<>(mUVCCameras.values());
    }

    /**
     * カメラパーミッションの有無を確認します.
     *
     * @return カメラパーミッションが許可されている場合はtrue、それ以外はfalse
     */
    private boolean checkCameraPermission() {
        if (mUsbSerialPortManager != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true;
            }
            Context context = mUsbSerialPortManager.getContext();
            return context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    /**
     * UVC カメラと接続します.
     *
     * @param serialPort USBデバイス
     * @return UVCカメラのインスタンス
     * @throws IOException UVCカメラの接続に失敗した場合に発生
     */
    private UVCCamera openUVCCamera(final UsbSerialPort serialPort) throws IOException {
        UVCCamera uvcCamera = new UVCCamera(serialPort);
        try {
            uvcCamera.init();
        } catch (Exception e) {
            uvcCamera.close();
            throw new IOException(e);
        }
        return uvcCamera;
    }

    /**
     * UVC カメラの接続を確認して、接続に成功した場合にはリスナーに通知を行います.
     *
     * @param serialPort USBデバイス
     * @throws IOException UVCカメラの接続に失敗した場合に発生
     */
    private synchronized void checkOpen(final UsbSerialPort serialPort) throws IOException {
        UVCCamera uvcCamera = openUVCCamera(serialPort);
        mUVCCameras.put(serialPort, uvcCamera);
        notifyConnected(uvcCamera);
    }

    /**
     * UVC カメラの切断を行い、リスナーに通知を行います.
     *
     * @param serialPort USBデバイス
     * @throws IOException UVCカメラの切断に失敗した場合に発生
     */
    private synchronized void checkClose(final UsbSerialPort serialPort) throws IOException {
        UVCCamera uvcCamera = mUVCCameras.remove(serialPort);
        if (uvcCamera != null) {
            uvcCamera.close();
            notifyDisconnected(uvcCamera);
        }
    }

    /**
     * 接続した UVC カメラを通知します.
     *
     * @param uvcCamera UVC カメラ
     */
    private void notifyConnected(final UVCCamera uvcCamera) {
        if (mOnEventListener != null) {
            mOnEventListener.onConnected(uvcCamera);
        }
    }

    /**
     * 切断した UVC カメラを通知します.
     *
     * @param uvcCamera UVC カメラ
     */
    private void notifyDisconnected(final UVCCamera uvcCamera) {
        if (mOnEventListener != null) {
            mOnEventListener.onDisconnected(uvcCamera);
        }
    }

    /**
     * エラーを通知します.
     *
     * @param e 例外
     */
    private void notifyError(final Exception e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * パーミッション要求を通知します.
     *
     * @param callback パーミッション要求の結果を受け取るコールバック
     */
    private void notifyOnRequestPermission(UsbSerialPortManager.PermissionCallback callback) {
        if (mOnEventListener != null) {
            mOnEventListener.onRequestPermission(callback);
        }
    }

    /**
     * UVC カメラの接続・切断イベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * UVC カメラが接続されたことを通知します.
         * @param uvcCamera UVC カメラ
         */
        void onConnected(UVCCamera uvcCamera);

        /**
         * UVC カメラが切断されたことを通知します.
         * @param uvcCamera UVC カメラ
         */
        void onDisconnected(UVCCamera uvcCamera);

        /**
         * 監視中に発生したエラーを通知します.
         * @param e 例外
         */
        void onError(Exception e);

        /**
         * パーミッション要求を通知します.
         *
         * @param callback パーミッション要求の結果を通知するコールバック
         */
        void onRequestPermission(UsbSerialPortManager.PermissionCallback callback);
    }
}
