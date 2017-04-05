/*
 DConnectServiceManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.service;


import android.content.Context;

import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Device Connect APIサービス管理インターフェースのデフォルト実装.
 * @author NTT DOCOMO, INC.
 */
public class DConnectServiceManager implements DConnectServiceProvider,
    DConnectService.OnStatusChangeListener {

    /**
     * プラグインがサポートするスペックを管理するクラス.
     */
    private DConnectPluginSpec mPluginSpec;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * デバイスプラグインが持っているサービスリスト.
     */
    private final Map<String, DConnectService> mDConnectServices
            = Collections.synchronizedMap(new HashMap<String, DConnectService>());

    /**
     * サービス通知リスナーリスト.
     */
    private final List<DConnectServiceListener> mServiceListeners
            = Collections.synchronizedList(new ArrayList<DConnectServiceListener>());

    /**
     * コンテキストを設定する.
     * @param context コンテキスト
     */
    public void setContext(final Context context) {
        mContext = context;
    }

    /**
     * コンテキストを取得する.
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * プラグインがサポートするスペックを管理するクラスを設定する.
     *
     * @param pluginSpec プラグインがサポートするスペック
     */
    public void setPluginSpec(final DConnectPluginSpec pluginSpec) {
        if (pluginSpec == null) {
            throw new NullPointerException("pluginSpec is null.");
        }
        mPluginSpec = pluginSpec;
    }

    @Override
    public void addService(final DConnectService service) {
        service.setOnStatusChangeListener(this);
        service.setContext(mContext);
        if (mPluginSpec != null) {
            for (DConnectProfile profile : service.getProfileList()) {
                DConnectProfileSpec profileSpec =
                    mPluginSpec.findProfileSpec(profile.getProfileName().toLowerCase());
                if (profileSpec != null) {
                    profile.setProfileSpec(profileSpec);
                }
            }
        }
        for (DConnectProfile profile : service.getProfileList()) {
            profile.setContext(mContext);
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
        List<DConnectService> list = new ArrayList<DConnectService>();
        list.addAll(mDConnectServices.values());
        return list;
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

    @Override
    public void onStatusChange(final DConnectService service) {
        notifyOnStatusChange(service);
    }
}
