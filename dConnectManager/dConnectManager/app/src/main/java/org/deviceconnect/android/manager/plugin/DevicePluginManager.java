/*
 DevicePluginManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.util.AndroidException;
import android.util.SparseArray;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.VersionName;
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
 * @author NTT DOCOMO, INC.
 */
public class DevicePluginManager {
    /** デバイスプラグインに格納されるプラグイン名称のメタタグ名. */
    private static final String PLUGIN_META_PLUGIN_NAME = "org.deviceconnect.android.deviceplugin.name";
    /** デバイスプラグインに格納されるアイコンのメタタグ名. */
    private static final String PLUGIN_META_PLUGIN_ICON = "org.deviceconnect.android.deviceplugin.icon";
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");
    /** デバイスプラグインSDKに格納されるメタタグ名. */
    private static final String PLUGIN_SDK_META_DATA = "org.deviceconnect.android.deviceplugin.sdk";
    /** デバイスプラグインに格納されるメタタグ名. */
    private static final String PLUGIN_META_DATA = "org.deviceconnect.android.deviceplugin";
    /** 再起動用のサービスを表すメタデータの値. */
    private static final String VALUE_META_DATA = "enable";
    /** デバイスプラグイン一覧. */
    private final Map<String, DevicePlugin> mPlugins = new ConcurrentHashMap<String, DevicePlugin>();
    /** dConnectManagerのドメイン名. */
    private String mDConnectDomain;

    /** イベントリスナーリスト. */
    private List<DevicePluginEventListener> mEventListeners = new ArrayList<>();
    /** イベントを通知するスレッド. */
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    /** コンテキスト. */
    private final Context mContext;
    /** 接続管理用インスタンスのファクトリー. */
    private ConnectionFactory mConnectionFactory;
    /** 接続管理用インスタンスのイベントリスナー. */
    private ConnectionStateListener mStateListener = new ConnectionStateListener() {
        @Override
        public void onConnectionStateChanged(final String pluginId, final ConnectionState state) {
            DevicePlugin plugin = mPlugins.get(pluginId);
            if (plugin != null) {
                notifyStateChange(plugin, state);
            }
        }
    };

    /**
     * コンストラクタ.
     * @param context コンテキスト
     * @param domain dConnect Managerのドメイン
     */
    public DevicePluginManager(final Context context, final String domain) {
        setDConnectDomain(domain);
        mContext = context;
    }

    public void setConnectionFactory(final ConnectionFactory factory) {
        mConnectionFactory = factory;
    }

