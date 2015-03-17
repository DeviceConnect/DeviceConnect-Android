/*
 HostTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.activity.TouchProfileActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Touch Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostTouchProfile extends TouchProfile {

    /** Error. */
    private static final int ERROR_PROCESSING_ERROR = 100;

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

    /** Finish touch profile activity action. */
    public static final String ACTION_FINISH_TOUCH_ACTIVITY =
            "org.deviceconnect.android.deviceplugin.host.touch.FINISH";

    @Override
    protected boolean onGetOnTouch(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle touches = ((HostDeviceService) getContext()).getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchStart(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle touches = ((HostDeviceService) getContext()).getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_START);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchEnd(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle touches = ((HostDeviceService) getContext()).getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_END);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnDoubleTap(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle touches = ((HostDeviceService) getContext()).getTouchCache(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchMove(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle touches = ((HostDeviceService) getContext()).getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnTouchCancel(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle touches = ((HostDeviceService) getContext()).getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL);
            if (touches == null) {
                response.putExtra(TouchProfile.PARAM_TOUCH, "");
            } else {
                response.putExtra(TouchProfile.PARAM_TOUCH, touches);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                setTouchEventFlag(FLAG_ON_TOUCH);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                setTouchEventFlag(FLAG_ON_TOUCH_START);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                setTouchEventFlag(FLAG_ON_TOUCH_END);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                setTouchEventFlag(FLAG_ON_DOUBLE_TAP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                setTouchEventFlag(FLAG_ON_TOUCH_MOVE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event registration.
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                execTouchProfileActivity(serviceId);
                setTouchEventFlag(FLAG_ON_TOUCH_CANCEL);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_START);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_END);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_DOUBLE_TAP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_MOVE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            // Event release.
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                resetTouchEventFlag(FLAG_ON_TOUCH_CANCEL);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    /**
     * Execute Touch Profile Activity.
     * 
     * @param serviceId service ID.
     * @return Always true.
     */
    private boolean execTouchProfileActivity(final String serviceId) {
        ActivityManager mActivityManager = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String mClassName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();

        if (!(TouchProfileActivity.class.getName().equals(mClassName))) {
            Intent mIntent = new Intent();
            mIntent.setClass(getContext(), TouchProfileActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
            this.getContext().startActivity(mIntent);
        }
        return true;
    }

    /**
     * Finish Touch Profile Activity.
     * 
     * @return Always true.
     */
    private boolean finishTouchProfileActivity() {
        String className = getClassnameOfTopActivity();
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
     * Check serviceID.
     * 
     * @param serviceId service ID.
     * @return If <code>serviceId</code> is equal to test for serviceId, true.
     *         Otherwise false.
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern mPattern = Pattern.compile(regex);
        Matcher match = mPattern.matcher(serviceId);
        return match.find();
    }

    /**
     * Creates an error of "serviceId is empty".
     * 
     * @param response Intent to store the response.
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

    /**
     * Creates an error of "sessionKey is empty".
     * 
     * @param response Intent to store the response.
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "SessionKey not found");
    }

    /**
     * Creates an error of "service not found".
     * 
     * @param response Intent to store the response.
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
    }

    /**
     * Get the class name of the Activity being displayed at the top of the screen.
     * 
     * @return class name.
     */
    private String getClassnameOfTopActivity() {
        ActivityManager activitMgr = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String className = activitMgr.getRunningTasks(1).get(0).topActivity.getClassName();
        return className;
    }
}
