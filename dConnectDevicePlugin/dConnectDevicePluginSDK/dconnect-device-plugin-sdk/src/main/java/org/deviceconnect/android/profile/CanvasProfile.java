/*
 CanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.CanvasProfileConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Canvas プロファイル.
 *
 * <p>
 * スマートデバイスに対してのキャンバス操作機能を提供するAPI.<br>
 * スマートデバイスに対してのキャンバス操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * <h1>各API提供メソッド</h1>
 * <p>
 * Canvas Profile の各APIへのリクエストに対し、以下のコールバックメソッド群が自動的に呼び出される。<br>
 * サブクラスは以下のメソッド群からデバイスプラグインが提供するAPI用のメソッドをオーバーライドし、機能を実装すること。<br>
 * オーバーライドされていない機能は自動的に非対応APIとしてレスポンスを返す。
 * </p>
 * <ul>
 * <li>Canvas DrawImage API [POST] :
 * {@link CanvasProfile#onPostDrawImage(Intent, Intent, String, String, byte[], double, double, String)}</li>
 * <li>Canvas DrawImage API [DELETE] :
 * {@link CanvasProfile#onDeleteDrawImage(Intent, Intent, String))}</li>
 * </ul>
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class CanvasProfile extends DConnectProfile implements CanvasProfileConstants {

    /**
     * コンストラクタ.
     */
    public CanvasProfile() {
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

//    @Override
//    protected boolean onPostRequest(final Intent request, final Intent response) {
//        String attribute = getAttribute(request);
//        boolean result = true;
//
//        if (ATTRIBUTE_DRAW_IMAGE.equals(attribute)) {
//            String serviceId = getServiceID(request);
//            String mimeType = getMIMEType(request);
//            String uri = request.getStringExtra(PARAM_URI);
//            byte[] data = null;
//            if (uri != null) {
//                if (uri.startsWith("content://")) {
//                    data = getContentData(uri);
//                    uri = null;
//                }
//            }
//
//            if (data == null && uri == null) {
//                MessageUtils.setInvalidRequestParameterError(response, "not found data.");
//                return result;
//            }
//            if (mimeType != null && !checkMimeTypeFormat(mimeType)) {
//                MessageUtils.setInvalidRequestParameterError(response, "mimeType format is incorrect.");
//                return result;
//            }
//            if (!checkXFormat(request)) {
//                MessageUtils.setInvalidRequestParameterError(response, "x is different type.");
//                return result;
//            }
//            if (!checkYFormat(request)) {
//                MessageUtils.setInvalidRequestParameterError(response, "y is different type.");
//                return result;
//            }
//
//            double x = getX(request);
//            double y = getY(request);
//            String mode = getMode(request);
//            result = onPostDrawImage(request, response, serviceId, mimeType, data, uri, x, y, mode);
//        } else {
//            MessageUtils.setUnknownAttributeError(response);
//        }
//
//        return result;
//    }

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

    protected byte[] getData(String uri) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        byte[] data = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            inputStream = connection.getInputStream();
            data = readAll(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        return data;
    }

    private byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = inputStream.read(buffer);
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }

}



