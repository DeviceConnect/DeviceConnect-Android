package org.deviceconnect.android.libmedia.streaming.rtsp;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.deviceconnect.android.libmedia.BuildConfig;

public class RtspRequest {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RTSP-REQUEST";

    /**
     * 改行コードを定義します.
     */
    private static final String NEW_LINE = "\r\n";

    /**
     * RTSP メソッド.
     */
    public enum Method {
        OPTIONS("OPTIONS"),
        DESCRIBE("DESCRIBE"),
        SETUP("SETUP"),
        PLAY("PLAY"),
        PAUSE("PAUSE"),
        TEARDOWN("TEARDOWN"),
        ANNOUNCE("ANNOUNCE"),
        GET_PARAMETER("GET_PARAMETER"),
        SET_PARAMETER("SET_PARAMETER"),
        RECORD("RECORD"),
        REDIRECT("REDIRECT"),
        UNKNOWN("UNKNOWN");

        private String mValue;

        Method(String value) {
            mValue = value;
        }

        public static Method methodOf(String value) {
            for (Method method : values()) {
                if (method.mValue.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * RTSP メソッド.
     */
    private Method mMethod;

    /**
     * RTSP の URI.
     */
    private String mUri;

    /**
     * RTSP のバージョン.
     */
    private String mVersion = "1.0";

    /**
     * RTSP のヘッダー.
     */
    private final HashMap<String,String> mHeaders = new HashMap<>();

    /**
     * リクエストの本文.
     */
    private String mBody;

    /**
     * RTSP のメソッドを取得します.
     *
     * @return RTSP のメソッド
     */
    public Method getMethod() {
        return mMethod;
    }

    /**
     * RTSP のメソッドを設定します.
     *
     * @param method RTSP のメソッド
     */
    public void setMethod(Method method) {
        mMethod = method;
    }

    /**
     * RTSP の URI を取得します.
     *
     * @return RTSP の URI
     */
    public String getUri() {
        return mUri;
    }

    /**
     * RTSP の URI を設定します.
     *
     * @param uri RTSP の URI
     */
    public void setUri(String uri) {
        mUri = uri;
    }

    /**
     * RTSP のバージョンを取得します.
     *
     * @return バージョン
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * RTSP のバージョンを設定します.
     *
     * @param version バージョン
     */
    public void setVersion(String version) {
        mVersion = version;
    }

    /**
     * 指定のキーに対応した値を取得します.
     *
     * <p>
     * 対応する値が存在しない場合は null を返却します。
     * </p>
     *
     * <p>
     * キーは全て小文字に変換されて保持されています。
     * </p>
     *
     * @param key キー
     * @return キーに対応した値
     */
    public String getHeader(String key) {
        return mHeaders.get(key.toLowerCase());
    }

    /**
     * ヘッダーの一覧を取得します.
     *
     * @return ヘッダーの一覧
     */
    public HashMap<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * ヘッダーを追加します.
     *
     * @param key キー
     * @param value 値
     */
    void addHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    /**
     * シーケンス ID を取得します.
     *
     * @return シーケンスID
     */
    public Integer getCSeq() {
        String cseq = getHeader("cseq");
        if (cseq != null) {
            try {
                return Integer.parseInt(cseq.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * シーケンス ID を設定します.
     *
     * @param cseq シーケンスID
     */
    public void setCSeq(int cseq) {
        mHeaders.put("CSeq", String.valueOf(cseq));
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public void send(OutputStream outputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(mMethod).append(" ").append(mUri).append(" RTSP/").append(mVersion).append(NEW_LINE);
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(NEW_LINE);
        }
        builder.append(NEW_LINE);

        if (mBody != null) {
            builder.append(mBody);
        }

        if (DEBUG) {
            Log.d(TAG, builder.toString());
        }

        outputStream.write(builder.toString().getBytes());
        outputStream.flush();
    }
}
