/*
 HostTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.profile.TouchProfileActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Touch Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostTouchProfile extends TouchProfile {


    /** Touch profile event management flag. */
    private static int sFlagTouchEventManage = 0;
    /** Touch profile event flag. (ontouch) */
    private static final int FLAG_ON_TOUCH = 0x0001;
    /** Touch profile event flag. (ontouchstart) */
    private static final int FLAG_ON_TOUCH_START = 0x0002;
    /** Touch profile event flag. (ontouchend) */
    private static final int FLAG_ON_TOUCH_END = 0x0004;
    /** Touch profile event flag. (ondoubletap) */
    private static final int FLAG_ON_DOUBLE_TAP = 0x0008;
    /** Touch profile event flag. (ontouchmove) */
    private static final int FLAG_ON_TOUCH_MOVE = 0x0010;
    /** Touch profile event flag. (ontouchcancel) */
    private static final int FLAG_ON_TOUCH_CANCEL = 0x0020;
    /** Touch profile event flag. (ontouchchange). */
    private static final int FLAG_ON_TOUCH_CHANGE = 0x0040;

    /** Finish touch profile activity action. */
    public static final String ACTION_FINISH_TOUCH_ACTIVITY =
            "org.deviceconnect.android.deviceplugin.host.touch.FINISH";
    /** Finish touch event profile activity action. */
    public static final String ACTION_TOUCH =
            "org.deviceconnect.android.deviceplugin.host.touch.action.KEY_EVENT";

    /** Notification Id */
    private final int NOTIFICATION_ID = 3527;

    /**
     * KeyEventProfileActivityからのKeyEventを中継するBroadcast Receiver.
     */
    private BroadcastReceiver mTouchEventBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_TOUCH)) {
                // ManagerにEventを送信する
                intent.setAction(IntentDConnectMessage.ACTION_EVENT);
                sendEvent(intent, intent.getStringExtra("accessToken"));
            }
        }
    };
    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_TOUCH_CHANGE = "onTouchChange";
    private final DConnectApi mGetOnTouchChangeApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(ATTRIBUTE_ON_TOUCH_CHANGE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mGetOnTouchApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchStartApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_START);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchEndApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_END);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnDoubleTapApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchMoveApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mGetOnTouchCancelApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            Bundle touches = getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };
    private final DConnectApi mPutOnTouchChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);

                setTouchEventFlag(FLAG_ON_TOUCH_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };
    private final DConnectApi mPutOnTouchApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);
                setTouchEventFlag(FLAG_ON_TOUCH);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mPutOnTouchStartApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);
                setTouchEventFlag(FLAG_ON_TOUCH_START);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mPutOnTouchEndApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);
                setTouchEventFlag(FLAG_ON_TOUCH_END);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mPutOnDoubleTapApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);
                setTouchEventFlag(FLAG_ON_DOUBLE_TAP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mPutOnTouchMoveApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);
                setTouchEventFlag(FLAG_ON_TOUCH_MOVE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    private final DConnectApi mPutOnTouchCancelApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                IntentFilter filter = new IntentFilter(ACTION_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTouchEventBR, filter);
                setTouchEventFlag(FLAG_ON_TOUCH_CANCEL);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };
    private final DConnectApi mDeleteOnTouchChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_CHANGE);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };
    private final DConnectApi mDeleteOnTouchApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchStartApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_START);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchEndApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_END);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnDoubleTap = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_DOUBLE_TAP);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchMove = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_MOVE);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnTouchCancel = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_CANCEL);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTouchEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    public HostTouchProfile() {
        addApi(mGetOnTouchChangeApi);
        addApi(mGetOnTouchApi);
        addApi(mGetOnTouchStartApi);
        addApi(mGetOnTouchEndApi);
        addApi(mGetOnDoubleTapApi);
        addApi(mGetOnTouchMoveApi);
        addApi(mGetOnTouchCancelApi);
        addApi(mPutOnTouchChangeApi);
        addApi(mPutOnTouchApi);
        addApi(mPutOnTouchStartApi);
        addApi(mPutOnTouchEndApi);
        addApi(mPutOnDoubleTapApi);
        addApi(mPutOnTouchMoveApi);
        addApi(mPutOnTouchCancelApi);
        addApi(mDeleteOnTouchChangeApi);
        addApi(mDeleteOnTouchApi);
        addApi(mDeleteOnTouchStartApi);
        addApi(mDeleteOnTouchEndApi);
        addApi(mDeleteOnDoubleTap);
        addApi(mDeleteOnTouchMove);
        addApi(mDeleteOnTouchCancel);
    }

    /**
     * Execute Touch Profile Activity.
     * 
     * @param serviceId service ID.
     * @return Always true.
     */
    private boolean execTouchProfileActivity(final String serviceId) {
        String mClassName = getApp().getClassnameOfTopActivity();

        if (!(TouchProfileActivity.class.getName().equals(mClassName))) {
            Intent mIntent = new Intent();
            mIntent.setClass(getContext(), TouchProfileActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                this.getContext().startActivity(mIntent);
            } else {
                NotificationUtils.createNotificationChannel(getContext());
                NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, mIntent,
                        getContext().getString(R.string.host_notification_touch_warnning));
            }
        }
        return true;
    }

    /**
     * Finish Touch Profile Activity.
     * 
     * @return Always true.
     */
    private boolean finishTouchProfileActivity() {
        String className = getApp().getClassnameOfTopActivity();
        if (TouchProfileActivity.class.getName().equals(className)) {
            Intent intent = new Intent(HostTouchProfile.ACTION_FINISH_TOUCH_ACTIVITY);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
        return true;
    }

    /**
     * Set touch event flag.
     * 
     * @param flag Set flag.
     */
    private void setTouchEventFlag(final int flag) {
        sFlagTouchEventManage |= flag;
    }

    /**
     * Reset touch event flag.
     * 
     * @param flag Reset flag.
     */
    private void resetTouchEventFlag(final int flag) {
        sFlagTouchEventManage &= ~(flag);
        if (sFlagTouchEventManage == 0) {
            finishTouchProfileActivity();
        }
    }

    /**
     * Check set Touch event manage flag.
     *
     * @return  set flag is true, otherwise false.
     */
    private boolean isSetTouchEventManageFlag() {
        return sFlagTouchEventManage != 0;
    }

    /**
     * Reset Touch profile.
     */
    public void resetTouchProfile() {
        if (isSetTouchEventManageFlag()) {
            resetTouchEventFlag(FLAG_ON_TOUCH | FLAG_ON_TOUCH_START | FLAG_ON_TOUCH_END
                    | FLAG_ON_DOUBLE_TAP | FLAG_ON_TOUCH_MOVE | FLAG_ON_TOUCH_CANCEL
                    | FLAG_ON_TOUCH_CHANGE);
        }
    }

    /**
     * Get touch cache.
     *
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public Bundle getTouchCache(final String attr) {
        return getApp().getTouchCache(attr);
    }

    /**
     * Get keyevent cache.
     *
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public Bundle getKeyEventCache(final String attr) {
        return getApp().getKeyEventCache(attr);
    }

    public HostDeviceApplication getApp() {
        return (HostDeviceApplication) getContext().getApplicationContext();
    }
}
