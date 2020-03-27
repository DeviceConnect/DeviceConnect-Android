package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

/**
 * フレームのタイプを定義します.
 */
enum FrameType {
    /**
     * 音声のフレームタイプを定義.
     */
    AUDIO,

    /**
     * 映像のフレームタイプを定義.
     */
    VIDEO,

    /**
     * 音声と映像の混合のフレームタイプを定義.
     */
    MIXED
}