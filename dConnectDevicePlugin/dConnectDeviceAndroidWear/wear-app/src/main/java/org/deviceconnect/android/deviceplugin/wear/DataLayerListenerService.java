/*
DataLayerListenerService.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.deviceconnect.android.deviceplugin.wear.activity.CanvasActivity;
import org.deviceconnect.android.deviceplugin.wear.activity.WearKeyEventProfileActivity;
import org.deviceconnect.android.deviceplugin.wear.activity.WearTouchProfileActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DataLayerListenerService.
 *
 * @author NTT DOCOMO, INC.
 */
public class DataLayerListenerService extends WearableListenerService {
    /** Device NodeID . */
    private final List<String> mIds = Collections.synchronizedList(new ArrayList<String>());

    /** Broadcast receiver. */
    private MyBroadcastReceiver mReceiver = null;

    /**
     * スレッド管理用クラス.
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        // set BroadcastReceiver
        mReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(WearConst.PARAM_DC_WEAR_KEYEVENT_ACT_TO_SVC);
        intentFilter.addAction(WearConst.PARAM_DC_WEAR_TOUCH_ACT_TO_SVC);
        intentFilter.addAction(WearConst.PARAM_DC_WEAR_CANVAS_ACT_TO_SVC);
        intentFilter.addAction(WearConst.ACTION_WEAR_PING_SERVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }


    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
        mIds.clear();
        super.onDestroy();
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            if (event.getType() == DataEvent.TYPE_CHANGED
                    && uri.getPath().startsWith(WearConst.PATH_CANVAS)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap map = dataMapItem.getDataMap();

                List<String> segments = uri.getPathSegments();
                String nodeId = segments.get(2);
                String requestId = segments.get(3);
                Asset profileAsset = map.getAsset(WearConst.PARAM_BITMAP);
                int x = map.getInt(WearConst.PARAM_X);
                int y = map.getInt(WearConst.PARAM_Y);
                int mode = map.getInt(WearConst.PARAM_MODE);

                Intent intent = new Intent();
                intent.setClass(this, CanvasActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(WearConst.PARAM_SOURCE_ID, nodeId);
                intent.putExtra(WearConst.PARAM_REQUEST_ID, requestId);
                intent.putExtra(WearConst.PARAM_BITMAP, profileAsset);
                intent.putExtra(WearConst.PARAM_X, x);
                intent.putExtra(WearConst.PARAM_Y, y);
                intent.putExtra(WearConst.PARAM_MODE, mode);
                startActivity(intent);
            }
        }
    }

    private void startSensorService(String id) {
        Intent intent = new Intent();
        intent.setAction(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER);
        intent.setClass(this, DeviceOrientationSensorService.class);
        intent.putExtra("id", id);
        startService(intent);
    }

    private void stopSensorService(String id) {
        Intent intent = new Intent();
        intent.setAction(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER);
        intent.setClass(this, DeviceOrientationSensorService.class);
        intent.putExtra("id", id);
        startService(intent);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        // get id of wear device
        String id = messageEvent.getSourceNodeId();
        String action = messageEvent.getPath();
        if (action.equals(WearConst.DEVICE_TO_WEAR_VIBRATION_RUN)) {
            startVibration(messageEvent);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_VIBRATION_DEL)) {
            stopVibration();
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            startSensorService(id);
            // For service destruction suppression.
            Intent i = new Intent(WearConst.ACTION_WEAR_PING_SERVICE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER)) {
            mIds.remove(id);
            if (mIds.isEmpty()) {
                stopSensorService(id);
            }
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_CANCAS_DELETE_IMAGE)) {
            deleteCanvas();
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_KEYEVENT_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_KEYEVENT_REGIST, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_KEYEVENT_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_KEYEVENT_REGIST, WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            execTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_TOUCH_REGIST, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_TOUCH_REGIST, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_TOUCH_REGIST, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_TOUCH_REGIST, WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_TOUCH_REGIST, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER);
            sendBroadcast(i);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER)) {
            mIds.remove(id);
            // Broadcast to Activity.
            Intent i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
            i.putExtra(WearConst.PARAM_TOUCH_REGIST, WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER);
            sendBroadcast(i);
        } else {
            if (BuildConfig.DEBUG) {
                Log.e("Wear", "unknown event");
            }
        }
    }

    @Override
    public void onPeerConnected(final Node peer) {
    }

    @Override
    public void onPeerDisconnected(final Node peer) {
    }

    /**
     * バイブレーションを開始する.
     * @param messageEvent メッセージ
     */
    private void startVibration(final MessageEvent messageEvent) {
        // get vibration pattern
        String mPattern = new String(messageEvent.getData());
        
        // Make array of pattern
        String[] mPatternArray = mPattern.split(",", 0);
        long[] mPatternLong = new long[mPatternArray.length + 1];
        mPatternLong[0] = 0;
        for (int i = 1; i < mPatternLong.length; i++) {
            mPatternLong[i] = Integer.parseInt(mPatternArray[i - 1]);
        }
        
        // vibrate
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(mPatternLong, -1);
    }

