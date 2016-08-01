/*
 DConnectProfileSpecJsonParser.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.parser;


import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSONからDConnectProfileSpecを生成するJSONパーサ.
 *
 * @author NTT DOCOMO, INC.
 */
public interface DConnectProfileSpecJsonParser {

    /**
     * 指定されたJSONオブジェクトを解析し、DConnectProfileSpecを生成する.
     *
     * @param json API仕様定義ファイルのJSON
     * @return DConnectProfileSpecのインスタンス
     * @throws JSONException JSONの解析に失敗した場合
     */
    DConnectProfileSpec parseJson(JSONObject json) throws JSONException;

}
