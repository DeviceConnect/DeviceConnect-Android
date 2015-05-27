/*
 DevicePluginXmlUtil.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deviceconnect.android.BuildConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;

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
     * デバイスプラグインのxmlファイルを参照し、スコープに対応する有効期限設定値があれば返す.
     * 
     * @param context コンテキスト
     * @param packageName デバイスプラグインのパッケージ名
     * @return not null: xmlで定義されているスコープ名と有効期限[msec]が対応付けされたMap / null:
     *         有効期限設定値無し
     */
    public static Map<String, DevicePluginXmlProfile> getSupportProfiles(final Context context,
            final String packageName) {
        Map<String, DevicePluginXmlProfile> supportProfiles = null;

        ActivityInfo receiverInfo = getActivityInfo(context, packageName);
        if (receiverInfo != null) {
            if (receiverInfo.metaData != null) {
                PackageManager pkgMgr = context.getPackageManager();
                XmlResourceParser xrp = receiverInfo.loadXmlMetaData(pkgMgr, PLUGIN_META_DATA);
                try {
                    supportProfiles = parseDevicePluginXML(xrp);
                } catch (XmlPullParserException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return supportProfiles;
    }

    /**
     * コンポーネントのActivityInfoを取得する.
     * 
     * @param context コンテキスト
     * @param packageName package name
     * @return コンポーネントのActivityInfo
     */
    private static ActivityInfo getActivityInfo(final Context context, final String packageName) {
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
                            Object value = receiverInfo.metaData.get(PLUGIN_META_DATA);
                            if (value != null) {
                                return receiverInfo;
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
     * @param xrp xmlパーサ
     * @throws XmlPullParserException xmlの解析に失敗した場合に発生
     * @throws IOException xmlの読み込みに失敗した場合
     * @return プロファイル名とプロファイル情報の一覧
     */
    private static Map<String, DevicePluginXmlProfile> parseDevicePluginXML(final XmlResourceParser xrp)
            throws XmlPullParserException, IOException {
        Map<String, DevicePluginXmlProfile> list = new HashMap<String, DevicePluginXmlProfile>();

        final String tagKeyLang = "lang";
        final String tagKeyName = "name";
        final String tagKeyDescription = "description";

        DevicePluginXmlProfile profile = null;
        String nameLang = null;
        String nameText = null;
        String descriptionLang = null;
        String descriptionText = null;

        int eventType = xrp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            final String tagName = xrp.getName();

            if (tagName != null && tagName.equals("profile")) {
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
            } else if (tagName != null && tagName.equals(tagKeyName) || nameLang != null) {
                if (tagName != null && tagName.equals(tagKeyName) && eventType == XmlPullParser.START_TAG) {
                    nameLang = xrp.getAttributeValue(null, tagKeyLang);
                } else if (nameLang != null && eventType == XmlPullParser.TEXT) {
                    nameText = xrp.getText();
                } else if (tagName != null && tagName.equals(tagKeyName) && eventType == XmlPullParser.END_TAG) {
                    if (profile != null) {
                        profile.putName(nameLang, nameText);
                        nameLang = null;
                        nameText = null;
                    }
                }
            } else if (tagName != null && tagName.equals(tagKeyDescription) || descriptionLang != null) {
                if (tagName != null && tagName.equals(tagKeyDescription) && eventType == XmlPullParser.START_TAG) {
                    descriptionLang = xrp.getAttributeValue(null, tagKeyLang);
                } else if (descriptionLang != null && eventType == XmlPullParser.TEXT) {
                    descriptionText = xrp.getText();
                } else if (tagName != null && tagName.equals(tagKeyDescription) && eventType == XmlPullParser.END_TAG) {
                    if (profile != null) {
                        profile.putDescription(descriptionLang, descriptionText);
                        descriptionLang = null;
                        descriptionText = null;
                    }
                }
            }

            eventType = xrp.next();
        }
            
        return list;
    }
}
