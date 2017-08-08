/*
 CompatibleRequestConverter.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.util.VersionName;
import org.deviceconnect.android.profile.DConnectProfile;

import java.util.List;


/**
 * リクエストパスの互換性を担保するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class CompatibleRequestConverter implements MessageConverter {

    private static final VersionName OLD_SDK = VersionName.parse("1.0.0");

    private final MessageConverter mNewPathConverter = new NewPathConverter();

    private final MessageConverter mOldPathConverter = new OldPathConverter();

    private final DevicePluginManager mPluginMgr;

    public CompatibleRequestConverter(final DevicePluginManager pluginMgr) {
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
            if (OLD_SDK.equals(plugin.getPluginSdkVersionName())) {
                mOldPathConverter.convert(request);
            } else {
                mNewPathConverter.convert(request);
            }
        }
    }
}
