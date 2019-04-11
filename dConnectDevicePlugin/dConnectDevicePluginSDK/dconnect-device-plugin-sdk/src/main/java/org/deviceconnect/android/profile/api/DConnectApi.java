/*
 DConnectApi.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.api;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectSpecConstants;

/**
 * Device Connect APIクラス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectApi implements DConnectSpecConstants {

    /**
     * {@link DConnectApi}の子クラスで実装されるAPI仕様.
     */
    private DConnectApiSpec mApiSpec;

    /**
     * インターフェース名を取得する.
     * @return インターフェース名
     */
    public String getInterface() {
        return null;
    }

    /**
     * アトリビュート名を取得する.
     * @return アトリビュート名
     */
    public String getAttribute() {
        return null;
    }

    /**
     * メソッドを取得する.
     * @return メソッド
     */
    public abstract Method getMethod();

    /**
     * API仕様を取得する.
     * @return API仕様
     */
    public DConnectApiSpec getApiSpec() {
        return mApiSpec;
    }

    /**
     * API仕様を設定する.
     * @param apiSpec API仕様
     */
    public void setApiSpec(final DConnectApiSpec apiSpec) {
        mApiSpec = apiSpec;
    }

    /**
     * RESPONSEメソッドハンドラー.
     *
     * <p>
     * リクエストパラメータに応じてデバイスのサービスを提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * </p>
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスパラメータを送信するか否か
     */
    public abstract boolean onRequest(final Intent request, final Intent response);

    /**
     * Service Information で API 仕様情報を返却する場合に呼び出されます.
     *
     * <p>
     * デバイス毎に API 仕様の更新を行う場合には、ここで更新を行います。
     * </p>
     *
     * <p>
     * API 仕様の情報は、Swagger 2.0 の形式で Bundle に格納されています。
     * どのようなパラメータが入っているかは、プロファイル定義ファイルを確認して、実装してください。
     *
     * 引数の Bundle には、以下のように method の中身が key-value で格納されています。
     * <pre>
     * "get": {
     *     ----- ここから ------
     *     "operationId": "batteryGet",
     *     "x-type": "one-shot",
     *     "summary": "スマートデバイスのバッテリー情報を取得する。",
     *     "description": "スマートデバイスのバッテリー情報として取得できない値がある場合は適切な値を代入してレスポンスを返却する。",
     *     "parameters": [
     *         {
     *             "name": "serviceId",
     *             "description": "サービスID。取得対象スマートデバイス",
     *             "in": "query",
     *             "required": true,
     *             "type": "string"
     *         }
     *      ],
     *      "responses": {
     *         "200": {
     *            "description": "",
     *            "schema": {
     *                "$ref": "#/definitions/AllResponse"
     *            },
     *            "examples": {
     *                "application/json": {
     *                     "result": 0,
     *                     "product": "Example System",
     *                     "version": "1.0.0",
     *                     "charging": true,
     *                     "chargingTime": 10,
     *                     "dischargingTime": 0,
     *                     "level": 0.8
     *                 }
     *             }
     *         }
     *     }
     *     ----- ここまでが格納される ------
     * }
     * </pre>
     *
     * <pre>
     *       private final DConnectApi mBatteryLevelApi = new GetApi() {
     *         <code>@</code>Override
     *         public boolean onStoreSpec(final Bundle spec) {
     *             // OperationId を取得する場合
     *             String operationId = spec.getString("operationId");
     *
     *             // OperationId を上書きする場合
     *             spec.putString("operationId", "newBatteryGet");
     *             return true;
     *         }
     *
     *         <code>@</code>Override
     *         public boolean onRequest(final Intent request, final Intent response) {
     *
     *         }
     *     };
     * </pre>
     *
     * @param spec API 仕様が格納された Bundle
     * @return ServiceInformationに情報を加える場合にはtrue、それ以外はServiceInformationから情報を削除
     */
    public boolean onStoreSpec(final Bundle spec) {
        return true;
    }
}
