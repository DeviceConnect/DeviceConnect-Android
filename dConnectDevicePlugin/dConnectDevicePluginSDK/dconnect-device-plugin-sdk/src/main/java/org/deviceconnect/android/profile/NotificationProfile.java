/*
 NotificationProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.profile.NotificationProfileConstants;

/**
 * Notification プロファイル.
 * 
 * <p>
 * スマートデバイスのノーティフィケーションの操作機能を提供するAPI.<br>
 * スマートデバイスのノーティフィケーションの操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class NotificationProfile extends DConnectProfile implements NotificationProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // レスポンスセッターメソッド群
    // ------------------------------------

    /**
     * レスポンスに通知IDを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param notificationId 通知ID
     */
    public static void setNotificationId(final Intent response, final String notificationId) {
        response.putExtra(PARAM_NOTIFICATION_ID, notificationId);
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストから通知タイプを取得する.
     * 
     * @param request リクエストパラメータ
     * @return 通知タイプ。無い場合は{@link NotificationType#UNKNOWN}を返す。
     *         <ul>
     *         <li>{@link NotificationType#PHONE}</li>
     *         <li>{@link NotificationType#MAIL}</li>
     *         <li>{@link NotificationType#SMS}</li>
     *         <li>{@link NotificationType#EVENT}</li>
     *         <li>{@link NotificationType#UNKNOWN}</li>
     *         </ul>
     */
    public static NotificationType getType(final Intent request) {
        Integer value = parseInteger(request, PARAM_TYPE);
        if (value == null) {
            value = NotificationType.UNKNOWN.getValue();
        }
        NotificationType type = NotificationType.getInstance(value);
        return type;
    }

    /**
     * リクエストから向きを取得する.
     * 
     * @param request リクエストパラメータ
     * @return 向きを表す文字列。無い場合は{@link Direction#UNKNOWN}を返す。
     *         <ul>
     *         <li>{@link Direction#AUTO}</li>
     *         <li>{@link Direction#RIGHT_TO_LEFT}</li>
     *         <li>{@link Direction#LEFT_TO_RIGHT}</li>
     *         <li>{@link Direction#UNKNOWN}</li>
     *         </ul>
     */
    public static Direction getDir(final Intent request) {
        String value = request.getStringExtra(PARAM_DIR);
        Direction dir = Direction.getInstance(value);
        return dir;
    }

    /**
     * リクエストから言語を取得する.
     * 
     * @param request リクエストパラメータ
     * @return 言語。無い場合はnullを返す。
     */
    public static String getLang(final Intent request) {
        String value = request.getStringExtra(PARAM_LANG);
        return value;
    }

    /**
     * リクエストから通知メッセージを取得する.
     * 
     * @param request リクエストパラメータ
     * @return 通知メッセージ。無い場合はnullを返す。
     */
    public static String getBody(final Intent request) {
        String value = request.getStringExtra(PARAM_BODY);
        return value;
    }

    /**
     * リクエストからタグを取得する.
     * 
     * @param request リクエストパラメータ
     * @return タグ。無い場合はnullを返す。
     */
    public static String getTag(final Intent request) {
        String value = request.getStringExtra(PARAM_TAG);
        return value;
    }

    /**
     * リクエストからファイルのURIを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイルのURI。無い場合はnullを返す。
     */
    public static String getUri(final Intent request) {
        return request.getStringExtra(PARAM_URI);
    }

    /**
     * リクエストからノーティフィケーションIDを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ノーティフィケーションID。無い場合はnullを返す。
     */
    public static String getNotificationId(final Intent request) {
        String value = request.getStringExtra(PARAM_NOTIFICATION_ID);
        return value;
    }

}
