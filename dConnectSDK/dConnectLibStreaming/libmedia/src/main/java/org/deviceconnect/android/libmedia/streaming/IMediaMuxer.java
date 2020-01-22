package org.deviceconnect.android.libmedia.streaming;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;

/**
 * 送られてきた映像をコンテナに格納するための処理を行うインターフェース.
 */
public interface IMediaMuxer {
    /**
     * 映像のコンテナ作成準備要求が送られてきたときの処理を行います.
     *
     * @param videoQuality 映像をエンコードする設定
     * @param audioQuality 音声をエンコードする設定
     */
    boolean onPrepare(VideoQuality videoQuality, AudioQuality audioQuality);

    /**
     * 映像のフォーマットが変更要求が送られてきたときの処理を行います.
     * @param newFormat 新しいフォーマット
     */
    void onVideoFormatChanged(MediaFormat newFormat);

    /**
     * 映像データの書き込み要求が送られてきたときの処理を行います.
     *
     * @param encodedData データ
     * @param bufferInfo データ情報
     */
    void onWriteVideoData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo);

    /**
     * 音声のフォーマットが変更要求が送られてきたときの処理を行います.
     * @param newFormat 新しいフォーマット
     */
    void onAudioFormatChanged(MediaFormat newFormat);

    /**
     * 音声データの書き込み要求が送られてきたときの処理を行います.
     *
     * @param encodedData データ
     * @param bufferInfo データ情報
     */
    void onWriteAudioData(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo);

    /**
     * コンテナの破棄要求が送られてきたときの処理を行います.
     */
    void onReleased();
}
