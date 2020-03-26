package org.deviceconnect.android.deviceplugin.host.recorder.util;

public final class RTMPUtils {
    private static final String RTMP_SCHEME = "rtmp";

    private RTMPUtils(){
    }
    /**
     * broadcastパラメータが仕様通りかチェック.
     * @param broadcast 渡されたbroadcastパラメータ
     * @return エラーを検出した場合はエラーメッセージを返す. エラーがなければnullを返す.
     */
    public static String checkValidBroadcastParameter(String broadcast) {
        if (broadcast == null || broadcast.length() <= 0) {
            return "Broadcast is not registered.";
        }

        if (!isBroadcastParamUri(broadcast)) {
            return "Broadcast is not RTMP URI.";
        }
        return null;
    }
    /**
     * broadcastパラメータの値が「URI」(RTMP URI)か判定.
     * @param broadcast 渡されたbroadcastパラメータ
     * @return URI(RTMP URI)であればtrueを返す.それ以外ならfalseを返す.
     */
    private static boolean isBroadcastParamUri(String broadcast) {
        if (broadcast == null || broadcast.length() <= 0) {
            return false;
        }
        return isRtmpUri(broadcast);
    }
    /**
     * URIがRTMP URLか判定.
     * @param uri URI
     * @return RTMP URIであればtrueを返す.それ以外ならfalseを返す.
     */
    private static boolean isRtmpUri(String uri) {
        if (uri != null && uri.substring(0, RTMP_SCHEME.length()).toLowerCase().equals(RTMP_SCHEME)) {
            return true;
        }
        return false;
    }
}
