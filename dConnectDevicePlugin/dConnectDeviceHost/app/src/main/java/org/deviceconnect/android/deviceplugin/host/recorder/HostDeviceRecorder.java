/*
 HostDeviceRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

import java.util.List;

/**
 * Host プラグインで使用する MediaRecorder のインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorder {
    /**
     * MediaRecorder で使用するデフォルトのマイムタイプ.
     */
    String MIME_TYPE_JPEG = "image/jpeg";

    /**
     * MediaRecorder の初期化処理を行います.
     */
    void initialize();

    /**
     * プロセス起動時の状態に戻す.
     *
     * プラグイン再起動時に呼び出すこと.
     */
    void clean();

    /**
     * オブジェクトを破棄する.
     *
     * プロセス終了時に呼び出すこと.
     */
    void destroy();

    /**
     * MediaRecorder を識別する ID を取得します.
     *
     * @return MediaRecorder を識別する ID
     */
    String getId();

    /**
     * MediaRecorder の名前を取得します.
     *
     * @return MediaRecorder の名前
     */
    String getName();

    /**
     * MediaRecorder のマイムタイプを取得します.
     *
     * @return マイムタイプ
     */
    String getMimeType();

    /**
     * MediaRecorder の状態を取得します.
     *
     * @return MediaRecorder の状態
     */
    RecorderState getState();

    /**
     * MediaRecorder に設定されている静止画のサイズを取得します.
     *
     * @return MediaRecorder に設定されている静止画のサイズ
     */
    PictureSize getPictureSize();

    /**
     * MediaRecorder に静止画のサイズを設定します.
     *
     * <p>
     * {@link #getSupportedPictureSizes()} で指定された範囲外の値が設定された場合には例外を発生します。
     * </p>
     *
     * @param size 静止画のサイズ
     */
    void setPictureSize(PictureSize size);

    /**
     * MediaRecorder に設定されているプレビューのサイズを取得します.
     *
     * @return MediaRecorder に設定されているプレビューのサイズ
     */
    PictureSize getPreviewSize();

    /**
     * MediaRecorder にプレビューサイズを設定します.
     *
     * <p>
     * {@link #getSupportedPreviewSizes()} ()} で指定された範囲外の値が設定された場合には例外を発生します。
     * </p>
     *
     * @param size プレビューサイズ
     */
    void setPreviewSize(PictureSize size);

    /**
     * 最大のフレームレートを取得します.
     *
     * @return 最大のフレームレート
     */
    double getMaxFrameRate();

    /**
     * 最大のフレームレートを設定します.
     *
     * <p>
     * 端末によっては、最大のフレームレートが設定できない場合には、
     * 最大のフレームレート以下の値が設定されることがあります。
     * </p>
     *
     * @param frameRate 最大のフレームレート
     */
    void setMaxFrameRate(double frameRate);

    /**
     * プレビューのビットレートを取得します.
     *
     * @return プレビューのビットレート
     */
    int getPreviewBitRate();

    /**
     * プレビューのビットレートを設定します.
     *
     * <p>
     * プレビューのビットレートを設定しますが、VBR (Variable Bitrate)
     * で動作するために、指定されたビットレートにはならない場合があります。
     * </p>
     *
     * @param bitRate ビットレート
     */
    void setPreviewBitRate(int bitRate);

    /**
     * サポートしている静止画のサイズを取得します.
     *
     * @return サポートしている静止画のサイズ
     */
    List<PictureSize> getSupportedPictureSizes();

    /**
     * サポートしているプレビューサイズを取得します.
     *
     * @return サポートしているプレビューサイズ
     */
    List<PictureSize> getSupportedPreviewSizes();

    /**
     * サポートしているマイムタイプを取得します.
     *
     * @return サポートしているマイムタイプ
     */
    List<String> getSupportedMimeTypes();

    /**
     * 指定されたサイズが静止画でサポートされているか確認します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    boolean isSupportedPictureSize(int width, int height);

    /**
     * 指定されたサイズがプレビューでサポートされているか確認します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    boolean isSupportedPreviewSize(int width, int height);

    /**
     * プレビュー配信サーバの管理クラスを取得します.
     *
     * @return プレビュー配信サーバ
     */
    PreviewServerProvider getServerProvider();

    /**
     * 端末の画面が回転したタイミングで実行されるメソッド.
     * @param degree 角度を示す定数
     */
    void onDisplayRotation(int degree);

    /**
     * パーミッションの要求結果を通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    void requestPermission(PermissionCallback callback);

    /**
     * パーミッション結果通知用コールバック.
     */
    interface PermissionCallback {
        /**
         * 許可された場合に呼び出されます.
         */
        void onAllowed();

        /**
         * 拒否された場合に呼び出されます.
         */
        void onDisallowed();
    }

    /**
     * MediaRecorder の状態.
     */
    enum RecorderState {
        /**
         * 動作していない.
         */
        INACTTIVE,

        /**
         * 録画が一時停止中の状態.
         */
        PAUSED,

        /**
         * 録画・静止画撮影中の状態.
         */
        RECORDING,

        /**
         * エラーで停止している状態.
         */
        ERROR
    }

    class PictureSize implements Parcelable {

        private final int mWidth;
        private final int mHeight;

        public PictureSize(final Size size) {
            this(size.getWidth(), size.getHeight());
        }

        public PictureSize(final int w, final int h) {
            mWidth = w;
            mHeight = h;
        }

        private PictureSize(final Parcel in) {
            this(in.readInt(), in.readInt());
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            out.writeInt(mWidth);
            out.writeInt(mHeight);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            @Override
            public PictureSize createFromParcel(Parcel in) {
                return new PictureSize(in);
            }
            @Override
            public PictureSize[] newArray(int size) {
                return new PictureSize[size];
            }
        };

        @Override
        public String toString() {
            return "(width = " + getWidth() + ", height = " + getHeight() + ")";
        }
    }
}
