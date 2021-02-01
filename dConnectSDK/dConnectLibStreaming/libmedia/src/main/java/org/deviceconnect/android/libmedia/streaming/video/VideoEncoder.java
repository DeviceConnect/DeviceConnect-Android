package org.deviceconnect.android.libmedia.streaming.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class VideoEncoder extends MediaEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "VIDEO-ENCODER";

    /**
     * キーフレームの同期フラグ.
     */
    private boolean mSyncKeyFrame;

    /**
     * 映像のビットレート変更要求フラグ.
     */
    private boolean mRequestChangeBitRate;

    // MediaEncoder

    @Override
    protected void prepare() throws IOException {
        VideoQuality videoQuality = getVideoQuality();
        int w = videoQuality.getVideoWidth();
        int h = videoQuality.getVideoHeight();
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
     * ハードウェアエンコーダか確認します.
     *
     * @param info MediaCodec 情報
     * @return ハードウェアエンコーダの場合にはtrue、それ以外はfalse
     */
    private boolean isHardware(MediaCodecInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return info.isHardwareAccelerated();
        } else {
            // エンコーダ名が OMX.qcom. または OMX.Exynos. から始まる場合はハードウェアエンコーダ
            // エンコーダ名が OMX.google. から始まる場合はソフトウェアエンコーダ
            String name = info.getName();
            return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Exynos.");
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

        boolean useSoftware = videoQuality.isUseSoftwareEncoder();

        List<MediaCodecInfo> infoList = getMediaCodecInfo(mimeType, colorFormat);
        if (infoList.isEmpty()) {
            throw new IOException(mimeType + " not supported.");
        }

        for (MediaCodecInfo info : infoList) {
            if (codecInfo == null || (useSoftware == !isHardware(info))) {
                codecInfo = info;
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
            Log.i(TAG, "---");
        }

        MediaCodecInfo.CodecCapabilities codecCapabilities = codecInfo.getCapabilitiesForType(videoQuality.getMimeType());
        MediaCodecInfo.EncoderCapabilities encoderCapabilities = codecCapabilities.getEncoderCapabilities();

        MediaFormat format = MediaFormat.createVideoFormat(videoQuality.getMimeType(), w, h);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoQuality.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoQuality.getFrameRate());
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoQuality.getIFrameInterval());

        // Surface で映像を入力するので、Input size を 0 に設定しておきます。
        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) {
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        }

        // 一定期間 Surface に更新がなかった場合に前の映像をエンコードします.(単位: microseconds)
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / videoQuality.getFrameRate());

        // ビットレートモードに設定します。
        // 機種ごとにサポートできるパラメータが異なるので、設定できない場合はデフォルトで動作します。
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
            // 0: realtime priority
            // 1: non-realtime priority (best effort).
            format.setInteger(MediaFormat.KEY_PRIORITY, 0x00);

            // エンコーダのプロファイルとレベルを設定
            int profile = videoQuality.getProfile();
            int level = videoQuality.getLevel();
            if (profile != 0 && level != 0) {
                if (isProfileSupported(codecCapabilities, profile, level)) {
                    format.setInteger(MediaFormat.KEY_PROFILE, profile);
                    format.setInteger(MediaFormat.KEY_LEVEL, level);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // エンコーダのレイテンシーを設定します。
            // 機種依存でサポートされていない場合には、この値は無視されます。
            format.setInteger(MediaFormat.KEY_LATENCY, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int intraRefresh = videoQuality.getIntraRefresh();
            if (intraRefresh != 0) {
                format.setInteger(MediaFormat.KEY_INTRA_REFRESH_PERIOD, intraRefresh);
            }
        }

        MediaCodec mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return mediaCodec;
    }

    /**
     * コーデックが指定されたプロファイルとレベルをサポートしているか確認します.
     *
     * @param codecCapabilities コーデックの機能
     * @param profile プロファイル
     * @param level レベル
     * @return サポートしている場合はtrue、それ以外はfalse
     */
    private boolean isProfileSupported(MediaCodecInfo.CodecCapabilities codecCapabilities, int profile, int level) {
        if (codecCapabilities.profileLevels != null) {
            for (MediaCodecInfo.CodecProfileLevel c : codecCapabilities.profileLevels) {
                if (c.profile == profile && c.level >= level) {
                    return true;
                }
            }
        }
        return false;
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
