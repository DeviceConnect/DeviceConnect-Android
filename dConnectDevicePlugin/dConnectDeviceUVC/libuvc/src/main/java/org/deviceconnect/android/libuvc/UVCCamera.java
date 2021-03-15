/*
 UVCCamera.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libusb.UsbSerialPort;
import org.deviceconnect.android.libuvc.utils.QueueThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * UVCカメラ操作クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCCamera {
    /**
     * 処理に成功を定義します.
     * <p>
     * NDK側で定義されている値に合わせる必要があります.
     * </p>
     */
    static final int UVC_SUCCESS = 0;

    /**
     * UsbSerialPortのインスタンス.
     */
    private UsbSerialPort mUsbSerialPort;

    /**
     * NDK側のポインタを格納する変数.
     */
    private long mNativePtr;

    /**
     * UVCカメラのフレームパラメータをキャッシュするためのリスト.
     */
    private final ArrayList<Parameter> mParameters = new ArrayList<>();

    /**
     * 設定されたフレームパラメータ.
     */
    private Parameter mCurrentParameter;

    /**
     * UVCカメラのオプション.
     */
    private Option mOption;

    /**
     * UVCカメラのプレビュー処理を行うスレッド.
     */
    private Thread mPreviewThread;

    /**
     * フレームを通知するためのスレッド.
     */
    private DispatchThread mDispatchThread;

    /**
     * プレビューのバッファを格納するバッファ.
     */
    private final FrameProvider mFrameProvider = new FrameProvider();

    /**
     * プレビューのフレーム通知を行うコールバック.
     */
    private PreviewCallback mPreviewCallback;

    /**
     * イベントを通知するリスナー.
     */
    private OnEventListener mOnEventListener;

    /**
     * デバイス名.
     */
    private String mDeviceName;

    private String mDeviceId;

    /**
     * コンストラクタ.
     * @param port USBデバイス
     */
    UVCCamera(final UsbSerialPort port) {
        if (port == null) {
            throw new IllegalArgumentException("port is null.");
        }
        mUsbSerialPort = port;
    }

    /**
     * UVC カメラのプレビューのコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setPreviewCallback(final PreviewCallback callback) {
        mPreviewCallback = callback;
    }

    /**
     * UVC カメラのイベントを通知するリスナーを設定します.
     *
     * @param listener リスナー
     */
    public void setOnEventListener(final OnEventListener listener) {
        mOnEventListener = listener;
    }

    /**
     * UVC カメラの初期化処理を行います.
     *
     * @throws IOException 初期化に失敗した場合に発生
     */
    void init() throws IOException {
        checkClosed();
        checkAndOpenNativePtr();

        mParameters.clear();
        UVCCameraNative.getParameter(mNativePtr, mParameters);

        mOption = new Option(this);
        UVCCameraNative.getOption(mNativePtr, mOption);

        mDeviceName = mUsbSerialPort.getDeviceName();
        if (mDeviceName == null) {
            // デバイス名が取得できない場合には、他のパラメータを使用してデバイス名を作成
            if (mUsbSerialPort.getManufacturerName() != null) {
                mDeviceName = "UVC " + mUsbSerialPort.getManufacturerName();
            } else {
                mDeviceName = "UVC " + mUsbSerialPort.getVendorId() + "-" + mUsbSerialPort.getVendorId();
            }
        }

        mDeviceId = mUsbSerialPort.getVendorId() + "-" + mUsbSerialPort.getProductId();
    }

    /**
     * UVCカメラのユニークなIDを取得します.
     *
     * @return デバイスID
     */
    public String getDeviceId() {
        return mDeviceId;
    }

    /**
     * UVCカメラのデバイス名を取得します.
     *
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }
    /**
     * UVC カメラが設定できるパラメータ一覧を取得します.
     *
     * @return WebCamパラメータの一覧
     * @throws IOException 既に close されていた場合に発生
     */
    public List<Parameter> getParameter() throws IOException {
        checkClosed();
        checkAndOpenNativePtr();
        return mParameters;
    }

    /**
     * UVC カメラが設定できるオプションを取得します.
     *
     * @return オプション
     */
    public Option getOptions() throws IOException {
        checkClosed();
        checkAndOpenNativePtr();
        return mOption;
    }

    /**
     * プレビューで設定されたパラメータを取得します.
     *
     * プレビューが行われていない場合は null を返却します。
     *
     * @return プレビューで設定されたパラメータ
     */
    public Parameter getCurrentParameter() {
        return mCurrentParameter;
    }

    /**
     * UVC カメラのプレビューを開始します.
     *
     * @param parameter パラメータ
     * @throws IllegalArgumentException パラメータが設定されていない場合に発生
     * @throws IOException 既に close されていた場合に発生
     */
    public synchronized void startVideo(final Parameter parameter) throws IOException {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter is null.");
        }

        checkClosed();
        checkAndOpenNativePtr();

        if (isRunning()) {
            return;
        }

        // フレームバッファの初期化
        mFrameProvider.init(parameter, 30);

        mCurrentParameter = parameter;

        int result = UVCCameraNative.startVideo(mNativePtr, parameter);
        if (result == UVC_SUCCESS) {
            startPreviewThread();
        } else {
            stopVideo();
            throw new IOException("Failed to start a preview. result=" + result);
        }
    }

    /**
     * UVC カメラのプレビューを停止します.
     *
     * @throws IOException 既に close されていた場合に発生
     */
    public synchronized void stopVideo() throws IOException {
        checkClosed();

        if (mNativePtr != 0) {
            UVCCameraNative.stopVideo(mNativePtr);
        }

        stopPreviewThread();

        closeNativePrt();

        mFrameProvider.clear();
    }

    /**
     * 静止画撮影を行います.
     *
     * @param parameter パラメータ
     * @param callback 撮影結果を通知するコールバック
     * @throws IOException 静止画撮影に失敗した場合に発生
     * @throws IllegalArgumentException 引数にnullが指定された場合に発生
     */
    public synchronized void captureStillImage(final Parameter parameter, final PictureCallback callback) throws IOException {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter is null.");
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback is null.");
        }

        final boolean isPreview = isRunning();

        checkClosed();
        checkAndOpenNativePtr();

        Thread thread = new Thread(() -> {
            // UVC の仕様で、bStillCaptureMethod が 2 の場合には、プレビューと
            // 排他的に静止画を使用する必要があります。
            // bStillCaptureMethod が 3 の場合には、プレビューを止めなくても
            // 撮影できますが、処理をまとめるために止めるようにしています。
            if (isPreview) {
                try {
                    stopVideo();
                } catch (IOException e) {
                    // ignore.
                }
            }

            Frame frame = null;
            try {
                checkAndOpenNativePtr();

                frame = new Frame(parameter, 1);
                int result = UVCCameraNative.captureStillImage(mNativePtr, frame);
                if (result == UVC_SUCCESS) {
                    callback.onPicture(frame);
                } else {
                    callback.onFailed(new UVCCameraException("Failed to take a photo. result=" + result));
                }
            } catch (Throwable t) {
                callback.onFailed(new UVCCameraException(t));
            } finally {
                closeNativePrt();

                if (frame != null) {
                    frame.release();
                }
            }

            if (isPreview) {
                try {
                    startVideo(mCurrentParameter);
                } catch (IOException e) {
                    // プレビュー再開失敗
                    postOnError(new UVCCameraException(e));
                }
            }
        });
        thread.setName("UVC-Still-Thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    /**
     * UVC カメラのプレビューが動作中か取得します.
     *
     * @return プレビュー表示中はtrue、それ以外はfalse
     * @throws IOException 既に close されていた場合に発生
     */
    public boolean isRunning() throws IOException {
        checkClosed();
        return mNativePtr != 0 && mPreviewThread != null && mDispatchThread != null;
    }

    /**
     * UVC カメラを閉じます.
     *
     * @throws IOException 既に close されていた場合に発生
     */
    public void close() throws IOException {
        checkClosed();

        if (isRunning()) {
            stopVideo();
        }

        mUsbSerialPort.close();
        mUsbSerialPort = null;
    }

    /**
     * UVC カメラが既に閉じられているか確認します.
     *
     * @return 既に閉じられている場合はtrue、それ以外はfalse
     */
    public boolean isClosed() {
        return mUsbSerialPort == null;
    }

    /**
     * JNI 側の初期化を確認して、初期化されていない場合には初期化します.
     *
     * @throws IOException 初期化に失敗した場合に発生
     */
    private void checkAndOpenNativePtr() throws IOException {
        if (mNativePtr == 0) {
            if (!mUsbSerialPort.isConnected()) {
                mUsbSerialPort.open();
            }

            mNativePtr = UVCCameraNative.open(mUsbSerialPort.getFileDescriptor());
            if (mNativePtr == 0) {
                throw new IOException("Failed to open uvc device.");
            }

            // Configuration が複数あった場合は、どちらの Configuration を使用するのかを検討する必要があります。
            // ここでは、UVC バージョンが大きい方を使用するように設定します。
            if (mUsbSerialPort.getConfigurationCount() > 1) {
                int configId = UVCParseDescriptor.getConfigId(mUsbSerialPort.getRawDescriptors());
                if (configId != 0) {
                    // interface が claim されていると Configuration の設定に失敗するので、
                    // ここでは、一度、全ての interface を release します。
                    UsbDevice device = mUsbSerialPort.getUsbDevice();
                    for (int i = 0; i < device.getInterfaceCount(); i++) {
                        // interface を releaseしてみて、失敗した場合には、OS 側で claim している可能性があります。
                        // そのために、NDK 側で OS で claim している interface を release を行います。
                        if (!mUsbSerialPort.releaseInterface(i)) {
                            UVCCameraNative.detachInterface(mNativePtr, device.getInterface(i).getId());
                        }
                    }

                    UVCCameraNative.setConfig(mNativePtr, configId);
                }
            }
        }
    }

    /**
     * JNI 側の後始末処理を行います.
     *
     */
    private void closeNativePrt() {
        if (mNativePtr != 0) {
            UVCCameraNative.close(mNativePtr);
        }
        mNativePtr = 0;

        mUsbSerialPort.close();
    }

    /**
     * 既に close されていないか確認します.
     *
     * @throws IOException 既に close されていた場合に発生
     */
    private void checkClosed() throws IOException {
        if (mUsbSerialPort == null) {
            throw new IOException("This device is already closed.");
        }
    }

    /**
     * プレビュー用のスレッドを開始します.
     */
    private void startPreviewThread() {
        if (mPreviewThread != null || mDispatchThread != null) {
            return;
        }

        mDispatchThread = new DispatchThread();
        mDispatchThread.setName("UVC-Dispatch-Thread");
        mDispatchThread.start();

        mPreviewThread = new Thread(() -> {
            if (mNativePtr != 0) {
                postOnStart();

                if (UVCCameraNative.handleEvent(mNativePtr, UVCCamera.this) != UVC_SUCCESS) {
                    // NDK側で UVC のポーリングに失敗したので、スレッドを停止して後始末処理を行う
                    stopPreviewThread();
                    postOnError(new UVCCameraException());
                }

                postOnStop();
            }
        });
        mPreviewThread.setName("UVC-Preview-Thread");
        mPreviewThread.start();
    }

    /**
     * プレビュー用のスレッドを停止します.
     */
    private void stopPreviewThread() {
        if (mDispatchThread != null) {
            mDispatchThread.close();
            mDispatchThread = null;
        }

        if (mPreviewThread != null) {
            try {
                mPreviewThread.interrupt();
                mPreviewThread.join(1000);
            } catch (InterruptedException e) {
                // ignore.
            }
            mPreviewThread = null;
        }
    }

    /**
     * コントロールの値を取得します.
     * <p>
     * 引数の value には、UVCから取得した値を格納します。<br>
     * 仕様にあるサイズのbyte配列を送ること。<br>
     * サイズが異なる場合には、エラーが発生します。
     * </p>
     * @param control コントロール
     * @param request リクエスト
     * @param value 取得した値を格納する配列
     * @return 0の場合には取得成功、それ以外は取得失敗
     */
    int getControl(final UVCCameraNative.Control control, final UVCCameraNative.RequestType request, final byte[] value) {
        return UVCCameraNative.getControl(mNativePtr, control, request, value);
    }

    /**
     * コントロールの値を取得します.
     *
     * @param control コントロール
     * @param value 設定する値を格納する配列
     * @return 0の場合には取得成功、それ以外は取得失敗
     */
    int setControl(final UVCCameraNative.Control control, final byte[] value) {
        return UVCCameraNative.setControl(mNativePtr, control, value);
    }

    /**
     * 使用していないフレームを取得します.
     * <p>
     * MEMO: NDK側から呼び出されるので、変更する場合には十分に注意すること。
     * </p>
     * <p>
     * 指定されたフレームバッファのサイズが Frame のサイズよりも大きい場合には
     * Frame のバッファをリサイズして大きくします。
     * </p>
     * <p>
     * 使用できるフレームバッファがない場合には null を返却します。
     * </p>
     * @param length 使用するフレームバッファのサイズ
     * @return 使用していないフレームバッファ
     */
    Frame getFrame(final int length) {
        try {
            return mFrameProvider.getFreeFrame(length);
        } catch (OutOfMemoryError e) {
            // NDKから要求されたバッファサイズでメモリ不足になった
            // 場合は、エラーを通知して、フレームは null を返す。
            postOnError(new UVCCameraException());
        }
        return null;
    }

    /**
     * UVC デバイスからフレームバッファの通知を受け取ります.
     * <p>
     * MEMO: NDK側から呼び出されるので、変更する場合には十分に注意すること。
     * </p>
     * <p>
     * 指定された ID のフレームバッファにデータが書き込まれています。
     * </p>
     * @param id フレームバッファのID
     * @param length 書き込まれたデータサイズ
     * @param pts presentation timestamp
     */
    void onFrame(final int id, final int length, final long pts) {
        if (mFrameProvider.isEmpty()) {
            return;
        }

        Frame frame = mFrameProvider.getFrameById(id);
        frame.setLength(length);
        frame.setPTS(pts);

        if (mDispatchThread != null && !mDispatchThread.isInterrupted()) {
            mDispatchThread.add(frame);
        }
    }

    /**
     * フレームバッファを配送するスレッド.
     */
    private class DispatchThread extends QueueThread<Frame> {
        private void exec() throws InterruptedException {
            Frame frame = get();
            try {
                if (mPreviewCallback != null) {
                    mPreviewCallback.onFrame(frame);
                }
            } catch (Throwable t) {
                // ignore.
            } finally {
                frame.release();
            }
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    exec();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * プレビュー開始イベントを通知します.
     */
    private void postOnStart() {
        if (mOnEventListener != null) {
            mOnEventListener.onStart();
        }
    }

    /**
     * プレビュー停止イベントを通知します.
     */
    private void postOnStop() {
        if (mOnEventListener != null) {
            mOnEventListener.onStop();
        }
    }

    /**
     * プレビューでエラーが発生したことを通知します.
     *
     * @param e エラー原因の例外
     */
    private void postOnError(UVCCameraException e) {
        if (mOnEventListener != null) {
            mOnEventListener.onError(e);
        }
    }

    /**
     * UVCカメラのイベントを通知するリスナー.
     */
    public interface OnEventListener {
        /**
         * UVCカメラのプレビューが開始されたことを通知します.
         */
        void onStart();

        /**
         * UVCカメラのプレビューが停止されたことを通知します.
         */
        void onStop();

        /**
         * UVCカメラのプレビューがエラーが発生したことを通知します.
         *
         * @param e エラーの情報を格納した例外
         */
        void onError(UVCCameraException e);
    }

    /**
     * 写真撮影の結果を通知するコールバック.
     */
    public interface PictureCallback {
        /**
         * 撮影された写真データの通知を行います。
         *
         * @param frame 写真データ
         */
        void onPicture(Frame frame);

        /**
         * 撮影失敗の通知を行います.
         *
         * @param e エラーの原因
         */
        void onFailed(Exception e);
    }

    /**
     * プレビューフレームバッファ通知を行うコールバック.
     */
    public interface PreviewCallback {
        /**
         * フレームバッファが送られてきたときの通知を行う.
         * <p>
         * 使用済みになった Frame は、{@link Frame#release()} を呼び出すこと。<br>
         * 使用済みになった Frame は、UVCCamera で使い回されます。<br>
         * 使用済みにならない場合には UVCCamera で使用できる Frame が枯渇して動作しなくなります。
         * </p>
         * @param frame フレームバッファ
         */
        void onFrame(Frame frame);
    }
}
