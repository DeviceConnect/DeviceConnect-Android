/*
 HostTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.activity.TouchActivity;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;

/**
 * Touch Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostTouchProfile extends TouchProfile {

    /** Error. */
    private static final int ERROR_PROCESSING_ERROR = 100;

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
                execTouchActivity(serviceId);
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
                execTouchActivity(serviceId);
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
                execTouchActivity(serviceId);
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
                execTouchActivity(serviceId);
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
                execTouchActivity(serviceId);
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
                execTouchActivity(serviceId);
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
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setError(response, ERROR_PROCESSING_ERROR, "Can not unregister event.");
            }
        }
        return true;
    }

    /**
     * Execute Touch Activity.
     * 
     * @param serviceId service ID.
     * @return Always true.
     */
    private boolean execTouchActivity(final String serviceId) {
        ActivityManager mActivityManager = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String mClassName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();

        if (!(TouchActivity.class.getName().equals(mClassName))) {
            Intent mIntent = new Intent();
            mIntent.setClass(getContext(), TouchActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, serviceId);
            this.getContext().startActivity(mIntent);
        }
        return true;
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
}
