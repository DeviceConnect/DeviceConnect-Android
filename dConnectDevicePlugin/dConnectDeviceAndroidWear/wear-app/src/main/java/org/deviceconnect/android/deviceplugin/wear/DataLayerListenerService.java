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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
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
public class DataLayerListenerService extends WearableListenerService implements SensorEventListener {
    /** radian. */
    private static final double RAD2DEG = 180 / Math.PI;

    /** SensorManager. */
    private SensorManager mSensorManager;

    /** Gyro x. */
    private float mGyroX;

    /** Gyro y. */
    private float mGyroY;

    /** Gyro z. */
    private float mGyroZ;

    /** Device NodeID . */
    private final List<String> mIds = Collections.synchronizedList(new ArrayList<String>());

    /** GyroSensor. */
    private Sensor mGyroSensor;

    /** AcceleratorSensor. */
    private Sensor mAccelerometer;

    /** The start time for measuring the interval. */
    private long mStartTime;

    /** Broadcast receiver. */
    MyBroadcastReceiver mReceiver = null;

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
        getApplicationContext().registerReceiver(mReceiver, intentFilter);
    }


    @Override
    public void onDestroy() {
        mIds.clear();
        unregisterSensor();
        super.onDestroy();
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED
                    && event.getDataItem().getUri().getPath().equals(WearConst.PATH_CANVAS)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap map = dataMapItem.getDataMap();

                Asset profileAsset = map.getAsset(WearConst.PARAM_BITMAP);
                int x = map.getInt(WearConst.PARAM_X);
                int y = map.getInt(WearConst.PARAM_Y);
                int mode = map.getInt(WearConst.PARAM_MODE);

                Intent intent = new Intent();
                intent.setClass(this, CanvasActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(WearConst.PARAM_BITMAP, profileAsset);
                intent.putExtra(WearConst.PARAM_X, x);
                intent.putExtra(WearConst.PARAM_Y, y);
                intent.putExtra(WearConst.PARAM_MODE, mode);
                startActivity(intent);
            }
        }

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mGyroSensor);
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
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
        } else if (action.equals(WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER)) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
            if (mSensorManager == null) {
                registerSensor();
            }

            // For service destruction suppression.
            Intent i = new Intent(WearConst.ACTION_WEAR_PING_SERVICE);
            startService(i);
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
                unregisterSensor();
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

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long time = System.currentTimeMillis();
            long interval = time - mStartTime;
            mStartTime = time;

            float accelX = sensorEvent.values[0];
            float accelY = sensorEvent.values[1];
            float accelZ = sensorEvent.values[2];
            final String data = accelX + "," + accelY + "," + accelZ
                    + "," + mGyroX + "," + mGyroY + "," + mGyroZ + "," + interval;
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (mIds) {
                        for (String id : mIds) {
                            sendSensorEvent(data, id);
                        }
                    }
                }
            });
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyroX = (float) (sensorEvent.values[0] * RAD2DEG);
            mGyroY = (float) (sensorEvent.values[1] * RAD2DEG);
            mGyroZ = (float) (sensorEvent.values[2] * RAD2DEG);
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }

    /**
     * センサーイベントをスマホ側に送信する.
     * @param data 送信するデータ
     * @param id 送信先のID
     */
    private void sendSensorEvent(final String data, final String id) {
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
        
        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(client, id,
                WearConst.WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA, data.getBytes()).await();
        if (!result.getStatus().isSuccess()) {
            if (BuildConfig.DEBUG) {
                Log.e("WEAR", "Failed to send a sensor event.");
            }
        }
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
     * センサーを登録する.
     */
    private synchronized void registerSensor() {
        GoogleApiClient client = getClient();
        if (client == null || !client.isConnected()) {
            client = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
            client.connect();
            ConnectionResult connectionResult = client.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                if (BuildConfig.DEBUG) {
                    Log.e("WEAR", "Failed to connect google play service.");
                }
                return;
            }
        }
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> accelSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (accelSensors.size() > 0) {
            mAccelerometer = accelSensors.get(0);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        List<Sensor> gyroSensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (gyroSensors.size() > 0) {
            mGyroSensor = gyroSensors.get(0);
            mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        mStartTime = System.currentTimeMillis();
    }
    
    /**
     * センサーを解除する.
     */
    private synchronized void unregisterSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mGyroSensor);
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
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
            final String profile;

            if (action.equals(WearConst.PARAM_DC_WEAR_KEYEVENT_ACT_TO_SVC)) {
                data = i.getStringExtra(WearConst.PARAM_KEYEVENT_DATA);
                profile = WearConst.WEAR_TO_DEVICE_KEYEVENT_DATA;
            } else if (action.equals(WearConst.PARAM_DC_WEAR_TOUCH_ACT_TO_SVC)) {
                data = i.getStringExtra(WearConst.PARAM_TOUCH_DATA);
                profile = WearConst.WEAR_TO_DEVICE_TOUCH_DATA;
            } else {
                return;
            }

            // Send message data.
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (mIds) {
                        for (String id : mIds) {
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

                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(client, id,
                                    profile, data.getBytes()).await();
                            if (!result.getStatus().isSuccess()) {
                                if (BuildConfig.DEBUG) {
                                    Log.e("WEAR", "Failed to send a sensor event.");
                                }
                            }
                        }
                    }
                }
            });
        }
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
