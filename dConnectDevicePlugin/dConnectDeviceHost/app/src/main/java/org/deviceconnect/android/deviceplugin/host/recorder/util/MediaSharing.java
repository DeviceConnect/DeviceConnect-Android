package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import org.deviceconnect.android.provider.FileManager;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * メディア共有ロジック.
 */
public abstract class MediaSharing {
    /**
     * 指定された静止画ファイルを端末内の他アプリと共有する.
     *
     * @param context コンテキスト
     * @param photoFile 静止画ファイル
     * @return 発行されたURI
     */
    public abstract Uri sharePhoto(final @NonNull Context context,
                                   final @NonNull File photoFile);

    /**
     * 指定された動画ファイルを端末内の他アプリと共有する.
     *
     * @param context コンテキスト
     * @param videoFile 動画ファイル
     * @param fileManager ファイルマネージャ
     * @return 発行されたURI
     */
    public abstract Uri shareVideo(final @NonNull Context context,
                                   final @NonNull File videoFile,
                                   final @NonNull FileManager fileManager);

    /**
     * 指定された音声ファイルを端末内の他アプリと共有する.
     *
     * @param context コンテキスト
     * @param audioFile 音声ファイル
     * @return 発行されたURI
     */
    public abstract Uri shareAudio(final @NonNull Context context,
                                   final @NonNull File audioFile);

    /**
     * 動作環境に合わせたメディア共有ロジックを取得する.
     * @return メディア共有ロジックのインスタンス
     */
    public static MediaSharing getInstance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new MediaSharingForApi29();
        } else {
            return new MediaSharingForLegacy();
        }
    }

    boolean checkMediaFile(final @NonNull File file) {
        return file.exists() && file.length() > 0;
    }
}
