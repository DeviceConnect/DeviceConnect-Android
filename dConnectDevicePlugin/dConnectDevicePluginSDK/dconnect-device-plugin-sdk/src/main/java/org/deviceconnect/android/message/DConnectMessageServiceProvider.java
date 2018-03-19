/*
 DConnectMessageServiceProvider.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.logging.Logger;

/**
 * Device Connectメッセージサービスプロバイダー.
 * 
 * <p>
 * Device Connectリクエストメッセージを受信し、Device Connectレスポンスメッセージを送信するサービスである。 本インスタンスで処理をするのではなく、
 * {@link #getServiceClass()} で返却した Service で応答処理を行う。
 * 
 * @param <T> サービスクラス
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectMessageServiceProvider<T extends Service> extends BroadcastReceiver {
    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    @Override
    public void onReceive(final Context context, final Intent intent) {
        /* KeepAlive応答処理 */
        if (intent.getAction().equals(IntentDConnectMessage.ACTION_KEEPALIVE)) {
            String status = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS);
            if (BuildConfig.DEBUG) {
                mLogger.info("ACTION_KEEPALIVE Receive. status: " + status);
            }
            if (status.equals("CHECK") || status.equals("START") || status.equals("STOP")) {
                Intent response = MessageUtils.createResponseIntent(intent);
                response.setAction(IntentDConnectMessage.ACTION_KEEPALIVE);
                response.putExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS, "RESPONSE");
                response.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID,
                        intent.getStringExtra(IntentDConnectMessage.EXTRA_SERVICE_ID));
                context.sendBroadcast(response);

                if (BuildConfig.DEBUG) {
                    mLogger.info("Send Broadcast.");
                }
            }
            return;
        }

        Intent service = new Intent(intent);
        service.setClass(context, getServiceClass());
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            context.startService(service);
        }
    }

    /**
     * サービスクラスを取得する.
     * 
     * @return サービスクラス
     */
    protected abstract Class<T> getServiceClass();

}
