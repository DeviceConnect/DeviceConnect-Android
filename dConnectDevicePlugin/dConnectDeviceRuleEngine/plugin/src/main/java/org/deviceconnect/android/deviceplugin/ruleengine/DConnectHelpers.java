/*
 DConnectHelper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine;

import android.content.Context;

import org.deviceconnect.message.DConnectEventMessage;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.message.DConnectSDKFactory;

/**
 * DConnectHelperクラス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectHelpers {
    /** シングルトンインスタンス. */
    public static final DConnectHelpers INSTANCE = new DConnectHelpers();
    /** SDK. */
    private DConnectSDK mSDK;
    /** コンテキスト. */
    private Context mContext;
    /** イベントハンドラー. */
    private EventHandler eventHandler = null;

    /** イベントハンドラー. */
    public interface EventHandler {
        /**
         * イベントが発生した時に呼ばれます.
         * @param event イベント
         */
        void onEvent(DConnectEventMessage event);
    }

    /** 処理完了コールバック. */
    public interface FinishCallback<Result> {
        /**
         * 処理が完了した時に呼ばれます.
         * @param result 結果
         * @param error エラー
         */
        void onFinish(Result result, Exception error);
    }


    /** DConnectHelperの基底Exception. */
    public abstract class DConnectHelperException extends Exception {
        public int errorCode = 0;
    }

    /** Resultが不正. */
    public class DConnectInvalidResultException extends DConnectHelperException {}

    /** 認証失敗. */
    public class DConnectAuthFailedException extends DConnectHelperException {}


    public void setContext(Context context) {
        mContext = context;

        if (context == null) {
            mSDK = null;
        } else {
            mSDK = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
            mSDK.setOrigin(context.getPackageName());

//            SettingData setting = SettingData.getInstance(context);
//            if (setting.accessToken != null) {
//                mSDK.setAccessToken(setting.accessToken);
//            }
        }

    }

    /**
     * イベントハンドラーを設定する.
     *
     * @param handler ハンドラー
     */
    public void setEventHandler(EventHandler handler) {
        this.eventHandler = handler;
    }

    /**
     * 接続先情報を設定する.
     *
     * @param ssl SSL通信を行う場合true
     * @param host ホスト名
     * @param port ポート番号
     */
    public void setHostInfo(boolean ssl, String host, int port) {
        mSDK.setSSL(ssl);
        mSDK.setHost(host);
        mSDK.setPort(port);
    }

    /**
     * availavility呼び出し.
     * @param callback コールバック関数.
     */
    public void availability(final FinishCallback<Void> callback) {
//        if (BuildConfig.DEBUG) Log.d(TAG, "availability");

        mSDK.availability(new DConnectSDK.OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                Exception e = null;
                if (response.getResult() == DConnectMessage.RESULT_ERROR) {
                    e = new Exception(response.getErrorMessage());
                }
                callback.onFinish(null, e);
            }
        });
    }

}
