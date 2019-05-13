/*
 Responses.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * API で利用されるレスポンスを保持するオブジェクト.
 *
 * @author NTT DOCOMO, INC.
 */
public class Responses extends AbstractSpec {

    /**
     * API で利用されるレスポンスを保持するオブジェクトのリスト.
     */
    private Map<String, Response> mResponses;

    /**
     * API で利用されるレスポンスを保持するオブジェクトを取得します.
     *
     * @return API で利用されるレスポンスを保持するオブジェクト.
     */
    public Map<String, Response> getResponses() {
        return mResponses;
    }

    /**
     * API で利用されるレスポンスを保持するオブジェクトを設定します.
     *
     * @param responses API で利用されるレスポンスを保持するオブジェクト
     */
    public void setResponses(Map<String, Response> responses) {
        mResponses = responses;
    }

    /**
     * API で利用されるレスポンスを保持するオブジェクトを追加します.
     *
     * @param statusCode ステータスコードの文字列
     * @param response API で利用されるレスポンスを保持するオブジェクト
     */
    public void addResponse(String statusCode, Response response) {
        if (mResponses == null) {
            mResponses = new HashMap<>();
        }
        mResponses.put(statusCode, response);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mResponses != null && !mResponses.isEmpty()) {
            for (Map.Entry<String, Response> entry : mResponses.entrySet()) {
                bundle.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
