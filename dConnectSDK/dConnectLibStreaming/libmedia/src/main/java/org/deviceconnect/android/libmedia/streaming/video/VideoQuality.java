package org.deviceconnect.android.libmedia.streaming.video;

public class VideoQuality {
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int DEFAULT_IFRAME_INTERVAL = 3;
    private static final int DEFAULT_BIT_RATE = 512 * 1024;

    private String mMimeType;
    private int mVideoWidth = 1920;
    private int mVideoHeight = 1080;
    private int mBitRate = DEFAULT_BIT_RATE;
    private int mFrameRate = DEFAULT_FRAME_RATE;
    private int mIFrameInterval = DEFAULT_IFRAME_INTERVAL;
    private EncoderType mEncoderType = EncoderType.HARDWARE;
    private BitRateMode mBitRateMode = BitRateMode.VBR;

    public VideoQuality(String mimeType) {
        mMimeType = mimeType;
    }

    public void set(VideoQuality quality) {
        mMimeType = quality.getMimeType();
        mVideoWidth = quality.getVideoWidth();
        mVideoHeight = quality.getVideoHeight();
        mBitRate = quality.getBitRate();
        mFrameRate = quality.getFrameRate();
        mIFrameInterval = quality.getIFrameInterval();
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public void setVideoWidth(int videoWidth) {
        mVideoWidth = videoWidth;
    }

    public void setVideoHeight(int videoHeight) {
        mVideoHeight = videoHeight;
    }

    public void setBitRate(int bitRate) {
        mBitRate = bitRate;
    }

    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    public void setIFrameInterval(int IFrameInterval) {
        mIFrameInterval = IFrameInterval;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public int getIFrameInterval() {
        return mIFrameInterval;
    }

    public EncoderType getEncoderType() {
        return mEncoderType;
    }

    public void setEncoderType(EncoderType encoderType) {
        mEncoderType = encoderType;
    }

    public BitRateMode getBitRateMode() {
        return mBitRateMode;
    }

    public void setBitRateMode(BitRateMode bitRateMode) {
        mBitRateMode = bitRateMode;
    }

    public enum BitRateMode {
        VBR,
        CBR,
        CQ
    }

    public enum EncoderType {
        SOFTWARE,
        HARDWARE
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
