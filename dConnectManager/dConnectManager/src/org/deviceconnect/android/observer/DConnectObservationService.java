/*
 DConnectObservationService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer;

import java.util.ArrayList;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.manager.DConnectSettings;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.observer.activity.WarningDialogActivity;
import org.deviceconnect.android.observer.receiver.ObserverReceiver;
import org.deviceconnect.android.observer.util.AndroidSocket;
import org.deviceconnect.android.observer.util.SockStatUtil;
import org.deviceconnect.android.observer.util.SocketState;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

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
     * 結果返却用コールバックオブジェクト
     */
    public static final String PARAM_RESULT_RECEIVER = "org.deviceconnect.android.intent.param.observer.RESULT_RECEIVER";

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        stopObservation();
                        String appName = getResources().getString(R.string.app_name);
                        String title = getResources().getString(R.string.service_observation_warning);
                        new AlertDialog.Builder(DConnectObservationService.this).setTitle(appName + ": " + title)
                                .setMessage(R.string.service_observation_msg_no_permission)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                        stopSelf();
                        return;
                    }

                    String appPackage = getHoldingAppSocketInfo();
                    if (appPackage != null) {
                        stopObservation();
                        Intent i = new Intent();
                        i.setClass(getApplicationContext(), WarningDialogActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION
                                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        i.putExtra(PARAM_PACKAGE_NAME, appPackage);
                        i.putExtra(PARAM_PORT, mPort);
                        getApplication().startActivity(i);
                        stopSelf();
                    }
                }
            }).start();
        } else if (ACTION_START.equals(action)) {
            final ResultReceiver resultReceiver = intent.getParcelableExtra(PARAM_RESULT_RECEIVER);
            if (resultReceiver == null) {
                return START_STICKY;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                startObservation();
                resultReceiver.send(Activity.RESULT_OK, null);
            } else {
                PermissionUtility.requestPermissions(this, new Handler(Looper.getMainLooper()),
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                startObservation();
                                resultReceiver.send(Activity.RESULT_OK, null);
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                resultReceiver.send(Activity.RESULT_CANCELED, null);
                            }
                        });
            }
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
        PendingIntent sender = PendingIntent.getBroadcast(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + mInterval, mInterval, sender);
    }

    /**
     * 監視を終了する.
     */
    private synchronized void stopObservation() {
        Intent intent = new Intent(this, ObserverReceiver.class);
        intent.setAction(ACTION_CHECK);
        PendingIntent sender = PendingIntent.getBroadcast(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    /**
     * ポートを占有しているアプリを取得する.
     * 
     * @return 占有しているアプリのパッケージ名
     */
    private String getHoldingAppSocketInfo() {
        ArrayList<AndroidSocket> sockets = SockStatUtil.getSocketList(this);
        String deviceConnectPackageName = getPackageName();
        for (AndroidSocket aSocket : sockets) {
            if (!aSocket.getAppName().equals(deviceConnectPackageName) && aSocket.getLocalPort() == mPort
                    && aSocket.getState() == SocketState.TCP_LISTEN) {
                return aSocket.getAppName();
            }
        }
        return null;
    }
}
