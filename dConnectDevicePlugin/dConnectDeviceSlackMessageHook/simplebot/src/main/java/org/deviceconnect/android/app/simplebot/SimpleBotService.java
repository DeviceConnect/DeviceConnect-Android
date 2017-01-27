/*
 SimpleBotService.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.x5.template.Chunk;

import org.deviceconnect.android.app.simplebot.data.DataManager;
import org.deviceconnect.android.app.simplebot.data.ResultData;
import org.deviceconnect.android.app.simplebot.data.SettingData;
import org.deviceconnect.android.app.simplebot.utils.DConnectHelper;
import org.deviceconnect.android.app.simplebot.utils.Utils;
import org.deviceconnect.message.DConnectEventMessage;
import org.deviceconnect.message.DConnectMessage;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SimpleBotサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class SimpleBotService extends Service {

    /**
     * サービス停止アクション
     */
    public static final String SERVICE_STOP_ACTION = "org.deviceconnect.android.app.simplebot.service_stop";

    /**
     * デバッグタグ
     */
    private static final String TAG = "SimpleBotService";

    /**
     * Notification ID.
     */
    private static final int ONGOING_NOTIFICATION_ID = 45632;

    /**
     * Device Connect Managerと接続を監視するスレッドクラス.
     */
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 監視スレッドをキャンセルするためのオブジェクト.
     */
    private ScheduledFuture mScheduledFuture;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "service started...");
        }

        connect();
        startMonitoringDeviceConnectManager();
        showNotification(getString(R.string.connecting_service));
        registerDozeStateReceiver();
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "service destroyed...");
        }

        unregisterDozeStateReceiver();
        hideNotification();
        stopMonitoringDeviceConnectManager();
        disconnect();

        notifyStopAction();

        super.onDestroy();
    }

    /**
     * サービス停止をBroadcastで通知を行う.
     */
    private void notifyStopAction() {
        // サービス停止を通知
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SERVICE_STOP_ACTION);
        getBaseContext().sendBroadcast(broadcastIntent);
    }

    /**
     * 通知バーにNotificationを表示して、サービスをフォアグランドに登録する.
     * @param content 通知に表示する文字列
     */
    private void showNotification(final String content) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    /**
     * 通知バーにNotificationを非表示する.
     */
    private void hideNotification() {
        stopForeground(true);
    }

    /**
     * Device Connect Managerの生存確認を開始する.
     */
    private void startMonitoringDeviceConnectManager() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startMonitoringDeviceConnectManager");
        }

        mScheduledFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Check WebSocket.");
                }

                if (!DConnectHelper.INSTANCE.isOpenWebSocket()) {
                    showNotification(getString(R.string.disconnected_service));
                    connect();
                }
            }
        }, 15, 15, TimeUnit.SECONDS);
    }

    /**
     * Device Connect Managerの生存確認を停止する.
     */
    private void stopMonitoringDeviceConnectManager() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopMonitoringDeviceConnectManager");
        }

        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(true);
            mScheduledFuture = null;
        }
    }

    /**
     * Device Connect Managerと接続を行う.
     */
    private void connect() {
        // アクティブじゃない場合は終了
        final Context context = getApplicationContext();
        final SettingData setting = SettingData.getInstance(context);
        if (!setting.active) {
            // サービス終了
            stopSelf();
            return;
        }

        // イベントハンドラー登録
        DConnectHelper.INSTANCE.setEventHandler(new DConnectHelper.EventHandler() {
            @Override
            public void onEvent(DConnectEventMessage event) {
                handleEvent(event);
            }
        });

        // Device Connect Managerの生存確認
        Utils.availability(context, new DConnectHelper.FinishCallback<Void>() {
            @Override
            public void onFinish(Void aVoid, Exception error) {
                if (error == null) {
                    // イベント登録
                    Utils.registerEvent(context, new DConnectHelper.FinishCallback<Void>() {
                        @Override
                        public void onFinish(Void aVoid, Exception error) {
                            if (error != null) {
                                Log.e(TAG, "Error on registerEvent", error);
                                setting.save();
                                showNotification(getString(R.string.disconnected_service));
                            } else {
                                showNotification(getString(R.string.connected_service));
                            }
                        }
                    });
                } else {
                    showNotification(getString(R.string.disconnected_service));
                }
            }
        });
    }

    /**
     * Device Connect Managerとの接続を切断する.
     */
    private void disconnect() {
        // イベントハンドラー登録
        DConnectHelper.INSTANCE.setEventHandler(null);
        // イベント解除
        Utils.unregisterEvent(getApplicationContext());
    }

    /**
     * messageHookからのイベントを受領し処理を行う.
     *
     * @param event イベント
     */
    private void handleEvent(final DConnectEventMessage event) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "handleEvent: " + event.toString(4));
        }

        if (!event.keySet().contains("message")) {
            return;
        }
        ResultData.Result result = new ResultData.Result();
        DConnectMessage message = event.getMessage("message");
        // Direct or Mentionのみ処理する
        if (!message.keySet().contains("messageType") ||
                !(message.getString("messageType").contains("direct") ||
                        message.getString("messageType").contains("mention"))) {
            return;
        }
        // textとchannelIdを取得
        if (message.keySet().contains("text")) {
            result.text = message.getString("text");
            result.channel = message.getString("channelId");
            result.from = message.getString("from");
        } else {
            return;
        }

        // 登録コマンドを取得
        DataManager dm = new DataManager(getApplicationContext());
        Cursor cursor = dm.getAll();
        if (cursor.moveToFirst()) {
            do {
                // 各コマンドを判定
                result.data = dm.convertData(cursor);
                if (handleData(result)) {
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 各コマンドを処理する.
     *
     * @param result 結果
     * @return 処理した場合はtrue
     */
    private boolean handleData(final ResultData.Result result) {
        final DataManager.Data data = result.data;
        if (data.serviceId == null) {
            return false;
        }
        final Context context = getApplicationContext();
        // 正規表現で検索
        Pattern pattern = Pattern.compile(data.keyword);
        Matcher matcher = pattern.matcher(result.text);
        if (!matcher.find()) {
            // 一致せず
            return false;
        }
        // 一致
        if (BuildConfig.DEBUG) Log.d(TAG, "match:" + data.keyword);
        // パラメータ作成
        final Map<String, Object> params = Utils.jsonToMap(data.body);
        if (params != null) {
            for (String key : params.keySet()) {
                String val = (String) params.get(key);
                // groupをパラメータに渡す
                for (int i = 0; i < matcher.groupCount() + 1; i++) {
                    val = val.replace("$" + i, matcher.group(i));
                    if (BuildConfig.DEBUG) Log.d(TAG, matcher.group(i));
                }
                params.put(key, val);
            }
            if (BuildConfig.DEBUG) Log.d(TAG, params.toString());
        }

        // Slackに受付メッセージ送信する.
        if ((data.accept != null && data.accept.length() > 0) || (data.acceptUri != null && data.acceptUri.length() > 0)) {
            Utils.sendMessage(context, result.channel, data.accept, data.acceptUri, new DConnectHelper.FinishCallback<Void>() {
                @Override
                public void onFinish(Void aVoid, Exception error) {
                    if (error != null) {
                        Log.e(TAG, "Error on sendMessage", error);
                    }
                }
            });
        }

        // リクエスト送信
        // コマンドに記載されたリクエストをDevice Connect Managerに送信する
        // 実行した結果を、メッセージとしてSlackに送信する。
        Utils.sendRequest(context, result.data.method, result.data.path, result.data.serviceId, params, new DConnectHelper.FinishCallback<Map<String, Object>>() {
            @Override
            public void onFinish(Map<String, Object> stringObjectMap, Exception error) {
                if (error == null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, stringObjectMap.toString());
                    sendResponse(result, result.data.success, result.data.successUri, stringObjectMap);
                } else {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Error on sendRequest", error);
                    sendResponse(result, result.data.error, result.data.errorUri, stringObjectMap);
                }
            }
        });

        return true;
    }

    /**
     * Slackに処理結果を送信する.
     *
     * @param result   結果
     * @param text     Text
     * @param uri      リソースURI
     * @param response Response
     */
    private void sendResponse(final ResultData.Result result, final String text, final String uri, final Map<String, Object> response) {
        // レスポンスがない
        if ((text == null || text.length() == 0) && (uri == null || uri.length() == 0)) {
            // 履歴に保存
            ResultData.INSTANCE.add(result);
            return;
        }
        // ChunkでTemplate処理
        try {
            Chunk chunk = new Chunk();
            chunk.append(text);
            chunk.putAll(response);
            result.response = chunk.toString();
            chunk = new Chunk();
            chunk.append(uri);
            chunk.putAll(response);
            result.responseUri = chunk.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error on Chunk command", e);
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, result.response);
            Log.d(TAG, result.responseUri);
        }

        // 履歴に保存
        ResultData.INSTANCE.add(result);

        // メッセージ送信
        Utils.sendMessage(this, result.channel, result.response, result.responseUri, new DConnectHelper.FinishCallback<Void>() {
            @Override
            public void onFinish(Void aVoid, Exception error) {
                if (error != null) {
                    Log.e(TAG, "Error on sendMessage", error);
                }
            }
        });
    }

    /**
     * Dozeモードの検知を行うReceiverの登録を行う.
     */
    private void registerDozeStateReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            IntentFilter intentFilter = new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
            registerReceiver(mDozeStateReceiver, intentFilter);
        }
    }

    /**
     * Dozeモードの検知を行うReceiverの解除を行う.
     */
    private void unregisterDozeStateReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            unregisterReceiver(mDozeStateReceiver);
        }
    }

    /**
     * Dozeモードの検知を行うReceiver.
     */
    private BroadcastReceiver mDozeStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PowerManager mgr = context.getSystemService(PowerManager.class);
                if (!mgr.isIgnoringBatteryOptimizations(context.getPackageName())) {
                    if (mgr.isDeviceIdleMode()) {
                        stopMonitoringDeviceConnectManager();
                        disconnect();
                        showNotification(getString(R.string.disconnected_service));
                    } else {
                        connect();
                        startMonitoringDeviceConnectManager();
                    }
                }
            }
        }
    };
}
