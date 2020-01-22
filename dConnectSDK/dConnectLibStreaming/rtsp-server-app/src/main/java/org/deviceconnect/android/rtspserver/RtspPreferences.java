package org.deviceconnect.android.rtspserver;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import org.deviceconnect.android.libmedia.streaming.camera2.Camera2Wrapper;


public class RtspPreferences {
    private SharedPreferences mPreferences;
    private Context mContext;

    RtspPreferences(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }

    private Integer getInt(int resId, int defaultResId) {
        return Integer.parseInt(mPreferences.getString(getString(resId), getString(defaultResId)));
    }

    public String getServerName() {
        return "TEST-RTSP-Server";
    }

    public int getServerPort() {
        return getInt(R.string.key_server_port, R.string.settings_server_port);
    }

    public String getFileName() {
        return "test.mp4";
    }

    public int getVideoWidth() {
        String resolution = mPreferences.getString(getString(R.string.key_video_resolution), "640x480");
        String[] sizes = resolution.split("x");
        return Integer.parseInt(sizes[0]);
    }

    public int getVideoHeight() {
        String resolution = mPreferences.getString(getString(R.string.key_video_resolution), "640x480");
        String[] sizes = resolution.split("x");
        return Integer.parseInt(sizes[1]);
    }

    public int getIFrameInterval() {
        return getInt(R.string.key_video_iframe_interval, R.string.settings_video_iframe_interval);
    }

    public int getFrameRate() {
        return getInt(R.string.key_video_frame_rate, R.string.settings_video_frame_rate);
    }

    public int getVideoBitRate() {
        return getInt(R.string.key_video_bit_rate, R.string.settings_video_bit_rate);
    }

    public Camera2Wrapper.Rotation getVideoRotation() {
        int rotation = getInt(R.string.key_video_rotation, R.string.settings_video_rotation);
        switch (rotation) {
            default:
                return Camera2Wrapper.Rotation.FREE;
            case 0:
                return Camera2Wrapper.Rotation.ROTATION_0;
            case 90:
                return Camera2Wrapper.Rotation.ROTATION_90;
            case 180:
                return Camera2Wrapper.Rotation.ROTATION_180;
            case 270:
                return Camera2Wrapper.Rotation.ROTATION_270;
        }
    }

    public boolean isEnabledAudio() {
        return mPreferences.getBoolean(getString(R.string.key_audio_enabled), true);
    }

    public int getSamplingRate() {
        return getInt(R.string.key_audio_sampling_rate, R.string.settings_audio_sampling_rate);
    }

    public int getAudioBitRate() {
        return getInt(R.string.key_audio_bit_rate, R.string.settings_audio_bit_rate);
    }

    public boolean isEnabledRecorder() {
        return mPreferences.getBoolean(getString(R.string.key_recorder_enabled), false);
    }
}
