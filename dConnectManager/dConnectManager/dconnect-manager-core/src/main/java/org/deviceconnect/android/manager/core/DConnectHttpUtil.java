/*
 DConnectHttpUtil.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;

import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.server.http.HttpRequest;
import org.deviceconnect.server.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;

/**
 * HTTPの操作を行うためのユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
final class DConnectHttpUtil {

    /**
     * HTTPリクエストのセグメント数(APIのみ) {@value}.
     */
    private static final int SEGMENT_API = 1;

    /**
     * HTTPリクエストのセグメント数(Profileのみ) {@value}.
     */
    private static final int SEGMENT_PROFILE = 2;

    /**
     * HTTPリクエストのセグメント数(ProfilesとAttribute) {@value}.
     */
    private static final int SEGMENT_ATTRIBUTE = 3;

    /**
     * HTTPリクエストのセグメント数(ProfileとInterfacesとAttribute) {@value}.
     */
    private static final int SEGMENT_INTERFACES = 4;

    /**
     * コンストラクタ.
     * <p>
     * ユーティリティクラスなので、private.
     * </p>
     */
    private DConnectHttpUtil() {}

    /**
     * HTTPリクエストを解析して、Intentのリクエストに変換します.
     * <p>
     * Device Connect のリクエストに変換できない場合に、nullを返却します。<br>
     * その場合には、response には、HTTP のエラーを格納してあります。
     * </p>
     * @param context コンテキスト
     * @param fileMgr ファイル管理クラス
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @return リクエスト情報を格納したIntent、
     */
    static Intent convertHttp2Intent(final Context context, final FileManager fileMgr, final HttpRequest request, final HttpResponse response) {
        final int requestCode = UUID.randomUUID().hashCode();

        String[] paths = parsePath(request);
        Map<String, String> parameters = request.getQueryParameters();
        Map<String, String> files = request.getFiles();
        String method = request.getMethod().name();

        String api = null;
        String httpMethod = null;
        String profile = null;
        String interfaces = null;
        String attribute = null;
        boolean existMethod = isHttpMethodIncluded(paths);

        if (existMethod) {
            // HTTPメソッドがパスに含まれている
            if (paths.length == SEGMENT_API) {
                api = paths[0];
            } else if (paths.length == SEGMENT_PROFILE) {
                api = paths[0];
                profile = paths[1];
            } else if (paths.length == SEGMENT_ATTRIBUTE) {
                api = paths[0];
                httpMethod = paths[1];
                profile = paths[2];
            } else if (paths.length == SEGMENT_INTERFACES) {
                api = paths[0];
                httpMethod = paths[1];
                profile = paths[2];
                attribute = paths[3];
            } else if (paths.length == (SEGMENT_INTERFACES + 1)) {
                api = paths[0];
                httpMethod = paths[1];
                profile = paths[2];
                interfaces = paths[3];
                attribute = paths[4];
            }
        } else {
            // HTTPメソッドがパスに含まれていない
            if (paths.length == SEGMENT_API) {
                api = paths[0];
            } else if (paths.length == SEGMENT_PROFILE) {
                api = paths[0];
                profile = paths[1];
            } else if (paths.length == SEGMENT_ATTRIBUTE) {
                api = paths[0];
                profile = paths[1];
                attribute = paths[2];
            } else if (paths.length == SEGMENT_INTERFACES) {
                api = paths[0];
                profile = paths[1];
                interfaces = paths[2];
                attribute = paths[3];
            }
        }

        if (api == null) {
            // apiが存在しない場合はエラー
            response.setCode(HttpResponse.StatusCode.BAD_REQUEST);
            setErrorResponse(response, 19, "api is empty.");
            return null;
        }

        // プロファイルが存在しない場合にはエラー
        if (profile == null) {
            response.setCode(HttpResponse.StatusCode.BAD_REQUEST);
            setErrorResponse(response, 19, "profile is empty.");
            return null;
        } else if (isMethod(profile)) { // Profile名がhttpMethodの場合
            response.setCode(HttpResponse.StatusCode.BAD_REQUEST);
            setInvalidProfile(response);
            return null;
        }

        // Httpメソッドに対応するactionを取得
        String action = DConnectUtil.convertHttpMethod2DConnectMethod(method);
        if (action == null) {
            response.setCode(HttpResponse.StatusCode.NOT_IMPLEMENTED);
            setErrorResponse(response, 1, "Not implements a http method.");
            return null;
        }

        // URLにmethodが指定されている場合は、そちらのHTTPメソッドを優先する
        if (httpMethod != null) {
            if (action.equals(IntentDConnectMessage.ACTION_GET)) {
                action = DConnectUtil.convertHttpMethod2DConnectMethod(httpMethod.toUpperCase());
            } else {
                // 元々のHTTPリクエストがGET以外の場合はエラーを返す.
                setInvalidURL(response);
                return null;
            }
        }

        // files の時は、Device Connect Managerまでは渡さずに、ここで処理を行う
        if ("files".equalsIgnoreCase(profile)) {
            if (request.getMethod().equals(HttpRequest.Method.GET)) {
                serveFile(context, request, response);
            } else {
                response.setCode(HttpResponse.StatusCode.BAD_REQUEST);
                setErrorResponse(response, 1, "Not implements a method.");
            }
            return null;
        }

        Intent intent = new Intent(action);
        intent.setClass(context, DConnectService.class);
        intent.putExtra(IntentDConnectMessage.EXTRA_API, api);
        intent.putExtra(IntentDConnectMessage.EXTRA_PROFILE, profile);
        if (interfaces != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_INTERFACE, interfaces);
        }
        if (attribute != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ATTRIBUTE, attribute);
        }
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                intent.putExtra(key, parameters.get(key));
            }
        }
        if (files != null && parameters != null) {
            // MEMO: Device Connect の仕様としてリクエストに添付できるファイルは1つと定義されています。
            // もしも、仕様が変更された場合には、ここを修正すること。
            for (String key : files.keySet()) {
                String v = files.get(key);
                if (v != null && !v.isEmpty()) {
                    String uri = fileMgr.getContentUri() + "/" + v.substring(v.lastIndexOf('/') + 1);
                    String fileName = parameters.get(key);
                    if (fileName != null) {
                        intent.putExtra(FileProfileConstants.PARAM_FILE_NAME, fileName);
                    }
                    intent.putExtra(FileProfileConstants.PARAM_URI, uri);
                }
            }
        }

        // アプリケーションのオリジン解析
        parseOriginHeader(request, intent);

        intent.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(DConnectConst.EXTRA_INNER_TYPE, DConnectConst.INNER_TYPE_HTTP);

        return intent;
    }

    /**
     * ファイルを読み込み.
     *
     * @param context コンテキスト
     * @param request リクエスト
     * @param response レスポンス
     */
    private static void serveFile(final Context context, final HttpRequest request, final HttpResponse response) {
        String uri = request.getQueryParameters().get("uri");
        String range = request.getHeaders().get("range");

        long startFrom = 0;
        long endAt = -1;
        if (range != null) {
            if (range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf('-');
                try {
                    if (minus > 0) {
                        startFrom = Long.parseLong(range.substring(0, minus));
                        endAt = Long.parseLong(range.substring(minus + 1));
                    }
                } catch (NumberFormatException ignored) {
                    // ignored.
                }
            }
        }

        try {
            ContentResolver r = context.getContentResolver();
            InputStream in = r.openInputStream(Uri.parse(uri));
            if (in == null) {
                response.setCode(HttpResponse.StatusCode.NOT_FOUND);
                setErrorResponse(response, 1, "Not found a resource.");
                return;
            }

            int fileLen = in.available();

            String etag = Integer.toHexString((uri + fileLen).hashCode());

            String ifRange = request.getHeaders().get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = request.getHeaders().get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                if (headerIfNoneMatchPresentAndMatching) {
                    response.setCode(HttpResponse.StatusCode.NOT_MODIFIED);
                    response.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }

                    int newLen = (int) (endAt - startFrom + 1);
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    in = new TempInputStream(in, newLen);
                    if (startFrom > 0) {
                        in.skip(startFrom);
                    }

                    response.addHeader("Accept-Ranges", "bytes");
                    response.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    response.addHeader("Content-Length", String.valueOf(newLen));
                    response.addHeader("ETag", etag);
                    response.setBody(in);
                    response.setContentLength(newLen);
                    response.setCode(HttpResponse.StatusCode.PARTIAL_CONTENT);
                }
            } else {
                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    response.setCode(HttpResponse.StatusCode.REQUEST_RANGE_NOT_SATISFIABLE);
                    response.addHeader("Content-Range", "bytes */" + fileLen);
                    response.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    response.setCode(HttpResponse.StatusCode.NOT_MODIFIED);
                    response.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified
                    response.setCode(HttpResponse.StatusCode.NOT_MODIFIED);
                    response.addHeader("ETag", etag);
                } else {
                    // supply the file
                    response.setBody(in);
                    response.setCode(HttpResponse.StatusCode.OK);
                    response.addHeader("Content-Length", String.valueOf(fileLen));
                    response.addHeader("ETag", etag);
                    response.addHeader("Accept-Ranges", "bytes");
                }
            }
        } catch (Exception e) {
            response.setCode(HttpResponse.StatusCode.NOT_FOUND);
            setErrorResponse(response, 1, "Not found a resource.");
        }
    }

    private static class TempInputStream extends InputStream {

        private InputStream mInputStream;
        private int mFileSize;

        TempInputStream(InputStream in, int fileSize) {
            mInputStream = in;
            mFileSize = fileSize;
        }

        @Override
        public int read(@NonNull byte[] b) throws IOException {
            return mInputStream.read(b);
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            return mInputStream.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return mInputStream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return mFileSize;
        }

        @Override
        public void close() throws IOException {
            mInputStream.close();
        }

        @Override
        public int read() throws IOException {
            return mInputStream.read();
        }
    }

    /**
     * URLパスを「/」で分割した配列を作成します.
     * <p>
     * 分割できない場合には、0の配列を返却します。
     * </p>
     *
     * @param request Httpリクエスト
     * @return パスの配列
     */
    private static String[] parsePath(final HttpRequest request) {
        String path = request.getUri();
        if (path == null || !path.contains("/")) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }

    /**
     * HTTPリクエストヘッダからアプリケーションのオリジンを取得する.
     *
     * @param request HTTPリクエスト
     * @param intent  key-valueを格納するIntent
     */
    private static void parseOriginHeader(final HttpRequest request, final Intent intent) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return;
        }
        String nativeOrigin = parseNativeOriginHeader(headers);
        if (nativeOrigin != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, nativeOrigin);
            return;
        }
        String webOrigin = parseWebOriginHeader(headers);
        if (webOrigin != null) {
            intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, webOrigin);
            intent.putExtra(DConnectConst.EXTRA_INNER_APP_TYPE, DConnectConst.INNER_APP_TYPE_WEB);
        }
    }

    /**
     * HTTPリクエストヘッダからAndroidネイティブアプリのオリジンを取得する.
     *
     * @param headers HTTPリクエストヘッダ
     * @return Androidネイティブアプリのオリジン
     */
    private static String parseNativeOriginHeader(final Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase(DConnectMessage.HEADER_GOTAPI_ORIGIN)) {
                String value = entry.getValue();
                if (value != null) {
                    return value;
                }
                break;
            }
        }
        return null;
    }

    /**
     * HTTPリクエストヘッダからWebアプリのオリジンを取得する.
     *
     * @param headers HTTPリクエストヘッダ
     * @return Webアプリのオリジン
     */
    private static String parseWebOriginHeader(final Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase("origin")) {
                String value = entry.getValue();
                if (value != null) {
                    return value;
                }
                break;
            }
        }
        return null;
    }

    /**
     * セグメントの中にHttpメソッドが含まれているか確認する.
     *
     * @param paths セグメント
     * @return Httpメソッドが含まれている場合はtrue、それ以外はfalse
     */
    private static boolean isHttpMethodIncluded(final String[] paths) {
        return paths != null && paths.length >= SEGMENT_ATTRIBUTE && isMethod(paths[1]);
    }

    /**
     * DeviceConnectがサポートしているOne ShotのHTTPメソッドかどうか.
     *
     * @param method HTTPメソッド
     * @return true:DeviceConnectがサポートしているOne shotのHTTPメソッドである。<br>
     * false:DeviceConnectがサポートしているOne shotのHTTPメソッドではない。
     */
    private static boolean isMethod(final String method) {
        return method.equalsIgnoreCase(DConnectMessage.METHOD_GET)
                || method.equalsIgnoreCase(DConnectMessage.METHOD_POST)
                || method.equalsIgnoreCase(DConnectMessage.METHOD_PUT)
                || method.equalsIgnoreCase(DConnectMessage.METHOD_DELETE);
    }

    /**
     * タイムアウトエラーのレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    public static void setTimeoutResponse(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.TIMEOUT);
    }

    /**
     * プロファイルが空の場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    public static void setEmptyProfile(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.NOT_SUPPORT_PROFILE);
    }

    /**
     * URLが不正の場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    private static void setInvalidURL(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.INVALID_URL);
    }

    /**
     * Profileが不正の場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    private static void setInvalidProfile(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.INVALID_PROFILE);
    }

    /**
     * 原因不明エラーが発生した場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    public static void setUnknownError(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.UNKNOWN);
    }

    /**
     * JSON変換エラーが発生した場合のエラーレスポンスを作成する.
     *
     * @param response レスポンスを格納するインスタンス
     */
    public static void setJSONFormatError(final HttpResponse response) {
        setErrorResponse(response, DConnectMessage.ErrorCode.UNKNOWN.getCode(), "JSON format is invalid");
    }

    /**
     * エラーコードをレスポンスに設定する.
     *
     * @param response  レスポンスを格納するインスタンス
     * @param errorCode エラーコード
     */
    private static void setErrorResponse(final HttpResponse response, final DConnectMessage.ErrorCode errorCode) {
        setErrorResponse(response, errorCode.getCode(), errorCode.toString());
    }

    /**
     * レスポンスにエラーを設定する.
     *
     * @param response     レスポンスを格納するHttpレスポンス
     * @param errorCode    エラーコード
     * @param errorMessage エラーメッセージ
     */
    private static void setErrorResponse(final HttpResponse response, final int errorCode, final String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"");
        sb.append(DConnectMessage.EXTRA_RESULT);
        sb.append("\":");
        sb.append(DConnectMessage.RESULT_ERROR);
        sb.append(",");
        sb.append("\"");
        sb.append(DConnectMessage.EXTRA_ERROR_CODE);
        sb.append("\": ");
        sb.append(errorCode);
        sb.append(",");
        sb.append("\"");
        sb.append(DConnectMessage.EXTRA_ERROR_MESSAGE);
        sb.append("\":\"");
        sb.append(errorMessage);
        sb.append("\"}");
        response.setContentType(DConnectConst.CONTENT_TYPE_JSON);
        response.setBody(sb.toString().getBytes());
    }

    /**
     * HTTPのレスポンスを組み立てる.
     * @param response 返答を格納するレスポンス
     * @param resp response用のIntent
     * @throws JSONException JSONの解析に失敗した場合
     * @throws UnsupportedEncodingException 文字列のエンコードに失敗した場合
     */
    public static void convertResponse(final DConnectSettings settings, final HttpResponse response, final Intent resp)
            throws JSONException, UnsupportedEncodingException {
        JSONObject root = new JSONObject();
        DConnectUtil.convertBundleToJSON(settings, root, resp.getExtras());
        response.setContentType(DConnectConst.CONTENT_TYPE_JSON);
        response.setBody(root.toString().getBytes("UTF-8"));
    }
}
