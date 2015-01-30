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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.deviceconnect.android.deviceplugin.sw.R;
import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.localoauth.CheckAccessTokenResult;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.profile.DeviceOrientationProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.KeyEventProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;

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
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
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

    /** Key Code (cancel). */
    private static final int KEYCODE_CANCEL = 9;
    /** Key Code (ok). */
    private static final int KEYCODE_OK = 10;

    /** Menu item number 0. */
    private static final int MENU_ITEM_0 = 0;
    /** Menu item number 1. */
    private static final int MENU_ITEM_1 = 1;
    /** Menu item number 2. */
    private static final int MENU_ITEM_2 = 2;
    /** Menu item number 3. */
    private static final int MENU_ITEM_3 = 3;

    /** Key Event menu (STD KEY). */
    private static final String MENU_STD_KEY = "STD KEY";
    /** Key Event menu (MEDIA). */
    private static final String MENU_MEDIA = "MEDIA";
    /** Key Event menu (DPAD). */
    private static final String MENU_DPAD = "DPAD";
    /** Key Event menu (USER). */
    private static final String MENU_USER = "USER";

    /** Key type (STD KEY). */
    private static final int KEYTYPE_STD_KEY = 0x00000000;
    /** Key type (MEDIA). */
    private static final int KEYTYPE_MEDIA = 0x00000200;
    /** Key type (DPAD). */
    private static final int KEYTYPE_DPAD = 0x00000400;
    /** Key type (USER). */
    private static final int KEYTYPE_USER = 0x00000800;

    /** Key type. */
    private int mKeyType = KEYTYPE_STD_KEY;

    /** Key Event item list 1. */
    Bundle[] mMenuItemsText1 = new Bundle[2];
    /** Key Event item list 2. */
    Bundle[] mMenuItemsText2 = new Bundle[2];

    /**
     * Used to toggle if text instead of icons will be used in the option menu.
     */
    private boolean mTextMenu = true;

    /**
     * Prepares icons and text menu items for the SmartWatch 2 options menu. For
     * each menu item you define an ID and a label, or an icon.
     */
    private void initializeMenus() {
        mMenuItemsText1[0] = new Bundle();
        mMenuItemsText1[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_0);
        mMenuItemsText1[0].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, MENU_STD_KEY);

        mMenuItemsText1[1] = new Bundle();
        mMenuItemsText1[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_1);
        mMenuItemsText1[1].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, MENU_MEDIA);

        mMenuItemsText2[0] = new Bundle();
        mMenuItemsText2[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_2);
        mMenuItemsText2[0].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, MENU_DPAD);

        mMenuItemsText2[1] = new Bundle();
        mMenuItemsText2[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_3);
        mMenuItemsText2[1].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, MENU_USER);
    }

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
     * 加速度取得インターバル.
     */
    private final long mInterval = SWConstants.DEFAULT_SENSOR_INTERVAL;

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
    protected boolean isUseLocalOAuth() {
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
     * LocalOAuthで無視するプロファイル群.
     */
    private static final String[] IGNORE_PROFILES = {AuthorizationProfileConstants.PROFILE_NAME,
            SystemProfileConstants.PROFILE_NAME, ServiceDiscoveryProfileConstants.PROFILE_NAME};

    /**
     * Creates a control extension.
     * 
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     */
    SWControlExtension(final Context context, final String hostAppPackageName) {
        super(context, hostAppPackageName);

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
        initializeMenus();
        showDisplay();
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
    public void onTouch(final ControlTouchEvent event) {
        super.onTouch(event);
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
                Log.e(SWConstants.LOG_TAG, "Failed to register listener", e);
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
        }

        @Override
        public void onSensorEvent(final AccessorySensorEvent sensorEvent) {
            if (mStart == 0 || System.currentTimeMillis() - mStart > mInterval) {
                float[] values = sensorEvent.getSensorValues();
                Bundle orientation = new Bundle();
                Bundle acceleration = new Bundle();
                orientation.putBundle(DeviceOrientationProfile.PARAM_ACCELERATION_INCLUDING_GRAVITY, acceleration);
                orientation.putLong(DeviceOrientationProfile.PARAM_INTERVAL, mInterval);
                acceleration.putDouble(DeviceOrientationProfile.PARAM_X, values[0]);
                acceleration.putDouble(DeviceOrientationProfile.PARAM_Y, values[1]);
                acceleration.putDouble(DeviceOrientationProfile.PARAM_Z, values[2]);

                String serviceId = findServiceId(mDeviceName);
                if (serviceId == null) {
                    return;
                }
                List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                        DeviceOrientationProfileConstants.PROFILE_NAME, null,
                        DeviceOrientationProfile.ATTRIBUTE_ON_DEVICE_ORIENTATION);

                for (Event event : events) {
                    Intent message = EventManager.createEventMessage(event);
                    message.putExtra(DeviceOrientationProfile.PARAM_ORIENTATION, orientation);
                    sendEvent(message, event.getAccessToken());
                }

                mStart = System.currentTimeMillis();
            }
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

    @Override
    public void onObjectClick(final ControlObjectClickEvent clickevent) {
        int keyCode = 0;
        if (clickevent.getLayoutReference() == R.id.btn_keyevent_1) {
            keyCode = KEYCODE_CANCEL;
        } else if (clickevent.getLayoutReference() == R.id.btn_keyevent_2) {
            keyCode = KEYCODE_OK;
        } else {
            return;
        }

        final String deviceName;
        String config = getConfig(keyCode);

        if (SWConstants.PACKAGE_SMART_WATCH_2.equals(mHostAppPackageName)) {
            deviceName = SWConstants.DEVICE_NAME_SMART_WATCH_2;
        } else {
            deviceName = SWConstants.DEVICE_NAME_SMART_WATCH;
        }

        String serviceId = findServiceId(deviceName);
        if (serviceId == null) {
            return;
        }

        List<Event> events = EventManager.INSTANCE.getEventList(serviceId, KeyEventProfileConstants.PROFILE_NAME, null,
                KeyEventProfile.ATTRIBUTE_ON_DOWN);
        for (Event event : events) {
            Bundle keyevent = new Bundle();
            Intent message = EventManager.createEventMessage(event);
            setKeyEventData(keyevent, keyCode + mKeyType, config);
            message.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            sendEvent(message, event.getAccessToken());
        }
    }

    /**
     * Get configure string.
     * 
     * @param keyCode Key code.
     * @return Configure string.
     */
    private String getConfig(final int keyCode) {
        switch (mKeyType) {
        case KEYTYPE_MEDIA:
            switch (keyCode) {
            case Control.KeyCodes.KEYCODE_ACTION:
                return "ACTION";
            case Control.KeyCodes.KEYCODE_BACK:
                return "BACK";
            case Control.KeyCodes.KEYCODE_NEXT:
                return "NEXT";
            case Control.KeyCodes.KEYCODE_OPTIONS:
                return "OPTIONS";
            case Control.KeyCodes.KEYCODE_PLAY:
                return "PLAY";
            case Control.KeyCodes.KEYCODE_PREVIOUS:
                return "PREVIOUS";
            case Control.KeyCodes.KEYCODE_VOLUME_DOWN:
                return "VOLUME_DOWN";
            case Control.KeyCodes.KEYCODE_VOLUME_UP:
                return "VOLUME_UP";
            case KEYCODE_CANCEL:
                return "MEDIA_STOP";
            case KEYCODE_OK:
                return "MEDIA_PLAY";
            default:
                return "";
            }
        case KEYTYPE_DPAD:
            switch (keyCode) {
            case KEYCODE_CANCEL:
                return "UP";
            case KEYCODE_OK:
                return "DOWN";
            default:
                return "";
            }
        case KEYTYPE_USER:
            switch (keyCode) {
            case KEYCODE_CANCEL:
                return "USER_CANCEL";
            case KEYCODE_OK:
                return "USER_OK";
            default:
                return "";
            }
        case KEYTYPE_STD_KEY:
        default:
            switch (keyCode) {
            case Control.KeyCodes.KEYCODE_ACTION:
                return "ACTION";
            case Control.KeyCodes.KEYCODE_BACK:
                return "BACK";
            case Control.KeyCodes.KEYCODE_NEXT:
                return "NEXT";
            case Control.KeyCodes.KEYCODE_OPTIONS:
                return "OPTIONS";
            case Control.KeyCodes.KEYCODE_PLAY:
                return "PLAY";
            case Control.KeyCodes.KEYCODE_PREVIOUS:
                return "PREVIOUS";
            case Control.KeyCodes.KEYCODE_VOLUME_DOWN:
                return "VOLUME_DOWN";
            case Control.KeyCodes.KEYCODE_VOLUME_UP:
                return "VOLUME_UP";
            case KEYCODE_CANCEL:
                return "CANCEL";
            case KEYCODE_OK:
                return "OK";
            default:
                return "";
            }
        }
    }

    @Override
    public void onKey(final int action, final int keyCode, final long timeStamp) {
        final String deviceName;
        String config = getConfig(keyCode);

        if (SWConstants.PACKAGE_SMART_WATCH_2.equals(mHostAppPackageName)) {
            deviceName = SWConstants.DEVICE_NAME_SMART_WATCH_2;
        } else {
            deviceName = SWConstants.DEVICE_NAME_SMART_WATCH;
        }

        String serviceId = findServiceId(deviceName);
        if (serviceId == null) {
            return;
        }

        if (action == Control.Intents.KEY_ACTION_PRESS) {
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId, KeyEventProfileConstants.PROFILE_NAME,
                    null, KeyEventProfile.ATTRIBUTE_ON_DOWN);

            for (Event event : events) {
                Bundle keyevent = new Bundle();
                Intent message = EventManager.createEventMessage(event);
                setKeyEventData(keyevent, keyCode, config);
                message.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
                sendEvent(message, event.getAccessToken());
            }
        } else if (action == Control.Intents.KEY_ACTION_RELEASE) {
            List<Event> events = EventManager.INSTANCE.getEventList(serviceId, KeyEventProfileConstants.PROFILE_NAME,
                    null, KeyEventProfile.ATTRIBUTE_ON_UP);

            for (Event event : events) {
                Bundle keyevent = new Bundle();
                Intent message = EventManager.createEventMessage(event);
                setKeyEventData(keyevent, keyCode, config);
                message.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
                sendEvent(message, event.getAccessToken());
            }
        }

        if (action == Control.Intents.KEY_ACTION_RELEASE && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
            if (mTextMenu) {
                showMenu(mMenuItemsText1);
            } else {
                showMenu(mMenuItemsText2);
            }
            mTextMenu = !mTextMenu;
        }
    }

    @Override
    public void onMenuItemSelected(final int menuItem) {
        Log.d("ABC", "onMenuItemSelected() - menu item " + menuItem);

        switch (menuItem) {
        case MENU_ITEM_1:
            mKeyType = KEYTYPE_MEDIA;
            break;
        case MENU_ITEM_2:
            mKeyType = KEYTYPE_DPAD;
            break;
        case MENU_ITEM_3:
            mKeyType = KEYTYPE_USER;
            break;
        case MENU_ITEM_0:
        default:
            mKeyType = KEYTYPE_STD_KEY;
            break;
        }
    }

    /**
     * Set keyevent data.
     * 
     * @param keyevent Bundle for storage keyevent.
     * @param keyCode Key code.
     * @param config Configure string.
     */
    private void setKeyEventData(final Bundle keyevent, final int keyCode, final String config) {
        keyevent.putInt(KeyEventProfile.PARAM_ID, keyCode);
        keyevent.putString(KeyEventProfile.PARAM_CONFIG, config);
    }
}
