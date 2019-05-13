package org.deviceconnect.android.compat;

import android.content.Intent;

/**
 * Device Connectメッセージの内容を変換するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public interface MessageConverter {
    /**
     * メッセージの変換処理を行います.
     *
     * @param message メッセージ
     */
    void convert(Intent message);
}
