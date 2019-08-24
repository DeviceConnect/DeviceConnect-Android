/*
 CompatibleRequestConverter.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;

import java.util.List;


/**
 * リクエストパスの互換性を担保するクラス.
 * <p>
 * プラグイン SDK の 1.0.0 の前後でパスの仕様が変更されたので、互換性のために変換処理を行います。
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class CompatibleRequestConverter implements MessageConverter {

    /**
     * プラグイン SDK の変換を切り替える閾値.
     */
    private static final VersionName OLD_SDK = VersionName.parse("1.0.0");

    /**
     * 1.0.0以降のプラグインSDKを使用している場合の変換を行う.
     */
    private final MessageConverter mNewPathConverter = new NewPathConverter();

    /**
     * 1.0.0以前のプラグインSDKを使用している場合の変換を行う.
     */
    private final MessageConverter mOldPathConverter = new OldPathConverter();

    /**
     * プラグイン管理クラス.
     */
    private final DevicePluginManager mPluginMgr;

    CompatibleRequestConverter(final DevicePluginManager pluginMgr) {
        if (pluginMgr == null) {
            throw new NullPointerException("pluginMgr is null");
        }
        mPluginMgr = pluginMgr;
    }

    @Override
    public void convert(final Intent request) {
        if (request == null) {
            return;
        }
        List<DevicePlugin> plugins = mPluginMgr.getDevicePlugins(DConnectProfile.getServiceID(request));
        if (plugins != null && plugins.size() > 0) {
            DevicePlugin plugin = plugins.get(0);
            // プラグイン SDK のバージョンによってパスの変換を行う
            if (OLD_SDK.equals(plugin.getPluginSdkVersionName())) {
                mOldPathConverter.convert(request);
            } else {
                mNewPathConverter.convert(request);
            }
        }
    }
}
