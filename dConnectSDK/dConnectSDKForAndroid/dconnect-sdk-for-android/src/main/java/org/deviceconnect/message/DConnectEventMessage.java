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
     * コンストラクタ.
     */
    public DConnectEventMessage() {
        super();
    }

    /**
     * メッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONエラー.
     */
    public DConnectEventMessage(final String json) throws JSONException {
        super(json);
    }

    /**
     * メッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONエラー.
     */
    public DConnectEventMessage(final JSONObject json) throws JSONException {
        super(json);
    }

    /**
     * メッセージをIntentから生成する.
     * @param intent メッセージIntent
     */
    public DConnectEventMessage(final Intent intent) throws JSONException {
        super(intent);
    }

}
