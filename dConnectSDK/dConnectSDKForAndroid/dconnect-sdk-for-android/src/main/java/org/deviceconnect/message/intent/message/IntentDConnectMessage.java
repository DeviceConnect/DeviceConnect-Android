/*
 IntentDConnectMessage.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message.intent.message;

import org.deviceconnect.message.DConnectMessage;

/**
 * メッセージ.
 * @author NTT DOCOMO, INC.
 */
public interface IntentDConnectMessage extends DConnectMessage {

    /**
     * アクション:GET.
     */
    String ACTION_GET = "org.deviceconnect.action.GET";

    /**
     * アクション:PUT.
     */
    String ACTION_PUT = "org.deviceconnect.action.PUT";

    /**
     * アクション:POST.
     */
    String ACTION_POST = "org.deviceconnect.action.POST";

    /**
     * アクション:DELETE.
     */
    String ACTION_DELETE = "org.deviceconnect.action.DELETE";

    /**
     * アクション:RESPONSE.
     */
    String ACTION_RESPONSE = "org.deviceconnect.action.RESPONSE";

    /**
     * アクション:EVENT.
     */
    String ACTION_EVENT = "org.deviceconnect.action.EVENT";

    /**
     * アクション:MANAGER_LAUNCHED.
     */
    String ACTION_MANAGER_LAUNCHED = "org.deviceconnect.action.MANAGER_LAUNCHED";

    /**
     * アクション:EVENT_TRANSMIT_DISCONNECT.
     */
    String ACTION_EVENT_TRANSMIT_DISCONNECT = "org.deviceconnect.action.EVENT_TRANSMIT_DISCONNECT";

    /**
     * アクション:MANAGER_TERMINATED.
     */
    String ACTION_MANAGER_TERMINATED = "org.deviceconnect.action.MANAGER_TERMINATED";

    /**
     * アクション:DEVICEPLUGIN_RESET.
     */
    String ACTION_DEVICEPLUGIN_RESET = "org.deviceconnect.action.DEVICEPLUGIN_RESET";

    /**
     * アクション:DEVICEPLUGIN_ENABLED.
     */
    String ACTION_DEVICEPLUGIN_ENABLED = "org.deviceconnect.action.DEVICEPLUGIN_ENABLED";

    /**
     * アクション:DEVICEPLUGIN_DISABLED.
     */
    String ACTION_DEVICEPLUGIN_DISABLED = "org.deviceconnect.action.DEVICEPLUGIN_DISABLED";

    /**
     * アクション:KEEPALIVE.
     */
    String ACTION_KEEPALIVE = "org.deviceconnect.action.KEEPALIVE";

    /**
     * パラメータ: {@value} .
     */
    String EXTRA_ORIGIN = "origin";

    /**
     * パラメータ: {@value} .
     */
    String EXTRA_KEY = "key";

    /**
     * パラメータ: {@value} .
     */
    String EXTRA_NONCE = "nonce";

    /**
     * パラメータ: {@value} .
     */
    String EXTRA_HMAC = "hmac";

    /**
     * パラメータ: {@value} .
     */
    String EXTRA_KEEPALIVE_STATUS = "keepalive_status";

    /**
     * アクションを取得する.
     * @return アクション
     */
    String getAction();

}
