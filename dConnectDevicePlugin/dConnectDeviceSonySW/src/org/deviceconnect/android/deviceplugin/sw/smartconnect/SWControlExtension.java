/*
Copyright (c) 2011 Sony Ericsson Mobile Communications AB
Copyright (C) 2012 Sony Mobile Communications AB

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

 * Neither the name of the Sony Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.deviceconnect.android.deviceplugin.sw.smartconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.deviceconnect.android.deviceplugin.sw.BuildConfig;
import org.deviceconnect.android.deviceplugin.sw.R;
import org.deviceconnect.android.deviceplugin.sw.SWApplication;
import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.TouchProfileConstants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration.SensorTypeValue;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensor;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEvent;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEventListener;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorException;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorManager;

/**
 * Sony SmartWatch Control Extension.
 */
class SWControlExtension extends ControlExtension {
    /**
     * LocalOAuthで無視するプロファイル群.
     */
    private static final String[] IGNORE_PROFILES = {AuthorizationProfileConstants.PROFILE_NAME,
            SystemProfileConstants.PROFILE_NAME, ServiceDiscoveryProfileConstants.PROFILE_NAME };

    /**
     * デバイスセンサー.
     */
    private AccessorySensor mSensor;
    /**
     * 画面サイズ(横).
     */
    private final int mWidth;
    /**
     * 画面サイズ(縦).
     */
    private final int mHeight;

    /**
     * このクラスが属するコンテキスト.
     */
    private Context mContext;

    /**
     * Creates a control extension.
     * 
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     */
    SWControlExtension(final Context context, final String hostAppPackageName) {
        super(context, hostAppPackageName);
        mContext = context;
        // Determine host application screen size.
        if (DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(context, hostAppPackageName)) {
            mWidth = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
            mHeight = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
        } else {
            mWidth = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
            mHeight = context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
        }

        AccessorySensorManager manager = new AccessorySensorManager(context, hostAppPackageName);

        // Add accelerometer, if supported by the host application.
        if (DeviceInfoHelper.isSensorSupported(context, hostAppPackageName, SensorTypeValue.ACCELEROMETER)) {
            mSensor = manager.getSensor(SensorTypeValue.ACCELEROMETER);
        }
        showDisplay();
    }

    /**
     * Last press time for touch event.
     */
    private static long sLastPressTime = 0;

    /**
     * Threshold double tap time.
     */
    private static final long THRESHOLD_DOUBLE_TAP_TIME = 500;

    /**
     * イベントの送信処理.
     * 
     * @param event イベント
     * @param accessToken アクセストークン
     * @return event
     */
    public final boolean sendEvent(final Intent event, final String accessToken) {
        if (event == null) {
            return false;
        }
        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(accessToken,
                event.getStringExtra(DConnectMessage.EXTRA_PROFILE), IGNORE_PROFILES);
        if (!checkAccessTokenResult(result)) {
            return false;
        }
        mContext.sendBroadcast(event);
        return true;
    }

    /**
     * Local OAuth使用フラグを取得する.
     * 
     * @return 使用する場合にはtrue、それ以外はfalse
     */
    private boolean isUseLocalOAuth() {
        return !LocalOAuth2Main.isAutoTestMode();
    }

    /**
     * アクセストークンのチェックを行う.
     * 
     * @param result アクセスのチェック結果
     * @return アクセストークンが正常の場合はtrue,それ以外の場合はfalse
     */
    private boolean checkAccessTokenResult(final CheckAccessTokenResult result) {
        if (!isUseLocalOAuth()) {
            return true;
        }
        return result.checkResult();
    }

    /**
     * SmartWatchDisplayへの表示.
     */
    private void showDisplay() {

        // Create bitmap to draw in.
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, SWConstants.DEFAULT_BITMAP_CONFIG);

