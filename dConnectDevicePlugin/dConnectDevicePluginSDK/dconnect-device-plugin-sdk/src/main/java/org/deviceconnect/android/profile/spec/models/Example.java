/*
 Example.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import org.deviceconnect.android.profile.spec.parser.OpenAPIParser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 応答メッセージの例.
 *
 * @author NTT DOCOMO, INC.
 */
public class Example implements DConnectSpec {
    /**
     * 応答メッセージの例.
     *
     * TODO 応答メッセージの例は、JSONオブジェクトのままで良いか？
     */
    private JSONObject mExample;

    /**
     * 応答メッセージの例を取得します.
     *
     * @return 応答メッセージの例
     */
    public JSONObject getExample() {
        return mExample;
    }

    /**
     * 応答メッセージの例を設定します.
     *
     * @param example 応答メッセージの例
     */
    public void setExample(JSONObject example) {
        mExample = example;
    }

    @Override
    public Bundle toBundle() {
        try {
            return OpenAPIParser.parseJSONObject(mExample);
        } catch (JSONException e) {
            return new Bundle();
        }
    }
}
