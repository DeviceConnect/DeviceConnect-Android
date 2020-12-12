package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;

public class AudioPreviewServerProvider extends AbstractPreviewServerProvider {
    /**
     * プレビュー配信サーバ停止用 Notification の識別子を定義.
     */
    private static final int BASE_NOTIFICATION_ID = 2001;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param recorder レコーダ
     */
    public AudioPreviewServerProvider(Context context, HostMediaRecorder recorder) {
        super(context, recorder, BASE_NOTIFICATION_ID);
    }
}
