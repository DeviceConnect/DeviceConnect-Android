package org.deviceconnect.android.deviceplugin.uvc.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.mjpeg.UvcMjpgRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.libuvc.Parameter;
import org.deviceconnect.android.libuvc.UVCCamera;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.provider.FileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaRecorderManager {
    /**
     * レコーディング停止アクションを定義.
     */
    public static final String ACTION_STOP_RECORDING = "org.deviceconnect.android.deviceplugin.uvc.STOP_RECORDING";

    /**
     * プレビュー停止用アクションを定義.
     */
    public static final String ACTION_STOP_PREVIEW = "org.deviceconnect.android.deviceplugin.uvc.STOP_PREVIEW";

    /**
     * ブロードキャスト停止用アクションを定義.
     */
    public static final String ACTION_STOP_BROADCAST = "org.deviceconnect.android.deviceplugin.uvc.STOP_BROADCAST";

    /**
     * サービスのIDを格納するためのキーを定義.
     */
    public static final String KEY_SERVICE_ID = "service_id";

    /**
     * レコーダのIDを格納するためのキーを定義.
     */
    public static final String KEY_RECORDER_ID = "recorder_id";

    /**
     * プレビュー停止・レコーディング停止・ブロードキャスト停止・画面の回転イベントなどを受け取るための BroadcastReceiver.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
           if (ACTION_STOP_PREVIEW.equals(action)) {
                stopPreviewServer(intent.getStringExtra(KEY_RECORDER_ID));
            } else if (ACTION_STOP_RECORDING.equals(action)) {
                stopRecording(intent.getStringExtra(KEY_RECORDER_ID));
            } else if (ACTION_STOP_BROADCAST.equals(action)) {
                stopBroadcast(intent.getStringExtra(KEY_RECORDER_ID));
            }
        }
    };

    private final Context mContext;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public MediaRecorderManager(final Context context) {
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    /**
     * 指定された ID のレコーダのプレビューを停止します.
     *
     * @param id レコーダの ID
     */
    private void stopPreviewServer(final String id) {
        if (id == null) {
            return;
        }
    }

    /**
     * 指定された ID のレコードの録音・録画を停止します.
     *
     * @param id レコードの ID
     */
    private void stopRecording(final String id) {
        if (id == null) {
            return;
        }
    }

    /**
     * 指定された ID のレコードのブロードキャストを停止します.
     *
     * @param id レコードの ID
     */
    private void stopBroadcast(final String id) {
        if (id == null) {
            return;
        }
    }
}