        // Set default density to avoid scaling.
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new ViewGroup.LayoutParams(mWidth, mHeight));
        root.setGravity(Gravity.CENTER);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout sensorLayout = (LinearLayout) inflater.inflate(R.layout.generic_sensor_values, root, true);

        root.measure(mWidth, mHeight);
        root.layout(0, 0, mWidth, mHeight);

        Canvas canvas = new Canvas(bitmap);
        sensorLayout.draw(canvas);

        showBitmap(bitmap);
    }

    @Override
    public void onResume() {
        // Note: Setting the screen to be always on will drain the accessory
        // battery. It is done here solely for demonstration purposes.
        setScreenState(Control.Intents.SCREEN_STATE_ON);

        // Start listening for sensor updates.
        register();
    }

    @Override
    public void onPause() {
        unregister();
    }

    @Override
    public void onDestroy() {
        unregisterAndDestroy();
    }

    /**
     * Checks if the control extension supports the given width.
     * 
     * @param context The context.
     * @param width The width.
     * @return True if the control extension supports the given width.
     */
    public static boolean isWidthSupported(final Context context, final int width) {
        return width == context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width)
                || width == context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
    }

    /**
     * Checks if the control extension supports the given height.
     * 
     * @param context The context.
     * @param height The height.
     * @return True if the control extension supports the given height.
     */
    public static boolean isHeightSupported(final Context context, final int height) {
        return height == context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height)
                || height == context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
    }

    @Override
    public void onTouch(final ControlTouchEvent touchEvent) {
        String[] attr = new String[2];
        switch (touchEvent.getAction()) {
        case Control.Intents.TOUCH_ACTION_PRESS:
            long pressTime = touchEvent.getTimeStamp();
            if (pressTime - sLastPressTime < THRESHOLD_DOUBLE_TAP_TIME) {
                attr[0] = TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP;
                attr[1] = null;
            } else {
                attr[0] = TouchProfile.ATTRIBUTE_ON_TOUCH;
                attr[1] = TouchProfile.ATTRIBUTE_ON_TOUCH_START;
            }
            sLastPressTime = pressTime;
            break;
        case Control.Intents.TOUCH_ACTION_RELEASE:
            attr[0] = TouchProfile.ATTRIBUTE_ON_TOUCH_END;
            attr[1] = null;
            break;
        default:
            super.onTouch(touchEvent);
            return;
        }

        String serviceId = findServiceId(getDeviceName());
        if (serviceId == null) {
            super.onTouch(touchEvent);
            return;
        }

        for (int i = 0; i < 2; i++) {
            if (attr[i] == null) {
                break;
            }

            List<Event> events = EventManager.INSTANCE.getEventList(serviceId, TouchProfileConstants.PROFILE_NAME,
                    null, attr[i]);

            for (Event event : events) {
                Bundle touchdata = new Bundle();
                List<Bundle> touchlist = new ArrayList<Bundle>();
                Bundle touches = new Bundle();
                touchdata.putInt(TouchProfile.PARAM_ID, 0);
                touchdata.putFloat(TouchProfile.PARAM_X, touchEvent.getX());
                touchdata.putFloat(TouchProfile.PARAM_Y, touchEvent.getY());
                touchlist.add((Bundle) touchdata.clone());
                touches.putParcelableArray(TouchProfile.PARAM_TOUCHES, touchlist.toArray(new Bundle[touchlist.size()]));

                String eventAttr = event.getAttribute();
                Intent message = EventManager.createEventMessage(event);
                message.putExtra(TouchProfile.PARAM_TOUCH, touches);
                sendEvent(message, event.getAccessToken());
                SWApplication.setTouchCache(eventAttr, touches);
            }
        }

        super.onTouch(touchEvent);
    }

    /**
     * Returns the sensor currently being used.
     * 
     * @return The sensor.
     */
    private AccessorySensor getCurrentSensor() {
        return mSensor;
    }

    /**
     * Checks if the sensor currently being used supports interrupt mode and
     * registers an interrupt listener if it does. If not, a fixed rate listener
     * will be registered instead.
     */
    private void register() {
        AccessorySensor sensor = getCurrentSensor();
        if (sensor != null) {
            try {
                AccelerometerEventListener listener = new AccelerometerEventListener();
                if (sensor.isInterruptModeSupported()) {
                    sensor.registerInterruptListener(listener);
                } else {
                    sensor.registerFixedRateListener(listener, Sensor.SensorRates.SENSOR_DELAY_UI);
                }
            } catch (AccessorySensorException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(SWConstants.LOG_TAG, "Failed to register listener", e);
                }
            }
        }
    }

    /**
     * Unregisters any sensor event listeners connected to the sensor currently
     * being used.
     */
    private void unregister() {
        AccessorySensor sensor = getCurrentSensor();
        if (sensor != null) {
            sensor.unregisterListener();
        }
    }

    /**
     * Unregisters any sensor event listeners and unsets the sensor currently
     * being used.
     */
    private void unregisterAndDestroy() {
        unregister();
        mSensor = null;
    }

    /**
     * 加速度センサーイベントリスナー.
     */
    private class AccelerometerEventListener implements AccessorySensorEventListener {
        /**
         * Interval Start Time.
         */
        long mStart = 0;
        /**
         * Device Name.
         */
        final String mDeviceName;

        /**
         * Constructor.
         */
        AccelerometerEventListener() {
            if (SWConstants.PACKAGE_SMART_WATCH_2.equals(mHostAppPackageName)) {
                mDeviceName = SWConstants.DEVICE_NAME_SMART_WATCH_2;
            } else {
                mDeviceName = SWConstants.DEVICE_NAME_SMART_WATCH;
            }
            mStart = System.currentTimeMillis();
        }

        @Override
        public void onSensorEvent(final AccessorySensorEvent sensorEvent) {
            long interval = System.currentTimeMillis() - mStart;
            float[] values = sensorEvent.getSensorValues();
            Bundle acceleration = new Bundle();
            acceleration.putDouble(DeviceOrientationProfile.PARAM_X, values[0]);
            acceleration.putDouble(DeviceOrientationProfile.PARAM_Y, values[1]);
            acceleration.putDouble(DeviceOrientationProfile.PARAM_Z, values[2]);
            
            Bundle orientation = new Bundle();
            DeviceOrientationProfile.setAccelerationIncludingGravity(orientation, acceleration);
            DeviceOrientationProfile.setInterval(orientation, interval);
            
            String serviceId = findServiceId();
            if (serviceId == null) {
                return;
            }
            
            // データをキャッシュする
            SWExtensionService service = (SWExtensionService) mContext;
            SWApplication application = (SWApplication) service.getApplication();
            application.setDeviceOrientationCache(serviceId, values, interval);
            
            // イベントを配送
            List<Event> events = EventManager.INSTANCE
            .getEventList(serviceId, DeviceOrientationProfileConstants.PROFILE_NAME,
                          null, DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);
            synchronized (events) {
                for (Event event : events) {
                    Intent message = EventManager.createEventMessage(event);
                    message.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION, orientation);
                    sendEvent(message, event.getAccessToken());
                }
            }
            
            mStart = System.currentTimeMillis();
        }
    }

    /**
     * Find Service ID.
     *
     * @param deviceName Device Name.
     * @return Service ID.
     */
    private String findServiceId(final String deviceName) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
            if (bondedDevices != null) {
                for (BluetoothDevice device : bondedDevices) {
                    if (deviceName.equals(device.getName())) {
                        String address = device.getAddress();
                        return address.replace(":", "").toLowerCase(Locale.ENGLISH);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get Device Name.
     * 
     * @return Device Name.
     */
    private String getDeviceName() {
        if (SWConstants.PACKAGE_SMART_WATCH_2.equals(mHostAppPackageName)) {
            return SWConstants.DEVICE_NAME_SMART_WATCH_2;
        } else {
            return SWConstants.DEVICE_NAME_SMART_WATCH;
        }
    }
}
