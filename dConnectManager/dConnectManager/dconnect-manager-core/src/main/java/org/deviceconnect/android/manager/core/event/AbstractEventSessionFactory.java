/*
 AbstractEventSessionFactory.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.event;

import android.content.Intent;

/**
 * イベントセッション作成ファクトリ.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractEventSessionFactory {
    /**
     * 指定したリクエストなどからイベントセッションを作成します.
     *
     * @param request リクエスト
     * @param serviceId サービスID
     * @param receiverId レシーバーID
     * @param pluginId プラグインID
     * @return イベントセッション
     */
    public abstract EventSession createSession(final Intent request, final String serviceId,
                                               final String receiverId, final String pluginId);
}
