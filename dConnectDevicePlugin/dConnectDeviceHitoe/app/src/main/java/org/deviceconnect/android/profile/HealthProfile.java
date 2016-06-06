/*
 HealthProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.HealthProfileConstants;

/**
 * Health プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての健康機器操作機能を提供するAPI.<br/>
 * スマートデバイスに対しての健康機器操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Health Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>Heart Rate GET API [GET] :
 * {@link HealthProfile#onGetHeartRate(Intent, Intent, String)}</li>
 * <li>Heart Rate Event API [Register] :
 * {@link HealthProfile#onPutOnHeartRate(Intent, Intent, String, String)}</li>
 * <li>Heart Rate Event API [Unregister] :
 * {@link HealthProfile#onDeleteOnHeartRate(Intent, Intent, String, String)}</li>
 * </ul>
 * @author NTT DOCOMO, INC.
 */
public class HealthProfile extends DConnectProfile implements HealthProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        String serviceId = getServiceID(request);
        if (ATTRIBUTE_HEART_RATE.equals(attribute)) {
            result = onGetHeartRate(request, response, serviceId);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_HEART_RATE.equals(attribute)) {
            String serviceId = getServiceID(request);
            String sessionKey = getSessionKey(request);
            result = onPutHeartRate(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }
    
    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_HEART_RATE.equals(attribute)) {
            String serviceId = getServiceID(request);
            String sessionKey = getSessionKey(request);
            result = onDeleteHeartRate(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    /**
     * heartreate属性取得リクエストハンドラー.<br/>
     * スマートフォンまたは周辺機器上のテキストや画像、音声、動画（リソースも含む）のデータを提供し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetHeartRate(final Intent request,final Intent response,
            final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * heartrateコールバック登録リクエストハンドラー.<br/>
     * heartrateコールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutHeartRate(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * heartrateコールバック解除リクエストハンドラー.<br/>
     * heartrateコールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeleteHeartRate(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに心拍数を設定する.
     * 
     * @param response レスポンス
     * @param heartRate 心拍数
     */
    public static void setHeartRate(final Intent response, final int heartRate) {
        response.putExtra(PARAM_HEART_RATE, heartRate);
    }

    // ------------------------------------
    // ゲッターメソッド群
    // ------------------------------------
}
