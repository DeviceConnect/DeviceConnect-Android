/*
 HostMediaRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Range;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.host.recorder.util.MediaProjectionProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.util.PropertyUtil;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.ArrayList;
import java.util.Arrays;
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
     * スクリーンキャスト管理クラスを取得します.
     *
     * @return MediaProjectionProvider
     */
    MediaProjectionProvider getMediaProjectionProvider();

    /**
     * 端末の画面が回転したタイミングで実行されるメソッド.
     *
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
     * レコーダで映像を配信したデータの BPS を取得します.
     *
     * @return レコーダで映像を配信したデータの BPS
     */
    long getBPS();

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

    enum AudioSource {
        DEFAULT("default"),
        MIC("mic"),
        APP("app");

        private final String mSource;

        AudioSource(String source) {
            mSource = source;
        }

        public String getValue() {
            return mSource;
        }

        public static AudioSource typeOf(String source) {
            for (AudioSource audioSource : values()) {
                if (audioSource.mSource.equalsIgnoreCase(source)) {
                    return audioSource;
                }
            }
            return null;
        }
    }

    enum VideoEncoderName {
        H264("h264", "video/avc"),
        H265("h265", "video/hevc");

        private final String mName;
        private final String mMimeType;

        VideoEncoderName(String name, String mimeType) {
            mName = name;
            mMimeType = mimeType;
        }

        public String getName() {
            return mName;
        }

        public String getMimeType() {
            return mMimeType;
        }

        public static VideoEncoderName nameOf(String name) {
            for (VideoEncoderName encoder : values()) {
                if (encoder.getName().equalsIgnoreCase(name)) {
                    return encoder;
                }
            }
            return H264;
        }
    }

    enum BitRateMode {
        VBR("vbr"),
        CBR("cbr"),
        CQ("cq");

        private final String mName;

        BitRateMode(String name) {
            mName = name;
        }
        public String getName() {
            return mName;
        }

        public static BitRateMode nameOf(String name) {
            for (BitRateMode mode : values()) {
                if (mode.mName.equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return null;
        }
    }

    class ProfileLevel {
        private final int mProfile;
        private final int mLevel;

        public ProfileLevel(int profile, int level) {
            mProfile = profile;
            mLevel = level;
        }

        public int getProfile() {
            return mProfile;
        }

        public int getLevel() {
            return mLevel;
        }
    }

    enum AudioFilter {
        LOW_PASS("low-pass"),
        HIGH_PASS("high-pass");

        private final String mName;

        AudioFilter(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static AudioFilter nameOf(String name) {
            for (AudioFilter filter : values()) {
                if (filter.mName.equalsIgnoreCase(name)) {
                    return filter;
                }
            }
            return null;
        }
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
         * ミュート状態の変更を通知します.
         *
         * @param mute ミュートの場合はtrue、それ以外はfalse
         */
        void onMuteChanged(boolean mute);

        /**
         * レコーダの設定が変更されたことを通知します.
         */
        void onConfigChanged();

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
         * プレビュー配信でエラーが発生したときに呼び出されます.
         *
         * @param e エラー原因の例外
         */
        void onPreviewError(Exception e);

        /**
         * ブロードキャストを開始した時に呼び出されます.
         *
         * @param broadcaster 開始したブロードキャスト
         */
        void onBroadcasterStarted(Broadcaster broadcaster);

        /**
         * ブロードキャストを停止した時に呼び出されます.
         *
         * @param broadcaster 停止したブロードキャスト
         */
        void onBroadcasterStopped(Broadcaster broadcaster);

        /**
         * ブロードキャストでエラーが発生したときに呼び出されます.
         *
         * @param broadcaster エラーが発生した Broadcaster
         * @param e エラー原因の例外
         */
        void onBroadcasterError(Broadcaster broadcaster, Exception e);

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
    abstract class Settings {
        private final PropertyUtil mPref;

        public Settings(Context context, HostMediaRecorder recorder) {
            mPref = new PropertyUtil(context, recorder.getId());
        }

        /**
         * 初期化されているか確認します.
         *
         * @return 初期化されている場合はtrue、それ以外はfalse
         */
        public boolean isInitialized() {
            return mPref.getString("test", null) != null;
        }

        /**
         * 初期化完了を書き込みます.
         */
        public void finishInitialization() {
            mPref.put("test", "test");
        }

        /**
         * 保存データを初期化します.
         */
        public void clear() {
            mPref.clear();
        }

        /**
         * 自動フォーカスモードを取得します.
         *
         * 未設定の場合は null を返却します。
         *
         * @return 自動フォーカスモード
         */
        public Integer getPreviewAutoFocusMode() {
            return mPref.getInteger("preview_auto_focus", null);
        }

        /**
         * 自動フォーカスモードを設定します.
         *
         * mode が設定された場合には、未設定にします。
         *
         * サポートされていないモードが設定された場合には例外が発生します。
         *
         * @param mode 自動フォーカスモード
         */
        public void setPreviewAutoFocusMode(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_auto_focus");
            } else {
                if (!isSupportedAutoFocusMode(mode)) {
                    throw new IllegalArgumentException("focus mode is not supported.");
                }
                mPref.put("preview_auto_focus", mode);
            }
        }

        /**
         * プレビュー配信エンコード名を取得します.
         *
         * @return エンコード名
         */
        public VideoEncoderName getPreviewEncoderName() {
            return VideoEncoderName.nameOf(getPreviewEncoder());
        }

        /**
         * プレビューの配信エンコードの名前を取得します.
         *
         * 未設定の場合は h264 を返却します。
         *
         * @return プレビューの配信エンコードの名前
         */
        public String getPreviewEncoder() {
            return mPref.getString("preview_encoder", "h264");
        }

        /**
         * プレビューの配信エンコードの名前を設定します.
         *
         * @param encoder プレビューの配信エンコードの名前
         */
        public void setPreviewEncoder(String encoder) {
            if (encoder == null) {
                mPref.remove("preview_encoder");
            } else {
                if (!isSupportedVideoEncoder(encoder)) {
                    throw new IllegalArgumentException("encoder is not supported.");
                }
                mPref.put("preview_encoder", encoder);
            }
        }

        /**
         * プロファイルとレベルを取得します.
         *
         * 未設定の場合には、null を返却します。
         *
         * @return プロファイルとレベル
         */
        public ProfileLevel getProfileLevel() {
            Integer profile = mPref.getInteger("preview_profile", null);
            Integer level = mPref.getInteger("preview_level", null);
            if (profile != null && level != null) {
                return new ProfileLevel(profile, level);
            }
            return null;
        }

        /**
         * プロファイルとレベルを設定します.
         *
         * null が設定された場合には、未設定にします。
         *
         * サポートされていないプロファイルとレベルが設定された場合には例外を発生します。
         *
         * @param pl プロファイルとレベル
         */
        public void setProfileLevel(ProfileLevel pl) {
            if (pl == null) {
                mPref.remove("preview_profile");
                mPref.remove("preview_level");
            } else {
                if (!isSupportedProfileLevel(pl.getProfile(), pl.getLevel())) {
                    throw new IllegalArgumentException("profile and level are not supported.");
                }
                mPref.put("preview_profile", pl.getProfile());
                mPref.put("preview_level", pl.getLevel());
            }
        }

        /**
         * 設定されているプロファイルを取得します.
         *
         * @return プロファイル
         */
        public Integer getProfile() {
            return mPref.getInteger("preview_profile", 0);
        }

        /**
         * 設定されているレベルを取得します.
         *
         * @return レベル
         */
        public Integer getLevel() {
            return mPref.getInteger("preview_level", 0);
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
            return mPref.getInteger("preview_jpeg_quality", 80);
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
            mPref.put("preview_jpeg_quality", quality);
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
            if (whiteBalance == null) {
                mPref.remove("preview_white_balance");
            } else {
                if (!isSupportedWhiteBalanceMode(whiteBalance)) {
                    throw new IllegalArgumentException("WhiteBalance is unsupported value.");
                }
                mPref.put("preview_white_balance", whiteBalance);
            }
        }

        public Integer getPreviewAutoExposureMode() {
            return mPref.getInteger("preview_auto_exposure_mode", null);
        }

        public void setPreviewAutoExposureMode(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_auto_exposure_mode");
            } else {
                if (!isSupportedAutoExposureMode(mode)) {
                    throw new IllegalArgumentException("Exposure mode is unsupported value.");
                }
                mPref.put("preview_auto_exposure_mode", mode);
            }
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
        public void setIntraRefresh(Integer refresh) {
            if (refresh == null) {
                mPref.remove("preview_intra_refresh");
            } else {
                mPref.put("preview_intra_refresh", refresh);
            }
        }

        /**
         * 手ぶれ補正モードを取得します.
         *
         * @return 手ぶれ補正モード
         */
        public Integer getStabilizationMode() {
            return mPref.getInteger("preview_stabilization_mode", null);
        }

        /**
         * 手ぶれ補正モードを設定します.
         *
         * @param mode 手ぶれ補正モード
         */
        public void setStabilizationMode(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_stabilization_mode");
            } else {
                if (!isSupportedStabilization(mode)) {
                    throw new IllegalArgumentException("Stabilization Mode is unsupported value.");
                }
                mPref.put("preview_stabilization_mode", mode);
            }
        }

        /**
         * 光学手ぶれ補正モードを取得します.
         *
         * @return 光学手ぶれ補正モード
         */
        public Integer getOpticalStabilizationMode() {
            return mPref.getInteger("preview_optical_stabilization_mode", null);
        }

        /**
         * 光学手ぶれ補正モードを設定します.
         *
         * @param mode 光学手ぶれ補正モード
         */
        public void setOpticalStabilizationMode(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_optical_stabilization_mode");
            } else {
                if (!isSupportedOpticalStabilization(mode)) {
                    throw new IllegalArgumentException("Optical Stabilization Mode is unsupported value.");
                }
                mPref.put("preview_optical_stabilization_mode", mode);
            }
        }

        /**
         * デジタルズームを取得します.
         *
         * @return デジタルズーム
         */
        public Float getDigitalZoom() {
            return mPref.getFloat("preview_digital_zoom", null);
        }

        /**
         * デジタルズームを設定します.
         *
         * @param zoom デジタルズーム
         */
        public void setDigitalZoom(Float zoom) {
            if (zoom == null) {
                mPref.remove("preview_digital_zoom");
            } else {
                if (!isSupportedDigitalZoom(zoom)) {
                    throw new IllegalArgumentException("Digital zoom is unsupported value.");
                }
                mPref.put("preview_digital_zoom", zoom);
            }
        }

        public Float getFocalLength() {
            Float value = mPref.getFloat("preview_focal_length", null);
            if (value == null) {
                return null;
            }
            List<Float> focalLengthList = getSupportedFocalLengthList();
            for (Float focalLength : focalLengthList) {
                if (Math.abs(focalLength - value) < 0.01f) {
                    return focalLength;
                }
            }
            return null;
        }

        public void setFocalLength(Float focalLength) {
            if (focalLength == null) {
                mPref.remove("preview_focal_length");
            } else {
                if (!isSupportedFocalLength(focalLength)) {
                    throw new IllegalArgumentException("focalLength cannot set.");
                }
                mPref.put("preview_focal_length", focalLength);
            }
        }

        public Integer getNoiseReduction() {
            return mPref.getInteger("preview_reduction_noise", null);
        }

        public void setNoiseReduction(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_reduction_noise");
            } else {
                if (!isSupportedNoiseReduction(mode)) {
                    throw new IllegalArgumentException("mode cannot set.");
                }
                mPref.put("preview_reduction_noise", mode);
            }
        }

        public BitRateMode getPreviewBitRateMode() {
            return BitRateMode.nameOf(mPref.getString("preview_bitrate_mode", null));
        }

        public void setPreviewBitRateMode(BitRateMode mode) {
            if (mode == null) {
                mPref.remove("preview_bitrate_mode");
            } else {
                mPref.put("preview_bitrate_mode", mode.getName());
            }
        }

        public Integer getAutoExposureMode() {
            return mPref.getInteger("preview_auto_exposure_mode", null);
        }

        public void setAutoExposureMode(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_auto_exposure_mode");
            } else {
                if (!isSupportedAutoExposureMode(mode)) {
                    throw new IllegalArgumentException("mode cannot set.");
                }
                mPref.put("preview_auto_exposure_mode", mode);
            }
        }

        public Long getSensorExposureTime() {
            return mPref.getLong("preview_sensor_exposure_time", null);
        }

        public void setSensorExposureTime(Long exposureTime) {
            if (exposureTime == null) {
                mPref.remove("preview_sensor_exposure_time");
            } else {
                if (!isSupportedSensorExposureTime(exposureTime)) {
                    throw new IllegalArgumentException("exposureTime cannot set.");
                }
                mPref.put("preview_sensor_exposure_time", exposureTime);
            }
        }

        public Integer getSensorSensitivity() {
            return mPref.getInteger("preview_sensor_sensitivity", null);
        }

        public void setSensorSensitivity(Integer sensitivity) {
            if (sensitivity == null) {
                mPref.remove("preview_sensor_sensitivity");
            } else {
                if (!isSupportedSensorSensorSensitivity(sensitivity)) {
                    throw new IllegalArgumentException("sensitivity cannot set.");
                }
                mPref.put("preview_sensor_sensitivity", sensitivity);
            }
        }

        public Long getSensorFrameDuration() {
            return mPref.getLong("preview_sensor_frame_duration", null);
        }

        public void setSensorFrameDuration(Long frameDuration) {
            if (frameDuration == null) {
                mPref.remove("preview_sensor_frame_duration");
            } else {
                if (!isSupportedSensorFrameDuration(frameDuration)) {
                    throw new IllegalArgumentException("frameDuration cannot set.");
                }
                mPref.put("preview_sensor_frame_duration", frameDuration);
            }
        }

        public Integer getPreviewWhiteBalanceTemperature() {
            return mPref.getInteger("preview_sensor_white_balance_temperature", null);
        }

        public void setPreviewWhiteBalanceTemperature(Integer temperature) {
            if (temperature == null) {
                mPref.remove("preview_sensor_white_balance_temperature");
            } else {
                if (!isSupportedWhiteBalanceTemperature(temperature)) {
                    throw new IllegalArgumentException("whiteBalanceTemperature cannot set.");
                }
                mPref.put("preview_sensor_white_balance_temperature", temperature);
            }
        }

        /**
         * 切り抜き範囲を取得します.
         *
         * 範囲ば設定されていない場合には、null を返却します.
         *
         * @return 切り抜き範囲
         */
        public Rect getDrawingRange() {
            return mPref.getRect("preview_clip_left",
                    "preview_clip_top",
                    "preview_clip_right",
                    "preview_clip_bottom");
        }

        /**
         * 切り抜き範囲を設定します.
         *
         * 引数に null が指定された場合には、切り抜き範囲を削除します。
         *
         * @param rect 切り抜き範囲
         */
        public void setDrawingRange(Rect rect) {
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

        /**
         * サポートしている写真サイズを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしている写真サイズ
         */
        public List<Size> getSupportedPictureSizes() {
            return new ArrayList<>();
        }

        /**
         * サポートしているプレビューサイズを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしているプレビューサイズ
         */
        public List<Size> getSupportedPreviewSizes() {
            return new ArrayList<>();
        }

        /**
         * サポートしている FPS のリストを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしている FPS のリスト
         */
        public List<Range<Integer>> getSupportedFps() {
            return new ArrayList<>();
        }

        /**
         * サポートしているビットレートモードのリストを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしているビットレートモードのリスト
         */
        public List<BitRateMode> getSupportedBitRateModeList() {
            return Arrays.asList(BitRateMode.values());
        }

        /**
         * サポートしている自動フォーカスモードのリストを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしている自動フォーカスモードのリスト
         */
        public List<Integer> getSupportedAutoFocusModeList() {
            return new ArrayList<>();
        }

        /**
         * サポートしているホワイトバランスのリストを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしているホワイトバランスのリスト
         */
        public List<Integer> getSupportedWhiteBalanceModeList() {
            return new ArrayList<>();
        }

        /**
         * サポートしている自動露出モードのリストを取得します.
         *
         * サポートしていない場合には空のリストを返却します。
         *
         * @return サポートしている自動露出モードのリスト
         */
        public List<Integer> getSupportedAutoExposureModeList() {
            return new ArrayList<>();
        }

        /**
         * サポートしている露出時間の範囲を取得します.
         *
         * サポートしていない場合には、null を返却します。
         *
         * @return 露出時間の範囲
         */
        public Range<Long> getSupportedSensorExposureTime() {
            return null;
        }

        /**
         * サポートしている ISO 感度の範囲を取得します.
         *
         * サポートしていない場合には、null を返却します。
         *
         * @return ISO 感度の範囲
         */
        public Range<Integer> getSupportedSensorSensitivity() {
            return null;
        }

        /**
         * サポートしているフレーム時間の最大値を取得します.
         *
         * サポートしていない場合には、null を返却します。
         *
         * @return フレーム時間の最大値
         */
        public Long getMaxSensorFrameDuration() {
            return null;
        }

        public Range<Integer> getSupportedWhiteBalanceTemperature() {
            return null;
        }

        /**
         * サポートしているエンコーダのリストを取得します.
         *
         * @return サポートしているエンコーダのリスト
         */
        public List<String> getSupportedVideoEncoders() {
            List<String> list = new ArrayList<>();
            List<String> supported = CapabilityUtil.getSupportedVideoEncoders();
            for (VideoEncoderName encoderName : VideoEncoderName.values()) {
                if (supported.contains(encoderName.getMimeType())) {
                    list.add(encoderName.getName());
                }
            }
            return list;
        }

        public List<ProfileLevel> getSupportedProfileLevel() {
            VideoEncoderName encoderName = getPreviewEncoderName();
            return CapabilityUtil.getSupportedProfileLevel(encoderName.getMimeType());
        }

        public List<Integer> getSupportedStabilizationList() {
            return new ArrayList<>();
        }

        public List<Integer> getSupportedOpticalStabilizationList() {
            return new ArrayList<>();
        }

        public List<Integer> getSupportedNoiseReductionList() {
            return new ArrayList<>();
        }

        public List<Float> getSupportedFocalLengthList() {
            return new ArrayList<>();
        }

        /**
         * デジタルズームの最大倍率を取得します.
         *
         * デジタルズームに対応していない場合には null を返却します。
         *
         * @return デジタルズームの最大倍率
         */
        public Float getMaxDigitalZoom() {
            return null;
        }

        /**
         * 指定されたサイズがサポートされているか確認します.
         *
         * @param size 確認するサイズ
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedPictureSize(final Size size) {
            for (Size s : getSupportedPictureSizes()) {
                if (s.getWidth() == size.getWidth()
                        && s.getHeight() == size.getHeight()) {
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
         * 指定されたサイズがプレビューでサポートされているか確認します.
         *
         * @param size 確認するサイズ
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedPreviewSize(Size size) {
            for (Size s : getSupportedPreviewSizes()) {
                if (s.getWidth() == size.getWidth()
                        && s.getHeight() == size.getHeight()) {
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
            for (Range<Integer> r : getSupportedFps()) {
                if (r.getLower().equals(fps.getLower()) &&
                        r.getUpper().equals(fps.getUpper())) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSupportedAutoFocusMode(int mode) {
            List<Integer> modeList = getSupportedAutoFocusModeList();
            if (modeList != null) {
                for (Integer m : modeList) {
                    if (m == mode) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 指定されたホワイトバランスがサポートされているか確認します.
         *
         * @param whiteBalance ホワイトバランス.
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedWhiteBalanceMode(int whiteBalance) {
            List<Integer> modeList = getSupportedWhiteBalanceModeList();
            if (modeList != null) {
                for (Integer wb : modeList) {
                    if (wb == whiteBalance) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isSupportedWhiteBalanceTemperature(int temperature) {
            Range<Integer> range = getSupportedWhiteBalanceTemperature();
            if (range != null) {
                return range.getLower() <= temperature && temperature <= range.getUpper();
            }
            return false;
        }

        public boolean isSupportedAutoExposureMode(Integer mode) {
            List<Integer> modeList = getSupportedAutoExposureModeList();
            if (modeList != null) {
                for (Integer m : modeList) {
                    if (m.equals(mode)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isSupportedSensorExposureTime(long time) {
            Range<Long> range = getSupportedSensorExposureTime();
            if (range != null) {
                return range.getLower() <= time && time <= range.getUpper();
            }
            return false;
        }

        public boolean isSupportedSensorSensorSensitivity(int sensitivity) {
            Range<Integer> range = getSupportedSensorSensitivity();
            if (range != null) {
                return range.getLower() <= sensitivity && sensitivity <= range.getUpper();
            }
            return false;
        }

        public boolean isSupportedSensorFrameDuration(long frameDuration) {
            Long maxFrameDuration = getMaxSensorFrameDuration();
            if (maxFrameDuration != null) {
                return 0 <= frameDuration && frameDuration <= maxFrameDuration;
            }
            return false;
        }

        /**
         * 指定されたエンコーダがサポートされているか確認します.
         *
         * @param encoder エンコーダ名
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedVideoEncoder(String encoder) {
            List<String> encoderList = getSupportedVideoEncoders();
            if (encoderList != null) {
                for (String e : encoderList) {
                    if (e.equalsIgnoreCase(encoder)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 指定されたプロファイルとレベルがサポートされているか確認します.
         *
         * @param profile プロファイル
         * @param level レベル
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedProfileLevel(int profile, int level) {
            List<ProfileLevel> list = getSupportedProfileLevel();
            if (list != null) {
                for (ProfileLevel pl : list) {
                    if (profile == pl.getProfile() && level == pl.getLevel()) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 手ぶれ補正がサポートされている確認します.
         *
         * @param mode 手ぶれ補正モード
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedStabilization(int mode) {
            List<Integer> modeList = getSupportedStabilizationList();
            if (modeList != null) {
                for (Integer m : modeList) {
                    if (m == mode) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 光学手ぶれ補正がサポートされている確認します.
         *
         * @param mode 光学手ぶれ補正モード
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedOpticalStabilization(int mode) {
            List<Integer> modeList = getSupportedOpticalStabilizationList();
            if (modeList != null) {
                for (Integer m : modeList) {
                    if (m == mode) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * ノイズ低減モードがサポートされているか確認します.
         *
         * @param mode ノイズ低減モード
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedNoiseReduction(Integer mode) {
            List<Integer> modeList = getSupportedNoiseReductionList();
            if (modeList != null) {
                for (Integer m : modeList) {
                    if (m.equals(mode)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * デジタルズームの倍率がサポートされているか確認します.
         *
         * @param zoom ズームの倍率
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedDigitalZoom(float zoom) {
            Float max = getMaxDigitalZoom();
            return max != null && 1.0f <= zoom && zoom <= max;
        }

        /**
         * 焦点距離をサポートしているか確認します.
         *
         * @param focalLength 焦点距離
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedFocalLength(Float focalLength) {
            List<Float> list = getSupportedFocalLengthList();
            if (list != null) {
                for (Float length : list) {
                    if (Math.abs(focalLength - length) < 0.01f) {
                        return true;
                    }
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
            return getPreviewAudioSource() != null;
        }

        /**
         * プレビューの音声タイプを取得します.
         *
         * @return 音声タイプ
         */
        public AudioSource getPreviewAudioSource() {
            return AudioSource.typeOf(mPref.getString("preview_audio_source", "none"));
        }

        /**
         * プレビューの音声タイプを設定します.
         *
         * @param audioSource 音声タイプ
         */
        public void setPreviewAudioSource(AudioSource audioSource) {
            if (audioSource == null) {
                mPref.put("preview_audio_source", "none");
            } else {
                mPref.put("preview_audio_source", audioSource.mSource);
            }
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
            return mPref.getInteger("preview_audio_sample_rate", 16000);
        }

        /**
         * プレビュー音声のサンプルレートを設定します.
         *
         * @param sampleRate プレビュー音声のサンプルレート
         */
        public void setPreviewSampleRate(Integer sampleRate) {
            if (sampleRate == null) {
                mPref.remove("preview_audio_sample_rate");
            } else {
                if (!isSupportedSampleRate(sampleRate)) {
                    throw new IllegalArgumentException("preivewSampleRate is invalid.");
                }
                mPref.put("preview_audio_sample_rate", sampleRate);
            }
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

        public AudioFilter getAudioFilter() {
            return AudioFilter.nameOf(mPref.getString("preview_audio_filter", "none"));
        }

        public void setAudioFilter(AudioFilter filter) {
            if (filter == null) {
                mPref.remove("preview_audio_filter");
            } else {
                mPref.put("preview_audio_filter", filter.mName);
            }
        }

        public float getAudioCoefficient() {
            return mPref.getInteger("preview_audio_coefficient", 10) / 100.0f;
        }

        public void setAudioCoefficient(float coefficient) {
            mPref.put("preview_audio_coefficient", (int) (coefficient * 100));
        }

        public boolean isSupportedAudioSource(AudioSource source) {
            List<AudioSource> sourceList = getSupportedAudioSource();
            if (sourceList != null) {
                for (AudioSource s : sourceList) {
                    if (s == source) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * サポートしている音声入力のリストを取得します.
         *
         * @return サポートしている音声入力のリスト
         */
        public List<AudioSource> getSupportedAudioSource() {
            List<AudioSource> list = new ArrayList<>();
            for (AudioSource audioSource : AudioSource.values()) {
                if (audioSource == AudioSource.APP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        list.add(audioSource);
                    }
                } else {
                    list.add(audioSource);
                }
            }
            return list;
        }

        /**
         * サポートしているサンプルレートのリストを取得します.
         *
         * @return サポートしているサンプルレートのリスト
         */
        public List<Integer> getSupportedSampleRateList() {
            return CapabilityUtil.getSupportedSampleRates();
        }

        /**
         * 指定されたサンプルレートがサポートされているか確認します.
         *
         * @param sampleRate サンプルレート
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedSampleRate(int sampleRate) {
            List<Integer> sampleRates = getSupportedSampleRateList();
            if (sampleRates != null) {
                for (Integer s : sampleRates) {
                    if (s == sampleRate) {
                        return true;
                    }
                }
            }
            return false;
        }

        // 配信

        /**
         * 配信先の URI を取得します.
         *
         * 設定されていない場合は null を返却します.
         *
         * @return 配信先の URI
         */
        public String getBroadcastURI() {
            return mPref.getString("broadcast_uri", null);
        }

        /**
         * 配信先の URI を設定します.
         *
         * @param broadcastURI 配信先の URI
         */
        public void setBroadcastURI(String broadcastURI) {
            mPref.put("broadcast_uri", broadcastURI);
        }

        /**
         * リトライ回数を取得します.
         *
         * @return リトライ回数
         */
        public int getRetryCount() {
            return mPref.getInteger("broadcast_retry_count", 0);
        }

        /**
         * リトライ回数を設定します.
         *
         * @param count リトライ回数
         */
        public void setRetryCount(int count) {
            if (count < 0) {
                mPref.remove("broadcast_retry_count");
            } else {
                mPref.put("broadcast_retry_count", count);
            }
        }

        /**
         * リトライのインターバルを取得します.
         *
         * @return リトライのインターバル
         */
        public int getRetryInterval() {
            return mPref.getInteger("broadcast_retry_interval", 3000);
        }

        /**
         * リトライのインターバルを設定します.
         *
         * @param interval リトライのインターバル
         */
        public void setRetryInterval(int interval) {
            if (interval < 0) {
                mPref.remove("broadcast_retry_interval");
            } else {
                mPref.put("broadcast_retry_interval", interval);
            }
        }

        // ポート番号

        /**
         * Motion JPEG サーバ用のポート番号を取得します.
         *
         * @return Motion JPEG サーバ用のポート番号
         */
        public Integer getMjpegPort() {
            return mPref.getInteger("mjpeg_port", 0);
        }

        /**
         * Motion JPEG サーバ用のポート番号を設定します.
         *
         * @param port Motion JPEG サーバ用のポート番号
         */
        public void setMjpegPort(int port) {
            mPref.put("mjpeg_port", port);
        }

        /**
         * SSL で暗号化された Motion JPEG サーバ用のポート番号を取得します.
         *
         * @return Motion JPEG サーバ用のポート番号
         */
        public Integer getMjpegSSLPort() {
            return mPref.getInteger("mjpeg_ssl_port", 0);
        }

        /**
         * SSL で暗号化された Motion JPEG サーバ用のポート番号を取得します.
         *
         * @param port Motion JPEG サーバ用のポート番号
         */
        public void setMjpegSSLPort(int port) {
            mPref.put("mjpeg_ssl_port", port);
        }

        /**
         * RTSP サーバ用のポート番号を取得します.
         *
         * @return RTSP サーバ用のポート番号
         */
        public Integer getRtspPort() {
            return mPref.getInteger("rtsp_port", 0);
        }

        /**
         * RTSP サーバ用のポート番号を設定します.
         *
         * @param port RTSP サーバ用のポート番号
         */
        public void setRtspPort(int port) {
            mPref.put("rtsp_port", port);
        }

        /**
         * SRT サーバ用のポート番号を取得します.
         *
         * @return SRT サーバ用のポート番号
         */
        public Integer getSrtPort() {
            return mPref.getInteger("srt_port", 0);
        }

        /**
         * SRT サーバ用のポート番号を設定します.
         *
         * @param port SRT サーバ用のポート番号
         */
        public void setSrtPort(int port) {
            mPref.put("srt_port", port);
        }
    }
}
