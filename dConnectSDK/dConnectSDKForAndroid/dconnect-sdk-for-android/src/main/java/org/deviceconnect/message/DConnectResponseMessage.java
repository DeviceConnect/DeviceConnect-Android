/*
 DConnectResponseMessage.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Device Connect Managerからのレスポンスメッセージ.
 * @author NTT DOCOMO, INC.
 */
public class DConnectResponseMessage extends BasicDConnectMessage {
    /**
     * 空のレスポンスメッセージを生成する.
     */
    public DConnectResponseMessage() {
        super();
    }

    /**
     * 結果コードを指定してメッセージを生成する.
     * @param result 結果コード
     */
    public DConnectResponseMessage(final int result) {
        super();
        setResult(result);
    }

    /**
     * エラーコードを指定してメッセージを生成する.
     * @param errorCode エラーコード
     */
    public DConnectResponseMessage(final ErrorCode errorCode) {
        setResult(RESULT_ERROR);
        setErrorCode(errorCode.getCode());
        setErrorMessage(errorCode.toString());
    }

    /**
     * Device Connect レスポンスメッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONへの変換に失敗した場合に発生.
     */
    public DConnectResponseMessage(final String json) throws JSONException {
        super(json);
    }

    /**
     * Device Connect レスポンスメッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONへの変換に失敗した場合に発生.
     */
    public DConnectResponseMessage(final JSONObject json) throws JSONException {
        super(json);
    }

    /**
     * Device Connect メッセージをIntentから生成する.
     *
     * @param intent メッセージIntent
     * @throws JSONException JSONへの変換に失敗した場合に発生.
     */
    public DConnectResponseMessage(final Intent intent) throws JSONException {
        super(intent);
    }

    /**
     * 指定されたメッセージをコピーしてレスポンスメッセージを生成する.
     *
     * @param message コピーするメッセージ
     */
    public DConnectResponseMessage(final DConnectMessage message) {
        super(message);
    }

    /**
     * 結果コードを取得する.
     * @return 結果コード
     */
    public int getResult() {
        return getInt(EXTRA_RESULT);
    }

    /**
     * 結果コードを設定する.
     * @param result 結果コード
     */
    public void setResult(final int result) {
        put(EXTRA_RESULT, result);
    }

    /**
     * エラーコードを取得する.
     * @return エラーコード
     */
    public int getErrorCode() {
        return getInt(EXTRA_ERROR_CODE);
    }

    /**
     * エラーコードを設定する.
     * @param error エラーコード
     */
    public void setErrorCode(final int error) {
        put(EXTRA_ERROR_CODE, error);
    }

    /**
     * エラーメッセージを取得する.
     * @return エラーメッセージ
     */
    public String getErrorMessage() {
        return getString(EXTRA_ERROR_MESSAGE);
    }

    /**
     * エラーメッセージを設定する.
     * @param message エラーメッセージ
     */
    public void setErrorMessage(final String message) {
        put(EXTRA_ERROR_MESSAGE, message);
    }

    /**
     * バージョン名を取得する.
     * @return バージョン名
     */
    public String getVersion() {
        return getString(EXTRA_VERSION);
    }

    /**
     * バージョン名を設定する.
     * @param version バージョン名
     */
    public void setVersion(final String version) {
        put(EXTRA_VERSION, version);
    }

    /**
     * プロダクト名を取得する.
     * @return プロダクト名
     */
    public String getProduct() {
        return getString(EXTRA_PRODUCT);
    }

    /**
     * プロダクト名を設定する.
     * @param product プロダクト名
     */
    public void setProduct(final String product) {
        put(EXTRA_PRODUCT, product);
    }
}
