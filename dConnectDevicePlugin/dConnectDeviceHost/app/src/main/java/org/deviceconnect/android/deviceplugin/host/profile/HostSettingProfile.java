/*
 HostSettingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;


import org.deviceconnect.android.activity.IntentHandlerActivity;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SettingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.RFC3339DateUtils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;

/**
 * Settingsプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostSettingProfile extends SettingProfile {

    /** Light Levelの最大値. */
    private static final int MAX_LIGHT_LEVEL = 255;


    private final DConnectApi mGetSoundVolumeApi = new GetApi() {

        @Override
        public String getInterface() {
            return INTERFACE_SOUND;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_VOLUME;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            VolumeKind kind = getVolumeKind(request);

            AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

            double volume = 0.0;
            double maxVolume = 1.0;
            if (kind == VolumeKind.ALARM) {
                volume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                setResult(response, DConnectMessage.RESULT_OK);
                setVolumeLevel(response, volume / maxVolume);
            } else if (kind == VolumeKind.CALL) {
                volume = manager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                setResult(response, DConnectMessage.RESULT_OK);
                setVolumeLevel(response, volume / maxVolume);
            } else if (kind == VolumeKind.RINGTONE) {
                volume = manager.getStreamVolume(AudioManager.STREAM_RING);
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
                setResult(response, DConnectMessage.RESULT_OK);
                setVolumeLevel(response, volume / maxVolume);
            } else if (kind == VolumeKind.MAIL) {
                volume = manager.getStreamVolume(AudioManager.STREAM_RING);
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
                setResult(response, DConnectMessage.RESULT_OK);
                setVolumeLevel(response, volume / maxVolume);
            } else if (kind == VolumeKind.MEDIA_PLAYER) {
                volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                setResult(response, DConnectMessage.RESULT_OK);
                setVolumeLevel(response, volume / maxVolume);
            } else if (kind == VolumeKind.OTHER) {
                MessageUtils.setNotSupportAttributeError(response, "volume type is not support.");
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "type is invalid.");
            }
            return true;
        }
    };

    private final DConnectApi mGetDateApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DATE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setDate(response, RFC3339DateUtils.nowTimeStampString());
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetDisplayBrightnessApi = new GetApi() {

        @Override
        public String getInterface() {
            return INTERFACE_DISPLAY;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BRIGHTNESS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // 自動調整ボタンが有効な場合 0が変える
            // 端末画面の明るさを取得(0～255)
            double level = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                0);
            double maxLevel = MAX_LIGHT_LEVEL;
            setLightLevel(response, level / maxLevel);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetDisplaySleepApi = new GetApi() {

        @Override
        public String getInterface() {
            return INTERFACE_DISPLAY;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_SLEEP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            int timeout = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                0);
            setTime(response, timeout);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutSoundVolumeApi = new PutApi() {

        @Override
        public String getInterface() {
            return INTERFACE_SOUND;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_VOLUME;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            VolumeKind kind = getVolumeKind(request);
            Double level = getVolumeLevel(request);

            AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            double maxVolume = 1;
            if (kind == VolumeKind.ALARM) {
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                manager.setStreamVolume(AudioManager.STREAM_ALARM, (int) (maxVolume * level), 1);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (kind == VolumeKind.CALL) {
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                manager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (int) (maxVolume * level), 1);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (kind == VolumeKind.RINGTONE) {
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
                manager.setStreamVolume(AudioManager.STREAM_RING, (int) (maxVolume * level), 1);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (kind == VolumeKind.MAIL) {
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
                manager.setStreamVolume(AudioManager.STREAM_RING, (int) (maxVolume * level), 1);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (kind == VolumeKind.MEDIA_PLAYER) {
                maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolume * level), 1);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (kind == VolumeKind.OTHER) {
                MessageUtils.setNotSupportAttributeError(response, "volume type is not support.");
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "type is invalid.");
            }
            return true;
        }
    };

    private final DConnectApi mPutDisplayBrightnessApi = new PutApi() {

        @Override
        public String getInterface() {
            return INTERFACE_DISPLAY;
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BRIGHTNESS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final Double level = getLightLevel(request);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(getContext())) {
                    onPutDisplayLightInternal(request, response, serviceId, level);
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getContext().getPackageName()));
                    IntentHandlerActivity.startActivityForResult(getContext(), intent,
                        new ResultReceiver(new Handler(Looper.getMainLooper())) {
                            @Override
                            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                                if (Settings.System.canWrite(getContext())) {
                                    onPutDisplayLightInternal(request, response, serviceId, level);
                                } else {
                                    MessageUtils.setIllegalServerStateError(response,
                                        "WRITE_SETTINGS permisson not granted");
                                }
                                sendResponse(response);
                            }
                        });
                    return false;
                }
            } else {
                onPutDisplayLightInternal(request, response, serviceId, level);
            }
            return true;
        }
    };

    private final DConnectApi mPutDisplaySleepApi = new PutApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final Integer time = getTime(request);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(getContext())) {
                    onPutDisplaySleepInternal(request, response, serviceId, time);
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getContext().getPackageName()));
                    IntentHandlerActivity.startActivityForResult(getContext(), intent,
                        new ResultReceiver(new Handler(Looper.getMainLooper())) {
                            @Override
                            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                                if (Settings.System.canWrite(getContext())) {
                                    onPutDisplaySleepInternal(request, response, serviceId, time);
                                } else {
                                    MessageUtils.setIllegalServerStateError(response,
                                        "WRITE_SETTINGS permisson not granted");
                                }
                                sendResponse(response);
                            }
                        });
                    return false;
                }
            } else {
                onPutDisplaySleepInternal(request, response, serviceId, time);
            }
            return true;
        }
    };

    public HostSettingProfile() {
        addApi(mGetSoundVolumeApi);
        addApi(mGetDateApi);
        addApi(mGetDisplayBrightnessApi);
        addApi(mGetDisplaySleepApi);
        addApi(mPutSoundVolumeApi);
        addApi(mPutDisplayBrightnessApi);
        addApi(mPutDisplaySleepApi);
    }

    private void onPutDisplayLightInternal(final Intent request, final Intent response, final String serviceId,
            final Double level) {
        if (level == null || level < 0 || level > 1.0) {
            MessageUtils.setInvalidRequestParameterError(response, "level is invalid.");
            return;
        }
        Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                (int) (MAX_LIGHT_LEVEL * level));
        setResult(response, DConnectMessage.RESULT_OK);
    }

    private void onPutDisplaySleepInternal(final Intent request, final Intent response, final String serviceId,
            final Integer time) {
        if (time == null || time < 0.0) {
            MessageUtils.setInvalidRequestParameterError(response, "time is invalid.");
            return;
        }
        if (Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time)) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setUnknownError(response, "Failed to set display sleep timeout.");
        }
    }

}
