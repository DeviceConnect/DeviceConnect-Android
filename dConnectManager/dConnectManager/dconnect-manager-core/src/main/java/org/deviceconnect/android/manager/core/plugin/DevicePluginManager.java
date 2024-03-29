/*
 DevicePluginManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.plugin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.TransactionTooLargeException;
import android.util.SparseArray;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.manager.core.BuildConfig;
import org.deviceconnect.android.manager.core.DConnectConst;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * デバイスプラグインを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginManager {
    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * デバイスプラグインに格納されるプラグイン名称のメタタグ名.
     */
    private static final String PLUGIN_META_PLUGIN_NAME = "org.deviceconnect.android.deviceplugin.name";

    /**
     * デバイスプラグインに格納されるアイコンのメタタグ名.
     */
    private static final String PLUGIN_META_PLUGIN_ICON = "org.deviceconnect.android.deviceplugin.icon";

    /**
     * デバイスプラグインSDKに格納されるメタタグ名.
     */
    private static final String PLUGIN_SDK_META_DATA = "org.deviceconnect.android.deviceplugin.sdk";

    /**
     * デバイスプラグインに格納されるメタタグ名.
     */
    private static final String PLUGIN_META_DATA = "org.deviceconnect.android.deviceplugin";

    /**
     * 再起動用のサービスを表すメタデータの値.
     */
    private static final String VALUE_META_DATA = "enable";

    /**
     * インストールされたPlug-inの情報を取得するためのReceiver.
     */
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                String packageName = getPackageName(intent);
                if (packageName != null) {
                    checkAndAddDevicePlugin(packageName);
                }
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                String packageName = getPackageName(intent);
                if (packageName != null) {
                    checkAndRemoveDevicePlugin(packageName);
                }
            }
        }
    };

    private String getPackageName(final Intent intent) {
        String pkgName = intent.getDataString();
        if (pkgName != null) {
            int idx = pkgName.indexOf(":");
            if (idx != -1) {
                pkgName = pkgName.substring(idx + 1);
            }
        }
        return pkgName;
    }

    /**
     * デバイスプラグイン一覧.
     */
    private final Map<String, DevicePlugin> mPlugins = new ConcurrentHashMap<>();

    /**
     * dConnectManagerのドメイン名.
     */
    private String mDConnectDomain;

    /**
     * イベントリスナーリスト.
     */
    private final List<DevicePluginEventListener> mEventListeners = new ArrayList<>();

    /**
     * イベントを通知するスレッド.
     */
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * 接続管理用インスタンスのファクトリー.
     */
    private ConnectionFactory mConnectionFactory;

    /**
     * 接続管理用インスタンスのイベントリスナー.
     */
    private final ConnectionStateListener mStateListener = (pluginId, state) -> {
        DevicePlugin plugin = mPlugins.get(pluginId);
        if (plugin != null) {
            notifyStateChange(plugin, state);
        }
    };

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param domain dConnect Managerのドメイン
     */
    public DevicePluginManager(final Context context, final String domain) {
        mContext = context;
        mDConnectDomain = domain;
    }

    /**
     * プラグインの追加、削除の監視を開始します.
     */
    public void startMonitoring() {
        // Plug-in情報受付用のIntent-filter
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addDataScheme("package");
        try {
            mContext.registerReceiver(mPackageReceiver, packageFilter);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                mLogger.severe("PluginManager: Failed to start plugin monitoring: " + e.getMessage());
            }
        }
    }

    /**
     * プラグインの追加、削除の監視を停止します.
     */
    public void stopMonitoring() {
        try {
            mContext.unregisterReceiver(mPackageReceiver);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                mLogger.severe("PluginManager: Failed to stop plugin monitoring: " + e.getMessage());
            }
        }
    }

    /**
     * 全てのプラグインの接続を切断します.
     */
    public void disconnectAllPlugins() {
        for (DevicePlugin plugin : mPlugins.values()) {
            plugin.dispose();
        }
    }

    /**
     * コネクション作成ファクトリーを設定します.
     *
     * @param factory ファクトリー
     */
    public void setConnectionFactory(final ConnectionFactory factory) {
        mConnectionFactory = factory;
    }

    /**
     * Device Connect Manager のドメイン名を設定する.
     *
     * @param domain ドメイン名
     */
    public void setDConnectDomain(final String domain) {
        mDConnectDomain = domain;
    }

    /**
     * プラグインを追加します.
     * <p>
     * 内包するプラグインなど外部から追加した場合に使用するために public にしておきます。
     * </p>
     * @param plugin プラグイン
     */
    public void addDevicePlugin(final DevicePlugin plugin) {
        if (mConnectionFactory != null) {
            plugin.setConnection(mConnectionFactory.createConnectionForPlugin(plugin));
            plugin.addConnectionStateListener(mStateListener);
        } else {
            if (BuildConfig.DEBUG) {
                mLogger.info("PluginManager: No connection factory: package=" + plugin.getPackageName());
            }
        }
        mPlugins.put(plugin.getPluginId(), plugin);
        plugin.apply();
        notifyFound(plugin);
    }

    /**
     * アプリ一覧からデバイスプラグイン一覧を作成する.
     *
     * @throws PluginDetectionException アプリケーション一覧のサイズが大きすぎて取得できなかった場合
     */
    public void createDevicePluginList() throws PluginDetectionException {
        PackageManager pkgMgr = mContext.getPackageManager();

        Map<String, List<DevicePlugin>> allPlugins;
        try {
            allPlugins = getInstalledPlugins(pkgMgr);
        } catch (Exception e) {
            PluginDetectionException.Reason reason;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (e.getClass() == TransactionTooLargeException.class) {
                    reason = PluginDetectionException.Reason.TOO_MANY_PACKAGES;
                } else {
                    reason = PluginDetectionException.Reason.OTHER;
                }
            } else {
                reason = PluginDetectionException.Reason.OTHER;
            }
            throw new PluginDetectionException(e, reason);
        }

        // 重複したプラグインを除外してからリストに追加
        for (Map.Entry<String, List<DevicePlugin>> entry : allPlugins.entrySet()) {
            List<DevicePlugin> pluginListPerPackage = entry.getValue();
            for (DevicePlugin plugin : filterPlugin(pluginListPerPackage)) {
                addDevicePlugin(plugin);
            }
        }
    }

    /**
     * インストールされているプラグインのリストを取得します.
     *
     * @param pkgMgr パッケージマネージャ
     * @return プラグインのリスト
     */
    private Map<String, List<DevicePlugin>> getInstalledPlugins(final PackageManager pkgMgr) {
        Map<String, List<DevicePlugin>> result = new HashMap<>();
        List<DevicePlugin> allPlugins = new ArrayList<>();
        allPlugins.addAll(getInstalledServices(pkgMgr));
        allPlugins.addAll(getInstalledReceivers(pkgMgr));
        for (DevicePlugin plugin : allPlugins) {
            String key = plugin.getPackageName();
            List<DevicePlugin> list = result.get(key);
            if (list == null) {
                list = new ArrayList<>();
                result.put(key, list);
            }
            list.add(plugin);
        }
        return result;
    }

    /**
     * パッケージ情報からインストールされているプラグインのリストを取得します.
     * @param pkgMgr パッケージマネージャ
     * @param pkg パッケージ情報
     * @return プラグインのリスト
     */
    private List<DevicePlugin> getInstalledPluginsForPackage(final PackageManager pkgMgr, final PackageInfo pkg) {
        List<DevicePlugin> result = new ArrayList<>();
        result.addAll(getInstalledServicesForPackage(pkgMgr, pkg));
        result.addAll(getInstalledReceiversForPackage(pkgMgr, pkg));
        return result;
    }

    /**
     * インストールされているService型プラグインのリストを取得します.
     * <p>
     * Android OS が O 以上になるとバックグラウンドで動作することができなくなります。<br>
     * そのために Device Connect では、Manager がフォアグラウンドで動作し、各プラグインのサービス
     * に対して Bind することで、プラグインもフォアグラウンドで動作するようにします。<br>
     * </p>
     * <p>
     * 上記の対応を行うためにプラグインもサービスで動作するように変更され、このメソッドで、そのプラグインを取得します。
     * </p>
     * @param pkgMgr パッケージマネージャ
     * @return サービスに対応したプラグインのリスト
     */
    private List<DevicePlugin> getInstalledServices(final PackageManager pkgMgr) {
        List<DevicePlugin> result = new ArrayList<>();
        List<PackageInfo> pkgList = pkgMgr.getInstalledPackages(PackageManager.GET_SERVICES | PackageManager.GET_PROVIDERS);
        for (PackageInfo pkg : pkgList) {
            result.addAll(getInstalledServicesForPackage(pkgMgr, pkg));
        }
        return result;
    }

    /**
     * パッケージ情報からプラグインのリストを取得します.
     *
     * @param pkgMgr パッケージマネージャ
     * @param pkg パッケージ情報
     * @return プラグインのリスト
     */
    private List<DevicePlugin> getInstalledServicesForPackage(final PackageManager pkgMgr, final PackageInfo pkg) {
        List<DevicePlugin> result = new ArrayList<>();
        if (pkg != null) {
            ServiceInfo[] array = pkg.services;
            if (array != null) {
                for (ServiceInfo info : array) {
                    ComponentName name = new ComponentName(info.packageName, info.name);
                    ServiceInfo infoWithMetaData = getServiceInfo(pkgMgr, name);
                    if (infoWithMetaData != null && isDevicePlugin(infoWithMetaData)) {
                        result.add(parsePlugin(pkg, infoWithMetaData));
                    }
                }
            }
        }
        return result;
    }

    /**
     * インストールされている BroadcastReceiver 型プラグインのリストを取得します.
     * <p>
     * 従来の Device Connect は BroadcastReceiver を用いて各プラグインと通信を行なっていました。<br>
     * ここでは、下位互換のために BroadcastReceiver 型のプラグインのリスト取得を行います。
     * </p>
     * @param pkgMgr パッケージマネージャ
     * @return サービスに対応したプラグインのリスト
     */
    private List<DevicePlugin> getInstalledReceivers(final PackageManager pkgMgr) {
        List<DevicePlugin> result = new ArrayList<>();
        List<PackageInfo> pkgList = pkgMgr.getInstalledPackages(PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS);
        for (PackageInfo pkg : pkgList) {
            result.addAll(getInstalledReceiversForPackage(pkgMgr, pkg));
        }
        return result;
    }

    /**
     * パッケージ情報からプラグインのリストを取得します.
     *
     * @param pkgMgr パッケージマネージャ
     * @param pkg パッケージ情報
     * @return プラグインのリスト
     */
    private List<DevicePlugin> getInstalledReceiversForPackage(final PackageManager pkgMgr, final PackageInfo pkg) {
        List<DevicePlugin> result = new ArrayList<>();
        if (pkg != null) {
            ActivityInfo[] array = pkg.receivers;
            if (array != null) {
                for (ActivityInfo info : array) {
                    ComponentName name = new ComponentName(info.packageName, info.name);
                    ActivityInfo infoWithMetaData = getReceiverInfo(pkgMgr, name);
                    if (infoWithMetaData != null && isDevicePlugin(infoWithMetaData)) {
                        result.add(parsePlugin(pkg, infoWithMetaData));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 指定されたパッケージの中にデバイスプラグインが存在するかチェックし追加する.
     * パッケージの中にデバイスプラグインがない場合には何もしない。
     *
     * @param packageName パッケージ名
     */
    private void checkAndAddDevicePlugin(final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("packageName is null.");
        }
        PackageManager pkgMgr = mContext.getPackageManager();
        try {
            int flag = PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS;
            PackageInfo pkg = pkgMgr.getPackageInfo(packageName, flag);
            if (pkg != null) {
                List<DevicePlugin> plugins = getInstalledPluginsForPackage(pkgMgr, pkg);
                for (DevicePlugin plugin : filterPlugin(plugins)) {
                    addDevicePlugin(plugin);
                }
            }
        } catch (NameNotFoundException e) {
            // NOP.
        }
    }

    /**
     * 重複したプラグインの宣言をフィルタリングする.
     *
     * @param plugins 検出されたプラグインのリスト
     * @return フィルタリングされたプラグインのリスト
     */
    private List<DevicePlugin> filterPlugin(final List<DevicePlugin> plugins) {
        // 同じプラグイン設定ファイルが指定されているプラグインを見つける。
        SparseArray<List<DevicePlugin>> array = new SparseArray<>();
        for (DevicePlugin plugin : plugins) {
            int key = plugin.getInfo().getPluginXmlId();
            List<DevicePlugin> list = array.get(key);
            if (list == null) {
                list = new ArrayList<>();
                array.append(key, list);
            }
            list.add(plugin);
        }

        List<DevicePlugin> result = new ArrayList<>();
        for (int index = 0; index < array.size(); index++) {
            List<DevicePlugin> list = array.valueAt(index);
            if (list != null && list.size() > 0) {
                Collections.sort(list, COMPARATOR);
                result.add(list.get(0));
            }
        }
        return result;
    }

    private static final Comparator<DevicePlugin> COMPARATOR = (Comparator<DevicePlugin>) (p1, p2) -> {
        // BroadcastよりもBinderを優先する.
        if (p1.getConnectionType() == p2.getConnectionType()) {
            return 0;
        } else if (p1.getConnectionType() == ConnectionType.BINDER) {
            return -1;
        } else {
            return 1;
        }
    };

    /**
     * 指定されたコンポーネントの ServiceInfo を取得します.
     * <p>
     * 一致する ServiceInfo が存在しない場合は null を返却します。
     * </p>
     * @param pkgMgr パッケージマネージャ
     * @param component コンポーネント
     * @return ServiceInfoのインスタンス
     */
    private ServiceInfo getServiceInfo(final PackageManager pkgMgr, final ComponentName component) {
        try {
            return pkgMgr.getServiceInfo(component, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 指定されたコンポーネントの ReceiverInfo を取得します.
     * <p>
     * 一致する ReceiverInfo が存在しない場合は null を返却します。
     * </p>
     * @param pkgMgr パッケージマネージャ
     * @param component コンポーネント
     * @return ReceiverInfoのインスタンス
     */
    private ActivityInfo getReceiverInfo(final PackageManager pkgMgr, final ComponentName component) {
        try {
            return pkgMgr.getReceiverInfo(component, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 指定されたコンポーネントがプラグインの情報か確認します.
     *
     * @param compInfo コンポーネント
     * @return プラグインの場合はtrue、それ以外はfalse
     */
    private boolean isDevicePlugin(final ComponentInfo compInfo) {
        if (!compInfo.exported) {
            return false;
        }
        Bundle metaData = compInfo.metaData;
        if (metaData == null) {
            return false;
        }
        if (metaData.get(PLUGIN_META_DATA) == null) {
            return false;
        }
        if (!(metaData.get(PLUGIN_META_DATA) instanceof Integer)) {
            return false;
        }
        DevicePluginXml xml = DevicePluginXmlUtil.getXml(mContext, compInfo);
        return xml != null;
    }

    /**
     * 指定されたコンポーネントからプラグインの情報を取得して DevicePlugin のインスタンスを作成します.
     * <p>
     * 既に同じコンポーネントのプラグインが存在する場合には上書きします。
     * </p>
     * @param pkgInfo パッケージ情報
     * @param componentInfo コンポーネント情報
     * @return DevicePlugin のインスタンス
     */
    private DevicePlugin parsePlugin(final PackageInfo pkgInfo, final ComponentInfo componentInfo) {
        PackageManager pkgMgr = mContext.getPackageManager();

        Bundle metaData = componentInfo.metaData;
        String pluginName = metaData.getString(PLUGIN_META_PLUGIN_NAME);
        if (pluginName == null) {
            pluginName = componentInfo.applicationInfo.loadLabel(pkgMgr).toString();
        }

        VersionName sdkVersionName = getPluginSDKVersion(componentInfo.applicationInfo);
        String packageName = componentInfo.packageName;
        String className = componentInfo.name;
        String versionName = pkgInfo.versionName;
        String hash = md5(packageName + className);
        if (hash == null) {
            throw new RuntimeException("Can't generate md5.");
        }
        Integer iconId = (Integer) metaData.get(PLUGIN_META_PLUGIN_ICON);

        if (BuildConfig.DEBUG) {
            mLogger.info("Added DevicePlugin: [" + hash + "]\n" +
                    "    PackageName: " + packageName + "\n" +
                    "    className: " + className + "\n" +
                    "    versionName: " + versionName + "\n" +
                    "    sdkVersionName: " + sdkVersionName);

            // MEMO 既に同じ名前のデバイスプラグインが存在した場合の処理
            // 現在は警告を表示し、上書きする.
            if (mPlugins.containsKey(hash)) {
                mLogger.warning("DevicePlugin[" + hash + "] already exists.");
            }
        }

        ConnectionType type;
        if (componentInfo instanceof ServiceInfo) {
            if (isSamePackage(componentInfo)) {
                type = ConnectionType.INTERNAL;
            } else {
                type = ConnectionType.BINDER;
            }
        } else {
            type = ConnectionType.BROADCAST;
        }

        DevicePlugin.Builder plugin = new DevicePlugin.Builder(mContext)
                .setPackageName(componentInfo.packageName)
                .setClassName(componentInfo.name)
                .setVersionName(versionName)
                .setPluginId(hash)
                .setDeviceName(pluginName)
                .setPluginXml(DevicePluginXmlUtil.getXml(mContext, componentInfo))
                .setPluginSdkVersionName(sdkVersionName)
                .setPluginIconId(iconId)
                .setConnectionType(type);

        ProviderInfo[] providers = pkgInfo.providers;
        if (providers != null) {
            for (ProviderInfo provider : providers) {
                if (provider.exported && provider.enabled) {
                    plugin.addProviderAuthority(provider.authority);
                }
            }
        }

        return plugin.build();
    }

    /**
     * 指定されたコンポーネントのパッケージ名が Device Connect Manager と同じか確認します.
     *
     * @param componentInfo コンポーネント
     * @return パッケージ名が同じ場合はtrue、それ以外はfalse
     */
    private boolean isSamePackage(final ComponentInfo componentInfo) {
        return mContext.getPackageName().equals(componentInfo.packageName);
    }

    /**
     * 指定されたパッケージ名に対応するデバイスプラグインを削除する.
     *
     * @param packageName パッケージ名
     */
    private void checkAndRemoveDevicePlugin(final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("packageName is null.");
        }

        for (String key : mPlugins.keySet()) {
            DevicePlugin plugin = mPlugins.get(key);
            if (plugin.getPackageName().equals(packageName)) {
                removePlugin(key);
            }
        }
    }

    /**
     * 指定されたキーのプラグインをリストから削除します.
     *
     * @param key 削除するプラグインのキー
     */
    private void removePlugin(final String key) {
        DevicePlugin plugin = mPlugins.remove(key);
        if (plugin != null) {
            plugin.removeConnectionStateListener(mStateListener);
            plugin.dispose();
            notifyLost(plugin);
        }
    }

    /**
     * 指定されたサービスIDと一致するデバイスプラグインを取得する.
     *
     * @param serviceId サービスID
     * @return デバイスプラグイン
     */
    public DevicePlugin getDevicePlugin(final String serviceId) {
        return mPlugins.get(serviceId);
    }

    /**
     * 指定されたサービスIDからデバイスプラグインを取得する.
     * <p>
     * 指定されたserviceIdに対応するデバイスプラグインが存在しない場合にはnullを返却する。
     * サービスIDのネーミング規則は以下のようになる。
     * [device].[deviceplugin].[dconnect].deviceconnect.org
     * [dconnect].deviceconnect.org が serviceIdに渡されたときには、
     * すべてのプラグインをListに格納して返します。
     * </p>
     * @param serviceId パースするサービスID
     * @return デバイスプラグイン
     */
    public List<DevicePlugin> getDevicePlugins(final String serviceId) {
        if (serviceId == null) {
            return null;
        }
        String pluginName = serviceId;
        int idx = pluginName.lastIndexOf(mDConnectDomain);
        if (idx > 0) {
            pluginName = pluginName.substring(0, idx - 1);
        } else {
            // ここで見つからない場合には、サービスIDとして不正なので
            // nullを返却する。
            return null;
        }
        idx = pluginName.lastIndexOf(DConnectConst.SEPARATOR);
        if (idx > 0) {
            pluginName = pluginName.substring(idx + 1);
        }
        if (mPlugins.containsKey(pluginName)) {
            List<DevicePlugin> plugins = new ArrayList<>();
            plugins.add(mPlugins.get(pluginName));
            return plugins;
        } else {
            return null;
        }
    }

    /**
     * すべてのデバイスプラグインを取得する.
     *
     * @return すべてのデバイスプラグイン
     */
    public List<DevicePlugin> getDevicePlugins() {
        return new ArrayList<>(mPlugins.values());
    }

    /**
     * 有効になっているプラグインのリストを取得します.
     *
     * @return プラグインのリスト
     */
    public List<DevicePlugin> getEnabledDevicePlugins() {
        List<DevicePlugin> result = new ArrayList<>();
        synchronized (mPlugins) {
            for (DevicePlugin plugin : mPlugins.values()) {
                if (plugin.isEnabled()) {
                    result.add(plugin);
                }
            }
        }

        return result;
    }

    /**
     * サービスIDにDevice Connect Managerのドメイン名を追加する.
     * <p>
     * サービスIDがnullのときには、サービスIDは無視します。
     *
     * @param plugin    デバイスプラグイン
     * @param serviceId サービスID
     * @return Device Connect Managerのドメインなどが追加されたサービスID
     */
    public String appendServiceId(final DevicePlugin plugin, final String serviceId) {
        if (serviceId == null) {
            return plugin.getPluginId() + DConnectConst.SEPARATOR + mDConnectDomain;
        } else {
            return serviceId + DConnectConst.SEPARATOR + plugin.getPluginId()
                    + DConnectConst.SEPARATOR + mDConnectDomain;
        }
    }

    /**
     * リクエストに含まれるセッションキーを変換する.
     * <p>
     * セッションキーにデバイスプラグインIDとreceiverを追加する。
     * 下記のように、分解できるようになっている。
     *
     * 【セッションキー.デバイスプラグインID@receiver】
     * </p>
     * @param request リクエスト
     * @param plugin  デバイスプラグイン
     */
    public void appendPluginIdToSessionKey(final Intent request, final DevicePlugin plugin) {
        String sessionKey = request.getStringExtra(DConnectMessage.EXTRA_SESSION_KEY);
        if (plugin != null && sessionKey != null) {
            sessionKey = sessionKey + DConnectConst.SEPARATOR + plugin.getPluginId();
            ComponentName receiver = (ComponentName) request.getExtras().get(DConnectMessage.EXTRA_RECEIVER);
            if (receiver != null) {
                sessionKey = sessionKey + DConnectConst.SEPARATOR_SESSION + receiver.flattenToString();
            }
            request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, sessionKey);
        }
    }

    /**
     * 指定されたリクエストのserviceIdからプラグインIDを削除する.
     *
     * @param request リクエスト
     */
    public void splitPluginIdToServiceId(final Intent request) {
        String serviceId = request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        if (serviceId != null) {
            List<DevicePlugin> plugins = getDevicePlugins(serviceId);
            // 各デバイスプラグインへ渡すサービスIDを作成
            String id = DevicePluginManager.splitServiceId(plugins.get(0), serviceId);
            request.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID, id);
        }
    }

    /**
     * サービスIDを分解して、Device Connect Managerのドメイン名を省いた本来のサービスIDにする.
     * Device Connect Managerのドメインを省いたときに、何もない場合には空文字を返します。
     *
     * @param plugin デバイスプラグイン
     * @param serviceId サービスID
     * @return Device Connect Managerのドメインが省かれたサービスID
     */
    public static String splitServiceId(final DevicePlugin plugin, final String serviceId) {
        String p = plugin.getPluginId();
        int idx = serviceId.indexOf(p);
        if (idx > 0) {
            return serviceId.substring(0, idx - 1);
        }
        return "";
    }

    /**
     * プラグインSDKのバージョンを取得します.
     *
     * @param info アプリケーション情報
     * @return バージョン名
     */
    private VersionName getPluginSDKVersion(final ApplicationInfo info) {
        VersionName versionName = null;
        if (info.metaData != null && info.metaData.get(PLUGIN_SDK_META_DATA) != null) {
            PackageManager pkgMgr = mContext.getPackageManager();
            XmlResourceParser xpp = info.loadXmlMetaData(pkgMgr, PLUGIN_SDK_META_DATA);
            try {
                String str = parsePluginSDKVersionName(xpp);
                if (str != null) {
                    versionName = VersionName.parse(str);
                }
            } catch (Exception e) {
                // NOP
            }
        }
        if (versionName != null) {
            return versionName;
        } else {
            return VersionName.parse("1.0.0");
        }
    }

    /**
     * プラグインSDKのバージョンを取得します.
     *
     * @param xpp xmlデータ
     * @return プラグインSDKのバージョン名
     * @throws XmlPullParserException xmlの解釈に失敗した場合に発生
     * @throws IOException xmlの読み込みに失敗した場合に発生
     */
    private String parsePluginSDKVersionName(final XmlResourceParser xpp)
            throws XmlPullParserException, IOException {
        String versionName = null;
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            final String name = xpp.getName();
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (name.equals("version")) {
                        versionName = xpp.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
            }
            eventType = xpp.next();
        }
        return versionName;
    }

    /**
     * 指定された文字列をMD5の文字列に変換する.
     * <p>
     * MD5への変換に失敗した場合にはnullを返却する。
     * </p>
     *
     * @param s MD5にする文字列
     * @return MD5にされた文字列
     */
    private String md5(final String s) {
        try {
            return DConnectUtil.toMD5(s);
        } catch (UnsupportedEncodingException e) {
            mLogger.warning("Not support Charset.");
        } catch (NoSuchAlgorithmException e) {
            mLogger.warning("Not support MD5.");
        }
        return null;
    }

    /**
     * イベントリスナーを追加します.
     *
     * @param listener リスナー
     */
    public void addEventListener(final DevicePluginEventListener listener) {
        synchronized (mEventListeners) {
            for (DevicePluginEventListener cache : mEventListeners) {
                if (cache == listener) {
                    return;
                }
            }
            mEventListeners.add(listener);
        }
    }

    /**
     * イベントリスナーを削除します.
     *
     * @param listener リスナー
     */
    public void removeEventListener(final DevicePluginEventListener listener) {
        synchronized (mEventListeners) {
            for (Iterator<DevicePluginEventListener> it = mEventListeners.iterator(); it.hasNext(); ) {
                DevicePluginEventListener cache = it.next();
                if (cache == listener) {
                    it.remove();
                    return;
                }
            }
        }
    }

    private void notifyFound(final DevicePlugin plugin) {
        synchronized (mEventListeners) {
            if (mEventListeners.size() > 0) {
                for (final DevicePluginEventListener l : mEventListeners) {
                    mExecutor.execute(() -> l.onDeviceFound(plugin));
                }
            }
        }
    }

    private void notifyLost(final DevicePlugin plugin) {
        synchronized (mEventListeners) {
            if (mEventListeners.size() > 0) {
                for (final DevicePluginEventListener l : mEventListeners) {
                    mExecutor.execute(() -> l.onDeviceLost(plugin));
                }
            }
        }
    }

    private void notifyStateChange(final DevicePlugin plugin, final ConnectionState state) {
        synchronized (mEventListeners) {
            if (mEventListeners.size() > 0) {
                for (final DevicePluginEventListener l : mEventListeners) {
                    mExecutor.execute(() -> l.onConnectionStateChanged(plugin, state));
                }
            }
        }
    }

    /**
     * デバイスプラグインの発見、見失う通知を行うリスナー.
     *
     * @author NTT DOCOMO, INC.
     */
    public interface DevicePluginEventListener {
        /**
         * デバイスプラグインが発見されたことを通知する.
         *
         * @param plugin 発見されたデバイスプラグイン
         */
        void onDeviceFound(DevicePlugin plugin);

        /**
         * デバイスプラグインを見失ったことを通知する.
         *
         * @param plugin 見失ったデバイスプラグイン
         */
        void onDeviceLost(DevicePlugin plugin);

        /**
         * デバイスプラグインとの接続状態が変更されたことを通知する
         *
         * @param plugin デバイスプラグイン
         * @param state  現在の接続状態
         */
        void onConnectionStateChanged(DevicePlugin plugin, ConnectionState state);
    }
}
