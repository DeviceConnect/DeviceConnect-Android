/*
 HostKeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.activity.KeyEventProfileActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Key Event Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostKeyEventProfile extends KeyEventProfile {

    /** Error. */
    private static final int ERROR_PROCESSING_ERROR = 100;

    /** Key Event profile event management flag. */
    private static int sFlagKeyEventEventManage = 0;
    /** Key Event profile event flag. (ondown) */
    private static final int FLAG_ON_DOWN = 0x0001;
    /** Key Event  profile event flag. (onup) */
    private static final int FLAG_ON_UP = 0x0002;

    /** Finish key event profile activity action. */
    public static final String ACTION_FINISH_KEYEVENT_ACTIVITY =
            "org.deviceconnect.android.deviceplugin.host.keyevent.FINISH";

    @Override
    protected boolean onGetOnDown(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle keyevent = ((HostDeviceService) getContext()).getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_DOWN);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onGetOnUp(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
        } else {
            Bundle keyevent = ((HostDeviceService) getContext()).getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_UP);
            if (keyevent == null) {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
            } else {
                response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
            }
            setResult(response, IntentDConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnDown(final Intent request, final Intent response, final String serviceId,
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
                execKeyEventActivity(serviceId);
                setKeyEventEventFlag(FLAG_ON_DOWN);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onPutOnUp(final Intent request, final Intent response, final String serviceId,
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
                execKeyEventActivity(serviceId);
                setKeyEventEventFlag(FLAG_ON_UP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not register event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDown(final Intent request, final Intent response, final String serviceId,
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
                resetKeyEventEventFlag(FLAG_ON_DOWN);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnUp(final Intent request, final Intent response, final String serviceId,
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
                resetKeyEventEventFlag(FLAG_ON_UP);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
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
            this.getContext().startActivity(mIntent);
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
     * Check deviceID.
     * 
     * @param serviceId service ID.
     * @return If <code>serviceId</code> is equal to test for deviceId, true.
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