    /**
     * dConnect Managerのドメイン名を設定する.
     * @param domain ドメイン名
     */
    public void setDConnectDomain(final String domain) {
        mDConnectDomain = domain;
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
            if (Build.VERSION.SDK_INT >= 15) {
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

        for (Map.Entry<String, List<DevicePlugin>> entry : allPlugins.entrySet()) {
            List<DevicePlugin> pluginListPerPackage = entry.getValue();

            // 重複したプラグインを除外
            for (DevicePlugin plugin : filterPlugin(pluginListPerPackage)) {
                mPlugins.put(plugin.getPluginId(), plugin);
                notifyFound(plugin);
            }
        }
    }

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

    private List<DevicePlugin> getInstalledPluginsForPackage(final PackageManager pkgMgr,
                                                             final PackageInfo pkg) {
        List<DevicePlugin> result = new ArrayList<>();
        result.addAll(getInstalledServicesForPackage(pkgMgr, pkg));
        result.addAll(getInstalledReceiversForPackage(pkgMgr, pkg));
        return result;
    }

    private List<DevicePlugin> getInstalledServices(final PackageManager pkgMgr) {
        List<DevicePlugin> result = new ArrayList<>();
        List<PackageInfo> pkgList = pkgMgr.getInstalledPackages(PackageManager.GET_SERVICES);
        for (PackageInfo pkg : pkgList) {
            result.addAll(getInstalledServicesForPackage(pkgMgr, pkg));
        }
        return result;
    }

    private List<DevicePlugin> getInstalledServicesForPackage(final PackageManager pkgMgr,
                                                              final PackageInfo pkg) {
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

    private List<DevicePlugin> getInstalledReceivers(final PackageManager pkgMgr) {
        List<DevicePlugin> result = new ArrayList<>();
        List<PackageInfo> pkgList = pkgMgr.getInstalledPackages(PackageManager.GET_RECEIVERS);
        for (PackageInfo pkg : pkgList) {
            result.addAll(getInstalledReceiversForPackage(pkgMgr, pkg));
        }
        return result;
    }

    private List<DevicePlugin> getInstalledReceiversForPackage(final PackageManager pkgMgr,
                                                               final PackageInfo pkg) {
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
     * @param packageName パッケージ名
     */
    public void checkAndAddDevicePlugin(final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("packageName is null.");
        }
        PackageManager pkgMgr = mContext.getPackageManager();
        try {
            int flag = PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS;
            PackageInfo pkg = pkgMgr.getPackageInfo(packageName, flag);
            if (pkg != null) {
                List<DevicePlugin> plugins = getInstalledPluginsForPackage(pkgMgr, pkg);
                for (DevicePlugin plugin : filterPlugin(plugins)) {
                    mPlugins.put(plugin.getPluginId(), plugin);
                    notifyFound(plugin);
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
                Collections.sort(list, new Comparator<DevicePlugin>() {
                    @Override
                    public int compare(final DevicePlugin p1, final DevicePlugin p2) {
                        // BroadcastよりもBinderを優先する.
                        if (p1.getConnectionType() == p2.getConnectionType()) {
                            return 0;
                        } else if (p1.getConnectionType() == ConnectionType.BINDER) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                result.add(list.get(0));
            }
        }
        return result;
    }

    private ServiceInfo getServiceInfo(final PackageManager pkgMgr, final ComponentName component) {
        try {
            return pkgMgr.getServiceInfo(component, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private ActivityInfo getReceiverInfo(final PackageManager pkgMgr, final ComponentName component) {
        try {
            return pkgMgr.getReceiverInfo(component, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

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

    private DevicePlugin parsePlugin(final PackageInfo pkgInfo,
                                     final ComponentInfo componentInfo) {
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

        mLogger.info("Added DevicePlugin: [" + hash + "]");
        mLogger.info("    PackageName: " + packageName);
        mLogger.info("    className: " + className);
        mLogger.info("    versionName: " + versionName);
        mLogger.info("    sdkVersionName: " + sdkVersionName);
        // MEMO 既に同じ名前のデバイスプラグインが存在した場合の処理
        // 現在は警告を表示し、上書きする.
        if (mPlugins.containsKey(hash)) {
            mLogger.warning("DevicePlugin[" + hash + "] already exists.");
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

        DevicePlugin plugin = new DevicePlugin.Builder(mContext)
            .setPackageName(componentInfo.packageName)
            .setClassName(componentInfo.name)
            .setVersionName(versionName)
            .setPluginId(hash)
            .setDeviceName(pluginName)
            .setPluginXml(DevicePluginXmlUtil.getXml(mContext, componentInfo))
            .setPluginSdkVersionName(sdkVersionName)
            .setPluginIconId(iconId)
            .setConnectionType(type)
            .build();
        if (mConnectionFactory != null) {
            plugin.setConnection(mConnectionFactory.createConnectionForPlugin(plugin));
            plugin.addConnectionStateListener(mStateListener);
        }
        return plugin;
    }

    private boolean isSamePackage(final ComponentInfo componentInfo) {
        return mContext.getPackageName().equals(componentInfo.packageName);
    }

    /**
     * 指定されたパッケージ名に対応するデバイスプラグインを削除する.
     * @param packageName パッケージ名
     */
    public void checkAndRemoveDevicePlugin(final String packageName) {
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

    private void removePlugin(final String key) {
        DevicePlugin plugin = mPlugins.remove(key);
        if (plugin != null) {
            plugin.removeConnectionStateListener(mStateListener);
            plugin.dispose();
            notifyLost(plugin);
        }
    }

    /**
     * 指定されたコンポーネントがデバイスプラグインがチェックを行い、デバイスプラグインの場合には削除を行う.
     * @param component 削除するコンポーネント
     */
    public void checkAndRemoveDevicePlugin(final ComponentName component) {
        ActivityInfo receiverInfo = null;
        try {
            PackageManager pkgMgr = mContext.getPackageManager();
            receiverInfo = pkgMgr.getReceiverInfo(component, PackageManager.GET_META_DATA);
            if (receiverInfo.metaData != null) {
                String value = receiverInfo.metaData.getString(PLUGIN_META_DATA);
                if (value != null) {
                    String packageName = receiverInfo.packageName;
                    String className = receiverInfo.name;
                    String hash = md5(packageName + className);
                    mLogger.info("Removed DevicePlugin: [" + hash + "]");
                    mLogger.info("    PackageName: " + packageName);
                    mLogger.info("    className: " + className);
                    removePlugin(hash);
                }
            }
        } catch (NameNotFoundException e) {
            return;
        }
    }

    /**
     * 指定されたサービスIDと一致するデバイスプラグインを取得する.
     * @param serviceId サービスID
     * @return デバイスプラグイン
     */
    public DevicePlugin getDevicePlugin(final String serviceId) {
        return mPlugins.get(serviceId);
    }

    /**
     * 指定されたサービスIDからデバイスプラグインを取得する.
     * 指定されたserviceIdに対応するデバイスプラグインが存在しない場合にはnullを返却する。
     * サービスIDのネーミング規則は以下のようになる。
     * [device].[deviceplugin].[dconnect].deviceconnect.org
     * [dconnect].deviceconnect.org が serviceIdに渡されたときには、
     * すべてのプラグインをListに格納して返します。
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
        idx = pluginName.lastIndexOf(DConnectMessageService.SEPARATOR);
        if (idx > 0) {
            pluginName = pluginName.substring(idx + 1);
        }
        if (mPlugins.containsKey(pluginName)) {
            List<DevicePlugin> plugins = new ArrayList<DevicePlugin>();
            plugins.add(mPlugins.get(pluginName));
            return plugins;
        } else {
            return null;
        }
    }

    /**
     * すべてのデバイスプラグインを取得する.
     * @return すべてのデバイスプラグイン
     */
    public List<DevicePlugin> getDevicePlugins() {
        return new ArrayList<DevicePlugin>(mPlugins.values());
    }

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
     *
     * サービスIDがnullのときには、サービスIDは無視します。
     *
     * @param plugin デバイスプラグイン
     * @param serviceId サービスID
     * @return Device Connect Managerのドメインなどが追加されたサービスID
     */
    public String appendServiceId(final DevicePlugin plugin, final String serviceId) {
        if (serviceId == null) {
            return plugin.getPluginId() + DConnectMessageService.SEPARATOR + mDConnectDomain;
        } else {
            return serviceId + DConnectMessageService.SEPARATOR + plugin.getPluginId()
                    + DConnectMessageService.SEPARATOR + mDConnectDomain;
        }
    }

    /**
     * リクエストに含まれるセッションキーを変換する.
     *
     * セッションキーにデバイスプラグインIDとreceiverを追加する。
     * 下記のように、分解できるようになっている。
     * 【セッションキー.デバイスプラグインID@receiver】
     *
     * @param request リクエスト
     */
    public void appendPluginIdToSessionKey(final Intent request) {
        String serviceId = request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        List<DevicePlugin> plugins = getDevicePlugins(serviceId);
        String sessionKey = request.getStringExtra(DConnectMessage.EXTRA_SESSION_KEY);
        if (plugins != null && sessionKey != null) {
            sessionKey = sessionKey + DConnectMessageService.SEPARATOR + plugins.get(0).getPluginId();
            ComponentName receiver = (ComponentName) request.getExtras().get(DConnectMessage.EXTRA_RECEIVER);
            if (receiver != null) {
                sessionKey = sessionKey + DConnectMessageService.SEPARATOR_SESSION
                        + receiver.flattenToString();
            }
            request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, sessionKey);
        }
    }

    /**
     * リクエストに含まれるセッションキーを変換する.
     *
     * セッションキーにデバイスプラグインIDとreceiverを追加する。
     * 下記のように、分解できるようになっている。
     * 【セッションキー.デバイスプラグインID@receiver】
     *
     * @param request リクエスト
     * @param plugin デバイスプラグイン
     */
    public void appendPluginIdToSessionKey(final Intent request, final DevicePlugin plugin) {
        String sessionKey = request.getStringExtra(DConnectMessage.EXTRA_SESSION_KEY);
        if (plugin != null && sessionKey != null) {
            sessionKey = sessionKey + DConnectMessageService.SEPARATOR + plugin.getPluginId();
            ComponentName receiver = (ComponentName) request.getExtras().get(DConnectMessage.EXTRA_RECEIVER);
            if (receiver != null) {
                sessionKey = sessionKey + DConnectMessageService.SEPARATOR_SESSION
                        + receiver.flattenToString();
            }
            request.putExtra(DConnectMessage.EXTRA_SESSION_KEY, sessionKey);
        }
    }

    /**
     * 指定されたリクエストのserviceIdからプラグインIDを削除する.
     * @param request リクエスト
     */
    public void splitPluginIdToServiceId(final Intent request) {
        String serviceId = request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        List<DevicePlugin> plugins = getDevicePlugins(serviceId);
        // 各デバイスプラグインへ渡すサービスIDを作成
        String id = DevicePluginManager.splitServiceId(plugins.get(0), serviceId);
        request.putExtra(IntentDConnectMessage.EXTRA_SERVICE_ID, id);
    }

    /**
     * サービスIDを分解して、Device Connect Managerのドメイン名を省いた本来のサービスIDにする.
     * Device Connect Managerのドメインを省いたときに、何もない場合には空文字を返します。
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
            } catch (XmlPullParserException e) {
                // NOP
            } catch (IOException e) {
                // NOP
            }
        }
        if (versionName != null) {
            return versionName;
        } else {
            return VersionName.parse("1.0.0");
        }
    }

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
     * deviceplugin.xmlの確認を行う.
     * @param info receiverのタグ情報
     * @return プロファイル一覧
     */
    public List<String> checkDevicePluginXML(final ComponentInfo info) {
        PackageManager pkgMgr = mContext.getPackageManager();
        XmlResourceParser xpp = info.loadXmlMetaData(pkgMgr, PLUGIN_META_DATA);
        try {
            return parseDevicePluginXML(xpp);
        } catch (XmlPullParserException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * xml/deviceplugin.xmlの解析を行う.
     *
     * @param xpp xmlパーサ
     * @throws XmlPullParserException xmlの解析に失敗した場合に発生
     * @throws IOException xmlの読み込みに失敗した場合
     * @return プロファイル一覧
     */
    private List<String> parseDevicePluginXML(final XmlResourceParser xpp)
            throws XmlPullParserException, IOException {
        ArrayList<String> list = new ArrayList<String>();
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            final String name = xpp.getName();
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (name.equals("profile")) {
                        list.add(xpp.getAttributeValue(null, "name"));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
            }
            eventType = xpp.next();
        }
        return list;
    }

    /**
     * 指定された文字列をMD5の文字列に変換する.
     * MD5への変換に失敗した場合にはnullを返却する。
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
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            l.onDeviceFound(plugin);
                        }
                    });
                }
            }
        }
    }

    private void notifyLost(final DevicePlugin plugin) {
        synchronized (mEventListeners) {
            if (mEventListeners.size() > 0) {
                for (final DevicePluginEventListener l : mEventListeners) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            l.onDeviceLost(plugin);
                        }
                    });
                }
            }
        }
    }

    private void notifyStateChange(final DevicePlugin plugin, final ConnectionState state) {
        synchronized (mEventListeners) {
            if (mEventListeners.size() > 0) {
                for (final DevicePluginEventListener l : mEventListeners) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            l.onConnectionStateChanged(plugin, state);
                        }
                    });
                }
            }
        }
    }

    /**
     * デバイスプラグインの発見、見失う通知を行うリスナー.
     * @author NTT DOCOMO, INC.
     */
    public interface DevicePluginEventListener {
        /**
         * デバイスプラグインが発見されたことを通知する.
         * @param plugin 発見されたデバイスプラグイン
         */
        void onDeviceFound(DevicePlugin plugin);

        /**
         * デバイスプラグインを見失ったことを通知する.
         * @param plugin 見失ったデバイスプラグイン
         */
        void onDeviceLost(DevicePlugin plugin);

        /**
         * デバイスプラグインとの接続状態が変更されたことを通知する
         * @param plugin デバイスプラグイン
         * @param state 現在の接続状態
         */
        void onConnectionStateChanged(DevicePlugin plugin, ConnectionState state);
    }
}
