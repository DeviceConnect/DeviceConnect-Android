package org.deviceconnect.android.libmedia.streaming.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.MediaEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AudioEncoder extends MediaEncoder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AUDIO-ENCODER";

    /**
     * ミュート設定.
     */
    private boolean mMute = true;

    /**
     * 音声のエンコード設定を取得します.
     *
     * @return 音声のエンコード設定
     */
    public abstract AudioQuality getAudioQuality();

    @Override
    protected void prepare() throws IOException {
        AudioQuality audioQuality = getAudioQuality();

        String mimeType = audioQuality.getMimeType();

        MediaCodecInfo codecInfo = null;

        List<MediaCodecInfo> infoList = getMediaCodecInfo(mimeType);
        if (infoList.isEmpty()) {
            throw new IOException(mimeType + " not supported.");
        }

        // 指定されたサンプルレートがサポートしている MediaCodec を選択する。
        // ハードウェアエンコーダ(OMX.) を優先的に選択する。
        for (MediaCodecInfo c : infoList) {
            if (c.isEncoder()) {
                try {
                    MediaCodecInfo.CodecCapabilities caps = c.getCapabilitiesForType(mimeType);
                    if (caps != null) {
                        MediaCodecInfo.AudioCapabilities audioCapabilities = caps.getAudioCapabilities();
                        for (int sampleRate : audioCapabilities.getSupportedSampleRates()) {
                            if (sampleRate == audioQuality.getSamplingRate()) {
                                if (codecInfo == null || c.getName().startsWith("OMX.")) {
                                    codecInfo = c;
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore.
                }
            }
        }

        if (codecInfo == null) {
            throw new RuntimeException(mimeType + " not supported.");
        }

        if (DEBUG) {
            Log.d(TAG, "List of MediaCodeInfo supported by MediaCodec.");
            for (MediaCodecInfo info : infoList) {
                Log.d(TAG, "  " + info.getName());
            }
            Log.i(TAG, "---");
            Log.i(TAG, "SELECT: " + codecInfo.getName());
            Log.i(TAG, "MIME_TYPE: " + audioQuality.getMimeType());
            Log.i(TAG, "SAMPLE_RATE: " + audioQuality.getSamplingRate());
            Log.i(TAG, "CHANNEL: " + audioQuality.getChannelCount());
            Log.i(TAG, "FORMAT: " + audioQuality.getFormat());
            Log.i(TAG, "BIT_RATE: " + audioQuality.getBitRate());
            Log.i(TAG, "---");
        }

        MediaFormat format = MediaFormat.createAudioFormat(audioQuality.getMimeType(),
                audioQuality.getSamplingRate(), audioQuality.getChannelCount());
        format.setString(MediaFormat.KEY_MIME, audioQuality.getMimeType());
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, audioQuality.getSamplingRate());
        format.setInteger(MediaFormat.KEY_BIT_RATE, audioQuality.getBitRate());
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, audioQuality.getChannelCount());
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, audioQuality.getChannel());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, audioQuality.getAACProfile());
        if (audioQuality.getMaxInputSize() > 0) {
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioQuality.getMaxInputSize());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            format.setInteger(MediaFormat.KEY_PCM_ENCODING, audioQuality.getFormat());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 0: realtime priority
            // 1: non-realtime priority (best effort).
            format.setInteger(MediaFormat.KEY_PRIORITY, 0x00);
        }

        mMediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    @Override
    protected void startRecording() {
    }

    @Override
    protected void stopRecording() {
    }

    @Override
    protected void release() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    /**
     * ミュート設定を取得します.
     *
     * @return ミュートの場合はtrue、それ以外はfalse
     */
    public boolean isMute() {
        return mMute;
    }

    /**
     * ミュートを設定します.
     *
     * <p>
     * デフォルトでは、ミュートは true になっています。
     * </p>
     *
     * @param mute ミュートにする場合はtrue、それ以外はfalse
     */
    public void setMute(boolean mute) {
        mMute = mute;
    }


    /**
     * 指定された MediaCodecInfo のマイムタイプとカラーフォーマットが一致するか確認します.
     *
     * @param codecInfo 確認する MediaCodecInfo
     * @param mimeType マイムタイプ
     * @return 一致する場合はtrue、それ以外はfalse
     */
    private boolean isMediaCodecInfo(MediaCodecInfo codecInfo, String mimeType) {
        if (!codecInfo.isEncoder()) {
            return false;
        }

        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 指定されたマイムタイプに対応した MediaCodecInfo のリストを取得します.
     *
     * <p>
     * 対応した MediaCodecInfo が存在しない場合には空のリストを返却します。
     * </p>
     *
     * @param mimeType マイムタイプ
     * @return MediaCodecInfo のリスト
     */
    private List<MediaCodecInfo> getMediaCodecInfo(String mimeType) {
        List<MediaCodecInfo> infoList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
            for (MediaCodecInfo codecInfo : list.getCodecInfos()) {
                if (isMediaCodecInfo(codecInfo, mimeType)) {
                    infoList.add(codecInfo);
                }
            }
        } else {
            for (int i = MediaCodecList.getCodecCount() - 1; i >= 0; i--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (isMediaCodecInfo(codecInfo, mimeType)) {
                    infoList.add(codecInfo);
                }
            }
        }
        return infoList;
    }
}
