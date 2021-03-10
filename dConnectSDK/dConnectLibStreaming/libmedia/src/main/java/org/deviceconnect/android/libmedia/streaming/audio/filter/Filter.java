package org.deviceconnect.android.libmedia.streaming.audio.filter;

import java.nio.ByteBuffer;

public interface Filter {
    /**
     * フィルタ処理を行います.
     *
     * @param input 音声入力データ
     * @param len 音声入力データサイズ
     */
    void onProcessing(ByteBuffer input, int len);

    /**
     * 音声入力の終了処理を行います.
     */
    void onRelease();
}
