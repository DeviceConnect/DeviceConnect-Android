package org.deviceconnect.android.libmedia.streaming.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class VideoEncoder extends MediaEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "VIDEO-ENCODER";

    /**
     * H.264 のマイムタイプを定義.
     */
    private static final String MIME_TYPE_H264 = "video/avc";

    /**
     * H.265 のマイムタイプを定義.
     */
    private static final String MIME_TYPE_H265 = "video/hevc";

    /**
     * VP8 のマイムタイプを定義.
     */
    private static final String MIME_TYPE_VP8 = "video/x-vnd.on2.vp8";

    /**
     * VP9 のマイムタイプを定義.
     */
    private static final String MIME_TYPE_VP9 = "video/x-vnd.on2.vp9";

    /**
     * キーフレームの同期フラグ.
     */
    private boolean mSyncKeyFrame;

    /**
     * 映像のビットレート変更要求フラグ.
     */
    private boolean mRequestChangeBitRate;

    /**
     * ソフトウェアエンコーダを優先的に使用するフラグ.
     */
    private boolean mUseSoftwareEncoder;

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        VideoQuality videoQuality = getVideoQuality();
        int videoWidth = videoQuality.getVideoWidth();
        int videoHeight = videoQuality.getVideoHeight();
        boolean isSwapped = isSwappedDimensions();
        int w = isSwapped ? videoHeight : videoWidth;
        int h = isSwapped ? videoWidth : videoHeight;
        mMediaCodec = createMediaCodec(getColorFormat(), w, h);
    }

    @Override
    protected void release() {
        // TODO: release を継承する場合には、必ず super.release() を呼び出すこと。
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
            } catch (Exception e) {
                // ignore.
            }
            try {
                mMediaCodec.release();
            } catch (Exception e) {
                // ignore.
            }
            mMediaCodec = null;
        }
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
     * MediaCodec に渡すカラーフォーマットを取得します.
     *
     * @return カラーフォーマット
     */
    public abstract int getColorFormat();

    /**
     * 映像のエンコード設定を取得します.
     *
     * @return 映像のエンコード設定
     */
    public abstract VideoQuality getVideoQuality();

    /**
     * 映像の回転を取得します.
     *
     * <p>
     * 端末の向きに合わせて、映像を回転する場合などに使用します。
     * </p>
     *
     * @return 映像の回転
     */
    protected int getDisplayRotation() {
        return Surface.ROTATION_0;
    }

    /**
     * MediaCodec に渡す解像度の縦横のサイズをスワップするか判断します.
     *
     * <p>
     * 端末の回転に合わせて、解像度を変更する時に使用します。
     * </p>
     *
     * @return スワップする場合は true、それ以外は false
     */
    public boolean isSwappedDimensions() {
        switch (getDisplayRotation()) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                return false;
            default:
                return true;
        }
    }

    /**
     * キーフレームを要求します.
     */
    public void requestSyncKeyFrame() {
        mSyncKeyFrame = true;
    }

    /**
     * ビットレートの変更を要求します.
     *
     * <p>
     * エンコード中にビットレートを変更したい場合に指定します。
     * {@link VideoQuality#getBitRate()} で取得できるビットレートを再設定します。
     * </p>
     */
    public void requestBitRate() {
        mRequestChangeBitRate = true;
    }

    /**
     * 映像の解像度の変更を要求します.
     *
     * <p>
     * エンコード中に映像の解像度を変更したい場合に指定します。
     *
     * {@link VideoQuality#getVideoWidth()}、 {@link VideoQuality#getVideoHeight()}
     * で取得できる映像の解像度を再設定します。
     * </p>
     */
    public void requestChangeVideoSize() {
        new Thread(this::restart).start();
    }

    /**
     * MediaCodec にキーフレームの作成を行います.
     */
    private void syncKeyFrame() {
        Bundle b = new Bundle();
        b.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        mMediaCodec.setParameters(b);
        mSyncKeyFrame = false;
    }

    /**
     * MediaCodec にビットレートの変更を行います.
     */
    private void changeBitRate() {
        Bundle b = new Bundle();
        b.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, getVideoQuality().getBitRate());
        mMediaCodec.setParameters(b);
        mRequestChangeBitRate = false;
    }

    /**
     * MediaCodec へのリクエスト処理を行います.
     */
    protected void executeRequest() {
        if (mSyncKeyFrame) {
            syncKeyFrame();
        }

        if (mRequestChangeBitRate) {
            changeBitRate();
        }
    }

    /**
     * MediaCodec を作成します.
     *
     * @param colorFormat カラーフォーマット
     * @param w 映像の横幅
     * @param h 映像の縦幅
     * @return 作成した MediaCodec のインスタンス
     * @throws IOException MediaCodec の作成に失敗した場合に発生
     */
    private MediaCodec createMediaCodec(int colorFormat, int w, int h) throws IOException {
        VideoQuality videoQuality = getVideoQuality();

        String mimeType = videoQuality.getMimeType();
        MediaCodecInfo codecInfo = null;

        boolean configureH264HighProfile = false;

        List<MediaCodecInfo> infoList = getMediaCodecInfo(mimeType, colorFormat);
        if (infoList.isEmpty()) {
            throw new IOException(mimeType + " not supported.");
        }

        // エンコーダ名が OMX.qcom. から始まる場合はハードウェアエンコーダ
        // エンコーダ名が OMX.google. から始まる場合はソフトウェアエンコーダ
        String encoderPrefix = "OMX.qcom.";
        if (mUseSoftwareEncoder) {
            encoderPrefix = "OMX.google.";
        }

        if (MIME_TYPE_H264.equalsIgnoreCase(mimeType)) {
            for (MediaCodecInfo info : infoList) {
                if (codecInfo == null || info.getName().startsWith(encoderPrefix)) {
                    codecInfo = info;
                }
            }

            for (MediaCodecInfo info : infoList) {
                if (info.getName().startsWith("OMX.Exynos.")) {
                    configureH264HighProfile = true;
                    codecInfo = info;
                }
            }
        } else if (MIME_TYPE_H265.equalsIgnoreCase(mimeType)) {
            for (MediaCodecInfo info : infoList) {
                if (codecInfo == null || info.getName().startsWith(encoderPrefix)) {
                    codecInfo = info;
                }
            }
        } else if (MIME_TYPE_VP8.equalsIgnoreCase(mimeType) || MIME_TYPE_VP9.equalsIgnoreCase(mimeType)) {
            for (MediaCodecInfo info : infoList) {
                if (codecInfo == null || info.getName().startsWith(encoderPrefix)) {
                    codecInfo = info;
                }
            }
        }

        if (codecInfo == null) {
            throw new IOException("Not found a codec. mimeType=" + mimeType);
        }

        if (DEBUG) {
            Log.d(TAG, "List of MediaCodeInfo supported by MediaCodec.");
            for (MediaCodecInfo info : infoList) {
                Log.d(TAG, "  " + info.getName());
            }
            Log.i(TAG, "---");
            Log.i(TAG, "SELECT: " + codecInfo.getName());
            Log.i(TAG, "MIME_TYPE: " + videoQuality.getMimeType());
            Log.i(TAG, "SIZE: " + w + "x" + h);
            Log.i(TAG, "BIT_RATE: " + videoQuality.getBitRate());
            Log.i(TAG, "FRAME_RATE: " + videoQuality.getFrameRate());
            Log.i(TAG, "I_FRAME_INTERVAL: " + videoQuality.getIFrameInterval());
        }

        MediaCodecInfo.CodecCapabilities codecCapabilities = codecInfo.getCapabilitiesForType(videoQuality.getMimeType());
        MediaCodecInfo.EncoderCapabilities encoderCapabilities =  codecCapabilities.getEncoderCapabilities();

        MediaFormat format = MediaFormat.createVideoFormat(videoQuality.getMimeType(), w, h);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoQuality.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoQuality.getFrameRate());
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoQuality.getIFrameInterval());
        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) {
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        }
        if (videoQuality.getBitRateMode() != null) {
            switch (videoQuality.getBitRateMode()) {
                case VBR:
                    if (encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)) {
                        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
                    }
                    break;
                case CBR:
                    if (encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)) {
                        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
                    }
                    break;
                case CQ:
                    if (encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)) {
                        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
                    }
                    break;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            format.setInteger(MediaFormat.KEY_PRIORITY, 0x00);
            if (configureH264HighProfile) {
                format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
                format.setInteger(MediaFormat.KEY_LEVEL,MediaCodecInfo.CodecProfileLevel.AVCLevel3);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            format.setInteger(MediaFormat.KEY_LATENCY, 0);
        }

        MediaCodec mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return mediaCodec;
    }

    /**
     * 指定された MediaCodecInfo のマイムタイプとカラーフォーマットが一致するか確認します.
     *
     * @param codecInfo 確認する MediaCodecInfo
     * @param mimeType マイムタイプ
     * @param colorFormat カラーフォーマット
     * @return 一致する場合はtrue、それ以外はfalse
     */
    private boolean isMediaCodecInfo(MediaCodecInfo codecInfo, String mimeType, int colorFormat) {
        if (!codecInfo.isEncoder()) {
            return false;
        }

        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (!type.equalsIgnoreCase(mimeType)) {
                continue;
            }

            try {
                MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                for (int i = 0; i < capabilities.colorFormats.length; i++) {
                    int format = capabilities.colorFormats[i];
                    if (colorFormat == format) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // ignore.
            }
        }

        return false;
    }

    /**
     * 指定されたマイムタイプとカラーフォマットに対応した MediaCodecInfo のリストを取得します.
     *
     * <p>
     * 対応した MediaCodecInfo が存在しない場合には空のリストを返却します。
     * </p>
     *
     * @param mimeType マイムタイプ
     * @param colorFormat カラーフォーマット
     * @return MediaCodecInfo のリスト
     */
    private List<MediaCodecInfo> getMediaCodecInfo(String mimeType, int colorFormat) {
        List<MediaCodecInfo> infoList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
            for (MediaCodecInfo codecInfo : list.getCodecInfos()) {
                if (isMediaCodecInfo(codecInfo, mimeType, colorFormat)) {
                    infoList.add(codecInfo);
                }
            }
        } else {
            for (int i = MediaCodecList.getCodecCount() - 1; i >= 0; i--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (isMediaCodecInfo(codecInfo, mimeType, colorFormat)) {
                    infoList.add(codecInfo);
                }
            }
        }
        return infoList;
    }


    private void printCodecInfo(MediaCodecInfo codecInfo) {
        Log.i(TAG, "CODEC: " + codecInfo.getName());

        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (!type.startsWith("video/")) {
                continue;
            }

            try {
                Log.i(TAG, "  TYPE: " + type);
                MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                for (int k = 0; k < capabilities.colorFormats.length; k++) {
                    int format = capabilities.colorFormats[k];
                    Log.i(TAG, "   FORMAT: " + format);
                }

                MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                if (videoCapabilities != null) {
                    Log.i(TAG, "    ----");
                    Log.i(TAG, "    Bitrate range: "
                            + videoCapabilities.getBitrateRange().getLower()
                            + " - "
                            + videoCapabilities.getBitrateRange().getUpper());
                    Log.i(TAG, "    Frame rate range: "
                            + videoCapabilities.getSupportedFrameRates().getLower()
                            + " - "
                            + videoCapabilities.getSupportedFrameRates().getUpper());
                    Log.i(TAG, "    Width range: "
                            + videoCapabilities.getSupportedWidths().getLower()
                            + " - "
                            + videoCapabilities.getSupportedWidths().getUpper());
                    Log.i(TAG, "    Height range: "
                            + videoCapabilities.getSupportedHeights().getLower()
                            + " - "
                            + videoCapabilities.getSupportedHeights().getUpper());
                    Log.i(TAG, "    ----");
                }
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    private void printCodecInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
            for (MediaCodecInfo codecInfo : list.getCodecInfos()) {
                if (codecInfo.isEncoder()) {
                    printCodecInfo(codecInfo);
                }
            }
        } else {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                if (codecInfo.isEncoder()) {
                    printCodecInfo(codecInfo);
                }
            }
        }
    }
}
