/*
 HostFileDescriptorProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.FileDescriptorProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;

/**
 * FileDescriptorプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostFileDescriptorProfile extends FileDescriptorProfile {

    /** Error. */
    private static final int ERROR_VALUE_IS_NULL = 100;

    @Override
    protected boolean onGetOpen(final Intent request, final Intent response, final String serviceId, final String path,
            final Flag flag) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (path == null || flag == Flag.UNKNOWN) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else if (path.trim().equals("") || path.trim().equals("/")) {
            MessageUtils.setUnknownError(response, "not found:" + path);
        } else {
            ((HostDeviceService) getContext()).openFile(response, serviceId, path, flag);
            return false;
        }
        return true;
    }

    @Override
    protected boolean onGetRead(final Intent request, final Intent response, final String serviceId, final String path,
            final Long length, final Long position) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (path == null || length == null || length < 0 || (position != null && position < 0)) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            long pos = 0;
            if (position != null) {
                pos = position.longValue();
            }
            ((HostDeviceService) getContext()).readFile(response, serviceId, path, pos, length);
            return false;
        }
        return true;
    }

    @Override
    protected boolean onPutClose(final Intent request, final Intent response,
            final String serviceId, final String path) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (path == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {
            ((HostDeviceService) getContext()).closeFile(response, serviceId, path);
            return false;
        }
        return true;
    }

    @Override
    protected boolean onPutWrite(final Intent request, final Intent response, final String serviceId, final String path,
            final byte[] data, final Long position) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (path == null || data == null || (position != null && position < 0)) {
            String d = "";
            if (data != null) {
                d = new String(data, 0, data.length);
            }
            MessageUtils.setInvalidRequestParameterError(response, "path=" + path + " , data="
                   + d + ", position="
                    + position);
        } else {
            ((HostDeviceService) getContext()).writeDataToFile(response, serviceId, path, data, position);
            return false;
        }
        return true;
    }

    @Override
    protected boolean onPutOnWatchFile(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {

            // イベントの登録
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                ((HostDeviceService) getContext()).registerFileDescriptorOnWatchfileEvent(serviceId);
                return true;
            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not register event.");
                return true;
            }

        }
        return true;
    }

    @Override
    protected boolean onDeleteOnWatchFile(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {

        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response);
        } else {

            // イベントの解除
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
                ((HostDeviceService) getContext()).unregisterFileDescriptorOnWatchfileEvent();
                return true;

            } else {
                MessageUtils.setError(response, ERROR_VALUE_IS_NULL, "Can not unregister event.");
                return true;

            }

        }
        return true;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return HostNetworkServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response, "Service ID is empty.");
    }

    /**
     * サービスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundDevice(final Intent response) {
        MessageUtils.setNotFoundDeviceError(response, "Device is not found.");
    }

}
