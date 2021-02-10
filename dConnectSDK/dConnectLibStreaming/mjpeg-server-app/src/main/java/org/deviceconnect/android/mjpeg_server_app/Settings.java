package org.deviceconnect.android.mjpeg_server_app;

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

    static final String DEFAULT_ENCODER_QUALITY = "80"; //%

    private final SharedPreferences mSharedPreferences;

    Settings(final Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    int getEncoderQuality() {
        String value = mSharedPreferences.getString("encoder_quality", DEFAULT_ENCODER_QUALITY);
        return Integer.parseInt(value);
    }
}
