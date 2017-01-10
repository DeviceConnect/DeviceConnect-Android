/*
 DConnectEventMessage.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Device Connect Managerのイベントメッセージ.
 * @author NTT DOCOMO, INC.
 */
public class DConnectEventMessage extends BasicDConnectMessage {
    /**
     * 空のイベントメッセージを生成する.
     */
    public DConnectEventMessage() {
        super();
    }

    /**
     * Device Connect イベントメッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONへの変換に失敗した場合に発生.
     */
    public DConnectEventMessage(final String json) throws JSONException {
        super(json);
    }

    /**
     * Device Connect イベントメッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONへの変換に失敗した場合に発生.
     */
    public DConnectEventMessage(final JSONObject json) throws JSONException {
        super(json);
    }

    /**
     * Device Connect イベントメッセージをIntentから生成する.
     *
     * @param intent メッセージIntent
     * @throws JSONException JSONへの変換に失敗した場合に発生.
     */
    public DConnectEventMessage(final Intent intent) throws JSONException {
        super(intent);
    }

}
