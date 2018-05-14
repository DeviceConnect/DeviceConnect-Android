/*
DataLayerListenerService.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.deviceconnect.android.deviceplugin.wear.activity.CanvasActivity;
import org.deviceconnect.android.deviceplugin.wear.activity.WearKeyEventProfileActivity;
import org.deviceconnect.android.deviceplugin.wear.activity.WearTouchProfileActivity;

import java.util.List;

/**
 * DataLayerListenerService.
 *
 * @author NTT DOCOMO, INC.
 */
public class DataLayerListenerService extends WearableListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
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

                String selfId = ((WearApplication) getApplication()).getSelfId();
                String wearId = segments.get(2);
                if (selfId == null || !selfId.equals(wearId)) {
                    //WearのIDが違っていれば無視
                    return;
                }
                String nodeId = uri.getHost();
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
        intent.setClass(this, WearAppService.class);
        intent.putExtra(WearConst.PARAM_SENSOR_ID, id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopSensorService(String id) {
        Intent intent = new Intent();
        intent.setAction(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER);
        intent.setClass(this, WearAppService.class);
        intent.putExtra(WearConst.PARAM_SENSOR_ID, id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
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
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_CANCAS_DELETE_IMAGE)) {
            deleteCanvas();
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER)) {
            startSensorService(id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER)) {
            stopSensorService(id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER)) {
            startKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER)) {
            startKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONDOWN_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER)) {
            startKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER)) {
            startKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONUP_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_REGISTER)) {
            startKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_UNREGISTER)) {
            startKeyEventActivity(WearConst.DEVICE_TO_WEAR_KEYEVENT_ONKEYCHANGE_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_REGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCHANGE_REGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCH_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHSTART_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHEND_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONDOUBLETAP_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHMOVE_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER)) {
            startTouchActivity(WearConst.DEVICE_TO_WEAR_TOUCH_ONTOUCHCANCEL_UNREGISTER, id);
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_SET_ID)) {
            String wearId = new String(messageEvent.getData());
            ((WearApplication) getApplication()).setSelfId(wearId);
        } else {
            if (BuildConfig.DEBUG) {
                Log.e("Wear", "unknown event:" + action);
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
        
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(mPatternLong, -1);
    }

    /**
     * バイブレーションを停止する.
     */
    private void stopVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // 停止のパターンの時にバイブレーションを止めようとした時にcancelが効かないため、
        // バイブレーションが停止している時は、一度バイブレーションを鳴らしたのちに停止を行う。
        vibrator.vibrate(new long[]{100}, -1);
        vibrator.cancel();
    }

    /**
     * Canvasの画面を削除する.
     */
    private void deleteCanvas() {
        Intent intent = new Intent();
        intent.setClass(this, CanvasActivity.class);
        intent.setAction(WearConst.ACTION_DELETE_CANVAS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Execute Key Event Activity.
     *
     * @param regist Register string.
     */
    private void startKeyEventActivity(final String regist, String id) {
        Intent i = new Intent(this, WearKeyEventProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(WearConst.PARAM_KEYEVENT_REGIST, regist);
        i.putExtra(WearConst.PARAM_KEYEVENT_ID, id);
        startActivity(i);
    }

    /**
     * Execute Touch Activity.
     *
     * @param regist Register string.
     */
    private void startTouchActivity(final String regist, String id) {
        Intent i = new Intent(this, WearTouchProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(WearConst.PARAM_TOUCH_REGIST, regist);
        i.putExtra(WearConst.PARAM_TOUCH_ID, id);
        startActivity(i);
    }
}
