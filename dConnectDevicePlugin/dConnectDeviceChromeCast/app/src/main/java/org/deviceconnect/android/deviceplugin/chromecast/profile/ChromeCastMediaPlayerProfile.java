/*
 ChromeCastMediaPlayerProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import com.google.android.gms.cast.MediaStatus;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastDiscovery;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastHttpServer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMediaPlayer;
import org.deviceconnect.android.deviceplugin.chromecast.core.MediaFile;
import org.deviceconnect.android.deviceplugin.chromecast.core.MediaStoreContent;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaPlayer プロファイル (Chromecast).
 * <p>
 * Chromecastのメディア操作を提供する
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastMediaPlayerProfile extends MediaPlayerProfile {

    /** イベントの登録・解除に失敗したときのエラーコード. */
    private static final int ERROR_VALUE_IS_NULL = 100;
    private static final String SAMPLE_URL = "https://github.com/DeviceConnect/DeviceConnect-Android/wiki/sphero_demo.MOV";
    /** １ミリ秒. */
    private static final int MILLISECOND = 1000;
    /** Chromecastの再生状態がバッファリング中であることを表す. */
    private static final String MESSAGE_BUFFERING                   = "buffering";
    /** Chromecastの再生状態がstop状態であることを表す. */
    private static final String MESSAGE_STOP = "stop";
    /** Chromecastの再生状態がpause状態であることを表す. */
    private static final String MESSAGE_PAUSED                      = "pause";
    /** Chromecastの再生状態がplay状態であることを表す. */
    private static final String MESSAGE_PALYING                     = "play";
    /** Chromecastの再生状態がunknownであることを表す. */
    private static final String MESSAGE_UNKNOWN                     = "unknown";
    /** Chromecastが有効になっていない時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_DEVICE_NOT_ENABLE     = "Device is not enable";
    /** Chromecastに再生するメディアファイルが設定されていない時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_NOT_SELECTED    = "Media is not selected";
    /** Chromecastの再生状態がない場合のエラーメッセージ. */
    private static final String ERROR_MESSAGE_PLAYSTATE_IS_NOT      = "Playstate is not";
    /** メディアファイルのPlay時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_PLAY            = ERROR_MESSAGE_PLAYSTATE_IS_NOT
                                                            + " " + MESSAGE_STOP + " or " + MESSAGE_PAUSED;
    /** メディアファイルのResume時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_RESUME          = ERROR_MESSAGE_PLAYSTATE_IS_NOT 
                                                                                + " " + MESSAGE_PAUSED;
    /** メディアファイルのStop時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_STOP            = ERROR_MESSAGE_PLAYSTATE_IS_NOT
                                + " " + MESSAGE_PALYING + " or " + MESSAGE_PAUSED + " or " + MESSAGE_BUFFERING;
    /** メディアファイルのPause時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_PAUSE           = ERROR_MESSAGE_PLAYSTATE_IS_NOT
                                                                                    + " " + MESSAGE_PALYING;
    /** メディアファイルがMute設定時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_MUTE            = ERROR_MESSAGE_PLAYSTATE_IS_NOT
                                                        + " " + MESSAGE_PALYING + " or " + MESSAGE_PAUSED;
    /** メディアファイルのVolume変更時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_VOLUME          = ERROR_MESSAGE_MEDIA_MUTE;
    /** メディアファイルのSeek変更時のエラーメッセージ. */
    private static final String ERROR_MESSAGE_MEDIA_SEEK            = ERROR_MESSAGE_MEDIA_MUTE;

    /** メディア情報の{@link java.util.Comparator}のマップ. */
    private static final Map<String, ParamComparator> COMPARATORS = new HashMap<String, ParamComparator>();

    /** 比較をしない{@link java.util.Comparator}. */
    private static final Comparator<Bundle> NOT_COMPARATOR = new Comparator<Bundle>() {
        @Override
        public int compare(final Bundle a, final Bundle b) {
            return 0;
        }
    };

    static {
        COMPARATORS.put(PARAM_TYPE, new ParamComparator(PARAM_TYPE));
        COMPARATORS.put(PARAM_LANGUAGE, new ParamComparator(PARAM_LANGUAGE));
        COMPARATORS.put(PARAM_MEDIA_ID, new ParamComparator(PARAM_MEDIA_ID));
        COMPARATORS.put(PARAM_MIME_TYPE, new ParamComparator(PARAM_MIME_TYPE));
        COMPARATORS.put(PARAM_TITLE, new ParamComparator(PARAM_TITLE));
        COMPARATORS.put(PARAM_DURATION, new ParamComparator(PARAM_DURATION));
    }

    public ChromeCastMediaPlayerProfile() {
        addApi(mGetMediaApi);
        addApi(mPutMediaApi);
        addApi(mGetSeekApi);
        addApi(mPutSeekApi);
        addApi(mGetVolumeApi);
        addApi(mPutMuteApi);
        addApi(mGetMuteApi);
        addApi(mDeleteMuteApi);
        addApi(mPutPauseApi);
        addApi(mPutPlayApi);
        addApi(mPutResumeApi);
        addApi(mPutStopApi);
        addApi(mPutVolumeApi);
        addApi(mPutOnStatusChangeApi);
        addApi(mDeleteOnStatusChangeApi);
        addApi(mGetMediaListApi);
        addApi(mGetPlayStatusApi);
    }

    /**
     * 指定したパラメータに対する{@link java.util.Comparator}を返す.
     *
     * @param paramName パラメータ名
     * @param isAsc 昇順である場合は<code>true</code>、そうでなければ<code>false</code>
     * @return {@link java.util.Comparator}
     */
    private static Comparator<Bundle> findComparator(final String paramName, final boolean isAsc) {
        ParamComparator comparator = COMPARATORS.get(paramName);
        if (comparator != null) {
            comparator.setOrder(isAsc);
            return comparator;
        }
        return NOT_COMPARATOR;
    }

    /**
     * 再生状態を文字列に変換する.
     * 
     * @param   playState   再生状態
     * @return  再生状態の文字列を返す
     */
    public String getPlayStatus(final int playState) {
        switch (playState) {
        case MediaStatus.PLAYER_STATE_BUFFERING:
            return MESSAGE_BUFFERING;
        case MediaStatus.PLAYER_STATE_IDLE:
            return MESSAGE_STOP;
        case MediaStatus.PLAYER_STATE_PAUSED:
            return MESSAGE_PAUSED;
        case MediaStatus.PLAYER_STATE_PLAYING:
            return MESSAGE_PALYING;
        case MediaStatus.PLAYER_STATE_UNKNOWN:
        default:
            return MESSAGE_UNKNOWN;
        }
    }

    /**
     * サービスからChromeCastMediaPlayerを取得する.
     * @return  ChromeCastMediaPlayer
     */
    private ChromeCastMediaPlayer getChromeCastMediaPlayer() {
        return ((ChromeCastService) getContext()).getChromeCastMediaPlayer();
    }
    /**
     * サービスからChromeCastDiscoveryrを取得する.
     * @return  ChromeCastDiscovery
     */
    private ChromeCastDiscovery getChromeCastDiscovery() {
        return ((ChromeCastService) getContext()).getChromeCastDiscovery();
    }
    /**
     * デバイスが有効か否かを返す<br>.
     * デバイスが無効の場合、レスポンスにエラーを設定する
     * 
     * @param   response    レスポンス
     * @param   app         ChromeCastMediaPlayer
     * @return  デバイスが有効か否か（有効: true, 無効: false）
     */
    private boolean isDeviceEnable(final Intent response, final ChromeCastMediaPlayer app) {
        if (!app.isDeviceEnable()) {
            MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_DEVICE_NOT_ENABLE);
            return false;
        }
        return true;
    }

    /**
     * メディアの状態を取得する.
     * @param   response    レスポンス
     * @param   app         ChromeCastMediaPlayer
     * @return  デバイスが有効か否か（有効: {@link MediaStatus}, 無効: <code>null</code>）
     */
    private MediaStatus getMediaStatus(final Intent response, final ChromeCastMediaPlayer app) {
        MediaStatus status = app.getMediaStatus();
        if (status == null) {
            MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_MEDIA_NOT_SELECTED);
        }
        return status;
    }

    private final DConnectApi mPutPlayApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_PLAY;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,
                (connected) -> {
                    if (!connected) {
                        MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                        sendResponse(response);
                        return;
                    }
                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                    if (!isDeviceEnable(response, app)) {
                        sendResponse(response);
                        return;
                    }

                    MediaStatus status = getMediaStatus(response, app);
                    if (status == null) {
                        sendResponse(response);
                        return;
                    }

                    if (status.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING) {
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                        return;
                    }
                    if (status.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE) {
                        app.play(response);
                    } else if (status.getPlayerState() == MediaStatus.PLAYER_STATE_PAUSED) {
                        app.resume(response);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_MEDIA_PLAY);
                        sendResponse(response);
                    }
            });
            return false;
        }
    };
    private final DConnectApi mPutResumeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_RESUME;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                MediaStatus status = getMediaStatus(response, app);
                if (status == null) {
                    sendResponse(response);
                    return;
                }

                if (status.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                    return;
                }
                if (status.getPlayerState() == MediaStatus.PLAYER_STATE_PAUSED) {
                    app.resume(response);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_MEDIA_RESUME);
                    sendResponse(response);
                }
            });
            return false;
        }
    };


    private final DConnectApi mPutStopApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_STOP;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                MediaStatus status = getMediaStatus(response, app);
                if (status == null) {
                    sendResponse(response);
                    return;
                }

                if (status.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                    return;
                }
                if (status.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING
                        || status.getPlayerState() == MediaStatus.PLAYER_STATE_PAUSED
                        || status.getPlayerState() == MediaStatus.PLAYER_STATE_BUFFERING) {
                    app.stop(response);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_MEDIA_STOP);
                    sendResponse(response);
                }
            });
            return false;
        }
    };
    private final DConnectApi mPutPauseApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_PAUSE;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                MediaStatus status = getMediaStatus(response, app);
                if (status == null) {
                    sendResponse(response);
                    return;
                }

                if (status.getPlayerState() == MediaStatus.PLAYER_STATE_PAUSED) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                    return;
                }
                app.pause(response);
            });
            return false;
        }
    };
    /**
     * メディアをミュートする<br/>.
     * エラーの場合、レスポンスにエラーを設定する
     * 
     * @param   request     リクエスト
     * @param   response    レスポンス
     * @param   serviceId    サービスID
     * @param   mute        ミュートするか否か（true: ミュートON, false: ミュートOFF）
     * @return  result      結果を返す（true: 成功, false: 失敗）
     */
    private boolean setMute(final Intent request, final Intent response,
            final String serviceId, final boolean mute) {
        ((ChromeCastService) getContext()).connectChromeCast(serviceId, new ChromeCastService.Callback() {

            @Override
            public void onResponse(final boolean connected) {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app))	{
                    sendResponse(response);
                    return;
                }
                MediaStatus status = getMediaStatus(response, app);
                if (status == null) {
                    sendResponse(response);
                    return;
                }
                app.setMute(response, mute);
            }
        });
        return false;
    }

    private final DConnectApi mPutMuteApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_MUTE;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            return setMute(request, response, serviceId, true);
        }
    };

    private final DConnectApi mDeleteMuteApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_MUTE;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            return setMute(request, response, serviceId, false);
        }
    };

    private final DConnectApi mGetMuteApi = new GetApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_MUTE;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                if (getMediaStatus(response, app) == null) {
                    sendResponse(response);
                    return;
                }

                int mute = app.getMute(response);
                if (mute == 1) {
                    setMute(response, true);
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                } else if (mute == 0) {
                    setMute(response, false);
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }
            });
            return false;

        }
    };

    private final DConnectApi mPutVolumeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_VOLUME;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final Double volume = MediaPlayerProfile.getVolume(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,
                 (connected) -> {
                    if (!connected) {
                        MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                        sendResponse(response);
                        return;
                    }
                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                    if (!isDeviceEnable(response, app)) {
                        sendResponse(response);
                        return;
                    }
                    MediaStatus status = getMediaStatus(response, app);
                    if (status == null) {
                        sendResponse(response);
                        return;
                    }

                    if (volume == null) {
                        MessageUtils.setInvalidRequestParameterError(response);
                    } else if (0.0 > volume || volume > 1.0) {
                        MessageUtils.setInvalidRequestParameterError(response);
                    } else {
                        app.setVolume(response, volume);
                        return;
                    }
                    sendResponse(response);
                });
            return false;
        }
    };

    private final DConnectApi mGetVolumeApi = new GetApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_VOLUME;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,
                 (connected) -> {
                    if (!connected) {
                        MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                        sendResponse(response);
                        return;
                    }
                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                    if (!isDeviceEnable(response, app)) {
                        sendResponse(response);
                        return;
                    }
                    if (getMediaStatus(response, app) == null) {
                        sendResponse(response);
                        return;
                    }

                    double volume = app.getVolume(response);
                    if (volume >= 0) {
                        setVolume(response, volume);
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            return false;

        }
    };

    private final DConnectApi mPutSeekApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_SEEK;
        }
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final Integer pos = MediaPlayerProfile.getPos(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }

                if (pos == null) {
                    MessageUtils.setInvalidRequestParameterError(response);
                    sendResponse(response);
                } else {
                    MediaStatus status = getMediaStatus(response, app);
                    if (status == null) {
                        sendResponse(response);
                        return;
                    }

                    long posMillisecond = pos * MILLISECOND;
                    if (0 > posMillisecond || posMillisecond > status.getMediaInfo().getStreamDuration()) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);
                        return;
                    }
                    app.setSeek(response, posMillisecond);
                }
            });
            return false;

        }
    };
    private final DConnectApi mGetSeekApi = new GetApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_SEEK;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                MediaStatus status = getMediaStatus(response, app);
                if (status == null) {
                    sendResponse(response);
                    return;
                }
                long streamPosition = status.getStreamPosition();
                long posSecond = app.getSeek(response) / MILLISECOND;
                if (posSecond > 0) {
                    setPos(response, (int) posSecond);
                } else if (streamPosition > 0) {
                    setPos(response, (int) streamPosition);
                } else {
                    setPos(response, 0);
                }
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);

            });
            return false;
        }
    };

    private final DConnectApi mGetPlayStatusApi = new GetApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_PLAY_STATUS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);

            ((ChromeCastService) getContext()).connectChromeCast(serviceId,
                (connected) -> {
                    if (!connected) {
                        MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                        sendResponse(response);
                        return;
                    }
                    ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                    if (!isDeviceEnable(response, app)) {
                        sendResponse(response);
                        return;
                    }

                    MediaStatus status = getMediaStatus(response, app);
                    if (status == null) {
                        sendResponse(response);
                        return;
                    }
                    String playStatus = getPlayStatus(status.getPlayerState());
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, playStatus);
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                });
            return false;
        }
    };


    private final DConnectApi mGetMediaApi = new GetApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_MEDIA;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String mediaId = MediaPlayerProfile.getMediaId(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,(connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                if (mediaId == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "mediaId is null.");
                    sendResponse(response);
                    return;
                }
                if (mediaId.equals("")) {
                    MessageUtils.setInvalidRequestParameterError(response, "mediaId is empty.");
                    sendResponse(response);
                    return;
                }
                Bundle media = getMedia(mediaId);
                for (String key : media.keySet()) {
                    Object value = media.get(key);
                    if (value instanceof String) {
                        response.putExtra(key, (String) value);
                    } else if (value instanceof String[]) {
                        response.putExtra(key, (String[]) value);
                    } else if (value instanceof Long) {
                        response.putExtra(key, (Long) value);
                    } else if (value instanceof Bundle) {
                        response.putExtra(key, (Bundle) value);
                    }
                }

                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            });
            return false;

        }
    };

    /**
     * 指定したURIからkeyとvalueに基づき、Cursorを取得する.
     * 
     * @param   uri     URI
     * @param   key     キー
     * @param   value   バリュー
     * @return  cursor  カーソル
     */
    private Cursor getCursorFrom(final Uri uri, final String key, final String value) {
        Cursor cursor = getContext().getApplicationContext()
                .getContentResolver()
                .query(uri, null, key + "=" + value + "", null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (cursor.getCount() == 1) {
                    return cursor;
                }
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 指定したメディアをローカルサーバ上で公開する.
     * 
     * @param   mediaId     メディアID
     * @return  placeHolderUrl    代用URL
     */
    private String exposeMedia(final long mediaId) {
        Uri targetUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri mediaUri = ContentUris.withAppendedId(targetUri, mediaId);

        ChromeCastHttpServer server = ((ChromeCastService) getContext())
                .getChromeCastHttpServer();
        return server.exposeFile(new MediaStoreContent(mediaUri));
    }

    private final DConnectApi mPutMediaApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_MEDIA;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String mediaId = MediaPlayerProfile.getMediaId(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,(connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                if (mediaId == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "mediaId is null.");
                    sendResponse(response);
                    return;
                }
                if (mediaId.equals("")) {
                    MessageUtils.setInvalidRequestParameterError(response, "mediaId is empty.");
                    sendResponse(response);
                    return;
                }
                if (!hasMedia(mediaId)) {
                    MessageUtils.setInvalidRequestParameterError(response, "media is not found.");
                    sendResponse(response);
                    return;
                }

                ChromeCastMediaPlayer app = getChromeCastMediaPlayer();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }

                String url = null;
                String title = null;
                Integer mId = -1;

                try {
                    mId = Integer.parseInt(mediaId);
                } catch (NumberFormatException e) {
                    url = mediaId;
                }

                if (url == null) {
                    Cursor cursor = getCursorFrom(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media._ID, mediaId);
                    try {
                        if (cursor == null) {
                            response.putExtra(DConnectMessage.EXTRA_VALUE, "mediaId is not exist");
                            setResult(response, DConnectMessage.RESULT_ERROR);
                            sendResponse(response);
                            return;
                        } else {
                            title = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Video.Media.TITLE));
                            url = exposeMedia(mId);
                            if (url == null) {
                                response.putExtra(DConnectMessage.EXTRA_VALUE, "url is null");
                                setResult(response, DConnectMessage.RESULT_ERROR);
                                sendResponse(response);
                                return;

                            }
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }

                if (title == null) {
                    title = "TITLE";
                }
                app.load(response, url, title);
            });
            return false;
        }
    };

    /**
     * Uriからパスを取得する.
     * 
     * @param   mUri    Uri
     * @return  パス
     */
    private String getPathFromUri(final Uri mUri) {
        String filename = null;
        Cursor c = this.getContext().getContentResolver().query(mUri, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            filename = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            c.close();
        }
        return filename;
    }

    /**
     * bundleにメディア情報を設定する.
     * 
     * @param   mediaType   メディアタイプ
     * @param   bundle      バンドル
     * @param   cursor      カーソル
     */
    private void setMediaInformation(final String mediaType, final Bundle bundle,
            final Cursor cursor) {
        if (mediaType.equals("Video")) {
            setType(bundle, mediaType);
            setLanguage(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.LANGUAGE)));
            setMediaId(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media._ID)));
            setMIMEType(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
            setTitle(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.TITLE)));
            int duration = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Video.Media.DURATION));
            if (duration < 0) {
                duration = 0;
            }
            setDuration(bundle, duration);
            Bundle creator = new Bundle();
            setCreator(creator, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.ARTIST)));
            setCreators(bundle, new Bundle[] {creator});

        } else if (mediaType.equals("Audio")) {
            setType(bundle, "Music");
            setMediaId(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID)));
            setMIMEType(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)));
            setTitle(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));
            int duration = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Video.Media.DURATION));
            if (duration < 0) {
                duration = 0;
            }
            setDuration(bundle, duration);
            Bundle creator = new Bundle();
            setCreator(creator, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            setRole(creator, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.COMPOSER)));
            setCreators(bundle, new Bundle[] {creator});

        } else if (mediaType.equals("Image")) {
            setType(bundle, mediaType);
            setMediaId(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media._ID)));
            setMIMEType(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.MIME_TYPE)));
            setTitle(bundle, cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.TITLE)));
            Bundle creator = new Bundle();
            setCreators(bundle, new Bundle[] {creator});
        } else {
            setType(bundle, mediaType);
        }
    }

    /**
     * メディア情報をリストアップする.
     * 
     * @param   mediaType   メディアタイプ
     * @param   list        リスト
     * @param   filter      フィルター
     * @param   orderBy     ソートオーダー
     */
    private void listupMedia(final String mediaType, final List<Bundle> list, final String filter,
            final String orderBy) {
        Uri uri = null;

        if (mediaType.equals("Video")) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (mediaType.equals("Audio")) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (mediaType.equals("Image")) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentResolver resolver = this.getContext().getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(uri, null, filter, null, orderBy);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Bundle medium = new Bundle();
                    setMediaInformation(mediaType, medium, cursor);
                    list.add(medium);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
	
    /**
     * メディア情報をリストアップする.
     * 
     * @param   mediaType   メディアタイプ
     * @param   list        リスト
     * @param   query       クエリー
     * @param   mimeType    MIMEタイプ
     * @param   orders      ソートオーダー
     */
    private void listupMedia(final String mediaType, final List<Bundle> list, final String query,
            final String mimeType, final String[] orders) {
        String filter = "";
        String orderBy;

        if (mimeType != null) {
            String key = null;
            if (mediaType.equals("Video")) {
                key = MediaStore.Video.Media.MIME_TYPE;
            } else if (mediaType.equals("Audio")) {
                key = MediaStore.Audio.Media.MIME_TYPE;
            } else if (mediaType.equals("Image")) {
                key = MediaStore.Images.Media.MIME_TYPE;
            }
            filter = "" + key + "='" + mimeType + "'";
        }
        if (query != null) {
            if (!filter.equals("")) {
                filter += " AND ";
            }
            if (mediaType.equals("Video")) {
                filter += "(" + MediaStore.Video.Media.TITLE + " LIKE '%" + query + "%')";
            } else if (mediaType.equals("Audio")) {
                filter += "(" + MediaStore.Audio.Media.TITLE + " LIKE '%" + query + "%'";
                filter += " OR " + MediaStore.Audio.Media.COMPOSER + " LIKE '%" + query + "%')";
            } else if (mediaType.equals("Image")) {
                filter += "(" + MediaStore.Images.Media.TITLE + " LIKE '%" + query + "%')";
            }
        }

        if (orders != null) {
            orderBy = orders[0] + " " + orders[1];
        } else {
            orderBy = "title asc";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String filt = filter;
            final String order = orderBy;
            PermissionUtility.requestPermissions(getContext(), new Handler(Looper.getMainLooper()),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    new PermissionUtility.PermissionRequestCallback() {
                        @Override
                        public void onSuccess() {

                            listupMedia(mediaType, list, filt, order);
                        }

                        @Override
                        public void onFail(@NonNull String deniedPermission) {
                        }
                    });
        } else {
            listupMedia(mediaType, list, filter, orderBy);
        }
    }

    /**
     * デバイス内のメディアを全検索する.
     * @param query 検索するタイトル
     * @param mimeType マイムタイプ
     * @param orders ソート
     * @return 検索結果
     */
    private List<Bundle> findAllMedia(final String query, final String mimeType,
                                      final String[] orders) {
        List<Bundle> list = new ArrayList<Bundle>();
        listupMedia("Video", list, query, mimeType, orders);
        Bundle medium = new Bundle();
        setType(medium, "Video");
        setLanguage(medium, "Language");
        setMediaId(medium,  SAMPLE_URL);
        setMIMEType(medium, "video/quicktime");
        setTitle(medium, "Title: Sample");
        setDuration(medium, 9999);
        Bundle creatorVideo = new Bundle();
        setCreator(creatorVideo, "Creator: Sample");
        setCreators(medium, new Bundle[]{creatorVideo});
        list.add(medium);

        return list;
    }

    /**
     * 指定したメディアの情報を取得する.
     * @param mediaId メディアID
     * @return メディア情報
     */
    private Bundle getMedia(final String mediaId) {
        List<Bundle> list = findAllMedia(null, null, null);
        for (Bundle b : list) {
            String id = b.getString(PARAM_MEDIA_ID);
            if (id.equals(mediaId)) {
                return b;
            }
        }
        return null;
    }

    /**
     * 指定したメディアを保有しているかどうかをチェックする.
     * @param mediaId メディアID
     * @return 保有している場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    private boolean hasMedia(final String mediaId) {
        return getMedia(mediaId) != null;
    }

    private final DConnectApi mGetMediaListApi = new GetApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_MEDIA_LIST;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String query = MediaPlayerProfile.getQuery(request);
            final String mimeType = MediaPlayerProfile.getMIMEType(request);
            final String[] orders = MediaPlayerProfile.getOrder(request);
            final Integer offset = MediaPlayerProfile.getOffset(request);
            final Integer limit = MediaPlayerProfile.getLimit(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,(connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                // パラメータの型チェック
                Bundle b = request.getExtras();
                if (b.getString(PARAM_LIMIT) != null) {
                    if (parseInteger(b.get(PARAM_LIMIT)) == null) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);
                        return;
                    }
                }
                if (b.getString(PARAM_OFFSET) != null) {
                    if (parseInteger(b.get(PARAM_OFFSET)) == null) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);
                        return;
                    }
                }

                // パラメータの範囲チェック
                if (orders != null && orders.length != 2) {
                    MessageUtils.setInvalidRequestParameterError(response, "order is invalid.");
                    sendResponse(response);
                    return;
                }
                final int offsetValue;
                if (offset != null) {
                    if (offset >= 0) {
                        offsetValue = offset;
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "offset is negative.");
                        sendResponse(response);
                        return;
                    }
                } else {
                    offsetValue = 0;
                }
                if (limit != null && limit < 0) {
                    MessageUtils.setInvalidRequestParameterError(response, "limit is negative.");
                    sendResponse(response);
                    return;
                }

                Comparator<Bundle> comparator = null;
                if (orders != null) {
                    boolean isAsc;
                    Order o = Order.getInstance(orders[1]);
                    switch (o) {
                        case ASC:
                            isAsc = true;
                            break;
                        case DSEC:
                            isAsc = false;
                            break;
                        default:
                            MessageUtils.setInvalidRequestParameterError(response, "order is invalid.");
                            sendResponse(response);
                            return;
                    }
                    comparator = findComparator(orders[0], isAsc);
                }
                // メディアリストのソート
                List<Bundle> foundMedia = findAllMedia(query, mimeType, orders);
                if (comparator != null) {
                    Collections.sort(foundMedia, comparator);
                }

                final int limitValue = limit != null ? limit : foundMedia.size();
                int endIndex = offsetValue + limitValue;
                if (endIndex > foundMedia.size()) {
                    endIndex = foundMedia.size();
                }

                List<Bundle> result;
                if (offsetValue < foundMedia.size()) {
                    result = foundMedia.subList(offsetValue, endIndex);
                } else {
                    result = new ArrayList<Bundle>();
                }

                setCount(response, result.size());
                setMedia(response, result.toArray(new Bundle[result.size()]));
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            });
            return false;
        }
    };

    private final DConnectApi mPutOnStatusChangeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId,(connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    ((ChromeCastService) getContext()).registerOnStatusChange(response,
                            serviceId);
                } else {
                    MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };
    private final DConnectApi mDeleteOnStatusChangeApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            ((ChromeCastService) getContext()).connectChromeCast(serviceId, (connected) -> {
                if (!connected) {
                    MessageUtils.setIllegalDeviceStateError(response, "The chromecast is not in local network.");
                    sendResponse(response);
                    return;
                }
                EventError error = EventManager.INSTANCE.removeEvent(request);
                if (error == EventError.NONE) {
                    ((ChromeCastService) getContext()).unregisterOnStatusChange(response);
                } else {
                    MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };


    /**
     * パラメータ比較クラス.
     */
    private static class ParamComparator implements Comparator<Bundle> {

        /** 比較するパラメータ名. */
        private final String mParamName;
        /** 昇順であることを示すフラグ. */
        private boolean mIsAsc;

        /**
         * コンストラクタ.
         *
         * @param paramName パラメータ名
         */
        ParamComparator(final String paramName) {
            mParamName = paramName;
        }

        /**
         * ソート順を設定する.
         * @param isAsc 昇順である場合は<code>true</code>、そうでなければ<code>false</code>
         */
        public void setOrder(final boolean isAsc) {
            mIsAsc = isAsc;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public int compare(final Bundle a, final Bundle b) {
            Object va = a.get(mParamName);
            Object vb = b.get(mParamName);
            if (!mIsAsc) {
                Object vt = va;
                va = vb;
                vb = vt;
            }
            if (va == null && vb == null) {
                return 0;
            }
            if (va != null && vb == null) {
                return -1;
            }
            if (va == null && vb != null) {
                return 1;
            }
            if (va.getClass() != vb.getClass()) {
                return 0;
            }
            if (va instanceof Comparable && !(vb instanceof Comparable)) {
                return -1;
            }
            if (!(va instanceof Comparable) && vb instanceof Comparable) {
                return 1;
            }
            if (!(va instanceof Comparable) && !(vb instanceof Comparable)) {
                return 0;
            }
            if (va instanceof Comparable && !(vb instanceof Comparable)) {
                return -1;
            }
            if (!(va instanceof Comparable) && vb instanceof Comparable) {
                return 1;
            }
            return ((Comparable) va).compareTo(vb);
        }
    }
}
