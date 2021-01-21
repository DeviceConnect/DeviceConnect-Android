/*
 HostMediaRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaCodecInfo;
import android.util.Range;
import android.util.Size;

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
        H264("h264"),
        H265("h265");

        private final String mName;

        VideoEncoderName(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
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

    enum H264Profile {
        AVCProfileBaseline("AVCProfileBaseline", MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline),
        AVCProfileConstrainedBaseline("AVCProfileConstrainedBaseline", MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedBaseline),
        AVCProfileConstrainedHigh("AVCProfileConstrainedHigh", MediaCodecInfo.CodecProfileLevel.AVCProfileConstrainedHigh),
        AVCProfileExtended("AVCProfileExtended", MediaCodecInfo.CodecProfileLevel.AVCProfileExtended),
        AVCProfileHigh("AVCProfileHigh", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh),
        AVCProfileHigh10("AVCProfileHigh10", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh10),
        AVCProfileHigh422("AVCProfileHigh422", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh422),
        AVCProfileHigh444("AVCProfileHigh444", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh444),
        AVCProfileMain("AVCProfileMain", MediaCodecInfo.CodecProfileLevel.AVCProfileMain);

        private final String mName;
        private final int mValue;

        H264Profile(String name, int value) {
            mName = name;
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static H264Profile nameOf(String name) {
            for (H264Profile p : values()) {
                if (p.mName.equalsIgnoreCase(name)) {
                    return p;
                }
            }
            return null;
        }
    }

    enum H264Level {
        AVCLevel1("AVCLevel1", MediaCodecInfo.CodecProfileLevel.AVCLevel1),
        AVCLevel11("AVCLevel1", MediaCodecInfo.CodecProfileLevel.AVCLevel1),
        AVCLevel12("AVCLevel12", MediaCodecInfo.CodecProfileLevel.AVCLevel12),
        AVCLevel13("AVCLevel13", MediaCodecInfo.CodecProfileLevel.AVCLevel13),
        AVCLevel1b("AVCLevel1b", MediaCodecInfo.CodecProfileLevel.AVCLevel1b),
        AVCLevel2("AVCLevel2", MediaCodecInfo.CodecProfileLevel.AVCLevel2),
        AVCLevel21("AVCLevel21", MediaCodecInfo.CodecProfileLevel.AVCLevel21),
        AVCLevel22("AVCLevel22", MediaCodecInfo.CodecProfileLevel.AVCLevel22),
        AVCLevel3("AVCLevel3", MediaCodecInfo.CodecProfileLevel.AVCLevel3),
        AVCLevel31("AVCLevel31", MediaCodecInfo.CodecProfileLevel.AVCLevel31),
        AVCLevel32("AVCLevel32", MediaCodecInfo.CodecProfileLevel.AVCLevel32),
        AVCLevel4("AVCLevel4", MediaCodecInfo.CodecProfileLevel.AVCLevel4),
        AVCLevel41("AVCLevel41", MediaCodecInfo.CodecProfileLevel.AVCLevel41),
        AVCLevel42("AVCLevel42", MediaCodecInfo.CodecProfileLevel.AVCLevel42),
        AVCLevel5("AVCLevel5", MediaCodecInfo.CodecProfileLevel.AVCLevel5),
        AVCLevel51("AVCLevel51", MediaCodecInfo.CodecProfileLevel.AVCLevel51),
        AVCLevel52("AVCLevel52", MediaCodecInfo.CodecProfileLevel.AVCLevel52),
        AVCLevel6("AVCLevel6", MediaCodecInfo.CodecProfileLevel.AVCLevel6),
        AVCLevel61("AVCLevel61", MediaCodecInfo.CodecProfileLevel.AVCLevel61),
        AVCLevel62("AVCLevel62", MediaCodecInfo.CodecProfileLevel.AVCLevel62);

        private final String mName;
        private final int mValue;

        H264Level(String name, int value) {
            mName = name;
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static H264Level nameOf(String name) {
            for (H264Level l : values()) {
                if (l.mName.equalsIgnoreCase(name)) {
                    return l;
                }
            }
            return null;
        }
    }

    enum H265Profile {
        HEVCProfileMain("HEVCProfileMain", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain),
        HEVCProfileMain10("HEVCProfileMain10", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10),
        HEVCProfileMain10HDR10("HEVCProfileMain10HDR10", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10),
        HEVCProfileMain10HDR10Plus("HEVCProfileMain10HDR10Plus", MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus),
        HEVCProfileMainStill("HEVCProfileMainStill", MediaCodecInfo.CodecProfileLevel.HEVCProfileMainStill);

        private final String mName;
        private final int mValue;

        H265Profile(String name, int value) {
            mName = name;
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static H265Profile nameOf(String name) {
            for (H265Profile p : values()) {
                if (p.mName.equalsIgnoreCase(name)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    enum H265Level {
        HEVCHighTierLevel1("HEVCHighTierLevel1", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel1),
        HEVCHighTierLevel2("HEVCHighTierLevel2", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel2),
        HEVCHighTierLevel21("HEVCHighTierLevel21", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel21),
        HEVCHighTierLevel3("HEVCHighTierLevel3", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel3),
        HEVCHighTierLevel31("HEVCHighTierLevel31", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel31),
        HEVCHighTierLevel4("HEVCHighTierLevel4", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel4),
        HEVCHighTierLevel41("HEVCHighTierLevel41", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel41),
        HEVCHighTierLevel5("HEVCHighTierLevel5", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel5),
        HEVCHighTierLevel51("HEVCHighTierLevel51", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel51),
        HEVCHighTierLevel52("HEVCHighTierLevel52", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel52),
        HEVCHighTierLevel6("HEVCHighTierLevel6", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel6),
        HEVCHighTierLevel61("HEVCHighTierLevel61", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel61),
        HEVCHighTierLevel62("HEVCHighTierLevel62", MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel62),
        HEVCMainTierLevel1("HEVCMainTierLevel1", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel1),
        HEVCMainTierLevel2("HEVCMainTierLevel2", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel2),
        HEVCMainTierLevel21("HEVCMainTierLevel21", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel21),
        HEVCMainTierLevel3("HEVCMainTierLevel3", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel3),
        HEVCMainTierLevel31("HEVCMainTierLevel31", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel31),
        HEVCMainTierLevel4("HEVCMainTierLevel4", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel4),
        HEVCMainTierLevel41("HEVCMainTierLevel41", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel41),
        HEVCMainTierLevel5("HEVCMainTierLevel5", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel5),
        HEVCMainTierLevel51("HEVCMainTierLevel51", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel51),
        HEVCMainTierLevel52("HEVCMainTierLevel52", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel52),
        HEVCMainTierLevel6("HEVCMainTierLevel6", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel6),
        HEVCMainTierLevel61("HEVCMainTierLevel61", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel61),
        HEVCMainTierLevel62("HEVCMainTierLevel62", MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel62);

        private final String mName;
        private final int mValue;

        H265Level(String name, int value) {
            mName = name;
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static H265Level nameOf(String name) {
            for (H265Level l : values()) {
                if (l.mName.equalsIgnoreCase(name)) {
                    return l;
                }
            }
            return null;
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

        public boolean load() {
            return mPref.getString("test", null) != null;
        }

        public void save() {
            mPref.put("test", "test");
        }

        /**
         * 保存データを初期化します.
         */
        public void clear() {
            mPref.clear();
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
         * @return プレビューの配信エンコードの名前
         */
        public String getPreviewEncoder() {
            return mPref.getString("preview_encoder", "video/avc");
        }

        /**
         * プレビューの配信エンコードの名前を設定します.
         *
         * @param encoder プレビューの配信エンコードの名前
         */
        public void setPreviewEncoder(String encoder) {
            if (!isSupportedVideoEncoder(encoder)) {
                throw new IllegalArgumentException("encoder is not supported.");
            }
            mPref.put("preview_encoder", encoder);
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

        public Integer getNoiseReduction() {
            return mPref.getInteger("preview_reduction_noise", null);
        }

        public void setNoiseReduction(Integer mode) {
            if (mode == null) {
                mPref.remove("preview_reduction_noise");
            } else {
                mPref.put("preview_reduction_noise", mode);
            }
        }

        public Integer getProfile() {
            return mPref.getInteger("preview_profile", 0);
        }

        public void setProfile(Integer profile) {
            if (profile == null) {
                mPref.remove("preview_profile");
            } else {
                mPref.put("preview_profile", profile);
            }
        }

        public Integer getLevel() {
            return mPref.getInteger("preview_level", 0);
        }

        public void setLevel(Integer level) {
            if (level == null) {
                mPref.remove("preview_level");
            } else {
                mPref.put("preview_level", level);
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

        public List<BitRateMode> getSupportedBitRateModes() {
            return Arrays.asList(BitRateMode.values());
        }

        /**
         * サポートしている写真サイズを取得します.
         *
         * @return サポートしている写真サイズ
         */
        public List<Size> getSupportedPictureSizes() {
            return new ArrayList<>();
        }

        /**
         * サポートしているプレビューサイズを取得します.
         *
         * @return サポートしているプレビューサイズ
         */
        public List<Size> getSupportedPreviewSizes() {
            return new ArrayList<>();
        }

        /**
         * サポートしている FPS のリストを取得します.
         *
         * @return サポートしている FPS のリスト
         */
        public List<Range<Integer>> getSupportedFps() {
            return new ArrayList<>();
        }

        /**
         * サポートしているホワイトバランスのリストを取得します.
         *
         * @return サポートしているホワイトバランスのリスト
         */
        public List<Integer> getSupportedWhiteBalances() {
            return new ArrayList<>();
        }

        /**
         * サポートしているエンコーダのリストを取得します.
         *
         * @return サポートしているエンコーダのリスト
         */
        public List<String> getSupportedVideoEncoders() {
            return new ArrayList<>();
        }

        public List<Integer> getSupportedStabilizations() {
            return new ArrayList<>();
        }

        public List<Integer> getSupportedOpticalStabilizations() {
            return new ArrayList<>();
        }

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

        /**
         * 指定されたホワイトバランスがサポートされているか確認します.
         *
         * @param whiteBalance ホワイトバランス.
         * @return サポートされている場合はtrue、それ以外はfalse
         */
        public boolean isSupportedWhiteBalance(int whiteBalance) {
            for (Integer wb : getSupportedWhiteBalances()) {
                if (wb == whiteBalance) {
                    return true;
                }
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
            for (String e : getSupportedVideoEncoders()) {
                if (e.equalsIgnoreCase(encoder)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSupportedStabilization(int mode) {
            for (Integer m : getSupportedStabilizations()) {
                if (m == mode) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSupportedOpticalStabilization(int mode) {
            for (Integer m : getSupportedOpticalStabilizations()) {
                if (m == mode) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSupportedDigitalZoom(float zoom) {
            Float max = getMaxDigitalZoom();
            return max != null && 0.0f <= zoom && zoom <= max;
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

        /**
         * プレビューの音声タイプを取得します.
         *
         * @return 音声タイプ
         */
        public AudioSource getAudioSource() {
            return AudioSource.typeOf(mPref.getString("preview_audio_source", "default"));
        }

        /**
         * プレビューの音声タイプを設定します.
         *
         * @param audioSource 音声タイプ
         */
        public void setAudioSource(AudioSource audioSource) {
            mPref.put("preview_audio_source", audioSource.mSource);
        }

        public boolean isSupportedAudioSource(AudioSource source) {
            for (AudioSource s : getSupportedAudioSource()) {
                if (s == source) {
                    return true;
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
            return Arrays.asList(AudioSource.values());
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
    }
}
