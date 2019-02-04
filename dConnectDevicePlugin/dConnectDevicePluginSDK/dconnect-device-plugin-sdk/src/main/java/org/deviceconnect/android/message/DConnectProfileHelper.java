/*
 DConnectProfileHelper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.content.Context;
import android.content.res.AssetManager;

import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * プロファイルのスペックを読み込むためのヘルパークラス.
 *
 * @author NTT DOCOMO, INC.
 */
final class DConnectProfileHelper {

    /**
     * プロファイル仕様定義ファイルの拡張子.
     */
    private static final String SPEC_FILE_EXTENSION = ".json";

    /**
     * コンストラクタ.
     * このクラスはインスタンスを作成させないのでprivate.
     */
    private DConnectProfileHelper() {}

    /**
     * プラグインが持っているプロファイル仕様を取得します.
     *
     * @return DConnectPluginSpec のインスタンス
     */
    static DConnectPluginSpec loadPluginSpec(final Context context, final Map<String, DevicePluginXmlProfile> supportedProfiles) {
        if (supportedProfiles == null) {
            return null;
        }

        final AssetManager assets = context.getAssets();
        final DConnectPluginSpec pluginSpec = new DConnectPluginSpec();
        for (Map.Entry<String, DevicePluginXmlProfile> entry : supportedProfiles.entrySet()) {
            String profileName = entry.getKey();
            DevicePluginXmlProfile profile = entry.getValue();
            try {
                List<String> dirList = new ArrayList<>();
                String assetsPath = profile.getSpecPath();
                if (assetsPath != null) {
                    dirList.add(assetsPath);
                }
                dirList.add("api");
                String filePath = null;
                for (String dir : dirList) {
                    String[] fileNames = assets.list(dir);
                    String fileName = findProfileSpecName(fileNames, profileName);
                    if (fileName != null) {
                        filePath = dir + "/" + fileName;
                        break;
                    }
                }
                if (filePath == null) {
                    throw new RuntimeException("Profile spec is not found: " + profileName);
                }
                pluginSpec.addProfileSpec(profileName.toLowerCase(), assets.open(filePath));
            } catch (IOException | JSONException e) {
                throw new RuntimeException("Failed to load a profile spec: " + profileName, e);
            }
        }
        return pluginSpec;
    }

    /**
     * プロファイル仕様のファイル名を取得します.
     * <p>
     * プロファイル名が見つからない場合は、nullを返却します。
     * </p>
     * @param fileNames ファイル一覧
     * @param profileName ファイル名
     * @return ファイル名
     */
    private static String findProfileSpecName(final String[] fileNames, final String profileName) {
        if (fileNames == null) {
            return null;
        }
        for (String fileFullName : fileNames) {
            if (!fileFullName.endsWith(SPEC_FILE_EXTENSION)) {
                continue;
            }
            String fileName = fileFullName.substring(0,
                    fileFullName.length() - SPEC_FILE_EXTENSION.length());
            if (fileName.equalsIgnoreCase(profileName)) {
                return fileFullName;
            }
        }
        return null;
    }
}
