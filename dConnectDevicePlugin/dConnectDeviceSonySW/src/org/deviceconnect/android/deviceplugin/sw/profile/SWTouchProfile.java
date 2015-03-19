/*
 SWTouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw.profile;

import org.deviceconnect.android.deviceplugin.sw.R;
import org.deviceconnect.android.deviceplugin.sw.SWApplication;
import org.deviceconnect.android.deviceplugin.sw.SWConstants;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

/**
 * SonySW device plug-in {@link KeyEventProfile} implementation.
 * 
 * @author NTT DOCOMO, INC.
 */
public class SWTouchProfile extends TouchProfile {

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

    @Override
    protected boolean onGetOnTouch(final Intent request, final Intent response, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH);
        if (touches == null) {
            response.putExtra(TouchProfile.PARAM_TOUCH, "");
        } else {
            response.putExtra(TouchProfile.PARAM_TOUCH, touches);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onGetOnTouchStart(final Intent request, final Intent response, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_START);
        if (touches == null) {
            response.putExtra(TouchProfile.PARAM_TOUCH, "");
        } else {
            response.putExtra(TouchProfile.PARAM_TOUCH, touches);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onGetOnTouchEnd(final Intent request, final Intent response, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_TOUCH_END);
        if (touches == null) {
            response.putExtra(TouchProfile.PARAM_TOUCH, "");
        } else {
            response.putExtra(TouchProfile.PARAM_TOUCH, touches);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onGetOnDoubleTap(final Intent request, final Intent response, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        Bundle touches = SWApplication.getTouchCache(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP);
        if (touches == null) {
            response.putExtra(TouchProfile.PARAM_TOUCH, "");
        } else {
            response.putExtra(TouchProfile.PARAM_TOUCH, touches);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onGetOnTouchMove(final Intent request, final Intent response, final String serviceId) {
        // SW not support "TouchMove".
        MessageUtils.setNotSupportAttributeError(response);
        return true;
    }

    @Override
    protected boolean onGetOnTouchCancel(final Intent request, final Intent response, final String serviceId) {
        // SW not support "TouchCancel".
        MessageUtils.setNotSupportAttributeError(response);
        return true;
    }

    @Override
    protected boolean onPutOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            displayTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            setTouchEventFlag(FLAG_ON_TOUCH);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            displayTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            setTouchEventFlag(FLAG_ON_TOUCH_START);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            displayTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            setTouchEventFlag(FLAG_ON_TOUCH_END);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onPutOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            displayTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            setTouchEventFlag(FLAG_ON_DOUBLE_TAP);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onPutOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        // SW not support "TouchMove".
        MessageUtils.setNotSupportAttributeError(response);
        return true;
    }

    @Override
    protected boolean onPutOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        // SW not support "TouchCancel".
        MessageUtils.setNotSupportAttributeError(response);
        return true;
    }

    @Override
    protected boolean onDeleteOnTouch(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            if (!(resetTouchEventFlag(FLAG_ON_TOUCH))) {
                clearTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            }
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchStart(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            if (!(resetTouchEventFlag(FLAG_ON_TOUCH_START))) {
                clearTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            }
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchEnd(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            if (!(resetTouchEventFlag(FLAG_ON_TOUCH_END))) {
                clearTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            }
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDoubleTap(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            if (!(resetTouchEventFlag(FLAG_ON_DOUBLE_TAP))) {
                clearTouchScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
            }
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchMove(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        // SW not support "TouchMove".
        MessageUtils.setNotSupportAttributeError(response);
        return true;
    }

    @Override
    protected boolean onDeleteOnTouchCancel(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        // SW not support "TouchCancel".
        MessageUtils.setNotSupportAttributeError(response);
        return true;
    }

    /**
     * Display Touch screen.
     * 
     * @param deviceName Device Name
     * @param serviceId serviceID
     */
    protected void displayTouchScreen(final String deviceName, final String serviceId) {
        Intent intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
        if (SWConstants.PACKAGE_SMART_WATCH_2.equals(deviceName)) {
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.touch_control_sw2);
        } else {
            return; // This function not implemented. Because SW could not redraw xml layout data.
        }
        sendToHostApp(intent, serviceId);
    }

    /**
     * Clear Touch screen.
     * 
     * @param deviceName Device Name
     * @param serviceId serviceID
     */
    protected void clearTouchScreen(final String deviceName, final String serviceId) {
        if (SWConstants.PACKAGE_SMART_WATCH_2.equals(deviceName)) {
            Intent intent = new Intent(Control.Intents.CONTROL_CLEAR_DISPLAY_INTENT);
            sendToHostApp(intent, serviceId);
            intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.touch_clear_control_sw2);
            sendToHostApp(intent, serviceId);
        } else  {
            return; // This function not implemented. Because SW could not redraw xml layout data.
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
     * @return true : Other event register. false : No event registration.
     */
    private boolean resetTouchEventFlag(final int flag) {
        sFlagTouchEventManage &= ~(flag);
        if (sFlagTouchEventManage == 0) {
            return false;
        }
        return true;
    }

    /**
     * Send intent to Host application.
     * 
     * @param intent Intent.
     * @param serviceId ServiceID
     */
    protected void sendToHostApp(final Intent intent, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        String deviceName = device.getName();
        intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, getContext().getPackageName());
        intent.setPackage(SWUtil.toHostAppPackageName(deviceName));
        getContext().sendBroadcast(intent, Registration.HOSTAPP_PERMISSION);
    }
}
