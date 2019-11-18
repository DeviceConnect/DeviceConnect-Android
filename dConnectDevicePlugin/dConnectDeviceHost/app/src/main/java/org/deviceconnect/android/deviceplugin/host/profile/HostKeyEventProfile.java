/*
 HostKeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.activity.KeyEventProfileActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Key Event Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostKeyEventProfile extends KeyEventProfile {

 
    /** Key Event profile event management flag. */
    private static int sFlagKeyEventEventManage = 0;
    /** Key Event profile event flag. (ondown) */
    private static final int FLAG_ON_DOWN = 0x0001;
    /** Key Event  profile event flag. (onup) */
    private static final int FLAG_ON_UP = 0x0002;
    /** Key Event  profile event flag. (onkeychange) */
    private static final int FLAG_ON_KEY_CHANGE = 0x0004;
    /** Finish key event profile activity action. */
    public static final String ACTION_FINISH_KEYEVENT_ACTIVITY =
            "org.deviceconnect.android.deviceplugin.host.keyevent.FINISH";
    /** Finish key event profile activity action. */
    public static final String ACTION_KEYEVENT =
            "org.deviceconnect.android.deviceplugin.host.keyevent.action.KEY_EVENT";

    /** Notification Id */
    private final int NOTIFICATION_ID = 3529;

    /** Notification Content */
    private final String NOTIFICATION_CONTENT = "Host Key Event Profileからの起動要求";

    /**
     * KeyEventProfileActivityからのKeyEventを中継するBroadcast Receiver.
     */
    private BroadcastReceiver mKeyEventBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_KEYEVENT)) {
                // ManagerにEventを送信する
                intent.setAction(IntentDConnectMessage.ACTION_EVENT);
                sendEvent(intent, intent.getStringExtra("accessToken"));
            }
        }
    };
    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_KEY_CHANGE = "onKeyChange";
    private final DConnectApi mGetOnKeyChangeApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyEvent = getApp().getKeyEventCache(ATTRIBUTE_ON_KEY_CHANGE);
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyEvent);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mPutOnKeyChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execKeyEventActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_KEYEVENT);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mKeyEventBR, filter);
                setKeyEventEventFlag(FLAG_ON_KEY_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,  "Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnKeyChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mKeyEventBR);
                resetKeyEventEventFlag(FLAG_ON_KEY_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };
    private final DConnectApi mGetOnDownApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyEvent = getApp().getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_DOWN);
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyEvent);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutOnDownApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execKeyEventActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_KEYEVENT);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mKeyEventBR, filter);
                setKeyEventEventFlag(FLAG_ON_DOWN);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDownApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetKeyEventEventFlag(FLAG_ON_DOWN);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mKeyEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mGetOnUpApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle keyEvent = getApp().getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_UP);
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyEvent);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutOnUpApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execKeyEventActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_KEYEVENT);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mKeyEventBR, filter);
                setKeyEventEventFlag(FLAG_ON_UP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnUpApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetKeyEventEventFlag(FLAG_ON_UP);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mKeyEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    public HostKeyEventProfile() {
        addApi(mGetOnKeyChangeApi);
        addApi(mPutOnKeyChangeApi);
        addApi(mDeleteOnKeyChangeApi);
        addApi(mGetOnDownApi);
        addApi(mPutOnDownApi);
        addApi(mDeleteOnDownApi);
        addApi(mGetOnUpApi);
        addApi(mPutOnUpApi);
        addApi(mDeleteOnUpApi);
    }

    /**
     * Execute Key Event Activity.
     * 
     * @param serviceId service ID.
     * @return Always true.
     */
    private boolean execKeyEventActivity(final String serviceId) {
        ActivityManager mActivityManager = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String mClassName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();

        if (!(KeyEventProfileActivity.class.getName().equals(mClassName))) {
            Intent mIntent = new Intent();
            mIntent.setClass(getContext(), KeyEventProfileActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                this.getContext().startActivity(mIntent);
            } else {
                NotificationUtils.createNotificationChannel(getContext());
                NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, mIntent, NOTIFICATION_CONTENT);
            }
        }
        return true;
    }

    /**
     * Finish Key Event Profile Activity.
     * 
     * @return Always true.
     */
    private boolean finishKeyEventProfileActivity() {
        String className = getClassnameOfTopActivity();
        if (KeyEventProfileActivity.class.getName().equals(className)) {
            Intent intent = new Intent(HostKeyEventProfile.ACTION_FINISH_KEYEVENT_ACTIVITY);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
        return true;
    }

    /**
     * Set key event event flag.
     * 
     * @param flag Set flag.
     */
    private void setKeyEventEventFlag(final int flag) {
        sFlagKeyEventEventManage |= flag;
    }

    /**
     * Reset key event event flag.
     * 
     * @param flag Reset flag.
     */
    private void resetKeyEventEventFlag(final int flag) {
        sFlagKeyEventEventManage &= ~(flag);
        if (sFlagKeyEventEventManage == 0) {
            finishKeyEventProfileActivity();
        }
    }

    /**
     * Get the class name of the Activity being displayed at the top of the screen.
     * 
     * @return class name.
     */
    private String getClassnameOfTopActivity() {
        ActivityManager activityMgr = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        return activityMgr.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    /**
     * Check set KeyEvent event manage flag.
     *
     * @return  set flag is true, otherwise false.
     */
    private boolean isSetKeyEventManageFlag() {
        return sFlagKeyEventEventManage != 0;
    }

    /**
     * Reset KeyEvent profile.
     */
    public void resetKeyEventProfile() {
        if (isSetKeyEventManageFlag()) {
            resetKeyEventEventFlag(FLAG_ON_DOWN | FLAG_ON_UP | FLAG_ON_KEY_CHANGE);
        }
    }

    private HostDeviceApplication getApp() {
        return (HostDeviceApplication) getContext().getApplicationContext();
    }
}
