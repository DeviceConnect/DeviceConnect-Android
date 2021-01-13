/*
 HostKeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.profile.KeyEventProfileActivity;
import org.deviceconnect.android.deviceplugin.host.sensor.HostEventManager;
import org.deviceconnect.android.deviceplugin.host.sensor.HostKeyEvent;
import org.deviceconnect.android.event.Event;
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

import java.util.List;

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

    /** Notification Id */
    private static final int NOTIFICATION_ID = 3529;

    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_KEY_CHANGE = "onKeyChange";

    // GET /gotapi/keyEvent/onKeyChange
    private final DConnectApi mGetOnKeyChangeApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostKeyEvent keyEvent = mHostEventManager.getKeyEventCache(getStateName(ATTRIBUTE_ON_KEY_CHANGE));
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, convertKeyEventToBundle(keyEvent, true));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // PUT /gotapi/keyEvent/onKeyChange
    private final DConnectApi mPutOnKeyChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchKeyEventActivity();
                setKeyEventEventFlag(FLAG_ON_KEY_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,  "Can not register event.");
            }
            return true;
        }
    };

    // DELETE /gotapi/keyEvent/onKeyChange
    private final DConnectApi mDeleteOnKeyChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_KEY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetKeyEventEventFlag(FLAG_ON_KEY_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    // GET /gotapi/keyEvent/onKeyChange
    private final DConnectApi mGetOnDownApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostKeyEvent keyEvent = mHostEventManager.getKeyEventCache(getStateName(KeyEventProfile.ATTRIBUTE_ON_DOWN));
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, convertKeyEventToBundle(keyEvent));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // PUT /gotapi/keyEvent/onDown
    private final DConnectApi mPutOnDownApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchKeyEventActivity();
                setKeyEventEventFlag(FLAG_ON_DOWN);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not register event.");
            }
            return true;
        }
    };

    // DELETE /gotapi/keyEvent/onDown
    private final DConnectApi mDeleteOnDownApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOWN;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetKeyEventEventFlag(FLAG_ON_DOWN);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    // GET /gotapi/keyEvent/onUp
    private final DConnectApi mGetOnUpApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostKeyEvent keyEvent = mHostEventManager.getKeyEventCache(getStateName(KeyEventProfile.ATTRIBUTE_ON_UP));
            if (keyEvent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, convertKeyEventToBundle(keyEvent));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // PUT /gotapi/keyEvent/onUp
    private final DConnectApi mPutOnUpApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchKeyEventActivity();
                setKeyEventEventFlag(FLAG_ON_UP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not register event.");
            }
            return true;
        }
    };

    // DELETE /gotapi/keyEvent/onUp
    private final DConnectApi mDeleteOnUpApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_UP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetKeyEventEventFlag(FLAG_ON_UP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    private final HostEventManager.OnKeyEventListener mOnKeyEventListener = keyEvent -> {
        sendKeyEvent(keyEvent);
        sendKeyChangeEvent(keyEvent);
    };

    private final HostEventManager mHostEventManager;

    public HostKeyEventProfile(HostEventManager hostEventManager) {
        mHostEventManager = hostEventManager;
        mHostEventManager.addOnKeyEventListener(mOnKeyEventListener);

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

    private void sendKeyEvent(HostKeyEvent keyEvent) {
        List<Event> events = EventManager.INSTANCE.getEventList(getService().getId(),
                KeyEventProfile.PROFILE_NAME, null, getAttributeName(keyEvent.getState()));
        Bundle bundle = convertKeyEventToBundle(keyEvent);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, bundle);
            getPluginContext().sendEvent(intent, event.getAccessToken());
        }
    }

    private void sendKeyChangeEvent(HostKeyEvent keyEvent) {
        List<Event> events = EventManager.INSTANCE.getEventList(getService().getId(),
                KeyEventProfile.PROFILE_NAME, null, HostKeyEventProfile.ATTRIBUTE_ON_KEY_CHANGE);
        Bundle bundle = convertKeyEventToBundle(keyEvent, true);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra(KeyEventProfile.PARAM_KEYEVENT, bundle);
            getPluginContext().sendEvent(intent, event.getAccessToken());
        }
    }

    private Bundle convertKeyEventToBundle(HostKeyEvent keyEvent) {
        return convertKeyEventToBundle(keyEvent, false);
    }

    private Bundle convertKeyEventToBundle(HostKeyEvent keyEvent, boolean hasState) {
        Bundle bundle = new Bundle();
        bundle.putInt(KeyEventProfile.PARAM_ID, keyEvent.getId());
        bundle.putString(KeyEventProfile.PARAM_CONFIG, keyEvent.getConfig());
        if (hasState) {
            bundle.putString("state", keyEvent.getState());
        }
        return bundle;
    }

    /**
     * キーイベントのステートに合わせた attribute 名を取得します.
     *
     * @param eventState イベントステート
     * @return attribute 名
     */
    private String getAttributeName(String eventState) {
        switch (eventState) {
            case HostKeyEvent.STATE_KEY_DOWN:
                return ATTRIBUTE_ON_DOWN;
            case HostKeyEvent.STATE_KEY_UP:
                return ATTRIBUTE_ON_UP;
            default:
                return "";
        }
    }

    /**
     * アトリビュート名からキーイベントのステート名を取得します.
     *
     * @param attribute アトリビュート名
     * @return ステート名
     */
    private String getStateName(String attribute) {
        switch (attribute) {
            case ATTRIBUTE_ON_DOWN:
                return HostKeyEvent.STATE_KEY_DOWN;
            case ATTRIBUTE_ON_UP:
                return HostKeyEvent.STATE_KEY_UP;
            case ATTRIBUTE_ON_KEY_CHANGE:
                return HostKeyEvent.STATE_KEY_CHANGE;
            default:
                return "";
        }
    }

    /**
     * Execute Key Event Activity.
     */
    private void launchKeyEventActivity() {
        if (!getApp().isClassnameOfTopActivity(KeyEventProfileActivity.class)) {
            Intent intent = new Intent();
            intent.setClass(getContext(), KeyEventProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getApp().isDeviceConnectClassOfTopActivity() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                getContext().startActivity(intent);
            } else {
                NotificationUtils.createNotificationChannel(getContext());
                NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, intent,
                        getContext().getString(R.string.host_notification_keyevent_warnning));
            }
        }
    }

    /**
     * Finish Key Event Profile Activity.
     */
    private void finishKeyEventProfileActivity() {
        if (getApp().isClassnameOfTopActivity(KeyEventProfileActivity.class)) {
            Intent intent = new Intent(HostKeyEventProfile.ACTION_FINISH_KEYEVENT_ACTIVITY);
            getContext().sendBroadcast(intent);
        }
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
