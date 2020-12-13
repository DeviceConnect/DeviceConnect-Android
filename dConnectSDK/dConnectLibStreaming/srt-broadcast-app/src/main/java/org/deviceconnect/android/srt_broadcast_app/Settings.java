package org.deviceconnect.android.srt_broadcast_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * 設定管理クラス.
 */
class Settings {

    static final int DEFAULT_CAMERA_FACING = 1; // 0=Front, 1=Back

    private static final int DEFAULT_CAMERA_PREVIEW_WIDTH = 640; //px
    private static final int DEFAULT_CAMERA_PREVIEW_HEIGHT = 480; //px

    static final String DEFAULT_ENCODER_FPS = "30"; //fps
    static final String DEFAULT_ENCODER_BITRATE = "1024";
    static final String DEFAULT_ENCODER_I_FRAME_INTERVAL = "1";

    static final String DEFAULT_AUDIO_SAMPLING_RATE = "8000";
    static final String DEFAULT_AUDIO_BITRATE = "64";

    private final SharedPreferences mSharedPreferences;

    Settings(final Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    String getBroadcastURI() {
        return mSharedPreferences.getString("broadcast_uri", null);
    }

    int getCameraFacing() {
        String value = mSharedPreferences.getString("camera_facing", null);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return DEFAULT_CAMERA_FACING;
    }

    @NonNull
    Size getCameraPreviewSize(final int facing) {
        String value = mSharedPreferences.getString("camera_preview_size_" + facing, null);
        if (value == null) {
            return new Size(DEFAULT_CAMERA_PREVIEW_WIDTH, DEFAULT_CAMERA_PREVIEW_HEIGHT);
        }
        String[] values = value.split(" x ");
        return new Size(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
    }

    int getEncoderFrameRate() {
        String value = mSharedPreferences.getString("encoder_frame_rate", DEFAULT_ENCODER_FPS);
        return Integer.parseInt(value);
    }

    int getEncoderBitrate() {
        String value = mSharedPreferences.getString("encoder_bitrate", DEFAULT_ENCODER_BITRATE);
        return Integer.parseInt(value);
    }

    int getEncoderIFrameInterval() {
        String value = mSharedPreferences.getString("encoder_i_frame_interval", DEFAULT_ENCODER_I_FRAME_INTERVAL);
        return Integer.parseInt(value);
    }

    public boolean isEnabledAudio() {
        return mSharedPreferences.getBoolean("audio_enable", false);
    }

    public int getSamplingRate() {
        String value = mSharedPreferences.getString("audio_sampling_rate", DEFAULT_AUDIO_SAMPLING_RATE);
        return Integer.parseInt(value);
    }

    public int getAudioBitRate() {
        String value = mSharedPreferences.getString("audio_bitrate", DEFAULT_AUDIO_BITRATE);
        return Integer.parseInt(value);
    }

    int getPeerLatency() {
        String value = mSharedPreferences.getString("settings_srt_peerlatency", "120");
        return Integer.parseInt(value);
    }

    int getLossMaxTTL() {
        String value = mSharedPreferences.getString("settings_srt_lossmaxttl", "0");
        return Integer.parseInt(value);
    }

    int getConnTimeo() {
        String value = mSharedPreferences.getString("settings_srt_conntimeo", "3000");
        return Integer.parseInt(value);
    }

    int getPeerIdleTimeo() {
        String value = mSharedPreferences.getString("settings_srt_peeridletimeo", "5000");
        return Integer.parseInt(value);
    }
}
