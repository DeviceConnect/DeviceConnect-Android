/*
 DevicePluginXmlUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;

import org.deviceconnect.android.BuildConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * デバイスプラグインxml関連ユーティリティ.
 * @author NTT DOCOMO, INC.
 */
public final class DevicePluginXmlUtil {

    /** デバイスプラグインに格納されるメタタグ名. */
    private static final String PLUGIN_META_DATA = "org.deviceconnect.android.deviceplugin";

    /**
     * コンストラクタ.
     */
    private DevicePluginXmlUtil() {
    }

    /**
     * デバイスプラグインのxmlファイルの内容を取得する.
     *
     * @param context コンテキスト
     * @param pluginComponent デバイスプラグインを宣言するコンポーネント
     * @return {@link DevicePluginXml}クラスのインスタンス
     */
    public static DevicePluginXml getXml(final Context context,
                                         final ComponentInfo pluginComponent) {
        PackageManager pkgMgr = context.getPackageManager();
        XmlResourceParser xrp = pluginComponent.loadXmlMetaData(pkgMgr, PLUGIN_META_DATA);
        try {
            if (xrp != null) {
                int xmlId = pluginComponent.metaData.getInt(PLUGIN_META_DATA);
                return parseDevicePluginXML(xmlId, xrp);
            }
        } catch (XmlPullParserException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * デバイスプラグインのxmlファイルを参照し、スコープに対応する有効期限設定値があれば返す.
     *
     * @param context コンテキスト
     * @param packageName デバイスプラグインのパッケージ名
     * @return not null: xmlで定義されているスコープ名と有効期限[msec]が対応付けされたMap / null:
     *         有効期限設定値無し
     */
    public static Map<String, DevicePluginXmlProfile> getSupportProfiles(final Context context,
                                                                         final String packageName) {
        ComponentInfo pluginComponent = getComponentInfo(context, packageName);
        return getSupportProfiles(context, pluginComponent);
    }

    /**
     * デバイスプラグインのxmlファイルを参照し、スコープに対応する有効期限設定値があれば返す.
     *
     * @param context コンテキスト
     * @param pluginComponent デバイスプラグインを宣言するコンポーネント
     * @return not null: xmlで定義されているスコープ名と有効期限[msec]が対応付けされたMap / null:
     *         有効期限設定値無し
     */
    public static Map<String, DevicePluginXmlProfile> getSupportProfiles(final Context context,
            final ComponentInfo pluginComponent) {
        if (pluginComponent != null) {
            if (pluginComponent.metaData != null) {
                DevicePluginXml xml = getXml(context, pluginComponent);
                if (xml != null) {
                    return xml.getSupportedProfiles();
                }
            }
        }
        return null;
    }

    private static ComponentInfo getComponentInfo(final Context context, final String packageName) {
        ComponentInfo compInfo = getServiceInfo(context, packageName);
        if (compInfo != null) {
            return compInfo;
        }
        return getReceiverInfo(context, packageName);
    }

    private static ServiceInfo getServiceInfo(final Context context, final String packageName) {
        try {
            PackageManager pkgMgr = context.getPackageManager();
            PackageInfo pkg = pkgMgr.getPackageInfo(packageName, PackageManager.GET_SERVICES);
            if (pkg != null) {
                ServiceInfo[] services = pkg.services;
                if (services != null) {
                    for (int i = 0; i < services.length; i++) {
                        String pkgName = services[i].packageName;
                        String className = services[i].name;
                        ComponentName component = new ComponentName(pkgName, className);
                        ServiceInfo serviceInfo = pkgMgr.getServiceInfo(component, PackageManager.GET_META_DATA);
                        if (serviceInfo.metaData != null) {
                            Object xmlData = serviceInfo.metaData.get(PLUGIN_META_DATA);
                            if (xmlData instanceof Integer) {
                                XmlResourceParser xrp = serviceInfo.loadXmlMetaData(pkgMgr, PLUGIN_META_DATA);
                                if (xrp != null) {
                                    return serviceInfo;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * コンポーネントのActivityInfoを取得する.
     * 
     * @param context コンテキスト
     * @param packageName package name
     * @return コンポーネントのActivityInfo
     */
    private static ActivityInfo getReceiverInfo(final Context context, final String packageName) {
        try {
            PackageManager pkgMgr = context.getPackageManager();
            PackageInfo pkg = pkgMgr.getPackageInfo(packageName, PackageManager.GET_RECEIVERS);
            if (pkg != null) {
                ActivityInfo[] receivers = pkg.receivers;
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        String pkgName = receivers[i].packageName;
                        String className = receivers[i].name;
                        ComponentName component = new ComponentName(pkgName, className);
                        ActivityInfo receiverInfo = pkgMgr.getReceiverInfo(component, PackageManager.GET_META_DATA);
                        if (receiverInfo.metaData != null) {
                            Object xmlData = receiverInfo.metaData.get(PLUGIN_META_DATA);
                            if (xmlData instanceof Integer) {
                                XmlResourceParser xrp = receiverInfo.loadXmlMetaData(pkgMgr, PLUGIN_META_DATA);
                                if (xrp != null) {
                                    return receiverInfo;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * xml/deviceplugin.xmlの解析を行う.
     *
     * @param resId XMLファイルのリソースID
     * @param xrp XMLパーサ
     * @throws XmlPullParserException XMLファイルの解析に失敗した場合に発生
     * @throws IOException XMLファイルの読み込みに失敗した場合
     * @return {@link DevicePluginXml}クラスのインスタンス
     */
    private static DevicePluginXml parseDevicePluginXML(final int resId,
                                                        final XmlResourceParser xrp)
            throws XmlPullParserException, IOException {
        Map<String, DevicePluginXmlProfile> list = new HashMap<String, DevicePluginXmlProfile>();

        final String tagKeyLang = "lang";
        final String tagKeyName = "name";
        final String tagKeyDescription = "description";

        DevicePluginXml xml = null;
        DevicePluginXmlProfile profile = null;
        String nameLang = null;
        String nameText = null;
        String descriptionLang = null;
        String descriptionText = null;
        String specPath = null;

        int eventType = xrp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            final String tagName = xrp.getName();

            if ("deviceplugin-provider".equals(tagName)) {
                if (eventType == XmlPullParser.START_TAG) {
                    xml = new DevicePluginXml(resId);
                    specPath = xrp.getAttributeValue(null, "spec-path");
                }
            } else if ("profile".equals(tagName)) {
                if (eventType == XmlPullParser.START_TAG) {
                    String profileName = xrp.getAttributeValue(null, "name");
                    String strExpirePeriod = xrp.getAttributeValue(null, "expireperiod");
                    long expirePeriod = LocalOAuth2Settings.DEFAULT_TOKEN_EXPIRE_PERIOD; /* 秒単位 */
                    if (strExpirePeriod != null) { /* expireperiodが設定されている */
                        try {
                            /* 分単位 */
                            expirePeriod = Long.parseLong(strExpirePeriod);
                            /* 秒単位に変換 */
                            expirePeriod *= LocalOAuth2Settings.MINUTE;

                        } catch (NumberFormatException e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }
                    }

                    /* profileデータ初期化してプロファイル名と有効期限を設定 */
                    profile = new DevicePluginXmlProfile(profileName, expirePeriod);
                } else if (eventType == XmlPullParser.END_TAG) {
                    /* 有効期限がマイナス値なら格納しない */
                    if (profile != null && profile.getExpirePeriod() >= 0) {
                        list.put(profile.getProfile(), profile);
                        profile = null;
                    }
                }
            } else if (tagKeyName.equals(tagName) || nameLang != null) {
                if (tagKeyName.equals(tagName) ) {
                    if (eventType == XmlPullParser.START_TAG) {
                        nameLang = xrp.getAttributeValue(null, tagKeyLang);
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (profile != null) {
                            profile.putName(nameLang, nameText);
                            nameLang = null;
                            nameText = null;
                        }
                    }
                } else if (nameLang != null && eventType == XmlPullParser.TEXT) {
                    nameText = xrp.getText();
                }
            } else if (tagKeyDescription.equals(tagName) || descriptionLang != null) {
                if (tagKeyDescription.equals(tagName)) {
                    if (eventType == XmlPullParser.START_TAG) {
                        descriptionLang = xrp.getAttributeValue(null, tagKeyLang);
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (profile != null) {
                            profile.putDescription(descriptionLang, descriptionText);
                            descriptionLang = null;
                            descriptionText = null;
                        }
                    }
                } else if (descriptionLang != null && eventType == XmlPullParser.TEXT) {
                    descriptionText = xrp.getText();
                }
            }

            eventType = xrp.next();
        }

        if (xml == null) {
            return null;
        }
        xml.mSpecPath = specPath;
        xml.mSupportedProfiles = list;
        for (DevicePluginXmlProfile xmlProfile : list.values()) {
            xmlProfile.setSpecPath(specPath);
        }
        return xml;
    }
}
