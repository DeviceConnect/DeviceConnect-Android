/*
 DConnectApi.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.api;


import android.content.Intent;

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
     * RESPONSEメソッドハンドラー.<br>
     * リクエストパラメータに応じてデバイスのサービスを提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     *
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスパラメータを送信するか否か
     */
    public abstract boolean onRequest(final Intent request, final Intent response);

}
