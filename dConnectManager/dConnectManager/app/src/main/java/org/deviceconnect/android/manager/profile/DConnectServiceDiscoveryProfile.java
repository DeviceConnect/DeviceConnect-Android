/*
 DConnectServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import android.content.Intent;

import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.request.ServiceDiscoveryRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

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
    public DConnectServiceDiscoveryProfile(final DConnectServiceProvider provider,
            final DevicePluginManager mgr) {
        super(provider);
        mDevicePluginManager = mgr;
        addApi(mGetRequest);
        addApi(mPutRequest);
        addApi(mDeleteRequest);
    }

    private final DConnectApi mGetRequest = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ServiceDiscoveryRequest req = new ServiceDiscoveryRequest();
            req.setContext(getContext());
            req.setRequest(request);
            req.setTimeout(TIMEOUT);
            req.setDevicePluginManager(mDevicePluginManager);
            ((DConnectMessageService) getContext()).addRequest(req);
            return false;
        }
    };

    private final DConnectApi mPutRequest = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SERVICE_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteRequest = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_SERVICE_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response);
            }
            return true;
        }
    };
}
