package org.deviceconnect.android.deviceplugin.wear;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WearAppService extends Service implements SensorEventListener {
    /** radian. */
    private static final double RAD2DEG = 180 / Math.PI;

    /** Device NodeID . */
    private final List<String> mIds = Collections.synchronizedList(new ArrayList<String>());

    /** SensorManager. */
    private SensorManager mSensorManager;

    /** Gyro x. */
    private float mGyroX;

    /** Gyro y. */
    private float mGyroY;

    /** Gyro z. */
    private float mGyroZ;

    /** The start time for measuring the interval. */
    private long mStartTime;

    /** GyroSensor. */
    private Sensor mGyroSensor;

    /** AcceleratorSensor. */
    private Sensor mAccelerometer;

    /**
     * スレッド管理用クラス.
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String id = intent.getStringExtra(WearConst.PARAM_SENSOR_ID);
            if (WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_REGISTER.equals(action)) {
                if (!mIds.contains(id)) {
                    mIds.add(id);
                }
                registerSensor();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                    String channelId = getString(R.string.android_wear_data_layer_channel_id);
                    NotificationChannel channel = new NotificationChannel(
                            channelId,
                            getString(R.string.android_wear_data_layer_channel_title),
                            NotificationManager.IMPORTANCE_LOW);
                    channel.setDescription(getResources().getString(R.string.android_wear_data_layer_channel_desc));
                    manager.createNotificationChannel(channel);
                    Notification.Builder builder = new Notification.Builder(this, channelId);
                    builder.setContentTitle(getString(R.string.android_wear_data_layer_channel_id));
                    builder.setContentText(getString(R.string.android_wear_data_layer_channel_desc));
                    builder.setWhen(System.currentTimeMillis());
                    builder.setAutoCancel(false);
                    startForeground(1, builder.build());
                }
            } else if (WearConst.DEVICE_TO_WEAR_DEIVCEORIENTATION_UNREGISTER.equals(action)) {
                mIds.remove(id);
                if (mIds.isEmpty()) {
                    unregisterSensor();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopForeground(true);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mIds.clear();
        unregisterSensor();
        super.onDestroy();
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
            mExecutorService.execute(() -> {
                synchronized (mIds) {
                    for (String id : mIds) {
                        sendSensorEvent(data, id);
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
        WearApplication application = getWearApplication();
        application.sendMessage(id, WearConst.WEAR_TO_DEVICE_DEIVCEORIENTATION_DATA, data);
    }

    /**
     * センサーを登録する.
     */
    private synchronized void registerSensor() {
        if (mSensorManager != null) {
            return;
        }
        new Thread(() -> {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            List<Sensor> accelSensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (accelSensors.size() > 0) {
                mAccelerometer = accelSensors.get(0);
                mSensorManager.registerListener(WearAppService.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            List<Sensor> gyroSensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            if (gyroSensors.size() > 0) {
                mGyroSensor = gyroSensors.get(0);
                mSensorManager.registerListener(WearAppService.this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            mStartTime = System.currentTimeMillis();
        }).start();
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
     * GoogleApiClientを取得する.
     * @return GoogleApiClient
     */
    private WearApplication getWearApplication() {
        return ((WearApplication) getApplication());
    }
}
