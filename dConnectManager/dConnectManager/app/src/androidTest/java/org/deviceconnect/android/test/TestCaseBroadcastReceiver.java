/*
 TestCaseBroadcastReceiver.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Device Connect Managerからのレスポンスを待つレシーバー.
 * @author NTT DOCOMO, INC.
 */
public class TestCaseBroadcastReceiver extends BroadcastReceiver {
    /**
     * テスト結果を受け取るレシーバー.
     * @param context コンテキスト
     * @param intent レスポンス
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        String testAction = null;
        if (IntentDConnectMessage.ACTION_RESPONSE.equals(action)) {
            testAction = DConnectTestCase.TEST_ACTION_RESPONSE;
        } else if (IntentDConnectMessage.ACTION_EVENT.equals(action)) {
            testAction = DConnectTestCase.TEST_ACTION_EVENT;
        } else if (IntentDConnectMessage.ACTION_MANAGER_LAUNCHED.equals(action)) {
            testAction = DConnectTestCase.TEST_ACTION_MANAGER_LAUNCHED;
        }
        if (testAction != null) {
            Intent targetIntent = new Intent(testAction);
            Bundle extra = intent.getExtras();
            if (extra != null) {
                targetIntent.putExtras(extra);
            }
            context.sendBroadcast(targetIntent);
        }
    }
}
