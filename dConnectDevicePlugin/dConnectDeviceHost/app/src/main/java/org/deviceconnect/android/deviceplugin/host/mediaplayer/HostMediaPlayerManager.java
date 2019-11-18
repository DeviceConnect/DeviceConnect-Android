/*
 HostMediaPlayerManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.mediaplayer;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaPlayerProfile;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.deviceconnect.android.profile.DConnectProfile.setResult;

public class HostMediaPlayerManager {
    /**
     * Mediaのステータス.
     */
    private int mMediaStatus = 0;

    /**
     * Mediaが未設定.
     */
    private static final int MEDIA_PLAYER_NO_DATA = 0;

    /**
     * Mediaがセット.
     */
    private static final int MEDIA_PLAYER_SET = 1;

    /**
     * Mediaが再生中.
     */
    private static final int MEDIA_PLAYER_PLAY = 2;

    /**
     * Mediaが一時停止中.
     */
    private static final int MEDIA_PLAYER_PAUSE = 3;

    /**
     * Mediaが停止.
     */
    private static final int MEDIA_PLAYER_STOP = 4;

    /**
     * Mediaが再生完了.
     */
    private static final int MEDIA_PLAYER_COMPLETE = 5;

    /**
     * MEDIAタイプ(動画).
     */
    private static final int MEDIA_TYPE_VIDEO = 1;

    /**
     * MEDIAタイプ(音楽).
     */
    private static final int MEDIA_TYPE_MUSIC = 2;

    // /** MEDIAタイプ(音声). */
    // private static final int MEDIA_TYPE_AUDIO = 3;

    /** ミリ秒 - 秒オーダー変換用. */
    private static final int UNIT_SEC = 1000;

    /** Video Current Position response. */
    private Intent mResponse = null;

    /** Intent filter for MediaPlayer (Video). */
    private IntentFilter mIfMediaPlayerVideo;

    /**
     * サポートしているaudioのタイプ一覧.
     */
    private static final List<String> AUDIO_TYPE_LIST = Arrays.asList("audio/mpeg", "audio/x-wav", "application/ogg",
            "audio/x-ms-wma", "audio/mp3", "audio/ogg", "audio/mp4");

    /**
     * サポートしているvideoのタイプ一覧.
     */
    private static final List<String> VIDEO_TYPE_LIST = Arrays.asList("video/3gpp", "video/mp4", "video/m4v",
            "video/3gpp2", "video/mpeg");

    /**
     * Media Status.
     */
    private int mSetMediaType = 0;

    /**
     * onStatusChange Eventの状態.
     */
    private boolean mOnStatusChangeEventFlag = false;

    /**
     * 現在再生中のファイルパス.
     */
    private String mMyCurrentFilePath = "";

    /**
     * 現在再生中のファイルパス.
     */
    private String mMyCurrentFileMIMEType = "";

    /**
     * 現在再生中のPosition.
     */
    private int mMyCurrentMediaPosition = 0;

    /**
     * MediaId.
     */
    private String mMyCurrentMediaId;

    /**
     * Media duration.
     */
    private int mMyCurrentMediaDuration = 0;

    /**
     * MediaPlayerのインスタンス.
     */
    private MediaPlayer mMediaPlayer = null;

    /**
     * コンテキスト.
     */
    private final DevicePluginContext mHostDevicePluginContext;

    /** Notification Id */
    private final int NOTIFICATION_ID = 3539;

    /** Notification Content */
    private final String NOTIFICATION_CONTENT = "Host Media Player Profileからの起動要求";

    /** Intent Action */
    public static final String INTENT_ACTION_ACTIVITY_START = "org.deviceconnect.android.deviceplugin.host.mediaplayer.ACTIVTY_START";

    public HostMediaPlayerManager(final DevicePluginContext pluginContext) {
        mHostDevicePluginContext = pluginContext;

        // MediaPlayer (Video) IntentFilter.
        mIfMediaPlayerVideo = new IntentFilter();
        mIfMediaPlayerVideo.addAction(VideoConst.SEND_VIDEOPLAYER_TO_HOSTDP);
        mIfMediaPlayerVideo.addAction(INTENT_ACTION_ACTIVITY_START);
    }

    private Context getContext() {
        return mHostDevicePluginContext.getContext();
    }

    private void sendResponse(final Intent intent) {
        mHostDevicePluginContext.sendResponse(intent);
    }

    private void sendEvent(final Intent intent, final String accessToken) {
        mHostDevicePluginContext.sendEvent(intent, accessToken);
    }

    private ContentResolver getContentResolver() {
        return mHostDevicePluginContext.getContext().getContentResolver();
    }

    public void forceStop() {
        mOnStatusChangeEventFlag = false;
        if (mMediaPlayer != null) {
            stopMedia(null);
        }
    }

    /**
     * 再生するメディアをセットする(Idから).
     *
     * @param response レスポンス
     * @param mediaId MediaID
     */
    public void putMediaId(final Intent response, final String mediaId) {
        // Check status.
        if (mMediaStatus == MEDIA_PLAYER_PLAY || mMediaStatus == MEDIA_PLAYER_PAUSE) {
            MessageUtils.setIllegalDeviceStateError(response, "Status is playing.");
            sendResponse(response);
            return;
        }

        // Backup MediaId.
        mMyCurrentMediaId = mediaId;

        // Videoとしてパスを取得
        Uri mUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));

        String filePath = getPathFromUri(mUri);

        // nullなら、Audioとしてパスを取得
        if (filePath == null) {
            mUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(mediaId));
            filePath = getPathFromUri(mUri);
        }

        // ファイル存在チェック
        if (filePath == null) {
            MessageUtils.setInvalidRequestParameterError(response, "The specified mediaId does not exist.");
            sendResponse(response);
            return;
        }

        String mMineType = getMIMEType(filePath);

        // パス指定の場合
        if (AUDIO_TYPE_LIST.contains(mMineType)) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mMediaPlayer = new MediaPlayer();

            try {
                mSetMediaType = MEDIA_TYPE_MUSIC;
                mMyCurrentFilePath = filePath;
                mMyCurrentFileMIMEType = mMineType;
                mMediaStatus = MEDIA_PLAYER_SET;
                mMediaPlayer.setDataSource(filePath);
                mMediaPlayer.setOnCompletionListener((mp) -> {
                    mMediaStatus = MEDIA_PLAYER_COMPLETE;
                    sendOnStatusChangeEvent("complete");
                });
                mMediaPlayer.prepareAsync();
                mMyCurrentMediaPosition = 0;
                mMediaPlayer.setOnPreparedListener((mp) -> {
                    mMyCurrentMediaDuration = mMediaPlayer.getDuration() / UNIT_SEC;
                });

                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    sendOnStatusChangeEvent("media");
                    sendResponse(response);
                }
            } catch (IOException e) {
                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.EXTRA_ERROR_CODE);
                    response.putExtra(DConnectMessage.EXTRA_ERROR_MESSAGE, "can't not mount:" + filePath);
                    sendResponse(response);
                }
            }
        } else if (VIDEO_TYPE_LIST.contains(mMineType)) {
            try {
                mSetMediaType = MEDIA_TYPE_VIDEO;
                mMyCurrentFilePath = filePath;
                mMyCurrentFileMIMEType = mMineType;

                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                mMediaPlayer = new MediaPlayer();

                FileInputStream fis = new FileInputStream(mMyCurrentFilePath);
                FileDescriptor mFd = fis.getFD();

                mMediaPlayer.setDataSource(mFd);
                mMediaPlayer.prepare();
                mMyCurrentMediaDuration = mMediaPlayer.getDuration() / UNIT_SEC;
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
                fis.close();

                if (response != null) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                    sendOnStatusChangeEvent("media");
                    sendResponse(response);
                }
            } catch (IllegalArgumentException | IllegalStateException | IOException e) {
                if (response != null) {
                    MessageUtils.setIllegalServerStateError(response, "can't mount:" + filePath);
                    sendResponse(response);
                }
            }
        } else {
            if (response != null) {
                MessageUtils.setIllegalServerStateError(response, "can't mount:" + filePath);
                sendResponse(response);
            }
        }
    }

    /**
     * onStatusChange Eventの登録.
     *
     * @param response レスポンス
     * @param serviceId サービスID
     */
    public void registerOnStatusChange(final Intent response, final String serviceId) {
        mOnStatusChangeEventFlag = true;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        sendResponse(response);
    }

    /**
     * onStatusChange Eventの解除.
     *
     * @param response レスポンス
     */
    public void unregisterOnStatusChange(final Intent response) {
        mOnStatusChangeEventFlag = false;
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        sendResponse(response);
    }

    /**
     * 状態変化のイベントを通知.
     *
     * @param status ステータス
     */
    public void sendOnStatusChangeEvent(final String status) {
        if (mOnStatusChangeEventFlag) {
            List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                    MediaPlayerProfile.PROFILE_NAME, null, MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);

            AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

            double maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            double mVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
            double mVolumeValue = mVolume / maxVolume;

            for (int i = 0; i < events.size(); i++) {

                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);

                MediaPlayerProfile.setAttribute(intent, MediaPlayerProfile.ATTRIBUTE_ON_STATUS_CHANGE);
                Bundle mediaPlayer = new Bundle();
                MediaPlayerProfile.setStatus(mediaPlayer, status);
                MediaPlayerProfile.setMediaId(mediaPlayer, mMyCurrentMediaId);
                MediaPlayerProfile.setMIMEType(mediaPlayer, mMyCurrentFileMIMEType);
                MediaPlayerProfile.setPos(mediaPlayer, mMyCurrentMediaPosition / UNIT_SEC);
                MediaPlayerProfile.setVolume(mediaPlayer, mVolumeValue);
                MediaPlayerProfile.setMediaPlayer(intent, mediaPlayer);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    /**
     * Mediaの再再生.
     *
     * @param response レスポンス
     * @return SessionID
     */
    public int resumeMedia(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                mMediaPlayer.start();
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("play");
            if (response != null) {
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
            return mMediaPlayer.getAudioSessionId();
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PLAY;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_RESUME);
            getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("play");
            if (response != null) {
                setResult(response, DConnectMessage.RESULT_OK);
            }
        } else if (mSetMediaType == 0) {
            if (response != null) {
                MessageUtils.setIllegalDeviceStateError(response, "Media is not set.");
            }
        } else {
            if (response != null) {
                MessageUtils.setUnknownError(response, "Unsupported media type is set.");
            }
        }

        if (response != null) {
            sendResponse(response);
        }
        return 0;
    }

    /**
     * メディアの再生.
     *
     * @param response レスポンス
     * @return セッションID
     */
    public int playMedia(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            if (response != null) {
                setResult(response, DConnectMessage.RESULT_OK);
            }
            try {
                if (mMediaStatus == MEDIA_PLAYER_STOP) {
                    mMediaPlayer.prepare();
                }
                if (mMediaStatus == MEDIA_PLAYER_STOP || mMediaStatus == MEDIA_PLAYER_PAUSE
                        || mMediaStatus == MEDIA_PLAYER_PLAY) {
                    mMediaPlayer.seekTo(0);
                    mMyCurrentMediaPosition = 0;
                    if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                        if (response != null) {
                            sendResponse(response);
                        }
                        return mMediaPlayer.getAudioSessionId();
                    }
                }
                mMediaPlayer.start();
                mMediaStatus = MEDIA_PLAYER_PLAY;
            } catch (IOException | IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            sendOnStatusChangeEvent("play");
            if (response != null) {
                sendResponse(response);
            }
            return mMediaPlayer.getAudioSessionId();
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mHostDevicePluginContext.getContext().registerReceiver(mMediaPlayerVideoBR, mIfMediaPlayerVideo);
            String className = getClassnameOfTopActivity();

            if (VideoPlayer.class.getName().equals(className)) {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY);
                getContext().sendBroadcast(mIntent);
                sendOnStatusChangeEvent("play");

            } else {
                mMediaStatus = MEDIA_PLAYER_PLAY;
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.setClass(getContext(), VideoPlayer.class);
                Uri data = Uri.parse(mMyCurrentFilePath);
                mIntent.setDataAndType(data, mMyCurrentFileMIMEType);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    mHostDevicePluginContext.getContext().startActivity(mIntent);
                    sendOnStatusChangeEvent("play");
                } else {
                    NotificationUtils.createNotificationChannel(getContext());
                    NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, mIntent, NOTIFICATION_CONTENT);
                }
            }

            if (response != null) {
                setResult(response, DConnectMessage.RESULT_OK);
            }
        } else if (mSetMediaType == 0) {
            if (response != null) {
                MessageUtils.setIllegalDeviceStateError(response, "Media is not set.");
            }
        } else {
            if (response != null) {
                MessageUtils.setUnknownError(response, "Unsupported media type is set.");
            }
        }

        if (response != null) {
            sendResponse(response);
        }
        return 0;
    }

    /**
     * メディアの一時停止.
     *
     * @param response レスポンス
     * @return セッションID
     */
    public int pauseMedia(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            if (mMediaStatus != MEDIA_PLAYER_STOP && mMediaStatus != MEDIA_PLAYER_SET) {
                try {
                    mMediaStatus = MEDIA_PLAYER_PAUSE;
                    mMediaPlayer.pause();
                } catch (IllegalStateException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                sendOnStatusChangeEvent("pause");
                if (response != null) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }
                return mMediaPlayer.getAudioSessionId();
            } else {
                if (response != null) {
                    setResult(response, DConnectMessage.RESULT_OK);
                }
            }
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PAUSE);
            getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("pause");
            if (response != null) {
                setResult(response, DConnectMessage.RESULT_OK);
            }
        } else if (mSetMediaType == 0) {
            if (response != null) {
                MessageUtils.setIllegalDeviceStateError(response, "Media is not set.");
            }
        } else {
            if (response != null) {
                MessageUtils.setUnknownError(response, "Unsupported media type is set.");
            }
        }

        if (response != null) {
            sendResponse(response);
        }
        return 0;
    }

    /**
     * ポジションを返す.
     *
     * @return 現在のポジション
     */
    public int getMediaPos() {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            return mMediaPlayer.getCurrentPosition() / UNIT_SEC;
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            String className = getClassnameOfTopActivity();
            if (VideoPlayer.class.getName().equals(className)) {
                Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
                mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_GET_POS);
                getContext().sendBroadcast(mIntent);
                return Integer.MAX_VALUE;
            } else {
                return -10;
            }
        } else if (mSetMediaType == 0) {
            return -1;
        } else {
            return -10;
        }
    }

    /**
     * Video ポジションを返す為のIntentを設定.
     *
     * @param response 応答用Intent.
     */
    public void setVideoMediaPosRes(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mResponse = response;
        }
    }

    /**
     * VideoPlayer用Broadcast Receiver.
     */
    private BroadcastReceiver mMediaPlayerVideoBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(VideoConst.SEND_VIDEOPLAYER_TO_HOSTDP)) {
                String mVideoAction = intent.getStringExtra(VideoConst.EXTRA_NAME);

                switch (mVideoAction) {
                    case VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY_POS:
                        mMyCurrentMediaPosition = intent.getIntExtra("pos", 0);
                        mResponse.putExtra("pos", mMyCurrentMediaPosition / UNIT_SEC);
                        mResponse.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                        sendResponse(mResponse);
                        break;
                    case VideoConst.EXTRA_VALUE_VIDEO_PLAYER_STOP:
                        getContext().unregisterReceiver(mMediaPlayerVideoBR);
                        break;
                    case VideoConst.EXTRA_VALUE_VIDEO_PLAYER_PLAY_COMPLETION:
                        mMediaStatus = MEDIA_PLAYER_COMPLETE;
                        sendOnStatusChangeEvent("complete");
                        getContext().unregisterReceiver(mMediaPlayerVideoBR);
                        break;
                }
            } else if (intent.getAction().equals(INTENT_ACTION_ACTIVITY_START)) {
                sendOnStatusChangeEvent("play");
            }
        }
    };

    /**
     * ポジションを変える.
     *
     * @param response レスポンス
     * @param pos ポジション
     */
    public void setMediaPos(final Intent response, final int pos) {
        if (mSetMediaType == 0) {
            MessageUtils.setIllegalDeviceStateError(response, "Media is not set.");
            sendResponse(response);
            return;
        }

        if (mSetMediaType != MEDIA_TYPE_MUSIC && mSetMediaType != MEDIA_TYPE_VIDEO) {
            MessageUtils.setUnknownError(response, "Unsupported media type is set.");
            sendResponse(response);
            return;
        }

        if (pos > mMyCurrentMediaDuration) {
            MessageUtils.setInvalidRequestParameterError(response);
            sendResponse(response);
            return;
        }

        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            mMediaPlayer.seekTo(pos * UNIT_SEC);
            mMyCurrentMediaPosition = pos * UNIT_SEC;
        } else {
            mMediaStatus = MEDIA_PLAYER_PAUSE;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_SEEK);
            mIntent.putExtra("pos", pos * UNIT_SEC);
            getContext().sendBroadcast(mIntent);
            mMyCurrentMediaPosition = pos * UNIT_SEC;
        }
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        sendResponse(response);
    }

    /**
     * メディアの停止.
     *
     * @param response レスポンス
     */
    public void stopMedia(final Intent response) {
        if (mSetMediaType == MEDIA_TYPE_MUSIC) {
            try {
                mMediaPlayer.stop();
                mMediaStatus = MEDIA_PLAYER_STOP;
                sendOnStatusChangeEvent("stop");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mMediaPlayer.reset();
                    putMediaId(null, mMyCurrentMediaId);
                }
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            }
        } else if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            mMediaStatus = MEDIA_PLAYER_STOP;
            Intent mIntent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEOPLAYER);
            mIntent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_PLAYER_STOP);
            getContext().sendBroadcast(mIntent);
            sendOnStatusChangeEvent("stop");
            if (response != null) {
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            }
        } else if (mSetMediaType == 0) {
            if (response != null) {
                MessageUtils.setIllegalDeviceStateError(response, "Media is not set.");
            }
        } else {
            if (response != null) {
                MessageUtils.setUnknownError(response, "Unsupported media type is set.");
            }
        }

        if (response != null) {
            sendResponse(response);
        }
    }

    /**
     * Play Status.
     *
     * @param response レスポンス
     */
    public void getPlayStatus(final Intent response) {
        String mClassName = getClassnameOfTopActivity();

        // VideoRecorderの場合は、画面から消えている場合
        if (mSetMediaType == MEDIA_TYPE_VIDEO) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);

            if (!VideoPlayer.class.getName().equals(mClassName)) {
                mMediaStatus = MEDIA_PLAYER_STOP;
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            } else {
                if (mMediaStatus == MEDIA_PLAYER_STOP) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
                } else if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "play");
                } else if (mMediaStatus == MEDIA_PLAYER_PAUSE) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "pause");
                } else if (mMediaStatus == MEDIA_PLAYER_NO_DATA) {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "no data");
                } else {
                    response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
                }
            }
            sendResponse(response);
        } else {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            if (mMediaStatus == MEDIA_PLAYER_STOP) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            } else if (mMediaStatus == MEDIA_PLAYER_PLAY) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "play");
            } else if (mMediaStatus == MEDIA_PLAYER_PAUSE) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "pause");
            } else if (mMediaStatus == MEDIA_PLAYER_NO_DATA) {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "no data");
            } else {
                response.putExtra(MediaPlayerProfile.PARAM_STATUS, "stop");
            }
            sendResponse(response);
        }
    }

    /**
     * URIからパスを取得.
     *
     * @param mUri URI
     * @return パス
     */
    private String getPathFromUri(final Uri mUri) {
        Cursor c = getContentResolver().query(mUri, null, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            }
            return null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * ファイルからMIME Typeを取得.
     *
     * @param path パス
     * @return MineType
     */
    private String getMIMEType(final String path) {
        String mFilename = new File(path).getName();
        int dotPos = mFilename.lastIndexOf(".");
        String mFormat = mFilename.substring(dotPos, mFilename.length());
        String mExt = MimeTypeMap.getFileExtensionFromUrl(mFormat);
        mExt = mExt.toLowerCase(Locale.getDefault());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExt);
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     *
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager mActivityManager = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        return mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
    }
}
