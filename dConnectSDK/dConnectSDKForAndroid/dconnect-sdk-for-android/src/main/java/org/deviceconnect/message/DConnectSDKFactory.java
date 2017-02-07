/*
 DConnectSDKFactory.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Context;

/**
 * DConnectSDKの実装クラスを生成するファクトリークラス.
 * @author NTT DOCOMO, INC.
 */
public final class DConnectSDKFactory {

    /**
     * DConnectSDKのタイプ.
     */
    public enum Type {
        /**
         * HTTP通信を行うDConnectSDK実装タイプ.
         */
        HTTP,

        /**
         * Intent通信を行うDConnectSDK実装タイプ.
         */
        INTENT
    }

    private DConnectSDKFactory() {
    }

    /**
     * DConnectSDKの実装クラスを生成する.
     * <p>
     * この関数を呼び出すごとに新規にDConnectSDKを生成してします。<br>
     * アプリで使用する場合には、できるだけDConnectSDKのインスタンスを使いまわしてください。
     * </p>
     * <h3>サンプルコード</h3>
     * <pre>
     * DConnectSDK httpSDK = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
     * </pre>
     * @param context コンテキスト
     * @param type タイプ
     * @return DConnectSDKの実装クラスのインスタンス
     */
    public static DConnectSDK create(final Context context, final Type type) {
        if (context == null) {
            throw new NullPointerException("context is null.");
        }

        if (type == null) {
            throw new NullPointerException("type is null.");
        }

        DConnectSDK sdk;
        switch (type) {
            case HTTP:
                sdk = new HttpDConnectSDK();
                break;
            case INTENT:
                sdk =  new IntentDConnectSDK(context);
                break;
            default:
                throw new IllegalArgumentException("type is invalid.");
        }
        sdk.setOrigin(context.getPackageName());
        return sdk;
    }
}
