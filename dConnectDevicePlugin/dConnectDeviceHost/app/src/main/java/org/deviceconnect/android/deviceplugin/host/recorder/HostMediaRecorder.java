/*
 HostMediaRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.graphics.Rect;
import android.util.Range;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.recorder.util.PropertyUtil;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.List;

import javax.net.ssl.SSLContext;

/**
 * Host プラグインで使用する MediaRecorder のインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostMediaRecorder extends HostDevicePhotoRecorder, HostDeviceStreamRecorder {
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
     * サポートしているマイムタイプを取得します.
     *
     * @return サポートしているマイムタイプ
     */
    List<String> getSupportedMimeTypes();

    /**
     * MediaRecorder の状態を取得します.
     *
     * @return MediaRecorder の状態
     */
    State getState();

    /**
     * HostMediaRecorder の設定を取得します.
     *
     * @return HostMediaRecorder の設定
     */
    Settings getSettings();

    /**
     * プレビュー配信サーバの管理クラスを取得します.
     *
     * @return プレビュー配信サーバ
     */
    PreviewServerProvider getServerProvider();

    /**
     * プレビュー配信管理クラスを取得します.
     *
     * @return BroadcasterProvider の実装クラス
     */
    BroadcasterProvider getBroadcasterProvider();

    /**
     * プレビュー配信サーバの動作状況を確認します.
     *
     * @return プレビュー配信サーバが動作している場合はtrue、それ以外はfalse
     */
    boolean isPreviewRunning();

    /**
     * プレビュー配信サーバを開始します.
     *
     * 開始できなかった場合には、空のリストを返します。
     *
     * @return 開始したプレビュー配信サーバのリスト
     */
    List<PreviewServer> startPreview();

    /**
     * プレビュー配信サーバを停止します.
     */
    void stopPreview();

    /**
     * ブロードキャストの動作状況を確認します.
     *
     * @return ブロードキャストされている場合はtrue、それ以外はfalse
     */
    boolean isBroadcasterRunning();

    /**
     * ブロードキャストを開始します.
     *
     * @param broadcastURI ブロードキャスト先のURI
     * @return ブロードキャストしているクラス
     */
    Broadcaster startBroadcaster(String broadcastURI);

    /**
     * ブロードキャストを停止します.
     */
    void stopBroadcaster();

    /**
     * 描画用オブジェクトを取得します.
     *
     * @return EGLSurfaceDrawingThread のインスタンス
     */
    EGLSurfaceDrawingThread getSurfaceDrawingThread();

    /**
     * 端末の画面が回転したタイミングで実行されるメソッド.
     * @param degree 角度を示す定数
     */
    void onDisplayRotation(int degree);

    /**
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

    /**
     * キーフレームを要求します.
     *
     * プレビュー配信サーバやブロードキャストしている場合に映像にキーフレームを要求します。
     */
    void requestKeyFrame();

    /**
     * ミュート設定を行います.
     *
     * @param mute ミュート設定
     */
    void setMute(boolean mute);

    /**
     * ミュート設定を取得します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    boolean isMute();

    /**
     * SSL コンテキストの設定を行います.
     *
     * @param sslContext SSL コンテキスト
     */
    void setSSLContext(SSLContext sslContext);

    /**
     * パーミッションの要求結果を通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    void requestPermission(PermissionCallback callback);

    /**
     * イベントを通知するためのリスナーを設定します.
     *
     * @param listener リスナー
     */
    void setOnEventListener(OnEventListener listener);

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
    enum State {
        /**
         * 動作していない.
         */
        INACTIVE,

        /**
         * 録画が一時停止中の状態.
         */
        PAUSED,

        /**
         * 録画・静止画撮影中の状態.
         */
        RECORDING,

        /**
         * プレビューが表示されている状態.
         */
        PREVIEW,

        /**
         * エラーで停止している状態.
         */
        ERROR
    }

    /**
     * HostMediaRecorder のイベントを通知するリスナー.
     */
    interface OnEventListener extends HostDeviceStreamRecorder.OnEventListener, HostDevicePhotoRecorder.OnEventListener {
        /**
         * プレビュー配信を開始した時に呼び出されます.
         *
         * @param servers 開始したプレビュー配信サーバ
         */
        void onPreviewStarted(List<PreviewServer> servers);

        /**
         * プレビュー配信を停止した時に呼び出されます.
         */
        void onPreviewStopped();

        /**
         * ブロードキャストを開始した時に呼び出されます.
         *
         * @param broadcaster 開始したブロードキャスト
         */
        void onBroadcasterStarted(Broadcaster broadcaster);

        /**
         * ブロードキャストを停止した時に呼び出されます.
         */
        void onBroadcasterStopped();

        /**
         * レコーダで発生したエラーを通知します.
         *
         * @param e エラー原因の例外
         */
        void onError(Exception e);
    }

    /**
     * HostMediaRecorder の設定を保持するクラス.
     */
    class Settings {
        // サポート範囲
        private List<Size> mSupportedPictureSizes;
        private List<Size> mSupportedPreviewSizes;
        private List<Range<Integer>> mSupportedFps;
        private List<Integer> mSupportedWhiteBalances;

        private PropertyUtil mPref;

        public Settings(Context context, HostMediaRecorder recorder) {
            mPref = new PropertyUtil(context, recorder.getId());
        }

        public boolean load() {
            return mPref.getString("test", null) != null;
        }

        public void save() {
            mPref.put("test", "test");
        }

        public void clear() {
            mPref.clear();
        }

        /**
         * プレビューの配信エンコードのマイムタイプを取得します.
         *
         * @return プレビューの配信エンコードのマイムタイプ
         */
        public String getPreviewMimeType() {
            return mPref.getString("preview_mime_type", "video/avc");
        }

        /**
         * プレビューの配信エンコードのマイムタイプを設定します.
         *
         * @param mimeType プレビューの配信エンコードのマイムタイプ
         */
        public void setPreviewMimeType(String mimeType) {
            mPref.put("preview_mime_type", mimeType);
        }

        /**
         * 写真サイズを取得します.
         *
         * @return 写真サイズ
         */
        public Size getPictureSize() {
            return mPref.getSize("picture_size_width", "picture_size_height");
        }

        /**
         * 写真サイズを設定します.
         *
         * サポートされていない写真サイズの場合は IllegalArgumentException を発生させます。
         *
         * @param pictureSize 写真サイズ
         */
        public void setPictureSize(Size pictureSize) {
            if (!isSupportedPictureSize(pictureSize)) {
                throw new IllegalArgumentException("pictureSize is not supported.");
            }
            mPref.put(
                    "picture_size_width",
                    "picture_size_height",
                    pictureSize);
        }

        /**
         * プレビューサイズを取得します.
         *
         * @return プレビューサイズ
         */
        public Size getPreviewSize() {
            return mPref.getSize("preview_size_width", "preview_size_height");
        }

        /**
         * プレビューサイズを設定します.
         *
         * サポートされていないプレビューサイズの場合は IllegalArgumentException を発生させます。
         *
         * @param previewSize プレビューサイズ
         */
        public void setPreviewSize(Size previewSize) {
            if (!isSupportedPreviewSize(previewSize)) {
                throw new IllegalArgumentException("previewSize is not supported.");
            }
            mPref.put("preview_size_width", "preview_size_height", previewSize);
        }

        /**
         * フレームレートを取得します.
         *
         * @return フレームレート
         */
        public int getPreviewMaxFrameRate() {
            return mPref.getInteger("preview_framerate", 30);
        }

        /**
         * フレームレートを設定します.
         *
         * @param previewMaxFrameRate フレームレート
         */
        public void setPreviewMaxFrameRate(Integer previewMaxFrameRate) {
            if (previewMaxFrameRate <= 0) {
                throw new IllegalArgumentException("previewMaxFrameRate is zero or negative.");
            }
            mPref.put("preview_framerate", previewMaxFrameRate);
        }

        /**
         * ビットレートを取得します.
         *
         * @return ビットレート(byte)
         */
        public int getPreviewBitRate() {
            return mPref.getInteger("preview_bitrate", 2 * 1024 * 1024);
        }

        /**
         * ビットレートを設定します.
         *
         * @param previewBitRate ビットレート(byte)
         */
        public void setPreviewBitRate(int previewBitRate) {
            if (previewBitRate <= 0) {
                throw new IllegalArgumentException("previewBitRate is zero or negative.");
            }
            mPref.put("preview_bitrate", String.valueOf(previewBitRate));
        }

        /**
         * キーフレームインターバルを取得します.
         *
         * @return キーフレームを発行する間隔(ミリ秒)
         */
        public int getPreviewKeyFrameInterval() {
            return mPref.getInteger("preview_i_frame_interval", 1);
        }

        /**
         * キーフレームインターバルを設定します.
         *
         * @param previewKeyFrameInterval キーフレームを発行する間隔(ミリ秒)
         */
        public void setPreviewKeyFrameInterval(int previewKeyFrameInterval) {
            if (previewKeyFrameInterval <= 0) {
                throw new IllegalArgumentException("previewKeyFrameInterval is zero or negative.");
            }
            mPref.put("preview_i_frame_interval", previewKeyFrameInterval);
        }

        /**
         * プレビューの品質を取得します.
         *
         * @return プレビューの品質
         */
        public int getPreviewQuality() {
            return mPref.getInteger("preview_quality", 80);
        }

        /**
         * プレビューの品質を設定します.
         *
         * 0 から 100 の間で設定することができます。
         * それ以外は例外が発生します。
         *
         * @param quality プレビューの品質
         */
        public void setPreviewQuality(int quality) {
            if (quality < 0) {
                throw new IllegalArgumentException("quality is negative value.");
            }
            if (quality > 100) {
                throw new IllegalArgumentException("quality is over 100.");
            }
            mPref.put("preview_quality", quality);
        }

        /**
         * ホワイトバランスの設定を取得します.
         *
         * @return ホワイトバランス
         */
        public Integer getPreviewWhiteBalance() {
            return mPref.getInteger("preview_white_balance", null);
        }

        /**
         * ホワイトバランスを設定します.
         *
         * @param whiteBalance ホワイトバランス
         */
        public void setPreviewWhiteBalance(Integer whiteBalance) {
            if (!isSupportedWhiteBalance(whiteBalance)) {
                throw new IllegalArgumentException("whiteBalance is unsupported value.");
            }
            mPref.put("preview_white_balance", whiteBalance);
        }

        /**
         * ソフトウェアエンコーダを優先的に使用するフラグを確認します.
         *
         * @return ソフトウェアエンコーダを優先的に使用する場合は true、それ以外は false
         */
        public boolean isUseSoftwareEncoder() {
            return mPref.getBoolean("preview_use_software_encoder", false);
        }

        /**
         * ソフトウェアエンコーダを優先的に使用するフラグを設定します.
         *
         * @param used ソフトウェアエンコーダを優先的に使用する場合は true、それ以外は false
         */
        public void setUseSoftwareEncoder(boolean used) {
            mPref.put("preview_use_software_encoder", used);
        }

        /**
         * イントラリフレッシュのフレーム数を取得します.
         *
         * @return イントラリフレッシュのフレーム数
         */
        public Integer getIntraRefresh() {
            return mPref.getInteger("preview_intra_refresh", 0);
        }

        /**
         * イントラリフレッシュのフレーム数を設定します.
         *
         * @param refresh イントラリフレッシュのフレーム数
         */
        public void setIntraRefresh(int refresh) {
            mPref.put("preview_intra_refresh", refresh);
        }

        /**
         * サポートしている写真サイズを取得します.
         *
         * @return サポートしている写真サイズ
         */
        public List<Size> getSupportedPictureSizes() {
            return mSupportedPictureSizes;
        }

        /**
         * サポートしている写真サイズを設定します.
         *
         * @param sizes サポートサイズ
         */
        public void setSupportedPictureSizes(List<Size> sizes) {
            mSupportedPictureSizes = sizes;
        }

        /**
         * サポートしているプレビューサイズを取得します.
         *
         * @return サポートしているプレビューサイズ
         */
        public List<Size> getSupportedPreviewSizes() {
            return mSupportedPreviewSizes;
        }

        /**
         * サポートしているプレビューサイズを設定します.
         *
         * @param sizes サポートサイズ
         */
        public void setSupportedPreviewSizes(List<Size> sizes) {
            mSupportedPreviewSizes = sizes;
        }

        /**
         * サポートしている FPS のリストを取得します.
         *
         * @return サポートしている FPS のリスト
         */
        public List<Range<Integer>> getSupportedFps() {
            return mSupportedFps;
        }

        /**
         * サポートしている FPS のリストを設定します.
         *
         * @param fps サポートしている FPS のリスト
         */
        public void setSupportedFps(List<Range<Integer>> fps) {
            mSupportedFps = fps;
        }

        /**
         * サポートしているホワイトバランスのリストを取得します.
         *
         * @return サポートしているホワイトバランスのリスト
         */
        public List<Integer> getSupportedWhiteBalances() {
            return mSupportedWhiteBalances;
        }

        /**
         * サポートしているホワイトバランスのリストを設定します.
         *
         * @param whiteBalances サポートしているホワイトバランスのリスト
         */
        public void setSupportedWhiteBalances(List<Integer> whiteBalances) {
            mSupportedWhiteBalances = whiteBalances;
        }

        /**
         * 指定されたサイズがサポートされているか確認します.
         *
         * @param size 確認するサイズ
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedPictureSize(final Size size) {
            for (Size s : mSupportedPictureSizes) {
                if (s.getWidth() == size.getWidth() &&
                        s.getHeight() == size.getHeight()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 指定されたサイズが静止画でサポートされているか確認します.
         *
         * @param width 横幅
         * @param height 縦幅
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedPictureSize(int width, int height) {
            return isSupportedPictureSize(new Size(width, height));
        }

        /**
         * 指定されたサイズがサポートされているか確認します.
         *
         * @param size 確認するサイズ
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedPreviewSize(final Size size) {
            for (Size s : mSupportedPreviewSizes) {
                if (s.getWidth() == size.getWidth() &&
                        s.getHeight() == size.getHeight()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 指定されたサイズがプレビューでサポートされているか確認します.
         *
         * @param width 横幅
         * @param height 縦幅
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedPreviewSize(int width, int height) {
            return isSupportedPreviewSize(new Size(width, height));
        }

        /**
         * 指定されたサイズがサポートされているか確認します.
         *
         * @param fps 確認する FPS
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedFps(Range<Integer> fps) {
            for (Range<Integer> r : mSupportedFps) {
                if (r.getLower().equals(fps.getLower()) &&
                        r.getUpper().equals(fps.getUpper())){
                    return true;
                }
            }
            return false;
        }

        public boolean isSupportedWhiteBalance(int whiteBalance) {
            for (Integer wb : mSupportedWhiteBalances) {
                if (wb == whiteBalance) {
                    return true;
                }
            }
            return false;
        }

        // 音声

        /**
         * プレビュー音声が有効化確認します.
         *
         * @return プレビュー音声が有効の場合はtrue、それ以外はfalse
         */
        public boolean isAudioEnabled() {
            return mPref.getBoolean("audio_enabled", false);
        }

        /**
         * プレビュー音声が有効化を設定します.
         *
         * @param enabled プレビュー音声が有効の場合はtrue、それ以外はfalse
         */
        public void setAudioEnabled(boolean enabled) {
            mPref.put("audio_enabled", enabled);
        }

        /**
         * プレビュー音声のビットレートを取得します.
         *
         * @return プレビュー音声のビットレート
         */
        public int getPreviewAudioBitRate() {
            return mPref.getInteger("preview_audio_bitrate", 64 * 1024);
        }

        /**
         * プレビュー音声のビットレートを設定します.
         *
         * @param bitRate プレビュー音声のビットレート
         */
        public void setPreviewAudioBitRate(int bitRate) {
            if (bitRate <= 0) {
                throw new IllegalArgumentException("previewAudioBitRate is zero or negative value.");
            }
            mPref.put("preview_audio_bitrate", bitRate);
        }

        /**
         * プレビュー音声のサンプルレートを取得します.
         *
         * @return プレビュー音声のサンプルレート
         */
        public int getPreviewSampleRate() {
            return mPref.getInteger("preview_audio_sample_rate", 8000);
        }

        /**
         * プレビュー音声のサンプルレートを設定します.
         *
         * @param sampleRate プレビュー音声のサンプルレート
         */
        public void setPreviewSampleRate(int sampleRate) {
            mPref.put("preview_audio_sample_rate", sampleRate);
        }

        /**
         * プレビュー音声のチャンネル数を取得します.
         *
         * @return プレビュー音声のチャンネル数
         */
        public int getPreviewChannel() {
            return mPref.getInteger("preview_audio_channel", 1);
        }

        /**
         * プレビュー音声のチャンネル数を設定します.
         *
         * @param channel プレビュー音声のチャンネル数
         */
        public void setPreviewChannel(int channel) {
            mPref.put("preview_audio_channel", channel);
        }

        /**
         * プレビュー配信のエコーキャンセラーを取得します.
         *
         * @return プレビュー配信のエコーキャンセラー
         */
        public boolean isUseAEC() {
            return mPref.getBoolean("preview_audio_aec", true);
        }

        /**
         * プレビュー配信のエコーキャンセラーを設定します.
         *
         * @param used プレビュー配信のエコーキャンセラー
         */
        public void setUseAEC(boolean used) {
            mPref.put("preview_audio_aec", used);
        }

        /**
         * プレビューの音声ミュート設定を確認します.
         *
         * @return ミュートの場合はtrue、それ以外の場合はfalse
         */
        public boolean isMute() {
            return mPref.getBoolean("preview_audio_mute", false);
        }

        /**
         * プレビューの音声ミュート設定を行います.
         *
         * @param mute ミュートにする場合はtrue、それ以外はfalse
         */
        public void setMute(boolean mute) {
            mPref.put("preview_audio_mute", mute);
        }

        public boolean isBroadcastEnabled() {
            return mPref.getBoolean("broadcast_enabled", false);
        }

        public void setBroadcastEnabled(boolean enabled) {
            mPref.put("broadcast_enabled", enabled);
        }

        public String getBroadcastURI() {
            return mPref.getString("broadcast_uri", null);
        }

        public void setBroadcastURI(String broadcastURI) {
            mPref.put("broadcast_uri", broadcastURI);
        }

        public Integer getMjpegPort() {
            return mPref.getInteger("mjpeg_port", 0);
        }

        public void setMjpegPort(int port) {
            mPref.put("mjpeg_port", port);
        }

        public Integer getMjpegSSLPort() {
            return mPref.getInteger("mjpeg_ssl_port", 0);
        }

        public void setMjpegSSLPort(int port) {
            mPref.put("mjpeg_ssl_port", port);
        }

        public Integer getRtspPort() {
            return mPref.getInteger("rtsp_port", 0);
        }

        public void setRtspPort(int port) {
            mPref.put("rtsp_port", port);
        }

        public Integer getSrtPort() {
            return mPref.getInteger("srt_port", 0);
        }

        public void setSrtPort(int port) {
            mPref.put("srt_port", port);
        }

        public Rect getCutOutSize() {
            return mPref.getRect("preview_clip_left",
                    "preview_clip_top",
                    "preview_clip_right",
                    "preview_clip_bottom");
        }

        public void setCutOutSize(Rect rect) {
            if (rect == null) {
                mPref.remove("preview_clip_left");
                mPref.remove("preview_clip_top");
                mPref.remove("preview_clip_right");
                mPref.remove("preview_clip_bottom");
            } else {
                mPref.put(
                        "preview_clip_left",
                        "preview_clip_top",
                        "preview_clip_right",
                        "preview_clip_bottom",
                        rect);
            }
        }
    }
}
