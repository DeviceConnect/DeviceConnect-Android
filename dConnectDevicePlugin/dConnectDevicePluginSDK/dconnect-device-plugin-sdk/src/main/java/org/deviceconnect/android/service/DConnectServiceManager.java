/*
 DConnectServiceManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.service;

import android.content.Context;
import android.content.res.AssetManager;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Device Connect APIサービス管理インターフェースのデフォルト実装.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectServiceManager implements DConnectServiceProvider, DConnectService.OnStatusChangeListener {
    /**
     * プラグインコンテキスト.
     */
    private DevicePluginContext mPluginContext;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * デバイスプラグインが持っているサービスリスト.
     */
    private final Map<String, DConnectService> mDConnectServices
            = Collections.synchronizedMap(new HashMap<>());

    /**
     * サービス通知リスナーリスト.
     */
    private final List<DConnectServiceListener> mServiceListeners
            = Collections.synchronizedList(new ArrayList<>());

    /**
     * プラグインコンテキストを取得します.
     *
     * @return プラグインコンテキスト
     */
    public DevicePluginContext getPluginContext() {
        return mPluginContext;
    }

    /**
     * プラグインコンテキストを設定します.
     *
     * @param pluginContext プラグインコンテキスト
     */
    public void setPluginContext(final DevicePluginContext pluginContext) {
        mPluginContext = pluginContext;
    }

    /**
     * コンテキストを取得する.
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * コンテキストを設定する.
     * @param context コンテキスト
     */
    public void setContext(final Context context) {
        mContext = context;
    }

    // DConnectServiceProvider Implements

    @Override
    public void addService(final DConnectService service) {
        service.setOnStatusChangeListener(this);
        service.setContext(getContext());
        service.setPluginContext(getPluginContext());

        // 既にサービスに登録されているプロファイルにコンテキストなどを設定
        DConnectPluginSpec spec = new DConnectPluginSpec();
        for (DConnectProfile profile : service.getProfileList()) {
            profile.setContext(getContext());
            profile.setPluginContext(getPluginContext());
            profile.setResponder(getPluginContext());

            // プロファイルの定義ファイルを読み込み
            String profileName = profile.getProfileName();
            try {
                spec.addProfileSpec(profileName, openApiSpec(profileName));
            } catch (IOException | JSONException e) {
                // ignore.
            }
        }
        service.setPluginSpec(spec);
        mDConnectServices.put(service.getId(), service);

        notifyOnServiceAdded(service);
    }

    @Override
    public boolean removeService(final DConnectService service) {
        return removeService(service.getId()) != null;
    }

    @Override
    public DConnectService removeService(final String serviceId) {
        if (serviceId == null) {
            return null;
        }
        DConnectService removed = mDConnectServices.remove(serviceId);
        if (removed != null) {
            notifyOnServiceRemoved(removed);
        }
        return removed;
    }

    @Override
    public DConnectService getService(final String serviceId) {
        if (serviceId == null) {
            return null;
        }
        return mDConnectServices.get(serviceId);
    }

    @Override
    public List<DConnectService> getServiceList() {
        return new ArrayList<>(mDConnectServices.values());
    }

    @Override
    public void removeAllServices() {
        mDConnectServices.clear();
    }

    @Override
    public boolean hasService(final String serviceId) {
        return getService(serviceId) != null;
    }

    @Override
    public void addServiceListener(final DConnectServiceListener listener) {
        synchronized (mServiceListeners) {
            if (!mServiceListeners.contains(listener)) {
                mServiceListeners.add(listener);
            }
        }
    }

    @Override
    public void removeServiceListener(final DConnectServiceListener listener) {
        synchronized (mServiceListeners) {
            for (Iterator<DConnectServiceListener> it = mServiceListeners.iterator(); ; it.hasNext()) {
                if (it.next() == listener) {
                    it.remove();
                    break;
                }
            }
        }
    }

    /**
     * サービスが追加されたことをリスナーに通知する.
     *
     * @param service 追加されたサービス
     */
    private void notifyOnServiceAdded(final DConnectService service) {
        synchronized (mServiceListeners) {
            for (DConnectServiceListener l : mServiceListeners) {
                l.onServiceAdded(service);
            }
        }
    }

    /**
     * サービスが削除されたことをリスナーに通知する.
     *
     * @param service 削除されたサービス
     */
    private void notifyOnServiceRemoved(final DConnectService service) {
        synchronized (mServiceListeners) {
            for (DConnectServiceListener l : mServiceListeners) {
                l.onServiceRemoved(service);
            }
        }
    }

    /**
     * サービスのステータスが変更されたことをリスナーに通知する.
     *
     * @param service ステータスが変更されたサービス
     */
    private void notifyOnStatusChange(final DConnectService service) {
        synchronized (mServiceListeners) {
            for (DConnectServiceListener l : mServiceListeners) {
                l.onStatusChange(service);
            }
        }
    }

    /**
     * ファイルパスから拡張子を削除します.
     *
     * @param filePath ファイルパス
     * @return 拡張子を削除したファイルパス
     */
    private static String removeExtension(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf('.'));
    }

    /**
     * プロファイル仕様の定義ファイルへのパスを取得します.
     *
     * <p>
     * プロファイル使用の定義ファイルが見つからない場合は、null を返却します。
     * </p>
     *
     * @param fileList ファイル一覧
     * @param profileName ファイル名
     * @return 定義ファイルへのパス
     */
    private static String findProfileSpecName(final String[] fileList, final String profileName) {
        if (fileList == null) {
            return null;
        }

        for (String filePath : fileList) {
            if (!filePath.endsWith(".json")) {
                // JSON 以外の拡張子は無視
                continue;
            }

            String fileName = removeExtension(filePath);
            if (fileName.equalsIgnoreCase(profileName)) {
                return filePath;
            }
        }
        return null;
    }

    /**
     * API 定義ファイルが格納されるフォルダの候補リストを取得します.
     *
     * <p>
     * 以下のようなリストが返却されます。
     *   - /assets/api
     *   - /assets/パッケージ名/api
     * </p>
     * <p>
     * MEMO: 必要に応じて、パスを追加すること。
     * </p>
     *
     * @return API 定義ファイルが格納されるフォルダの候補リスト
     */
    private List<String> getApiPath() {
        List<String> dirList = new ArrayList<>();
        DevicePluginXml xml = DevicePluginXmlUtil.getXml(getContext(), getPluginContext().getPluginXmlResId());
        if (xml != null && xml.getSpecPath() != null) {
            dirList.add(xml.getSpecPath());
        }
        dirList.add("api");
        return dirList;
    }

    /**
     * プロファイルの API 定義ファイルを検索します.
     *
     * <p>
     * プロファイルの API 定義ファイルへのパスが見つからない場合には例外が発生します。
     * </p>
     *
     * @param profileName プロファイル名
     * @return API定義ファイルへのパス
     * @throws IOException assetsへのアクセスに失敗した場合
     * @throws FileNotFoundException プロファイルの API 定義ファイルが見つからない場合
     */
    private String findApiPath(String profileName) throws IOException {
        AssetManager assets = getContext().getAssets();
        for (String dir : getApiPath()) {
            String[] fileNames = assets.list(dir);
            String fileName = findProfileSpecName(fileNames, profileName);
            if (fileName != null) {
                return dir + "/" + fileName;
            }
        }
        throw new FileNotFoundException(profileName + " is not found.");
    }

    /**
     * プロファイルの API 定義ファイルのストリームを開きます.
     *
     * <p>
     * ストリームを閉じる処理は、メソッドを呼び出した側で行うこと。
     * </p>
     *
     * @param profileName プロファイル名
     * @return API定義ファイルへのストリーム
     * @throws IOException assetsへのアクセスに失敗した場合
     * @throws FileNotFoundException ファイルが見つからない場合
     */
    private InputStream openApiSpec(String profileName)throws IOException {
        return getContext().getAssets().open(findApiPath(profileName));
    }

    // DConnectService.OnStatusChangeListener

    @Override
    public void onStatusChange(final DConnectService service) {
        notifyOnStatusChange(service);
    }
}
