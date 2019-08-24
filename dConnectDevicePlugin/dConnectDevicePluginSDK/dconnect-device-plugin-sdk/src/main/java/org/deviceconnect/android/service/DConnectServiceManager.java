/*
 DConnectServiceManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.service;

import android.content.Context;

import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.DConnectServiceSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

        DConnectServiceSpec spec = service.getServiceSpec();
        if (spec == null) {
            // プロファイル定義が設定されていない場合は新規に登録しておく
            spec = new DConnectServiceSpec(getPluginContext());
            service.setServiceSpec(spec);
        }

        // 既にサービスに登録されているプロファイルにコンテキストなどを設定
        for (DConnectProfile profile : service.getProfileList()) {
            profile.setContext(getContext());
            profile.setPluginContext(getPluginContext());
            profile.setResponder(getPluginContext());

            // プロファイルの定義ファイルが既に登録されている場合は、そのままにしておく
            if (spec.findProfileSpec(profile.getProfileName()) == null) {
                try {
                    spec.addProfileSpec(profile.getProfileName());
                } catch (Exception e) {
                    // プロファイル定義ファイルが不正の場合は無視
                }
            }
        }

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
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }

        synchronized (mServiceListeners) {
            if (!mServiceListeners.contains(listener)) {
                mServiceListeners.add(listener);
            }
        }
    }

    @Override
    public void removeServiceListener(final DConnectServiceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }

        synchronized (mServiceListeners) {
            mServiceListeners.remove(listener);
        }
    }

    // DConnectService.OnStatusChangeListener Implements

    @Override
    public void onStatusChange(final DConnectService service) {
        notifyOnStatusChange(service);
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
}
