/*
 DevicePluginXml.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;


import java.util.Map;

/**
 * DevicePlugin.xmlのルート要素.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginXml {

    /** API仕様定義ファイルを格納するディレクトリへのパス. */
    String mSpecPath;

    /** プラグインのサポートするプロファイルリストの宣言. */
    Map<String, DevicePluginXmlProfile> mSupportedProfiles;

    /**
     * API仕様定義ファイルを格納するディレクトリへのパスを取得する.
     * @return API仕様定義ファイルを格納するディレクトリへのパス
     */
    public String getSpecPath() {
        return mSpecPath;
    }

    /**
     * プラグインのサポートするプロファイルリストの宣言を取得する.
     * @return プラグインのサポートするプロファイルリストの宣言
     */
    public Map<String, DevicePluginXmlProfile> getSupportedProfiles() {
        return mSupportedProfiles;
    }
}
