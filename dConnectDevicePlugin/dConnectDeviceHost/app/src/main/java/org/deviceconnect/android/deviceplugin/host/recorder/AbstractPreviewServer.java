package org.deviceconnect.android.deviceplugin.host.recorder;

import android.content.Context;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

/**
 * プレビュー配信サーバ.
 */
public abstract class AbstractPreviewServer extends AbstractLiveStreaming implements PreviewServer {
    protected static final boolean DEBUG = BuildConfig.DEBUG;
    protected static final String TAG = "host.dplugin";

    /**
     * コンストラクタ.
     *
     * <p>
     * デフォルトでは、ミュート設定は true に設定しています。
     * </p>
     *
     * @param recorder プレビューで表示するレコーダ
     * @param id 名前
     */
    public AbstractPreviewServer(HostMediaRecorder recorder, String id) {
        super(recorder, id);
    }
}
