/*
 DConnectServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.request.ServiceDiscoveryRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.content.Intent;

/**
 * Service Discovery プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class DConnectServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * タイムアウト時間を定義. (8秒)
     */
    private static final int TIMEOUT = 8000;

    /** デバイスプラグイン管理クラス. */
    private DevicePluginManager mDevicePluginManager;

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     * @param mgr デバイスプラグイン管理クラス
     */
    public DConnectServiceDiscoveryProfile(final DConnectProfileProvider provider,
            final DevicePluginManager mgr) {
        super(provider);
        mDevicePluginManager = mgr;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String inter = getInterface(request);
        String attribute = getAttribute(request);
        if (inter == null && attribute == null) {
            return onGetServices(request, response);
        } else {
            MessageUtils.setUnknownAttributeError(response);
            ((DConnectService) getContext()).sendResponse(request, response);
            return true;
        }
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        ServiceDiscoveryRequest req = new ServiceDiscoveryRequest();
        req.setContext(getContext());
        req.setRequest(request);
        req.setTimeout(TIMEOUT);
        req.setDevicePluginManager(mDevicePluginManager);
        ((DConnectMessageService) getContext()).addRequest(req);

        // 各デバイスプラグインに送信する場合にはfalseを返却、
        // dConnectManagerで止める場合にはtrueを返却する
        // ここでは、各デバイスには渡さないのでtrueを返却する。
        return true;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        MessageUtils.setNotSupportActionError(response);
        ((DConnectService) getContext()).sendResponse(request, response);

        // 各デバイスプラグインに送信する場合にはfalseを返却、
        // dConnectManagerで止める場合にはtrueを返却する
        // ここでは、各デバイスには渡さないのでtrueを返却する。
        return true;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (ATTRIBUTE_ON_SERVICE_CHANGE.equals(attribute)) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
            }
        } else {
            MessageUtils.setNotSupportAttributeError(response);
        }
        ((DConnectService) getContext()).sendResponse(request, response);

        // 各デバイスプラグインに送信する場合にはfalseを返却、
        // dConnectManagerで止める場合にはtrueを返却する
        // ここでは、各デバイスには渡さないのでtrueを返却する。
        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        if (ATTRIBUTE_ON_SERVICE_CHANGE.equals(attribute)) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
            }
        } else {
            MessageUtils.setNotSupportAttributeError(response);
        }
        ((DConnectService) getContext()).sendResponse(request, response);

        // 各デバイスプラグインに送信する場合にはfalseを返却、
        // dConnectManagerで止める場合にはtrueを返却する
        // ここでは、各デバイスには渡さないのでtrueを返却する。
        return true;
    }
}
