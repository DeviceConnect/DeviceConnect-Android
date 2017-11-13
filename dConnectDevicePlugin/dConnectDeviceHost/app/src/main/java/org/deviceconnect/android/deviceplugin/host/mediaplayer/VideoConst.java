/*
 VideoConst.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.mediaplayer;

/**
 * 映像録画Broadcastで使用する定数を定義.
 * 
 * [映像録画開始リクエストBroadcast]
 * ・ホストデバイスプラグインのHostMediaStreamingRecordingProfileから送信される。
 * ・action: SEND_HOSTDP_TO_VIDEO
 * ・putExtra(EXTRA_NAME, EXTRA_NAME_VIDEO_RECORD_START);
 *
 * [映像録画停止リクエストBroadcast]
 * ・ホストデバイスプラグインのHostMediaStreamingRecordingProfileへレスポンスを返す。
 * ・action: SEND_CAMERA_TO_VIDEO
 * ・putExtra(EXTRA_NAME, EXTRA_NAME_VIDEO_RECORD_STOP);
 *
 * [映像録画一時停止リクエストBroadcast]
 * ・ホストデバイスプラグインのHostMediaStreamingRecordingProfileへレスポンスを返す。
 * ・action: SEND_CAMERA_TO_VIDEO
 * ・putExtra(EXTRA_NAME, EXTRA_NAME_VIDEO_RECORD_PAUSE);
 *
 * @author NTT DOCOMO, INC.
 */
public final class VideoConst {
    /**
     * Constructor.
     */
    private VideoConst() {

    }

    /** Video起動のAction名. */
    public static final String SEND_HOSTDP_TO_VIDEO = "org.deviceconnect.android.intent.action.SEND_HOSTDP_TO_VIDEO";

    /** Video起動のAction名. */
    public static final String SEND_VIDEO_TO_HOSTDP = "org.deviceconnect.android.intent.action.SEND_VIDEO_TO_HOSTDP";

    /** Video操作のコマンド名. */
    public static final String EXTRA_NAME = "command";

    /** 録画開始. */
    public static final String EXTRA_VALUE_VIDEO_RECORD_START = "start";

    /** 録画停止. */
    public static final String EXTRA_VALUE_VIDEO_RECORD_STOP = "stop";

    /** 現在の録画状態. */
    public static final String EXTRA_VIDEO_RECORDER_STATE = "state";

    /** 使用するレコーダーのID. */
    public static final String EXTRA_RECORDER_ID = "recorderId";
    /** ServiceのID. */
    public static final String EXTRA_SERVICE_ID = "serviceId";

    /** Camera ID. */
    public static final String EXTRA_CAMERA_ID = "cameraId";

    /** Picture size. */
    public static final String EXTRA_PICTURE_SIZE = "pictureSize";

    /** ファイル名. */
    public static final String EXTRA_FILE_NAME = "filename";

    /** フレームレート. */
    public static final String EXTRA_FRAME_RATE = "frameRate";

    /** コールバック */
    public static final String EXTRA_CALLBACK = "callback";

    /** コールバックのエラーメッセージ。 */
    public static final String EXTRA_CALLBACK_ERROR_MESSAGE = "callback_error_message";

    /** Video起動のAction名. */
    public static final String SEND_HOSTDP_TO_VIDEOPLAYER =
            "org.deviceconnect.android.intent.action.SEND_HOSTDP_TO_VIDEOPLAYER";

    /** Video起動のAction名. */
    public static final String SEND_VIDEOPLAYER_TO_HOSTDP =
            "org.deviceconnect.android.intent.action.SEND_VIDEOPLAYER_TO_HOSTDP";

    /** 再生開始. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_PLAY = "play";

    /** 再生停止. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_STOP = "stop";

    /** 再生一時停止. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_PAUSE = "pause";

    /** 再生再開. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_RESUME = "resume";

    /** 再生場所指定. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_SEEK = "seek";

    /** 現在再生場所取得. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_GET_POS = "getpos";

    /** 現在再生場所. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_PLAY_POS = "playpos";

    /** Play completion. */
    public static final String EXTRA_VALUE_VIDEO_PLAYER_PLAY_COMPLETION = "complation";

    /** 撮影するサイズ(横). */
    public static final int VIDEO_WIDTH = 320;

    /** 撮影するサイズ(縦). */
    public static final int VIDEO_HEIGHT = 240;

    /** Format Type. */
    public static final String FORMAT_TYPE = ".3gp";

}
