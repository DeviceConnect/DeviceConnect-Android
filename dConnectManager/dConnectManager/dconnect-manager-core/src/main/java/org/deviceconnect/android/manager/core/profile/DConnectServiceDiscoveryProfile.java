/*
 DConnectServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.profile;

import android.content.Intent;

import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.request.DConnectRequestManager;
import org.deviceconnect.android.manager.core.request.ServiceDiscoveryRequest;
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
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * デバイスプラグイン管理クラス.
     */
    private DevicePluginManager mDevicePluginManager;

    /**
     * リクエスト管理クラス.
     */
    private DConnectRequestManager mRequestManager;

    /**
     * コンストラクタ.
     *
     * @param provider プロファイルプロバイダ
     * @param pluginManager デバイスプラグイン管理クラス
     * @param requestManager リクエスト管理クラス
     */
    public DConnectServiceDiscoveryProfile(final DConnectServiceProvider provider,
                                           final DevicePluginManager pluginManager,
                                           final DConnectRequestManager requestManager) {
        super(provider);
        mDevicePluginManager = pluginManager;
        mRequestManager = requestManager;
        addApi(mGetRequest);
        addApi(mPutRequest);
        addApi(mDeleteRequest);
    }

    /**
     * GET /gotapi/serviceDiscovery.
     */
    private final DConnectApi mGetRequest = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            ServiceDiscoveryRequest req = new ServiceDiscoveryRequest(mRequestManager);
            req.setContext(getContext());
            req.setRequest(request);
            req.setTimeout(ServiceDiscoveryRequest.TIMEOUT);
            req.setDevicePluginManager(mDevicePluginManager);
            req.setOnResponseCallback((resp) -> sendResponse(resp));
            new Thread(req::run).start();
            return false;
        }
    };

    /**
     * PUT /gotapi/onServiceChange.
     */
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

    /**
     * DELETE /gotapi/onServiceChange.
     */
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
