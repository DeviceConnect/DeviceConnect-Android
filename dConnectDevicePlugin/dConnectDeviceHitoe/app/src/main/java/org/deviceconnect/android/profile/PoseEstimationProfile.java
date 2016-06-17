/*
 PoseEstimationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.PoseEstimationProfileConstants;

/**
 * PoseEstimation プロファイル.
 * 
 * <p>
 * スマートデバイスに対しての姿勢推定機能を提供するAPI.<br/>
 * スマートデバイスに対しての姿勢推定機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br/>
 * </p>
 * 
 * <h1>各API提供メソッド</h1>
 * <p>
 * Pose Estimation Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br/>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br/>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>Pose Estimation GET API [GET] :
 * {@link PoseEstimationProfile#onGetPoseEstimation(Intent, Intent, String)}</li>
 * <li>Pose Estimation Event API [Register] :
 * {@link PoseEstimationProfile#onPutPoseEstimation(Intent, Intent, String, String)}</li>
 * <li>Pose Estimation Event API [Unregister] :
 * {@link PoseEstimationProfile#onDeletePoseEstimation(Intent, Intent, String, String)}</li>
 * </ul>
 * @author NTT DOCOMO, INC.
 */
public class PoseEstimationProfile extends DConnectProfile implements PoseEstimationProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        String serviceId = getServiceID(request);
        if (ATTRIBUTE_ON_POSE_ESTIMATION.equals(attribute)) {
            result = onGetPoseEstimation(request, response, serviceId);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_ON_POSE_ESTIMATION.equals(attribute)) {
            String serviceId = getServiceID(request);
            String sessionKey = getSessionKey(request);
            result = onPutPoseEstimation(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }
    
    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_ON_POSE_ESTIMATION.equals(attribute)) {
            String serviceId = getServiceID(request);
            String sessionKey = getSessionKey(request);
            result = onDeletePoseEstimation(request, response, serviceId, sessionKey);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    /**
     * 姿勢推定取得リクエストハンドラー.<br/>
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onGetPoseEstimation(final Intent request,final Intent response,
            final String serviceId) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 姿勢推定コールバック登録リクエストハンドラー.<br/>
     * 姿勢推定コールバックを登録し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onPutPoseEstimation(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }

    /**
     * 姿勢推定コールバック解除リクエストハンドラー.<br/>
     * 姿勢推定コールバックを解除し、その結果をレスポンスパラメータに格納する。
     * レスポンスパラメータの送信準備が出来た場合は返り値にtrueを指定する事。
     * 送信準備ができていない場合は、返り値にfalseを指定し、スレッドを立ち上げてそのスレッドで最終的にレスポンスパラメータの送信を行う事。
     * 
     * @param request リクエストパラメータ
     * @param response レスポンスパラメータ
     * @param serviceId サービスID
     * @param sessionKey セッションキー
     * @return レスポンスパラメータを送信するか否か
     */
    protected boolean onDeletePoseEstimation(final Intent request, final Intent response,
            final String serviceId, final String sessionKey) {
        setUnsupportedError(response);
        return true;
    }




    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにPoseを設定する.
     *
     * @param response レスポンス
     * @param stress Poseオブジェクト
     */
    public static void setPose(final Intent response, final Bundle stress) {
        response.putExtra(PARAM_STRESS, stress);
    }

    /**
     * レスポンスに姿勢状態を設定する.
     *
     * @param response レスポンス
     * @param state 姿勢
     */
    public static void setState(final Bundle response, final String state) {
        response.putString(PARAM_STATE, state);
    }

    /**
     * レスポンスにTimeStamp値を設定する.
     *
     * @param response レスポンス
     * @param timeStamp TimeStamp
     */
    public static void setTimestamp(final Bundle response, final long timeStamp) {
        response.putLong(PARAM_TIMESTAMP, timeStamp);
    }
    /**
     * レスポンスにTimeStampString値を設定する.
     *
     * @param response レスポンス
     * @param timeStampString TimeStampString
     */
    public static void setTimestampString(final Bundle response, final String timeStampString) {
        response.putString(PARAM_TIMESTAMP_STRING, timeStampString);
    }


    // ------------------------------------
    // ゲッターメソッド群
    // ------------------------------------
}