    /**
     * バイブレーションを停止する.
     */
    private void stopVibration() {
        // stop vibrate
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.cancel();
    }

    /**
     * Canvasの画面を削除する.
     */
    private void deleteCanvas() {
        String className = getClassnameOfTopActivity();
        if (CanvasActivity.class.getName().equals(className)) {
            Intent intent = new Intent();
            intent.setClass(this, CanvasActivity.class);
            intent.setAction(WearConst.ACTION_DELETE_CANVAS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * Execute Key Event Activity.
     *
     * @param regist Register string.
     */
    private void execKeyEventActivity(final String regist) {
        // Start Activity.
        Intent i = new Intent(this, WearKeyEventProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(WearConst.PARAM_KEYEVENT_REGIST, regist);
        this.startActivity(i);

        // Send event regist to Activity.
        i = new Intent(WearConst.PARAM_DC_WEAR_KEYEVENT_SVC_TO_ACT);
        i.putExtra(WearConst.PARAM_KEYEVENT_REGIST, regist);
        sendBroadcast(i);
    }

    /**
     * Execute Touch Activity.
     *
     * @param regist Register string.
     */
    private void execTouchActivity(final String regist) {
        // Start Activity.
        Intent i = new Intent(this, WearTouchProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(WearConst.PARAM_TOUCH_REGIST, regist);
        this.startActivity(i);
        
        // Send event regist to Activity.
        i = new Intent(WearConst.PARAM_DC_WEAR_TOUCH_SVC_TO_ACT);
        i.putExtra(WearConst.PARAM_TOUCH_REGIST, regist);
        sendBroadcast(i);
    }

    /**
     * Broadcast Receiver.
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent i) {
            String action = i.getAction();
            final String data;
            final String path;

            if (action.equals(WearConst.PARAM_DC_WEAR_KEYEVENT_ACT_TO_SVC)) {
                data = i.getStringExtra(WearConst.PARAM_KEYEVENT_DATA);
                path = WearConst.WEAR_TO_DEVICE_KEYEVENT_DATA;
                sendEvent(path, data);
            } else if (action.equals(WearConst.PARAM_DC_WEAR_TOUCH_ACT_TO_SVC)) {
                data = i.getStringExtra(WearConst.PARAM_TOUCH_DATA);
                path = WearConst.WEAR_TO_DEVICE_TOUCH_DATA;
                sendEvent(path, data);
            } else if (action.equals(WearConst.PARAM_DC_WEAR_CANVAS_ACT_TO_SVC)) {
                String destId = i.getStringExtra(WearConst.PARAM_DESTINATION_ID);
                String requestId = i.getStringExtra(WearConst.PARAM_REQUEST_ID);
                String result = i.getStringExtra(WearConst.PARAM_RESULT);
                data = requestId + "," + result;
                path = WearConst.WEAR_TO_DEVICE_CANVAS_RESULT;
                sendMessage(destId, path, data);
            }
        }
    }

    private void sendEvent(final String path, final String data) {
        synchronized (mIds) {
            for (String id : mIds) {
                sendMessage(id, path, data);
            }
        }
    }

    private void sendMessage(final String destinationId, final String path, final String data) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                GoogleApiClient client = getClient();
                if (!client.isConnected()) {
                    ConnectionResult connectionResult = client.blockingConnect(30, TimeUnit.SECONDS);
                    if (!connectionResult.isSuccess()) {
                        if (BuildConfig.DEBUG) {
                            Log.e("WEAR", "Failed to connect google play service.");
                        }
                        return;
                    }
                }

                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(client, destinationId,
                    path, data.getBytes()).await();
                if (!result.getStatus().isSuccess()) {
                    if (BuildConfig.DEBUG) {
                        Log.e("WEAR", "Failed to send a sensor event.");
                    }
                }
            }
        });
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     *
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager manager = (ActivityManager) getSystemService(Service.ACTIVITY_SERVICE);
        return manager.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    /**
     * GoogleApiClientを取得する.
     * @return GoogleApiClient
     */
    private GoogleApiClient getClient() {
        return ((WearApplication) getApplication()).getGoogleApiClient();
    }
}
