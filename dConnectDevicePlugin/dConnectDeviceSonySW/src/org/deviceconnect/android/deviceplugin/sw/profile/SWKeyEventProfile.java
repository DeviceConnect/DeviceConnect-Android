/*
 SWKeyEventProfile.java
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
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.message.DConnectMessage;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

/**
 * SonySW device plugin {@link KeyEventProfile} implementation.
 * 
 * @author NTT DOCOMO, INC.
 */
public class SWKeyEventProfile extends KeyEventProfile {
    
    @Override
    protected boolean onGetOnDown(final Intent request, final Intent response, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        if (SWUtil.toHostAppPackageName(device.getName()).equals(SWConstants.PACKAGE_SMART_WATCH)) {
            // SW not support "KeyEvent Profile".
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        Bundle keyevent = SWApplication.getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_DOWN);
        if (keyevent == null) {
            response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
        } else {
            response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onGetOnUp(final Intent request, final Intent response, final String serviceId) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }
        
        if (SWUtil.toHostAppPackageName(device.getName()).equals(SWConstants.PACKAGE_SMART_WATCH)) {
            // SW not support "KeyEvent Profile".
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        Bundle keyevent = SWApplication.getKeyEventCache(KeyEventProfile.ATTRIBUTE_ON_UP);
        if (keyevent == null) {
            response.putExtra(KeyEventProfile.PARAM_KEYEVENT, "");
        } else {
            response.putExtra(KeyEventProfile.PARAM_KEYEVENT, keyevent);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        if (SWUtil.toHostAppPackageName(device.getName()).equals(SWConstants.PACKAGE_SMART_WATCH)) {
            // SW not support "KeyEvent Profile".
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            displayKeyEventScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onPutOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        if (SWUtil.toHostAppPackageName(device.getName()).equals(SWConstants.PACKAGE_SMART_WATCH)) {
            // SW not support "KeyEvent Profile".
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            displayKeyEventScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnDown(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        if (SWUtil.toHostAppPackageName(device.getName()).equals(SWConstants.PACKAGE_SMART_WATCH)) {
            // SW not support "KeyEvent Profile".
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            clearKeyEventScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnUp(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        BluetoothDevice device = SWUtil.findSmartWatch(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response, "No service is found: " + serviceId);
            return true;
        }

        if (SWUtil.toHostAppPackageName(device.getName()).equals(SWConstants.PACKAGE_SMART_WATCH)) {
            // SW not support "KeyEvent Profile".
            MessageUtils.setNotSupportProfileError(response);
            return true;
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
            clearKeyEventScreen(SWUtil.toHostAppPackageName(device.getName()), serviceId);
        } else if (error == EventError.INVALID_PARAMETER) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            MessageUtils.setUnknownError(response);
        }
        return true;
    }

    /**
     * Display Key Event screen.
     * 
     * @param deviceName Device Name
     * @param serviceId serviceID
     */
    protected void displayKeyEventScreen(final String deviceName, final String serviceId) {
        Intent intent = new Intent(Control.Intents.CONTROL_PROCESS_LAYOUT_INTENT);
        if (SWConstants.PACKAGE_SMART_WATCH_2.equals(deviceName)) {
            intent.putExtra(Control.Intents.EXTRA_DATA_XML_LAYOUT, R.layout.keyevent_control);
        } else {
            return; // This function not implemented. Because SW could not redraw xml layout data.
        }
        sendToHostApp(intent, serviceId);
    }

    /**
     * Clear KeyEvent screen.
     * 
     * @param deviceName Device Name
     * @param serviceId serviceID
     */
    protected void clearKeyEventScreen(final String deviceName, final String serviceId) {
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
