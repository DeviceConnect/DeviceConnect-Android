/*
 PhoneProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.PhoneProfileConstants;

/**
 * Phone プロファイル.
 * 
 * <p>
 * 通話操作機能を提供するAPI.<br>
 * 通話操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class PhoneProfile extends DConnectProfile implements PhoneProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストから発信先の電話番号を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 発信先の電話番号。無い場合はnullを返す。
     */
    public static String getPhoneNumber(final Intent request) {
        String value = request.getStringExtra(PARAM_PHONE_NUMBER);
        return value;
    }

    /**
     * リクエストから電話のモードを取得する.
     * 
     * @param request リクエストパラメータ
     * @return モード。無い場合は{@link org.deviceconnect.profile.PhoneProfileConstants.PhoneMode#UNKNOWN}を返す。
     *         <ul>
     *         <li>{@link org.deviceconnect.profile.PhoneProfileConstants.PhoneMode#SILENT}</li>
     *         <li>{@link org.deviceconnect.profile.PhoneProfileConstants.PhoneMode#MANNER}</li>
     *         <li>{@link org.deviceconnect.profile.PhoneProfileConstants.PhoneMode#SOUND}</li>
     *         <li>{@link org.deviceconnect.profile.PhoneProfileConstants.PhoneMode#UNKNOWN}</li>
     *         </ul>
     */
    public static PhoneMode getMode(final Intent request) {
        Integer value = parseInteger(request, PARAM_MODE);
        if (value == null) {
            value = PhoneMode.UNKNOWN.getValue();
        }
        PhoneMode mode = PhoneMode.getInstance(value);
        return mode;
    }

    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにイベントオブジェクトを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param phoneStatus イベントオブジェクト
     */
    public static void setPhoneStatus(final Intent response, final Bundle phoneStatus) {
        response.putExtra(PARAM_PHONE_STATUS, phoneStatus);
    }

    /**
     * レスポンスのphoneStatusイベントオブジェクトに通話状態を設定する.
     * 
     * @param phoneStatus イベントオブジェクトのパラメータ
     * @param state 通話状態
     */
    public static void setState(final Bundle phoneStatus, final CallState state) {

        if (state == CallState.UNKNOWN) {
            throw new IllegalArgumentException("State should not be UNKNOWN.");
        }

        phoneStatus.putInt(PARAM_STATE, state.getValue());
    }

    /**
     * レスポンスのphoneStatusイベントオブジェクトに発信先の電話番号を設定する.
     * 
     * @param phoneStatus イベントオブジェクト
     * @param phoneNumber 発信先の電話番号
     */
    public static void setPhoneNumber(final Bundle phoneStatus, final String phoneNumber) {
        phoneStatus.putString(PARAM_PHONE_NUMBER, phoneNumber);
    }
}
