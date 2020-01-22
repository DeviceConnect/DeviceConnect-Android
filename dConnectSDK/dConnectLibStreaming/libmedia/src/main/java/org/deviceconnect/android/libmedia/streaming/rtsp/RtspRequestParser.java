package org.deviceconnect.android.libmedia.streaming.rtsp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.libmedia.BuildConfig;

class RtspRequestParser {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "RTSP-PARSE";

    /**
     * RTSP リクエストからメソッドを取得するための正規表現を定義します.
     */
    private static final Pattern REGEX_METHOD = Pattern.compile("(\\w+) (\\S+) RTSP/(\\S+)", Pattern.CASE_INSENSITIVE);

    /**
     * RTSP リクエストからヘッダーを取得するための正規表現を定義します.
     */
    private static final Pattern REGEX_HEADER = Pattern.compile("(\\S+):(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * trackID を取得するための正規表現を定義します.
     */
    private static final Pattern TRACK_ID = Pattern.compile("trackID=(\\w+)", Pattern.CASE_INSENSITIVE);

    /**
     * client_port (クライアントの RTP、RTCP のポート番号)を取得するための正規表現を定義します.
     */
    private static final Pattern CLIENT_PORT = Pattern.compile("client_port=(\\d+)-(\\d+)", Pattern.CASE_INSENSITIVE);

    private RtspRequestParser() {
    }

    /**
     * RTSP リクエストを解析して、RtspRequest に変換します.
     *
     * <p>
     * ヘッダーの要素名は全て小文字にして格納します。<br>
     * ヘッダーを取得する場合には注意してください。
     * </p>
     *
     * @param input RTSP リクエストが格納されたストリーム
     * @return RtspRequest
     * @throws IOException ストリームから RTSP リクエストの読み込みに失敗した場合に発生
     * @throws RtspRequestParserException RTSP リクエストの解析に失敗した場合に発生
     */
    static RtspRequest parse(BufferedReader input) throws IOException, RtspRequestParserException {
        RtspRequest request = new RtspRequest();
        String line;
        Matcher matcher;

        if ((line = input.readLine()) == null) {
            throw new SocketException("Client socket disconnected.");
        }

        matcher = REGEX_METHOD.matcher(line);
        if (!matcher.find()) {
            throw new RtspRequestParserException(RtspResponse.Status.STATUS_BAD_REQUEST);
        }

        if (DEBUG) {
            Log.d(TAG, "RTSP REQUEST START");
            Log.d(TAG, "    " + line);
        }

        request.setMethod(RtspRequest.Method.methodOf(matcher.group(1)));
        request.setUri(matcher.group(2));
        request.setVersion(matcher.group(3));

        while ((line = input.readLine()) != null && line.length() > 3) {
            if (DEBUG) {
                Log.d(TAG, "    " + line);
            }

            matcher = REGEX_HEADER.matcher(line);
            if (!matcher.find()) {
                throw new RtspRequestParserException(RtspResponse.Status.STATUS_BAD_REQUEST);
            }
            request.addHeader(matcher.group(1).toLowerCase(), matcher.group(2));
        }

        return request;
    }

    /**
     * RTSP リクエストから TrackID を取得します.
     *
     * <p>
     * RTSP リクエストの中に TrackID が格納されていない場合は null を返却します。
     * </p>
     *
     * @param request RTSP リクエスト
     * @return TrackID
     */
    static String parseTrackID(RtspRequest request) {
        Matcher matcher = TRACK_ID.matcher(request.getUri());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * RTSP リクエストからクライアントの RTP と RTCP のポート番号を取得します.
     *
     * <p>
     * RTSP リクエストにポート番号が格納されていない場合には null を返却します。
     * </p>
     *
     * @param request RTSP リクエスト
     * @return クライアントのポート番号
     */
    static int[] parseClientPort(RtspRequest request) {
        Matcher matcher = CLIENT_PORT.matcher(request.getHeader("transport"));
        if (matcher.find()) {
            return new int[]{
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            };
        }
        return null;
    }
}
