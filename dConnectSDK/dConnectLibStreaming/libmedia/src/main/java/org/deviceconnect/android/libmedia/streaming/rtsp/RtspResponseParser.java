package org.deviceconnect.android.libmedia.streaming.rtsp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.libmedia.BuildConfig;

class RtspResponseParser {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTSP-PARSE";

    /**
     * レスポンスのステータスコードを取得するための正規表現を定義します.
     */
    private static final Pattern REGEX_STATUS = Pattern.compile("RTSP/\\d.\\d (\\d+) (\\w+)", Pattern.CASE_INSENSITIVE);

    /**
     * レスポンスのヘッダーを取得するための正規表現を定義します.
     */
    private static final Pattern REGEX_HEADER = Pattern.compile("(\\S+):(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * client_port (クライアントの RTP、RTCP のポート番号)を取得するための正規表現を定義します.
     */
    private static final Pattern CLIENT_PORT = Pattern.compile("client_port=(\\d+)-(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * server_port (クライアントの RTP、RTCP のポート番号)を取得するための正規表現を定義します.
     */
    private static final Pattern SERVER_PORT = Pattern.compile("server_port=(\\d+)-(\\d+)", Pattern.CASE_INSENSITIVE);

    private RtspResponseParser() {
    }

    /**
     * RTSP サーバからのレスポンスを解析して {@link RtspResponse} に値を格納します.
     *
     * <p>
     * ヘッダーの要素名は、全て小文字に変換して格納します。<br>
     * ヘッダーを取得する場合には注意してください。
     * </p>
     *
     * @param input レスポンスのストリーム
     * @return RTSP サーバからのレスポンス
     * @throws IOException レスポンスの読み込みに失敗した場合に発生
     */
    static RtspResponse parse(BufferedReader input) throws IOException {
        RtspResponse response = new RtspResponse();
        String line;
        Matcher matcher;

        if ((line = input.readLine()) == null) {
            throw new SocketException("Client socket disconnected.");
        }

        if (DEBUG) {
            Log.d(TAG, "RTSP RESPONSE START");
            Log.d(TAG, "  " + line);
        }

        matcher = REGEX_STATUS.matcher(line);
        if (matcher.find()) {
            response.setStatus(RtspResponse.Status.statusOf(Integer.parseInt(matcher.group(1))));
        }

        while ((line = input.readLine()) != null) {
            if (DEBUG) {
                Log.d(TAG, "  " + line);
            }

            if (line.length() > 3) {
                matcher = REGEX_HEADER.matcher(line);
                if (matcher.find()) {
                    response.addAttribute(matcher.group(1).trim().toLowerCase(), matcher.group(2).trim());
                }
            } else {
                break;
            }
        }

        String contentLengthStr = response.getAttribute("content-length");
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            if (contentLength > 0) {
                char[] buf = new char[contentLength];
                int offset = 0;
                while (offset < contentLength) {
                    offset += input.read(buf, offset, contentLength - offset);
                }
                response.setContent(new String(buf));

                if (DEBUG) {
                    Log.i(TAG, "");
                    Log.i(TAG, new String(buf));
                }
            }
        }

        return response;
    }


    /**
     * RTSP レスポンスからクライアントの RTP と RTCP のポート番号を取得します.
     *
     * <p>
     * RTSP レスポンスにポート番号が格納されていない場合には null を返却します。
     * </p>
     *
     * @param response RTSP レスポンス
     * @return クライアントのポート番号
     */
    static int[] parseClientPort(RtspResponse response) {
        Matcher matcher = CLIENT_PORT.matcher(response.getAttribute("transport"));
        if (matcher.find()) {
            return new int[]{
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            };
        }
        return null;
    }

    /**
     * RTSP レスポンスらサーバの RTP と RTCP のポート番号を取得します.
     *
     * <p>
     * RTSP レスポンスにポート番号が格納されていない場合には null を返却します。
     * </p>
     *
     * @param response RTSP レスポンス
     * @return クライアントのポート番号
     */
    static int[] parseServerPort(RtspResponse response) {
        Matcher matcher = SERVER_PORT.matcher(response.getAttribute("transport"));
        if (matcher.find()) {
            return new int[]{
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            };
        }
        return null;
    }
}
