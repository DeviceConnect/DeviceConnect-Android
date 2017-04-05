/*
 ConnectionProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.ConnectionProfileConstants;

/**
 * Connection プロファイル.
 * 
 * <p>
 * スマートデバイスとのネットワーク接続情報を提供するAPI.<br>
 * ネットワーク接続情報を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * AndroidManifest.xmlに追加する必要の有るパーミッション： wifi: ACCESS_WIFI_STATE,
 * CHANGE_WIFI_STATE bluetooth: BLUETOOTH, BLUETOOTH_ADMIN nfc: NFC
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class ConnectionProfile extends DConnectProfile implements ConnectionProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに接続状態フラグを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param enable ON:true、OFF:false
     */
    public static void setEnable(final Intent response, final boolean enable) {
        response.putExtra(PARAM_ENABLE, enable);
    }

    /**
     * メッセージに接続状態を設定する.
     * 
     * @param message メッセージパラメータ
     * @param connectStatus 接続状態パラメータ
     */
    public static void setConnectStatus(final Intent message, final Bundle connectStatus) {
        message.putExtra(PARAM_CONNECT_STATUS, connectStatus);
    }

    /**
     * 接続状態パラメータに接続状態フラグを設定する.
     * 
     * @param connectStatus 接続状態パラメータ
     * @param enable ON: true、OFF: false
     */
    public static void setEnable(final Bundle connectStatus, final boolean enable) {
        connectStatus.putBoolean(PARAM_ENABLE, enable);
    }
}
