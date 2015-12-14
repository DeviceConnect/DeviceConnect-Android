/*
 DConnectDeliveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.profile;

import java.util.List;

import org.deviceconnect.android.manager.DConnectLocalOAuth;
import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.DevicePluginManager;
import org.deviceconnect.android.manager.request.DeliveryRequest;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.profile.SystemProfileConstants;

import android.content.Intent;

/**
 * 指定されたリクエストを各デバイスプラグインに送信するためのプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class DConnectDeliveryProfile extends DConnectProfile {
    /** デバイスプラグイン管理クラス. */
    private DevicePluginManager mDevicePluginManager;

    /** LocalOAuth管理クラス. */
    private DConnectLocalOAuth mLocalOAuth;

    /** オリジン有効フラグ. */
    private final boolean mRequireOrigin;

    /**
     * コンストラクタ.
     * @param mgr デバイスプラグイン管理クラス
     * @param auth LocalOAuth管理クラス
     * @param requireOrigin オリジン有効フラグ
     */
    public DConnectDeliveryProfile(final DevicePluginManager mgr, final DConnectLocalOAuth auth,
                                   final boolean requireOrigin) {
        mDevicePluginManager = mgr;
        mLocalOAuth = auth;
        mRequireOrigin = requireOrigin;
    }

    @Override
    public String getProfileName() {
        return "*";
    }

    @Override
    public boolean onRequest(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);

        // TODO wakeup以外にも例外的な動きをするProfileがある場合には再検討すること。
        // System Profileのwakeupは例外的にpluginIdで宛先を決める
        // ここでは、/system/device/wakeupの場合のみpluginIdを使用するようにする
        String profileName = getProfile(request);
        if (SystemProfileConstants.PROFILE_NAME.equals(profileName)) {
            String inter = getInterface(request);
            String attr = getAttribute(request);
            if (SystemProfileConstants.INTERFACE_DEVICE.equals(inter)
                    && SystemProfileConstants.ATTRIBUTE_WAKEUP.equals(attr)) {
                serviceId = request.getStringExtra(SystemProfileConstants.PARAM_PLUGIN_ID);
                if (serviceId == null) {
                    sendEmptyPluginId(request, response);
                    return true;
                }
            }
        }

        if (serviceId == null) {
            sendEmptyServiceId(request, response);
        } else {
            List<DevicePlugin> plugins = mDevicePluginManager.getDevicePlugins(serviceId);
            if (plugins != null && plugins.size() > 0) {
                DeliveryRequest req = new DeliveryRequest();
                req.setContext(getContext());
                req.setLocalOAuth(mLocalOAuth);
                req.setUseAccessToken(isUseLocalOAuth(profileName));
                req.setRequireOrigin(mRequireOrigin);
                req.setRequest(request);
                req.setDevicePluginManager(mDevicePluginManager);
                req.setDestination(plugins.get(0));
                ((DConnectMessageService) getContext()).addRequest(req);
            } else {
                sendNotFoundService(request, response);
            }
        }

        return true;
    }

    /**
     * 送信元のリクエストにserviceIdがnullの場合のエラーを返却する.
     * @param request 送信元のリクエスト
     * @param response レスポンス
     */
    private void sendEmptyServiceId(final Intent request, final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
        sendResponse(request, response);
    }

    /**
     * 送信元のリクエストにデバイスプラグインが発見できなかったエラーを返却する.
     * @param request 送信元のリクエスト
     * @param response レスポンス
     */
    private void sendNotFoundService(final Intent request, final Intent response) {
        MessageUtils.setNotFoundServiceError(response);
        sendResponse(request, response);
    }

    /**
     * 送信元のリクエストにプラグインIDが発見できなかったエラーを返却する.
     * @param request 送信元のリクエスト
     * @param response レスポンス
     */
    private void sendEmptyPluginId(final Intent request, final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, "pluginId is required.");
        sendResponse(request, response);
    }

    /**
     * リクエストの送信元にレスポンスを返却する.
     * @param request 送信元のリクエスト
     * @param response 返却するレスポンス
     */
    public final void sendResponse(final Intent request, final Intent response) {
        ((DConnectService) getContext()).sendResponse(request, response);
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
