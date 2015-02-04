/*
 DConnectObservationService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer;

import java.util.ArrayList;

import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.observer.activity.WarningDialogActivity;
import org.deviceconnect.android.observer.receiver.ObserverReceiver;
import org.deviceconnect.android.observer.util.AndroidSocket;
import org.deviceconnect.android.observer.util.SockStatUtil;
import org.deviceconnect.android.observer.util.SocketState;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

/**
 * DeviceConnectの生存確認を行うサービス.
 * 
 * @author NTT DOCOMO, INC.
 */
public class DConnectObservationService extends Service {

    /**
     * オブザーバー監視開始アクション.
     */
    public static final String ACTION_START = "org.deviceconnect.android.intent.action.observer.START";
    
    /**
     * オブザーバー監視停止アクション.
     */
    public static final String ACTION_STOP = "org.deviceconnect.android.intent.action.observer.STOP";

    /**
     * 監視アクション.
     */
    public static final String ACTION_CHECK = "org.deviceconnect.android.intent.action.observer.CHECK";

    /**
     * 占有しているパッケージ名.
     */
    public static final String PARAM_PACKAGE_NAME = "org.deviceconnect.android.intent.param.observer.PACKAGE_NAME";

    /**
     * 占有されているポート番号.
     */
    public static final String PARAM_PORT = "org.deviceconnect.android.intent.param.observer.PORT";

    /**
     * リクエストコード.
     */
    private static final int REQUEST_CODE = 0x0F0F0F;

    /**
     * Device Connect のポート番号.
     */
    private int mPort;

    /**
     * チェックの間隔.
     */
    private int mInterval;

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stopObservation();
        DConnectSettings settings = DConnectSettings.getInstance();
        settings.load(DConnectObservationService.this);
        mPort = settings.getPort();
        mInterval = settings.getObservationInterval();
        // onDestroyが呼ばれずに死ぬこともあるようなので必ず最初に解除処理を入れる。
        stopObservation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopObservation();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_CHECK.equals(action)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String appPackage = getHoldingAppSocketInfo();
                    if (appPackage != null) {
                        stopObservation();
                        Intent i = new Intent();
                        i.setClass(getApplicationContext(), WarningDialogActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                                | Intent.FLAG_ACTIVITY_NO_ANIMATION
                                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        i.putExtra(PARAM_PACKAGE_NAME, appPackage);
                        i.putExtra(PARAM_PORT, mPort);
                        getApplication().startActivity(i);
                        stopSelf();
                    }
                }
            }).start();
        } else if (ACTION_START.equals(action)) {
            startObservation();
        } else if (ACTION_STOP.equals(action)) {
            stopObservation();
            stopSelf();
        }

        return START_STICKY;
    }

    /**
     * 監視を開始する.
     */
    private synchronized void startObservation() {
        Intent intent = new Intent(this, ObserverReceiver.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent sender = PendingIntent
                .getBroadcast(this, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + mInterval, mInterval, sender);
    }

    /**
     * 監視を終了する.
     */
    private synchronized void stopObservation() {
        Intent intent = new Intent(this, ObserverReceiver.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent sender = PendingIntent
                .getBroadcast(this, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
    /**
     * ポートを占有しているアプリを取得する.
     * @return 占有しているアプリのパッケージ名
     */
    private String getHoldingAppSocketInfo() {
        ArrayList<AndroidSocket> sockets = SockStatUtil.getSocketList(this);
        String deviceConnectPackageName = getPackageName();
        for (AndroidSocket aSocket:sockets) {
           if (!aSocket.getAppName().equals(deviceConnectPackageName)
                && aSocket.getLocalPort() == mPort
                && aSocket.getState() == SocketState.TCP_LISTEN) {
               return aSocket.getAppName();
           }
        }
        return null;
    }
}
