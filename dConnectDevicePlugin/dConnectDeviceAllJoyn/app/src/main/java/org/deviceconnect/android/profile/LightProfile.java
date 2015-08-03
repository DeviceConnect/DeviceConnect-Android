/*
LightProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;

import java.util.ArrayList;


/**
 * Light Profile.
 * <p>
 * 標準化されたProfile,Interface,Attributeのメソッドの呼び出しを行う振り分けクラス.
 * 例外処理では標準化されたエラー結果を返す.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class LightProfile extends DConnectProfile implements LightProfileConstants {

    /**
     * RGBの文字列の長さ.
     */
    private static final int RGB_LENGTH = 6;

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            return onGetLight(request, response, getServiceID(request));
        } else if (isLightGroupAttribute(request)) {
            return onGetLightGroup(request, response, getServiceID(request));
        } else {
            return onGetOther(request, response, getServiceID(request));
        }
    }

    /**
     * POSTメソッドハンドラー.
     *
     * @param request  リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスパラメータを送信するか否か
     */
    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            Float brightness = null;
            String brightnessParam = getBrightness(request);
            if (brightnessParam != null) {
                brightness = parseBrightnessParam(brightnessParam);
                if (brightness == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'brightness' must be a value between 0 and 1.0 .");
                    return true;
                }
            }

            int[] color = new int[3];
            String colorParam = getColor(request);
            if (colorParam != null) {
                if (!parseColorParam(colorParam, color)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'color' is invalid.");
                    return true;
                }
            }

            long[] flashing = null;
            if (request.hasExtra(PARAM_FLASHING)) {
                String flashingParam = getFlashing(request);
                flashing = parseFlashingParam(flashingParam);
                if (flashing == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'flashing' is invalid.");
                    return true;
                }
            }

            return onPostLight(request, response, getServiceID(request), getLightID(request),
                    brightness, color, flashing);
        } else if (isLightGroupAttribute(request)) {
            Float brightness = null;
            String brightnessParam = getBrightness(request);
            if (brightnessParam != null) {
                brightness = parseBrightnessParam(brightnessParam);
                if (brightness == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'brightness' must be a value between 0 and 1.0 .");
                    return true;
                }
            }

            int[] color = new int[3];
            String colorParam = getColor(request);
            if (colorParam != null) {
                if (!parseColorParam(colorParam, color)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'color' is invalid.");
                    return true;
                }
            }

            long[] flashing = null;
            if (request.hasExtra(PARAM_FLASHING)) {
                String flashingParam = getFlashing(request);
                flashing = parseFlashingParam(flashingParam);
                if (flashing == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'flashing' is invalid.");
                    return true;
                }
            }

            return onPostLightGroup(request, response, getServiceID(request), getGroupId(request),
                    brightness, color, flashing);
        } else if (isLightGroupCreateAttribute(request)) {
            String[] lightIDs = getLightIds(request).split(",");
            return onPostLightGroupCreate(request, response, getServiceID(request), lightIDs
                    , getGroupName(request));
        } else {
            return onPostOther(request, response, getServiceID(request));
        }
    }

    /**
     * DELETEメソッドハンドラー.<br>
     *
     * @param request  リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスパラメータを送信するか否か
     */
    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            return onDeleteLight(request, response, getServiceID(request), getLightID(request));
        } else if (isLightGroupAttribute(request)) {
            return onDeleteLightGroup(request, response, getServiceID(request), getGroupId(request));
        } else if (isLightGroupClearAttribute(request)) {
            return onDeleteLightGroupClear(request, response, getServiceID(request), getGroupId(request));
        } else {
            return onDeleteOther(request, response, getServiceID(request));
        }
    }

    /**
     * PUTメソッドハンドラー.<br>
     *
     * @param request  リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスパラメータを送信するか否か
     */
    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        if (isNullAttribute(request)) {
            String name = getName(request);

            Float brightness = null;
            String brightnessParam = getBrightness(request);
            if (brightnessParam != null) {
                brightness = parseBrightnessParam(brightnessParam);
                if (brightness == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'brightness' must be a value between 0 and 1.0 .");
                    return true;
                }
            }

            int[] color = new int[3];
            String colorParam = getColor(request);
            if (colorParam != null) {
                if (!parseColorParam(colorParam, color)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'color' is invalid.");
                    return true;
                }
            }

            long[] flashing = null;
            if (request.hasExtra(PARAM_FLASHING)) {
                String flashingParam = getFlashing(request);
                flashing = parseFlashingParam(flashingParam);
                if (flashing == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'flashing' is invalid.");
                    return true;
                }
            }

            return onPutLight(request, response, getServiceID(request), getLightID(request)
                    , name, brightness, color, flashing);
        } else if (isLightGroupAttribute(request)) {
            String name = getName(request);

            Float brightness = null;
            String brightnessParam = getBrightness(request);
            if (brightnessParam != null) {
                brightness = parseBrightnessParam(brightnessParam);
                if (brightness == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'brightness' must be a value between 0 and 1.0 .");
                    return true;
                }
            }

            int[] color = new int[3];
            String colorParam = getColor(request);
            if (colorParam != null) {
                if (!parseColorParam(colorParam, color)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'color' is invalid.");
                    return true;
                }
            }

            long[] flashing = null;
            if (request.hasExtra(PARAM_FLASHING)) {
                String flashingParam = getFlashing(request);
                flashing = parseFlashingParam(flashingParam);
                if (flashing == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Parameter 'flashing' is invalid.");
                    return true;
                }
            }

            return onPutLightGroup(request, response, getServiceID(request), getGroupId(request)
                    , name, brightness, color, flashing);
        } else {
            return onPutOther(request, response, getServiceID(request));
        }
    }

    /**
     * onGetLightメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onPostLightメソッドハンドラー.<br>
     *
     * @param request    リクエストパラメータ
     * @param response   レスポンスパラメータ
     * @param serviceId  サービスID
     * @param lightId    ライトID
     * @param brightness ライトの明るさ
     * @param color      ライトの色
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostLight(Intent request, Intent response, String serviceId, String lightId
            , Float brightness, final int[] color, long[] flashing) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onDeleteLightメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId, String lightId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onPutLightメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutLight(Intent request, Intent response, String serviceId, String lightId
            , String name, Float brightness, int[] color, long[] flashing) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onGetLightGroupメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetLightGroup(final Intent request, final Intent response, final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onPostLightGroupメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostLightGroup(Intent request, Intent response, String serviceId
            , String groupId, Float brightness, int[] color, long[] flashing) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onDeleteLightGroupメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteLightGroup(final Intent request, final Intent response, final String serviceId, String groupId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onPutLightGroupメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutLightGroup(Intent request, Intent response, String serviceId
            , String groupID, String name, Float brightness, int[] color, long[] flashing) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onPostLightGroupCreateメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostLightGroupCreate(final Intent request, final Intent response
            , final String serviceId, String[] lightIDs, String groupName) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onDeleteLightGroupClearメソッドハンドラー.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteLightGroupClear(final Intent request, final Intent response
            , final String serviceId, String groupID) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * onGetOtherメソッドハンドラー AttributeやInterfaceがある場合はコチラを継承.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetOther(final Intent request, final Intent response, final String serviceId) {
        setErrAttribute(response);
        return true;
    }

    /**
     * onPostOtherメソッドハンドラー AttributeやInterfaceがある場合はコチラを継承.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPostOther(final Intent request, final Intent response, final String serviceId) {
        setErrAttribute(response);
        return true;
    }

    /**
     * onDeleteOtherメソッドハンドラー AttributeやInterfaceがある場合はコチラを継承.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteOther(final Intent request, final Intent response, final String serviceId) {
        setErrAttribute(response);
        return true;
    }

    /**
     * onPutOtherメソッドハンドラー AttributeやInterfaceがある場合はコチラを継承.<br>
     *
     * @param request   リクエストパラメータ
     * @param response  レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutOther(final Intent request, final Intent response, final String serviceId) {
        setErrAttribute(response);
        return true;
    }

    /**
     * Attributeがnullかどうか.
     *
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    protected boolean isNullAttribute(final Intent request) {
        return getAttribute(request) == null;
    }

    /**
     * Interfaceがnullかどうか.
     *
     * @param request リクエストパラメータ
     * @return Interfaceがnullの場合はtrue
     */
    protected boolean isNullInterface(final Intent request) {
        return getInterface(request) == null;
    }

    /**
     * Attributeがlight/groupかどうか.
     *
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    protected boolean isLightGroupAttribute(final Intent request) {
        String attribute = getAttribute(request);
        return isNullInterface(request) && ATTRIBUTE_GROUP.equals(attribute);
    }

    /**
     * Attributeがlight/group/createかどうか.
     *
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    protected boolean isLightGroupCreateAttribute(final Intent request) {
        String myInterface = getInterface(request);
        String attribute = getAttribute(request);
        return INTERFACE_GROUP.equals(myInterface) && ATTRIBUTE_CREATE.equals(attribute);
    }

    /**
     * Attributeがlight/group/clearかどうか.
     *
     * @param request リクエストパラメータ
     * @return Attributeがnullの場合はtrue
     */
    protected boolean isLightGroupClearAttribute(final Intent request) {
        String myInterface = getInterface(request);
        String attribute = getAttribute(request);
        return INTERFACE_GROUP.equals(myInterface) && ATTRIBUTE_CLEAR.equals(attribute);
    }

    /**
     * NotSupportActionのレスポンスを返す.
     *
     * @param response レスポンスパラメータ
     */
    protected void setErrNotSupportAction(final Intent response) {
        MessageUtils.setNotSupportActionError(response);
    }

    /**
     * UnknownAttributeErrorのレスポンスを返す.
     *
     * @param response レスポンスパラメータ
     */
    protected void setErrAttribute(final Intent response) {
        MessageUtils.setUnknownAttributeError(response);
    }

    /**
     * InvalidRequestParameterErrorのレスポンスを返す.
     *
     * @param e        Exceptionパラメータ
     * @param response レスポンスパラメータ
     */
    protected void setErrParameter(final Exception e, final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
    }

    /**
     * UnknownErrorのレスポンスを返す.
     *
     * @param e        Exceptionパラメータ
     * @param response レスポンスパラメータ
     */
    protected void setErrUnknown(final Exception e, final Intent response) {
        MessageUtils.setUnknownError(response);
    }

    /**
     * ライトID取得.
     *
     * @param request request
     * @return lightid
     */
    private static String getLightID(final Intent request) {
        return request.getStringExtra(PARAM_LIGHT_ID);
    }

    /**
     * 名前取得.
     *
     * @param request request
     * @return myName
     */
    private static String getName(final Intent request) {
        return request.getStringExtra(PARAM_NAME);
    }

    /**
     * グループ名取得.
     *
     * @param request request
     * @return myName
     */
    private static String getGroupName(final Intent request) {
        return request.getStringExtra(PARAM_GROUP_NAME);
    }

    /**
     * ライトID取得.
     *
     * @param request request
     * @return myName
     */
    private static String getLightIds(final Intent request) {
        return request.getStringExtra(PARAM_LIGHT_IDS);
    }

    /**
     * グループID取得.
     *
     * @param request request
     * @return myName
     */
    private static String getGroupId(final Intent request) {
        return request.getStringExtra(PARAM_GROUP_ID);
    }

    /**
     * 輝度取得.
     *
     * @param request request
     * @return PARAM_BRIGHTNESS
     */
    private static String getBrightness(final Intent request) {
        return request.getStringExtra(PARAM_BRIGHTNESS);
    }

    /**
     * リクエストからcolorパラメータを取得する.
     *
     * @param request リクエスト
     * @return colorパラメータ
     */
    private static String getColor(final Intent request) {
        return request.getStringExtra(PARAM_COLOR);
    }

    private static String getFlashing(final Intent request) {
        return request.getStringExtra(PARAM_FLASHING);
    }

    /**
     * Get brightness parameter.
     *
     * @param brightnessParam brightness in string expression
     * @return Brightness parameter, or null if parameter error is encountered.
     */
    private static Float parseBrightnessParam(final String brightnessParam) {
        float brightness;
        try {
            brightness = Float.valueOf(brightnessParam);
            if (brightness > 1.0 || brightness < 0) {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return brightness;
    }

    /**
     * Get color parameter.
     *
     * @param colorParam color in string expression
     * @param color      Color parameter.
     * @return true : Success, false : failure.
     */
    private static boolean parseColorParam(final String colorParam, final int[] color) {
        try {
            String rr = colorParam.substring(0, 2);
            String gg = colorParam.substring(2, 4);
            String bb = colorParam.substring(4, 6);
            if (colorParam.length() == RGB_LENGTH) {
                if (rr == null || gg == null || bb == null) {
                    return false;
                }
                color[0] = Integer.parseInt(rr, 16);
                color[1] = Integer.parseInt(gg, 16);
                color[2] = Integer.parseInt(bb, 16);
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * フラッシュパターンを文字列から解析し、数値の配列に変換する.<br/>
     * 数値の前後の半角のスペースは無視される。その他の半角、全角のスペースは不正なフォーマットとして扱われる。
     *
     * @param pattern フラッシュパターン文字列。
     * @return 鳴動パターンの配列。解析できないフォーマットの場合nullを返す。
     */
    private static long[] parseFlashingParam(final String pattern) {

        if (pattern.length() == 0) {
            return null;
        }

        long[] result = null;

        if (pattern.contains(",")) {
            String[] times = pattern.split(",");
            ArrayList<Long> values = new ArrayList<Long>();
            for (String time : times) {
                try {
                    String valueStr = time.trim();
                    if (valueStr.length() == 0) {
                        if (values.size() != times.length - 1) {
                            // 数値の間にスペースがある場合はフォーマットエラー
                            // ex. 100, , 100
                            values.clear();
                        }
                        break;
                    }
                    long value = Long.parseLong(time.trim());
                    values.add(value);
                } catch (NumberFormatException ignored) {
                    values.clear();
                    break;
                }
            }

            if (values.size() != 0) {
                result = new long[values.size()];
                for (int i = 0; i < values.size(); ++i) {
                    result[i] = values.get(i);
                }
            }
        } else {
            try {
                long time = Long.parseLong(pattern);
                result = new long[]{time};
            } catch (NumberFormatException ignored) {
            }
        }

        return result;
    }

}
