/*
LightProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;


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
            int[] color = new int[3];
            Float brightness;

            brightness = getBrightnessParam(request, response);
            if (brightness == -1) {
                brightness = null;
            }
            if (!getColorParam(request, response, color)) {
                color = null;
            }
            return onPostLight(request, response, getServiceID(request), getLightID(request),
                    brightness, color);
        } else if (isLightGroupAttribute(request)) {
            return onPostLightGroup(request, response, getServiceID(request));
        } else if (isLightGroupCreateAttribute(request)) {
            return onPostLightGroupCreate(request, response, getServiceID(request));
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
            return onDeleteLightGroup(request, response, getServiceID(request));
        } else if (isLightGroupClearAttribute(request)) {
            return onDeleteLightGroupClear(request, response, getServiceID(request));
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
            return onPutLight(request, response, getServiceID(request));
        } else if (isLightGroupAttribute(request)) {
            return onPutLightGroup(request, response, getServiceID(request));
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
    protected boolean onPostLight(final Intent request, final Intent response,
                                  final String serviceId, final String lightId,
                                  final Float brightness, final int[] color) {
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
    protected boolean onPutLight(final Intent request, final Intent response, final String serviceId) {
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
    protected boolean onPostLightGroup(final Intent request, final Intent response, final String serviceId) {
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
    protected boolean onDeleteLightGroup(final Intent request, final Intent response, final String serviceId) {
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
    protected boolean onPutLightGroup(final Intent request, final Intent response, final String serviceId) {
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
    protected boolean onPostLightGroupCreate(final Intent request, final Intent response, final String serviceId) {
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
    protected boolean onDeleteLightGroupClear(final Intent request, final Intent response, final String serviceId) {
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

    /**
     * Get brightness parameter.
     *
     * @param request  request
     * @param response response
     * @return Brightness parameter, if -1, parameter error.
     */
    private static float getBrightnessParam(final Intent request, final Intent response) {
        float brightness = 0;
        if (getBrightness(request) != null) {
            try {
                brightness = Float.valueOf(getBrightness(request));
                if (brightness > 1.0 || brightness < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "brightness should be a value between 0 and 1.0");
                    return -1;
                }
            } catch (NumberFormatException e) {
                MessageUtils
                        .setInvalidRequestParameterError(response, "brightness should be a value between 0 and 1.0");
                return -1;
            }
        } else {
            brightness = 1;
        }
        return brightness;
    }

    /**
     * Get color parameter.
     *
     * @param request  request
     * @param response response
     * @param color    Color parameter.
     * @return true : Success, false : failure.
     */
    private static boolean getColorParam(final Intent request, final Intent response, final int[] color) {
        if (getColor(request) != null) {
            try {
                String colorParam = getColor(request);
                String rr = colorParam.substring(0, 2);
                String gg = colorParam.substring(2, 4);
                String bb = colorParam.substring(4, 6);
                if (colorParam.length() == RGB_LENGTH) {
                    if (rr == null || gg == null || bb == null) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        return false;
                    }
                    color[0] = Integer.parseInt(rr, 16);
                    color[1] = Integer.parseInt(gg, 16);
                    color[2] = Integer.parseInt(bb, 16);
                } else {
                    MessageUtils.setInvalidRequestParameterError(response);
                    return false;
                }
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response);
                return false;
            } catch (IllegalArgumentException e) {
                MessageUtils.setInvalidRequestParameterError(response);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

}
