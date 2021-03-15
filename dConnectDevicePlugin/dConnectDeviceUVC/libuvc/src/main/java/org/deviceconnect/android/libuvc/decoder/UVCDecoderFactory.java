/*
 UVCDecoderFactory.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc.decoder;

import android.os.Build;

import org.deviceconnect.android.libuvc.FrameType;
import org.deviceconnect.android.libuvc.Parameter;

import java.io.IOException;

/**
 * UVCのデコーダを作成するファクトリークラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class UVCDecoderFactory {
    /**
     * コンストラクタ.
     *
     * ファクトリークラスなので、インスタンスは作成させない。
     */
    private UVCDecoderFactory() {}

    /**
     * デコーダを作成します.
     *
     * @param param デコーダのタイプ
     * @return UVCDecoderのインスタンス
     * @throws IOException デコーダの作成に失敗した場合に発生
     */
    public static UVCDecoder create(final Parameter param) throws IOException {

        // TODO サポートするフレームタイプを増やしたい場合にはここに追加すること。

        switch (param.getFrameType()) {
            case H264:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    return new UVCH264Decoder();
                } else {
                    throw new IOException("Not support frame type. type=" + param.getFrameType());
                }
            case MJPEG:
                if (param.isUseH264()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        return new UVCH264Decoder();
                    } else {
                        throw new IOException("Not support frame type. type=" + param.getFrameType());
                    }
                } else {
                    return new UVCMJPEGDecoder();
                }
            case UNCOMPRESSED:
                return new UVCUncompressedDecoder();
            default:
                throw new IOException("Not support frame type. type=" + param.getFrameType());
        }
    }
}
