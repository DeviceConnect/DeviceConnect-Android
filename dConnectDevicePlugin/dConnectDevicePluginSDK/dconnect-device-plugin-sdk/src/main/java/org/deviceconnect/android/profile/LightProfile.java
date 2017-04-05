/*
LightProfile
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import org.deviceconnect.profile.LightProfileConstants;

import java.util.List;

/**
 * Light プロファイル. 
 * <p>
 * スマートデバイス上のライトを操作要求するAPI.
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class LightProfile extends DConnectProfile implements LightProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * 明るさのチェックする.
     * <p>
     * brightnessが0より小さい、または、1.0よりも大きい場合にはエラーとする。
     * </p>
     * <p>
     * brightnessがnullの場合には無視する。
     * </p>
     * @param brightness 明るさ
     * @return 範囲外の場合はtrue、それ以外はfalse
     */
    private static boolean checkBrightness(final Double brightness) {
        return (brightness != null && (brightness < 0.0 || brightness > 1.0));
    }

    /**
     * Attributeがnullかどうか.
     * 
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    private boolean isNullAttribute(final Intent request) {
        return getAttribute(request) == null;
    }

    /**
     * Interfaceがnullかどうか.
     * 
     * @param request リクエストパラメータ
     * @return Interfaceがnullの場合はtrue
     */
    private boolean isNullInterface(final Intent request) {
        return getInterface(request) == null;
    }

    /**
     * Attributeがlight/groupかどうか.
     * 
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    private boolean isLightGroupAttribute(final Intent request) {
        String attribute = getAttribute(request);
        return isNullInterface(request) && ATTRIBUTE_GROUP.equalsIgnoreCase(attribute);
    }

    /**
     * Attributeがlight/group/createかどうか.
     * 
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    private boolean isLightGroupCreateAttribute(final Intent request) {
        String myInterface = getInterface(request);
        String attribute = getAttribute(request);
        return INTERFACE_GROUP.equalsIgnoreCase(myInterface)
            && ATTRIBUTE_CREATE.equalsIgnoreCase(attribute);
    }

    /**
     * Attributeがlight/group/clearかどうか.
     * 
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    private boolean isLightGroupClearAttribute(final Intent request) {
        String myInterface = getInterface(request);
        String attribute = getAttribute(request);
        return INTERFACE_GROUP.equalsIgnoreCase(myInterface)
            && ATTRIBUTE_CLEAR.equalsIgnoreCase(attribute);
    }

    /**
     * リクエストからlightIdを取得する.
     * <p>
     * lightIdが省略された場合にはnullを返却する。
     * </p>
     * @param request リクエスト
     * @return lightId
     */
    public static final String getLightId(final Intent request) {
        return request.getStringExtra(PARAM_LIGHT_ID);
    }

    /**
     * リクエストからgroupIdを取得する.
     * <p>
     * 省略された場合にはnullを返却する。
     * </p>
     * @deprecated 廃止します。
     * @param request リクエスト
     * @return groupId
     */
    public static final String getGroupId(final Intent request) {
        return request.getStringExtra(PARAM_GROUP_ID);
    }

    /**
     * リクエストからlightIdsを取得する.
     * <p>
     * 省略された場合にはnullを返却する。
     * </p>
     * @param request リクエスト
     * @return lightIds
     */
    public static final String[] getLightIds(final Intent request) {
        String lightIds = request.getStringExtra(PARAM_LIGHT_IDS);
        if (lightIds == null) {
            return null;
        }
        return lightIds.split(",");
    }

    /**
     * リクエストからライト名を取得する.
     * <p>
     * ライト名が省略された場合にはnullを返却する。
     * </p>
     * @param request リクエスト
     * @return ライト名
     */
    public static final String getName(final Intent request) {
        return request.getStringExtra(PARAM_NAME);
    }

    /**
     * リクエストからgroupNameを取得する.
     * <p>
     * 省略された場合にはnullを返却する。
     * </p>
     * @deprecated 廃止します。
     * @param request リクエスト
     * @return groupName
     */
    public static final String getGroupName(final Intent request) {
        return request.getStringExtra(PARAM_GROUP_NAME);
    }

    /**
     * リクエストから色を取得する.
     * <p>
     * 色が指定されていない場合にはnullを返却する。
     * </p>
     * <p>
     * 16進数のRGB形式になっていない場合には、IllegalArgumentExceptionを発生する。
     * </p>
     * @param request リクエスト
     * @return 色情報
     * @throws IllegalArgumentException 色指定がフォーマットエラーの場合に発生
     */
    public static final Integer getColor(final Intent request) {
        Bundle bundle = request.getExtras();
        if (bundle == null) {
            return null;
        }
        String color = bundle.getString(PARAM_COLOR);
        if (color == null) {
            return null;
        }
        if (color.length() != 6) {
            throw new IllegalArgumentException("color is invalid.");
        }
        try {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            return Color.rgb(r, g, b);
        } catch (Exception e) {
            throw new IllegalArgumentException("color is invalid.");
        }
    }

    /**
     * ライトの明るさを取得する.
     * <p>
     * 省略された場合にはnullを返却する。
     * </p>
     * @param request リクエスト
     * @return ライトの明るさ
     * @throws IllegalArgumentException brightnessのフォーマットが不正な場合に発生
     */
    public static final Double getBrightness(final Intent request) {
        Bundle bundle = request.getExtras();
        if (bundle == null) {
            return null;
        }

        Object param = bundle.get(PARAM_BRIGHTNESS);
        if (param == null) {
            return null;
        }

        try {
            Double brightness = null;
            if (param instanceof String) {
                brightness = Double.parseDouble((String) param);
            } else {
                brightness = (Double) param;
            }
            if (checkBrightness(brightness)) {
                throw new IllegalArgumentException("brightness should be a value between 0 and 1.0");
            }
            return brightness;
        } catch (Exception e) {
            throw new IllegalArgumentException("brightness is invalid.");
        }
    }

    /**
     * 点滅間隔を取得する.
     * <p>
     * flashingが省略された場合にはnullを返却する。
     * </p>
     * @param request リクエスト
     * @return 点滅間隔
     * @throws IllegalArgumentException flashingのフォーマットが不正な場合に発生
     */
    public static final long[] getFlashing(final Intent request) {
        String flashing = request.getStringExtra(PARAM_FLASHING);
        if (flashing == null) {
            return null;
        }
        if (flashing.length() == 0) {
            throw new IllegalArgumentException("flashing is invalid.");
        }
        String[] split = flashing.split(",");
        long[] list = new long[split.length];
        for (int i = 0; i < split.length; i++) {
            try {
                list[i] = Integer.parseInt(split[i]);
                if (list[i] <= 0) {
                    throw new IllegalArgumentException("flashing is negative value.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("flashing is invalid.");
            }
        }
        return list;
    }

    /**
     * レスポンスにライト情報一覧を設定する.
     * 
     * @param response レスポンスデータ
     * @param lights ライト情報一覧
     */
    public static final void setLights(final Intent response, final List<Bundle> lights) {
        Bundle[] bundles = new Bundle[lights.size()];
        lights.toArray(bundles);
        setLights(response, bundles);
    }

    /**
     * レスポンスにライト情報一覧を設定する.
     * 
     * @param response レスポンスデータ
     * @param lights ライト情報一覧
     */
    public static final void setLights(final Intent response, final Bundle[] lights) {
        response.putExtra(PARAM_LIGHTS, lights);
    }

    /**
     * レスポンスにライト情報一覧を設定する.
     * 
     * @param group レスポンスデータ
     * @param lights ライト情報一覧
     */
    public static final void setLights(final Bundle group, final List<Bundle> lights) {
        Bundle[] bundles = new Bundle[lights.size()];
        lights.toArray(bundles);
        setLights(group, bundles);
    }

    /**
     * レスポンスにライト情報一覧を設定する.
     * 
     * @param group レスポンスデータ
     * @param lights ライト情報一覧
     */
    public static final void setLights(final Bundle group, final Bundle[] lights) {
        group.putParcelableArray(PARAM_LIGHTS, lights);
    }

    /**
     * レスポンスにライトIDを設定する.
     * 
     * @param response レスポンスデータ
     * @param lightId ライトID
     */
    public static final void setLightId(final Intent response, final String lightId) {
        response.putExtra(PARAM_LIGHT_ID, lightId);
    }

    /**
     * ライト情報にライトIDを設定する.
     * 
     * @param light レスポンスデータ
     * @param lightId ライトID
     */
    public static final void setLightId(final Bundle light, final String lightId) {
        light.putString(PARAM_LIGHT_ID, lightId);
    }


    /**
     * レスポンスに名前を設定する.
     * 
     * @param response レスポンスデータ
     * @param name 名前
     */
    public static final void setName(final Intent response, final String name) {
        response.putExtra(PARAM_NAME, name);
    }

    /**
     * ライト情報に名前を設定する.
     * 
     * @param light ライト情報
     * @param name 名前
     */
    public static final void setName(final Bundle light, final String name) {
        light.putString(PARAM_NAME, name);
    }

    /**
     * ライト情報に点灯状態を設定する.
     * 
     * @param light ライト情報
     * @param on 点灯状態 (true: 点灯 false: 消灯)
     */
    public static final void setOn(final Bundle light, final boolean on) {
        light.putBoolean(PARAM_ON, on);
    }

    /**
     * ライト情報に固有の設定を設定する.
     * 
     * @param light ライト情報
     * @param config 設定
     */
    public static final void setConfig(final Bundle light, final String config) {
        light.putString(PARAM_CONFIG, config);
    }

    /**
     * レスポンスにライトグループ一覧を設定する.
     *
     * @deprecated 廃止します。
     * @param response レスポンスデータ
     * @param lightGroups ライトグループ一覧
     */
    public static final void setLightGroups(final Intent response, final List<Bundle> lightGroups) {
        Bundle[] bundles = new Bundle[lightGroups.size()];
        lightGroups.toArray(bundles);
        setLightGroups(response, bundles);
    }

    /**
     * レスポンスにライトグループ一覧を設定する.
     *
     * @deprecated 廃止します。
     * @param response レスポンスデータ
     * @param lightGroups ライトグループ一覧
     */
    public static final void setLightGroups(final Intent response, final Bundle[] lightGroups) {
        response.putExtra(PARAM_LIGHT_GROUPS, lightGroups);
    }

    /**
     * レスポンスにグループIDを設定する.
     *
     * @deprecated 廃止します。
     * @param response レスポンスデータ
     * @param groupId グループID
     */
    public static final void setGroupId(final Intent response, final String groupId) {
        response.putExtra(PARAM_GROUP_ID, groupId);
    }

    /**
     * ライトグループ情報にライトグループIDを設定する.
     *
     * @deprecated 廃止します。
     * @param lightGroup ライトグループ情報
     * @param groupId グループID
     */
    public static final void setGroupId(final Bundle lightGroup, final String groupId) {
        lightGroup.putString(PARAM_GROUP_ID, groupId);
    }

    /**
     * ライトグループ情報にライトグループ名を設定する.
     *
     * @deprecated 廃止します。
     * @param lightGroup ライトグループ情報
     * @param name グループ名前
     */
    public static final void setGroupName(final Bundle lightGroup, final String name) {
        lightGroup.putString(PARAM_NAME, name);
    }

    /**
     * ライトグループ情報にライトグループの設定を設定する.
     *
     * @deprecated 廃止します。
     * @param lightGroup ライトグループ情報
     * @param config グループの設定
     */
    public static final void setGroupConfig(final Bundle lightGroup, final String config) {
        lightGroup.putString(PARAM_CONFIG, config);
    }
}
