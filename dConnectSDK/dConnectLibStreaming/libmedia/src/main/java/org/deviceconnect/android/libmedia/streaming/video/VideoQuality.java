package org.deviceconnect.android.libmedia.streaming.video;

import android.graphics.Rect;

public class VideoQuality {
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int DEFAULT_IFRAME_INTERVAL = 3;
    private static final int DEFAULT_BIT_RATE = 1024 * 1024;

    private String mMimeType;
    private int mVideoWidth = 1920;
    private int mVideoHeight = 1080;
    private int mBitRate = DEFAULT_BIT_RATE;
    private int mFrameRate = DEFAULT_FRAME_RATE;
    private int mIFrameInterval = DEFAULT_IFRAME_INTERVAL;
    private int mIntraRefresh = 0;
    private int mProfile = 0;
    private int mLevel = 0;
    private Integer mLowLatency;

    /**
     * ビットレートのモード.
     */
    private BitRateMode mBitRateMode = BitRateMode.VBR;

    /**
     * ソフトウェアエンコーダを優先的に使用するフラグ.
     */
    private boolean mUseSoftwareEncoder;

    /**
     * 描画範囲.
     * null の場合は範囲してい無し。
     */
    private Rect mCropRect;

    /**
     * 画面の向き.
     */
    private int mOrientation;

    /**
     * コンストラクタ.
     * @param mimeType エンコードのマイムタイプ
     */
    public VideoQuality(String mimeType) {
        mMimeType = mimeType;
    }

    /**
     * VideoQuality をコピーします.
     *
     * @param quality コピー元の VideoQuality
     */
    public void set(VideoQuality quality) {
        mMimeType = quality.getMimeType();
        mVideoWidth = quality.getVideoWidth();
        mVideoHeight = quality.getVideoHeight();
        mBitRate = quality.getBitRate();
        mFrameRate = quality.getFrameRate();
        mIFrameInterval = quality.getIFrameInterval();
        mIntraRefresh = quality.getIntraRefresh();
        mBitRateMode = quality.getBitRateMode();
        mUseSoftwareEncoder = quality.isUseSoftwareEncoder();
        mProfile = quality.getProfile();
        mLevel = quality.getLevel();
        if (quality.mCropRect != null) {
            mCropRect = new Rect(quality.mCropRect);
        }
    }

    /**
     * 解像度の横幅を設定します.
     *
     * @param videoWidth 横幅
     */
    public void setVideoWidth(int videoWidth) {
        mVideoWidth = videoWidth;
    }

    /**
     * 解像度の縦幅を設定します.
     *
     * @param videoHeight 縦幅
     */
    public void setVideoHeight(int videoHeight) {
        mVideoHeight = videoHeight;
    }

    /**
     * ビットレートを設定します.
     *
     * @param bitRate ビットレート
     */
    public void setBitRate(int bitRate) {
        mBitRate = bitRate;
    }

    /**
     * フレームレートを設定します.
     * <p>
     * デフォルトでは、30 が設定されています。
     * </p>
     * @param frameRate フレームレート
     */
    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    /**
     * I フレームインターバルを設定します.
     *
     * @param IFrameInterval I フレームインターバル
     */
    public void setIFrameInterval(int IFrameInterval) {
        mIFrameInterval = IFrameInterval;
    }

    /**
     * エンコーダのマイムタイムを取得します.
     *
     * @return マイムタイプ
     */
    public String getMimeType() {
        return mMimeType;
    }

    /**
     * 解像度の横幅を取得します.
     *
     * @return 横幅
     */
    public int getVideoWidth() {
        return mVideoWidth;
    }

    /**
     * 解像度の縦幅を取得します.
     *
     * @return 縦幅
     */
    public int getVideoHeight() {
        return mVideoHeight;
    }

    /**
     * ビットレートを取得します.
     *
     * @return ビットレート
     */
    public int getBitRate() {
        return mBitRate;
    }

    /**
     * フレームレートを取得します.
     *
     * @return フレームレート
     */
    public int getFrameRate() {
        return mFrameRate;
    }

    /**
     * I フレームインターバルを取得します.
     *
     * @return I フレームインターバル
     */
    public int getIFrameInterval() {
        return mIFrameInterval;
    }

