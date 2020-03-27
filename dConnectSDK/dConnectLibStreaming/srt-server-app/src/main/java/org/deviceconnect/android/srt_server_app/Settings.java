package org.deviceconnect.android.srt_server_app;

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

    static final int DEFAULT_ENCODER_FPS = 30; //fps

    static final String DEFAULT_ENCODER_BITRATE_BASE = "3"; //Mbps

    private final SharedPreferences mSharedPreferences;

    Settings(final Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    String getEncoderName() {
        return mSharedPreferences.getString("encoder_name", "video_avc");
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
        String value = mSharedPreferences.getString("encoder_frame_rate", null);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return DEFAULT_ENCODER_FPS;
    }

    int getEncoderBitRate() {
        String value = mSharedPreferences.getString("encoder_bit_rate", DEFAULT_ENCODER_BITRATE_BASE);
        float base = Float.parseFloat(value);
        return (int) base * 1024 * 1024;
    }

    boolean isAudioEnabled() {
        return mSharedPreferences.getBoolean("audio_enabled", true);
    }

    int getSamplingRate() {
        String value = mSharedPreferences.getString("audio_sampling_rate", "8000");
        return Integer.parseInt(value);
    }

    int getAudioBitRate() {
        String value = mSharedPreferences.getString("audio_bit_rate", "64");
        return Integer.parseInt(value) * 1024;
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
