/*
 MediaRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.os.Parcel;
import android.os.Parcelable;

import org.deviceconnect.android.deviceplugin.uvc.recorder.preview.PreviewServer;

import java.util.List;

/**
 * メディアレコーダのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface MediaRecorder {

    /**
     * レコーダの初期化処理を行います.
     */
    void initialize();

    /**
     * レコーダの後始末処理を行います.
     */
    void clean();

    /**
     * レコーダのIDを取得します.
     *
     * @return レコーダID
     */
    String getId();

    /**
     * レコーダ名を取得します.
     *
     * @return レコーダ名
     */
    String getName();

    /**
     * レコーダに設定されているマイムタイプを取得します.
     *
     * @return マイムタイプ
     */
    String getMimeType();

    /**
     * レコーダにマイムタイプを設定します.
     *
     * @param mimeType マイムタイプ
     */
    void setMimeType(final String mimeType);

    /**
     * レコーダの状態を取得します.
     *
     * @return レコーダの状態
     */
    State getState();

    /**
     * レコーダに設定されている写真サイズを取得します.
     *
     * @return 写真サイズ
     */
    Size getPictureSize();

    /**
     * レコーダに写真サイズを設定します.
     *
     * @param size 写真サイズ
     */
    void setPictureSize(Size size);

    /**
     * レコーダに設定されているプレビューサイズを取得します.
     *
     * @return プレビューサイズ
     */
    Size getPreviewSize();

    /**
     * レコーダにプレビューサイズを設定します.
     *
     * @param size プレビューサイズ
     */
    void setPreviewSize(Size size);

    /**
     * レコーダに設定されているフレームレートを取得します.
     *
     * @return フレームレート
     */
    double getMaxFrameRate();

    /**
     * レコーダにフレームレートを設定します.
     *
     * @param frameRate フレームレート
     */
    void setMaxFrameRate(double frameRate);

    /**
     * レコーダに設定されているビットレートを取得します.
     *
     * @return ビットレート
     */
    int getPreviewBitRate();

    /**
     * レコーダにビットレートを設定します.
     *
     * @param bitRate ビットレート
     */
    void setPreviewBitRate(int bitRate);

    /**
     * サポートしている写真サイズを取得します.
     *
     * @return サポートしている写真サイズ
     */
    List<Size> getSupportedPictureSizes();

    /**
     * サポートしているプレビューサイズを取得します.
     *
     * @return サポートしているプレビューサイズ
     */
    List<Size> getSupportedPreviewSizes();

    /**
     * サポートしているマイムタイプを取得します.
     *
     * @return サポートしているマイムタイプ
     */
    List<String> getSupportedMimeTypes();

    /**
     * 写真撮影を行います.
     *
     * @param listener 写真撮影のイベントを通知するリスナー
     */
    void takePhoto(OnPhotoEventListener listener);

    /**
     * プレビュー用のサーバを取得します.
     *
     * @return プレビュー用サーバ
     */
    List<PreviewServer> getServers();

    /**
     * プレビューを開始します.
     * サーバが起動できなかった場合には、空のリストを返却する。
     * @return 起動したプレビュー配信サーバのリスト
     */
    List<PreviewServer> startPreview();

    /**
     * プレビューを停止します.
     */
    void stopPreview();

    /**
     * プレビューが廃止されているか.
     * @return
     */
    boolean isStartedPreview();

    /**
     * {@link #takePhoto(OnPhotoEventListener)} のイベントを受け取るリスナー.
     */
    interface OnPhotoEventListener {
        /**
         * 写真が取られた時のイベントを通知します.
         *
         * @param uri 写真へのURI
         * @param filePath 写真へのパス
         */
        void onTakePhoto(String uri, String filePath);

        /**
         * 写真撮影に失敗した時のイベントを通知します.
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailedTakePhoto(String errorMessage);
    }

    /**
     * レコーダの状態.
     */
    enum State {
        /**
         * アイドル中.
         */
        INACTTIVE,

        /**
         * 一時停止中.
         */
        PAUSED,

        /**
         * レコーディング中.
         */
        RECORDING,

        /**
         * エラー中.
         */
        ERROR
    }

    /**
     * レコーダで使用するサイズ.
     */
    class Size implements Parcelable {
        /**
         * 横幅.
         */
        private final int mWidth;

        /**
         * 縦幅.
         */
        private final int mHeight;

        /**
         * コンストラクタ.
         * @param w 横幅
         * @param h 縦幅
         */
        public Size(final int w, final int h) {
            mWidth = w;
            mHeight = h;
        }

        /**
         * コンストラクタ.
         * @param in Parcelのストリーム
         */
        private Size(final Parcel in) {
            this(in.readInt(), in.readInt());
        }

        /**
         * 横幅を取得します.
         *
         * @return 横幅
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * 縦幅を取得します.
         *
         * @return 縦幅
         */
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

        public static final Creator CREATOR = new Creator() {
            @Override
            public Size createFromParcel(Parcel in) {
                return new Size(in);
            }
            @Override
            public Size[] newArray(int size) {
                return new Size[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Size size = (Size) o;

            return mWidth == size.mWidth && mHeight == size.mHeight;
        }

        @Override
        public int hashCode() {
            int result = mWidth;
            result = 31 * result + mHeight;
            return result;
        }

        @Override
        public String toString() {
            return "(width = " + getWidth() + ", height = " + getHeight() + ")";
        }
    }
}