    /**
     * ソフトウェアエンコーダを使用するか確認します.
     *
     * デフォルトでは、 false に設定されています。
     *
     * @return ソフトウェアエンコーダを使用する場合はtrue、それ以外はfalse
     */
    public boolean isUseSoftwareEncoder() {
        return mUseSoftwareEncoder;
    }

    /**
     * ソフトウェアエンコーダを使用するか設定します.
     *
     * @param useSoftwareEncoder ソフトウェアエンコーダを使用する場合はtrue、それ以外はfalse
     */
    public void setUseSoftwareEncoder(boolean useSoftwareEncoder) {
        mUseSoftwareEncoder = useSoftwareEncoder;
    }

    /**
     * イントラリフレッシュのインターバルを取得します.
     *
     * 0 が指定されている場合には、イントラリフレッシュを使用しない。
     *
     * @return イントラリフレッシュのインターバル
     */
    public int getIntraRefresh() {
        return mIntraRefresh;
    }

    /**
     * イントラリフレッシュのインターバルを設定します.
     *
     * 0 が指定された場合には、イントラリフレッシュを使用しない。
     *
     * @param refresh イントラリフレッシュのインターバル
     */
    public void setIntraRefresh(int refresh) {
        mIntraRefresh = refresh;
    }

    /**
     * コーデックのプロファイルを取得します.
     *
     * @return プロファイル
     */
    public int getProfile() {
        return mProfile;
    }

    /**
     * コーデックのプロファイルを設定します.
     *
     * 0 が指定された場合には、デフォルト値を使用します。
     * また、{@link #setLevel(int)} が同時に設定されている必要があります。
     *
     * @param profile プロファイル
     */
    public void setProfile(int profile) {
        mProfile = profile;
    }

    /**
     * コーデックのレベルを取得します.
     *
     * @return コーデックのレベル
     */
    public int getLevel() {
        return mLevel;
    }

    /**
     * コーデックのレベルを設定します.
     *
     * 0 が指定された場合には、デフォルト値を使用します。
     * また、{@link #setProfile(int)} が同時に設定されている必要があります。
     *
     * @param level コーデックのレベル
     */
    public void setLevel(int level) {
        mLevel = level;
    }

    /**
     * ビットレートモードを取得します.
     *
     * @return ビットレートモード
     */
    public BitRateMode getBitRateMode() {
        return mBitRateMode;
    }

    /**
     * ビットレートモードを設定します.
     *
     * <p>
     * デフォルトでは、{@link BitRateMode#VBR} が設定されています。
     * </p>
     * <p>
     * また、{@link BitRateMode#CBR} や {@link BitRateMode#CQ} は、端末によってサポートされていない場合があります。
     * その場合には、この値を変更しても、端末のデフォルトの設定が使用されます。
     * </p>
     *
     * @param bitRateMode  ビットレートモードを取得します.
     */
    public void setBitRateMode(BitRateMode bitRateMode) {
        mBitRateMode = bitRateMode;
    }

    /**
     * 描画範囲を取得します.
     *
     * 描画範囲指定されていない場合は null を返却します。
     *
     * @return 描画範囲
     */
    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 描画範囲を設定します.
     *
     * null が指定された場合には、描画範囲を削除します。
     *
     * @param cropRect 描画範囲
     */
    public void setCropRect(Rect cropRect) {
        mCropRect = cropRect;
    }

    /**
     * Low Latency フラグを設定します.
     *
     * 1: 有効
     * 0: 無効
     * null: デフォルト設定
     *
     * @param lowLatency Low Latency フラグ
     */
    public void setLowLatency(Integer lowLatency) {
        mLowLatency = lowLatency;
    }

    /**
     * Low Latency フラグを取得します.
     *
     * @return Low Latency フラグ
     */
    public Integer getLowLatency() {
        return mLowLatency;
    }

    /**
     * ビットレートモード.
     */
    public enum BitRateMode {
        /**
         * 可変ビットレート.
         */
        VBR,

        /**
         * 固定ビットレート.
         */
        CBR,

        /**
         * 固定品質ビットレート.
         */
        CQ
    }

    @Override
    public String toString() {
        return "VideoQuality{" +
                "mMimeType='" + mMimeType + '\'' +
                ", mVideoWidth=" + mVideoWidth +
                ", mVideoHeight=" + mVideoHeight +
                ", mBitRate=" + mBitRate +
                ", mFrameRate=" + mFrameRate +
                ", mIFrameInterval=" + mIFrameInterval +
                '}';
    }
}
