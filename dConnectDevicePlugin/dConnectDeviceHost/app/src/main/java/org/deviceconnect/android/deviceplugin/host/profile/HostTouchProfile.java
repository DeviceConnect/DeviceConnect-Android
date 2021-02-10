/*
 HostTouchProfile.java
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
import org.deviceconnect.android.deviceplugin.host.activity.profile.TouchProfileActivity;
import org.deviceconnect.android.deviceplugin.host.sensor.HostEventManager;
import org.deviceconnect.android.deviceplugin.host.sensor.HostTouchEvent;
import org.deviceconnect.android.event.Event;
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

import java.util.ArrayList;
import java.util.List;

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

    /** Notification Id */
    private static final int NOTIFICATION_ID = 3527;

    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ON_TOUCH_CHANGE = "onTouchChange";

    // GET /gotapi/touch/onTouchChange
    private final DConnectApi mGetOnTouchChangeApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_TOUCH_CHANGE));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches, true));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // GET /gotapi/touch/onTouch
    private final DConnectApi mGetOnTouchApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_TOUCH_START));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // GET /gotapi/touch/onTouchStart
    private final DConnectApi mGetOnTouchStartApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_TOUCH_START));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // GET /gotapi/touch/onTouchEnd
    private final DConnectApi mGetOnTouchEndApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_TOUCH_END));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // GET /gotapi/touch/onDoubleTap
    private final DConnectApi mGetOnDoubleTapApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_DOUBLE_TAP));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // GET /gotapi/touch/onTouchMove
    private final DConnectApi mGetOnTouchMoveApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_TOUCH_MOVE));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // GET /gotapi/touch/onTouchCancel
    private final DConnectApi mGetOnTouchCancelApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            HostTouchEvent touches = mHostEventManager.getTouchCache(getStateName(ATTRIBUTE_ON_TOUCH_CANCEL));
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, convertEventToBundle(touches));
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
            return true;
        }
    };

    // PUT /gotapi/touch/onTouchChange
    private final DConnectApi mPutOnTouchChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_TOUCH_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // PUT /gotapi/touch/onTouch
    private final DConnectApi mPutOnTouchApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_TOUCH);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // PUT /gotapi/touch/onTouchStart
    private final DConnectApi mPutOnTouchStartApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_TOUCH_START);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // PUT /gotapi/touch/onTouchEnd
    private final DConnectApi mPutOnTouchEndApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_TOUCH_END);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // PUT /gotapi/touch/onDoubleTap
    private final DConnectApi mPutOnDoubleTapApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_DOUBLE_TAP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // PUT /gotapi/touch/onTouchMove
    private final DConnectApi mPutOnTouchMoveApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_TOUCH_MOVE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // PUT /gotapi/touch/onTouchCancel
    private final DConnectApi mPutOnTouchCancelApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                launchTouchProfileActivity();
                setTouchEventFlag(FLAG_ON_TOUCH_CANCEL);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not register event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onTouchChange
    private final DConnectApi mDeleteOnTouchChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_CHANGE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onTouch
    private final DConnectApi mDeleteOnTouchApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onTouchStart
    private final DConnectApi mDeleteOnTouchStartApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_START;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_START);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onTouchEnd
    private final DConnectApi mDeleteOnTouchEndApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_END;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_END);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onDoubleTap
    private final DConnectApi mDeleteOnDoubleTap = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_DOUBLE_TAP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_DOUBLE_TAP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onTouchMove
    private final DConnectApi mDeleteOnTouchMove = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_MOVE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_MOVE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    // Delete /gotapi/touch/onTouchCancel
    private final DConnectApi mDeleteOnTouchCancel = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_TOUCH_CANCEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_CANCEL);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response,"Can not unregister event.");
            }
            return true;
        }
    };

    private final HostEventManager mHostEventManager;

    private final HostEventManager.OnTouchEventListener mOnTouchEventListener = touchEvent -> {
        sendTouchEvent(touchEvent);
        sendTouchChangeEvent(touchEvent);
    };

    public HostTouchProfile(HostEventManager hostEventManager) {
        mHostEventManager = hostEventManager;
        mHostEventManager.addOnTouchEventListener(mOnTouchEventListener);

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
     * 各タッチイベントを配信します.
     *
     * @param touchEvent タッチイベント
     */
    private void sendTouchEvent(HostTouchEvent touchEvent) {
        // touch start のイベントの場合のみ、 onTouch にもイベントを配送します。
        if (touchEvent.getState().equals(HostTouchEvent.STATE_TOUCH_START)) {
            List<Event> touchEvents = EventManager.INSTANCE.getEventList(getService().getId(),
                    TouchProfile.PROFILE_NAME, null, TouchProfile.ATTRIBUTE_ON_TOUCH);
            Bundle touches = convertEventToBundle(touchEvent);
            for (Event evt : touchEvents) {
                Intent intent = EventManager.createEventMessage(evt);
                intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
                getPluginContext().sendEvent(intent, evt.getAccessToken());
            }
        }

        List<Event> touchEvents = EventManager.INSTANCE.getEventList(getService().getId(),
                TouchProfile.PROFILE_NAME, null, getAttributeName(touchEvent.getState()));
        Bundle touches = convertEventToBundle(touchEvent);
        for (Event evt : touchEvents) {
            Intent intent = EventManager.createEventMessage(evt);
            intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
            getPluginContext().sendEvent(intent, evt.getAccessToken());
        }
    }

    /**
     * 各タッチイベントを onTouchChange に配信します.
     *
     * @param touchEvent タッチイベント
     */
    private void sendTouchChangeEvent(HostTouchEvent touchEvent) {
        List<Event> touchEvents = EventManager.INSTANCE.getEventList(getService().getId(),
                TouchProfile.PROFILE_NAME, null, ATTRIBUTE_ON_TOUCH_CHANGE);
        Bundle touches = convertEventToBundle(touchEvent, true);

        for (Event evt : touchEvents) {
            Intent intent = EventManager.createEventMessage(evt);
            intent.putExtra(TouchProfile.PARAM_TOUCH, touches);
            getPluginContext().sendEvent(intent, evt.getAccessToken());
        }
    }
    /**
     * タッチイベントを Bundle に変換します.
     *
     * @param event タッチイベント
     * @return 変換された Bundle
     */
    private Bundle convertEventToBundle(HostTouchEvent event) {
        return convertEventToBundle(event, false);
    }

    /**
     * タッチイベントを Bundle に変換します.
     *
     * @param event タッチイベント
     * @param hasState state の情報も含むかを指定
     * @return 変換された Bundle
     */
    private Bundle convertEventToBundle(HostTouchEvent event, boolean hasState) {
        List<Bundle> touchList = new ArrayList<>();
        Bundle touches = new Bundle();
        for (int n = 0; n < event.getCount(); n++) {
            Bundle touchData = new Bundle();
            touchData.putInt(TouchProfile.PARAM_ID, event.getId(n));
            touchData.putFloat(TouchProfile.PARAM_X, event.getX(n));
            touchData.putFloat(TouchProfile.PARAM_Y, event.getY(n));
            touchList.add((Bundle) touchData);
        }
        touches.putParcelableArray(TouchProfile.PARAM_TOUCHES, touchList.toArray(new Bundle[0]));
        if (hasState) {
            touches.putString("state", event.getState());
        }
        return touches;
    }

    /**
     * タッチイベントのステートに合わせた attribute 名を取得します.
     *
     * @param eventState イベントステート
     * @return attribute 名
     */
    private String getAttributeName(String eventState) {
        switch (eventState) {
            case HostTouchEvent.STATE_TOUCH_START:
                return ATTRIBUTE_ON_TOUCH_START;
            case HostTouchEvent.STATE_TOUCH_MOVE:
                return ATTRIBUTE_ON_TOUCH_MOVE;
            case HostTouchEvent.STATE_TOUCH_END:
                return ATTRIBUTE_ON_TOUCH_END;
            case HostTouchEvent.STATE_TOUCH_CANCEL:
                return ATTRIBUTE_ON_TOUCH_CANCEL;
            case HostTouchEvent.STATE_TOUCH_DOUBLE_TAP:
                return ATTRIBUTE_ON_DOUBLE_TAP;
            default:
                return "";
        }
    }

    /**
     * アトリビュート名からタッチイベントのステート名を取得します.
     *
     * @param attribute アトリビュート名
     * @return ステート名
     */
    private String getStateName(String attribute) {
        switch (attribute) {
            case ATTRIBUTE_ON_TOUCH_START:
                return HostTouchEvent.STATE_TOUCH_START;
            case ATTRIBUTE_ON_TOUCH_MOVE:
                return HostTouchEvent.STATE_TOUCH_MOVE;
            case ATTRIBUTE_ON_TOUCH_END:
                return HostTouchEvent.STATE_TOUCH_END;
            case ATTRIBUTE_ON_TOUCH_CANCEL:
                return HostTouchEvent.STATE_TOUCH_CANCEL;
            case ATTRIBUTE_ON_DOUBLE_TAP:
                return HostTouchEvent.STATE_TOUCH_DOUBLE_TAP;
            case ATTRIBUTE_ON_TOUCH_CHANGE:
                return HostTouchEvent.STATE_TOUCH_CHANGE;
            default:
                return "";
        }
    }

    /**
     * Execute Touch Profile Activity.
     */
    private void launchTouchProfileActivity() {
        if (!getApp().isClassnameOfTopActivity(TouchProfileActivity.class)) {
            Intent intent = new Intent();
            intent.setClass(getContext(), TouchProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getApp().isDeviceConnectClassOfTopActivity() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                getContext().startActivity(intent);
            } else {
                NotificationUtils.createNotificationChannel(getContext());
                NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, intent,
                        getContext().getString(R.string.host_notification_touch_warnning));
            }
        }
    }

    /**
     * Finish Touch Profile Activity.
     *
     */
    private void finishTouchProfileActivity() {
        if (getApp().isClassnameOfTopActivity(TouchProfileActivity.class)) {
            Intent intent = new Intent(HostTouchProfile.ACTION_FINISH_TOUCH_ACTIVITY);
            getContext().sendBroadcast(intent);
        }
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

    private HostDeviceApplication getApp() {
        return (HostDeviceApplication) getContext().getApplicationContext();
    }
}
