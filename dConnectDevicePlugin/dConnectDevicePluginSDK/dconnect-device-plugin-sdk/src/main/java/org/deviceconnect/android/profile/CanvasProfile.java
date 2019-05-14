/*
 CanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.profile.CanvasProfileConstants;

/**
 * Canvas プロファイル.
 *
 * <p>
 * スマートデバイスに対してのキャンバス操作機能を提供するAPI.
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class CanvasProfile extends DConnectProfile implements CanvasProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    public boolean onRequest(final Intent request, final Intent response) {
        String uri = request.getStringExtra(PARAM_URI);
        if (uri != null) {
            if (uri.startsWith("content://")) {
                byte[] data = getContentData(uri);
                request.putExtra(PARAM_DATA, data);
                request.removeExtra(PARAM_URI);
            }
        }
        return super.onRequest(request, response);
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * メッセージににファイルのMIMEタイプを設定する.
     *
     * @param response メッセージ
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Intent response, final String mimeType) {
        response.putExtra(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * レスポンスにファイルデータ一覧を設定する.
     *
     * @param response レスポンスパラメータ
     * @param data     画像ファイルのバイナリ
     */
    public static void setData(final Intent response, final Byte[] data) {
        response.putExtra(PARAM_DATA, data);
    }

    /**
     * レスポンスにX座標を設定する.
     *
     * @param response レスポンスパラメータ
     * @param x        X座標
     */
    public static void setX(final Intent response, final double x) {
        response.putExtra(PARAM_X, x);
    }

    /**
     * レスポンスにY座標を設定する.
     *
     * @param response レスポンスパラメータ
     * @param y        Y座標
     */
    public static void setY(final Intent response, final double y) {
        response.putExtra(PARAM_Y, y);
    }

    /**
     * レスポンスに画像描画モードを設定する.
     *
     * @param response レスポンスパラメータ
     * @param mode     画像描画モード
     */
    public static void setY(final Intent response, final String mode) {
        response.putExtra(PARAM_MODE, mode);
    }

    // ------------------------------------
    // ゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストからファイルのMIMEタイプを取得する.
     *
     * @param request リクエストパラメータ
     * @return ファイルのMIMEタイプ。無い場合はnullを返す。
     */
    public static String getMIMEType(final Intent request) {
        return request.getStringExtra(PARAM_MIME_TYPE);
    }

    /**
     * リクエストから画像ファイルのURIを取得する.
     *
     * @param request リクエストパラメータ
     * @return 画像ファイルのURI。無い場合はnullを返す。
     */
    public static String getURI(final Intent request) {
        return request.getStringExtra(PARAM_URI);
    }

    /**
     * リクエストから画像ファイルのバイナリを取得する.
     *
     * @param request リクエストパラメータ
     * @return 画像ファイルのバイナリ。無い場合はnullを返す。
     */
    public static byte[] getData(final Intent request) {
        return request.getByteArrayExtra(PARAM_DATA);
    }

    /**
     * リクエストからX座標を取得する.
     *
     * @param request リクエストパラメータ
     * @return X座標。無い場合は0.0を返す。
     */
    public static double getX(final Intent request) {
        Double x = parseDouble(request, PARAM_X);
        if (x == null) {
            x = 0.0;
        }
        return x;
    }

    /**
     * リクエストからY座標を取得する.
     *
     * @param request リクエストパラメータ
     * @return Y座標。無い場合は0.0を返す。
     */
    public static double getY(final Intent request) {
        Double y = parseDouble(request, PARAM_Y);
        if (y == null) {
            y = 0.0;
        }
        return y;
    }

    /**
     * リクエストから画像描画モードを取得する.
     *
     * @param request リクエストパラメータ
     * @return 画像描画モード。無い場合はnullを返す。
     */
    public static String getMode(final Intent request) {
        return request.getStringExtra(PARAM_MODE);
    }

    /**
     * check MimeType format.
     *
     * @param mimeType MimeType
     * @return true: OK / false: ERROR
     */
    protected boolean checkMimeTypeFormat(final String mimeType) {
        final String pattern = "^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$";
        return mimeType.matches(pattern);
    }

    /**
     * check x value format.
     *
     * @param request request
     * @return true: check OK or nothing / false: check ERROR
     */
    protected boolean checkXFormat(final Intent request) {
        if (request.getStringExtra(PARAM_X) != null) {
            return parseDouble(request, PARAM_X) != null;
        } else {
            // nothing.
            return true;
        }
    }

    /**
     * check y value format.
     *
     * @param request request
     * @return true: check OK or nothing / false: check ERROR
     */
    protected boolean checkYFormat(final Intent request) {
        if (request.getStringExtra(PARAM_Y) != null) {
            return parseDouble(request, PARAM_Y) != null;
        } else {
            // nothing.
            return true;
        }
    }

}



