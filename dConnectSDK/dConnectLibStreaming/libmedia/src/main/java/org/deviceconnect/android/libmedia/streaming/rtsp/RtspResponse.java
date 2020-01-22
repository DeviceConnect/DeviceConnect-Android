package org.deviceconnect.android.libmedia.streaming.rtsp;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deviceconnect.android.libmedia.BuildConfig;

public class RtspResponse {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "RTSP-RESPONSE";

    /**
     * 改行コードを定義します.
     */
    private static final String NEW_LINE = "\r\n";

    /**
     * RTSP レスポンスで返却するステータスコード.
     */
    public enum Status {
        STATUS_OK(200, "OK"),
        STATUS_BAD_REQUEST(400, "Bad RSTP Request"),
        STATUS_NOT_FOUND(404, "Not Found"),
        STATUS_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        STATUS_NOT_IMPLEMENTED(501, " Not Implemented"),
        STATUS_UNKNOWN(-1, "Unknown");

        private int mStatus;
        private String mValue;

        Status(int status, String value) {
            mStatus = status;
            mValue = value;
        }

        public int getStatus() {
            return mStatus;
        }

        public String getValue() {
            return mValue;
        }

        public static Status statusOf(int status) {
            for (Status s : values()) {
                if (s.mStatus == status) {
                    return s;
                }
            }
            return STATUS_UNKNOWN;
        }
    }

    /**
     * ステーテスコード.
     */
    private Status mStatus = Status.STATUS_INTERNAL_SERVER_ERROR;

    /**
     * レスポンスの本文.
     */
    private String mContent;

    /**
     * レスポンスのアトリビュート(ヘッダー).
     */
    private Map<String, String> mAttribute = new LinkedHashMap<>();

    /**
     * サーバ名.
     */
    private String mServerName = "";

    /**
     * シーケンス ID.
     */
    private int mSequenceId;

    /**
     * ステータスコードを設定します.
     *
     * @param status ステータスコード
     */
    public void setStatus(Status status) {
        mStatus = status;
    }

    /**
     * ステータスコードを取得します.
     *
     * @return ステータスコード
     */
    public Status getStatus() {
        return mStatus;
    }

    /**
     * レスポンスの本文を設定します.
     *
     * @param content レスポンスの本文
     */
    public void setContent(String content) {
        mContent = content;
    }

    /**
     * レスポンスの本文を取得します.
     *
     * <p>
     * 未設定の場合は null を返却します。
     * </p>
     *
     * @return レスポンスの本文
     */
    public String getContent() {
        return mContent;
    }

    /**
     * アトリビュートを追加します.
     *
     * @param key キー
     * @param value 値
     */
    public void addAttribute(String key, String value) {
        mAttribute.put(key, value);
    }

    /**
     * 指定されたキーに対応するアトリビュートを取得します.
     *
     * @param key キー
     * @return 対応したアトリビュートの値
     */
    public String getAttribute(String key) {
        return mAttribute.get(key.toLowerCase());
    }

    /**
     * サーバ名を設定します.
     *
     * @param serverName サーバ名
     */
    public void setServerName(String serverName) {
        mServerName = serverName;
    }

    /**
     * シーケンス ID を設定します.
     *
     * @param sequenceId シーケンス ID
     */
    public void setSequenceId(int sequenceId) {
        mSequenceId = sequenceId;
    }

    /**
     * シーケンスIDを取得します.
     *
     * @return シーケンス ID
     */
    public int getSequenceId() {
        return mSequenceId;
    }

    /**
     * レスポンスをストリームに書き込みます.
     *
     * @param outputStream レスポンスを書き込むストリーム
     * @throws IOException レスポンスの書き込みに失敗した場合に発生
     */
    public void send(OutputStream outputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("RTSP/1.0 ").append(mStatus.getStatus()).append(" ").append(mStatus.getValue()).append(NEW_LINE)
                .append("Server: ").append(mServerName).append(NEW_LINE);
        if (mSequenceId > 0) {
            builder.append("Cseq: ").append(mSequenceId).append(NEW_LINE);
        }
        builder.append("Content-Length: ").append(mContent != null ? mContent.length() : 0).append(NEW_LINE);
        for (Map.Entry<String, String> entry : mAttribute.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(NEW_LINE);
        }
        builder.append(NEW_LINE);

        if (mContent != null) {
            builder.append(mContent);
        }

        if (DEBUG) {
            Log.i(TAG, "RESPONSE");
            Log.i(TAG, builder.toString());
        }

        outputStream.write(builder.toString().getBytes());
    }
}
