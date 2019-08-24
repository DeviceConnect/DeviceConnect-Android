/*
 Response.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * API 操作の期待する応答用.
 *
 * @author NTT DOCOMO, INC.
 */
public class Response extends AbstractSpec {
    /**
     * API 操作の期待する応答用の詳細.
     */
    private String mDescription;

    /**
     * API 操作の期待する応答の構造体.
     */
    private Schema mSchema;

    /**
     * API 操作の期待する応答に付与するヘッダーのリスト.
     */
    private Map<String, Header> mHeaders;

    /**
     * API 操作の期待する応答例.
     */
    private Map<String, Example> mExamples;

    /**
     * API 操作の期待する応答用の詳細を取得します.
     *
     * @return API 操作の期待する応答用の詳細
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * API 操作の期待する応答用の詳細を設定します.
     *
     * @param description API 操作の期待する応答用の詳細
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * API 操作の期待する応答の構造体を取得します.
     *
     * @return API 操作の期待する応答の構造体
     */
    public Schema getSchema() {
        return mSchema;
    }

    /**
     * API 操作の期待する応答の構造体を設定します.
     *
     * @param schema API 操作の期待する応答の構造体
     */
    public void setSchema(Schema schema) {
        mSchema = schema;
    }

    /**
     * API 操作の期待する応答に付与するヘッダーのリストを取得します.
     *
     * @return API 操作の期待する応答に付与するヘッダーのリスト
     */
    public Map<String, Header> getHeaders() {
        return mHeaders;
    }

    /**
     * API 操作の期待する応答に付与するヘッダーのリストを取得します.
     *
     * @param headers API 操作の期待する応答に付与するヘッダー
     */
    public void setHeaders(Map<String, Header> headers) {
        mHeaders = headers;
    }

    /**
     * API 操作の期待する応答に付与するヘッダーをリストに追加します.
     *
     * @param key キー
     * @param header ヘッダー
     */
    public void addHeader(String key, Header header) {
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mHeaders.put(key, header);
    }

    /**
     * キーに対応するヘッダーを削除します.
     *
     * <p>
     * 指定されたキーに対応するヘッダーが存在しない場合には null を返却します。
     * </p>
     *
     * @param key キー
     * @return 削除したヘッダー
     */
    public Header removeHeader(String key) {
        if (mHeaders != null) {
            return mHeaders.remove(key);
        }
        return null;
    }

    /**
     * API 操作の期待する応答例を取得します.
     *
     * @return API 操作の期待する応答例
     */
    public Map<String, Example> getExamples() {
        return mExamples;
    }

    /**
     * API 操作の期待する応答例を設定します.
     *
     * @param examples API 操作の期待する応答例
     */
    public void setExamples(Map<String, Example> examples) {
        mExamples = examples;
    }

    /**
     * キーに対応する応答例を追加します.
     *
     * @param key キー (MIME Type)
     * @param example 応答例
     */
    public void addExample(String key, Example example) {
        if (mExamples == null) {
            mExamples = new HashMap<>();
        }
        mExamples.put(key, example);
    }

    /**
     * API 操作の期待する応答例を削除します.
     * <p>
     * 指定されたキー(MIME Type) に対応する応答例がない場合には null を返却します
     *
     * </p>
     * @param key キー (MIME Type)
     * @return 削除された応答例
     */
    public Example removeExample(String key) {
        if (mExamples != null) {
            return mExamples.remove(key);
        }
        return null;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mDescription != null) {
            bundle.putString("description", mDescription);
        }

        if (mSchema != null) {
            bundle.putParcelable("schema", mSchema.toBundle());
        }

        if (mHeaders != null && !mHeaders.isEmpty()) {
            Bundle headers = new Bundle();
            for (Map.Entry<String, Header> entry : mHeaders.entrySet()) {
                headers.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("headers", headers);
        }

        if (mExamples != null && !mExamples.isEmpty()) {
            Bundle examples = new Bundle();
            for (Map.Entry<String, Example> entry : mExamples.entrySet()) {
                examples.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
            bundle.putParcelable("examples", examples);
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
