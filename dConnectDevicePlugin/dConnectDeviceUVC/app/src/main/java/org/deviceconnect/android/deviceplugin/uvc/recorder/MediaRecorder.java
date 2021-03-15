/*
 MediaRecorder.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.uvc.util.CapabilityUtil;
import org.deviceconnect.android.deviceplugin.uvc.util.PropertyUtil;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

/**
 * メディアレコーダのインターフェース.
 *
 * @author NTT DOCOMO, INC.
 */
public interface MediaRecorder {
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
     * 設定が変更されたことを通知します.
     */
    void onConfigChange();

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
    interface OnEventListener {
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

    /**
     * HostMediaRecorder の設定を保持するクラス.
     */
    abstract class Settings {
        private final PropertyUtil mPref;

        public Settings(Context context, String name) {
            mPref = new PropertyUtil(context, name);
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

        /**
         * サポートしているプロファイル・レベルのリストを取得します.
         *
         * @return サポートしているプロファイル・レベルのリスト
         */
        public List<ProfileLevel> getSupportedProfileLevel() {
            VideoEncoderName encoderName = getPreviewEncoderName();
            return CapabilityUtil.getSupportedProfileLevel(encoderName.getMimeType());
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
