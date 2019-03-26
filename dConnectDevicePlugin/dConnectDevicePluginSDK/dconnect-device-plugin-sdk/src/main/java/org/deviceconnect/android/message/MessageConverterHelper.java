/*
 MessageConverterHelper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.content.Intent;

import org.deviceconnect.android.compat.AuthorizationRequestConverter;
import org.deviceconnect.android.compat.LowerCaseConverter;
import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.compat.ServiceDiscoveryRequestConverter;

/**
 * リクエストのパスを変換するヘルパークラス.
 *
 * @author NTT DOCOMO, INC.
 */
final class MessageConverterHelper {
    /**
     * リクエストを変換するコンバータクラス.
     */
    private static final MessageConverter[] REQUEST_CONVERTERS = {
            new ServiceDiscoveryRequestConverter(),
            new AuthorizationRequestConverter(),
            new LowerCaseConverter()
    };

    /**
     * コンストラクタ.
     * <p>
     * インスタンスは作成させないのでprivate.
     * </p>
     */
    private MessageConverterHelper() {}

    /**
     * リクエストのプロファイル名などを変換する.
     *
     * @param request 変換処理を行うリクエスト
     */
    static void convert(final Intent request) {
        for (MessageConverter converter : REQUEST_CONVERTERS) {
            converter.convert(request);
        }
    }
}
