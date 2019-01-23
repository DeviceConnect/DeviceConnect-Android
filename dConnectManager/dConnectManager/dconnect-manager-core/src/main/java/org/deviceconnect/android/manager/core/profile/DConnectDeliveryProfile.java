/*
 DConnectDeliveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.profile;

import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectLocalOAuth;
import org.deviceconnect.android.manager.core.event.EventBroker;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.request.DConnectRequestManager;
import org.deviceconnect.android.manager.core.request.DeliveryRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.profile.SystemProfileConstants;

import java.util.List;

/**
 * 指定されたリクエストを各デバイスプラグインに送信するためのプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectDeliveryProfile extends DConnectProfile {
    /**
     * デバイスプラグイン管理クラス.
     */
    private final DevicePluginManager mDevicePluginManager;

    /**
     * リクエスト管理クラス.
     */
    private final DConnectRequestManager mRequestManager;

    /**
     * LocalOAuth管理クラス.
     */
    private final DConnectLocalOAuth mLocalOAuth;

    /**
     * イベントハンドラー.
     */
    private final EventBroker mEventBroker;

    /**
     * オリジン有効フラグ.
     */
    private final boolean mRequireOrigin;

    /**
     * コンストラクタ.
     * @param pluginManager デバイスプラグイン管理クラス
     * @param requestManager リクエスト管理クラス
     * @param auth LocalOAuth管理クラス
     * @param eventBroker イベントハンドラー
     * @param requireOrigin オリジン有効フラグ
     */
    public DConnectDeliveryProfile(final DevicePluginManager pluginManager, final DConnectRequestManager requestManager,
                                   final DConnectLocalOAuth auth,final EventBroker eventBroker, final boolean requireOrigin) {
        mDevicePluginManager = pluginManager;
        mRequestManager = requestManager;
        mLocalOAuth = auth;
        mEventBroker = eventBroker;
        mRequireOrigin = requireOrigin;
    }

    @Override
    public String getProfileName() {
        return "*";
    }

    @Override
    public boolean onRequest(final Intent request, final Intent response) {
        String profileName = getProfile(request);
        String serviceId = getServiceID(request);

        // TODO wakeup以外にも例外的な動きをするProfileがある場合には再検討すること。
        // System Profileのwakeupは例外的にpluginIdで宛先を決める
        // ここでは、/system/device/wakeupの場合のみpluginIdを使用するようにする
        if (DConnectSystemProfile.isWakeUpRequest(request)) {
            serviceId = request.getStringExtra(SystemProfileConstants.PARAM_PLUGIN_ID);
            if (serviceId == null) {
                MessageUtils.setInvalidRequestParameterError(response, "pluginId is required.");
                return true;
            }
        }

        if (serviceId == null) {
            MessageUtils.setInvalidRequestParameterError(response, "pluginId is required.");
        } else {
            List<DevicePlugin> plugins = mDevicePluginManager.getDevicePlugins(serviceId);
            if (plugins != null && !plugins.isEmpty()) {
                DevicePlugin plugin = plugins.get(0);
                mEventBroker.parseEventSession(request, plugin);

                DeliveryRequest req = new DeliveryRequest(mEventBroker);
                req.setContext(getContext());
                req.setLocalOAuth(mLocalOAuth);
                req.setUseAccessToken(isUseLocalOAuth(profileName));
                req.setRequireOrigin(mRequireOrigin);
                req.setRequest(request);
                req.setDevicePluginManager(mDevicePluginManager);
                req.setDestination(plugin);
                req.setOnResponseCallback(this::sendResponse);
                mRequestManager.addRequest(req);
                return false;
            } else {
                MessageUtils.setNotFoundServiceError(response);
            }
        }

        return true;
    }

    /**
     * Local OAuthの使用フラグをチェックする.
     * @param profileName プロファイル名
     * @return 使用する場合はtrue,使用しない場合はfalse
     */
    private boolean isUseLocalOAuth(final String profileName) {
        return !mLocalOAuth.checkProfile(profileName);
    }
}
