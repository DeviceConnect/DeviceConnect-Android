/*
 DConnectApi.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.api;

import android.content.Intent;

import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.profile.spec.models.Operation;

/**
 * Device Connect APIクラス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectApi {
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
     * API 仕様の情報は、Swagger 2.0 の形式で Operation に格納されています。
     * どのようなパラメータが入っているかは、プロファイル定義ファイルを確認して、実装してください。
     * </p>
     *
     * <p>
     * 返り値に false を返却した場合は、API がサポートされていないと判断して、プロファイル定義から削除され
     * Service Information の情報からも削除されます。
     * また、次回からは呼び出されなくなります。
     * </p>
     *
     * @param spec API 仕様が格納された Operation
     * @return ServiceInformationに情報を加える場合にはtrue、それ以外はServiceInformationから情報を削除
     */
    public boolean onStoreSpec(final Operation spec) {
        return true;
    }
}
