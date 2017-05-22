/*
 SlackManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.slack;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import org.deviceconnect.android.deviceplugin.slackmessagehook.BuildConfig;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;


/**
 * Slackの制御を管理するクラス.
 */
public class SlackManager {

    //---------------------------------------------------------------------------------------
    //region Declaration

    /** シングルトンなManagerのインスタンス */
    public static final SlackManager INSTANCE = new SlackManager();

    /** TAG. */
    private final static String TAG = "SlackManager";

    /** SlackAPIベースURL */
    private final static String BASE_URL = "https://slack.com/api/";

    /** 接続状態（未接続） */
    private static final int CONNECT_STATE_NONE = 0;
    /** 接続状態（切断） */
    private static final int CONNECT_STATE_DISCONNECTED = 1;
    /** 接続状態（切断中） */
    private static final int CONNECT_STATE_DISCONNECTING = 2;
    /** 接続状態（接続中） */
    private static final int CONNECT_STATE_CONNECTING = 3;
    /** 接続状態（接続） */
    private static final int CONNECT_STATE_CONNECTED = 4;

    /** 接続状態 */
    private int connectState = CONNECT_STATE_NONE; // 0:None 1:Disconnected 2:Disconnecting 3:Connecting 4:Connected

    /** WebSocketのKeepAlive時間 */
    private static final int KEEPALIVE_SPAN = 5000;

    /** SlackManagerの基底Exception */
    public abstract class SlackManagerException extends Exception {}
    /** APITokenが不正 */
    public class SlackAPITokenValueException extends SlackManagerException {}
    /** 接続エラー */
    public class SlackConnectionException extends SlackManagerException {}
    /** Slack認証エラー */
    public class SlackAuthException extends SlackManagerException {}
    /** 未知のエラー */
    public class SlackUnknownException extends SlackManagerException {}


    /** WebSocket */
    private WebSocketClient webSocket;

    /** SlackBotのApiToken */
    private String token = null;

    /** 処理完了コールバック */
    public interface FinishCallback<Result> {
        /**
         * 処理が完了した時に呼ばれます.
         * @param result 結果
         * @param error エラー
         */
        void onFinish(Result result, Exception error);
    }

    /** 接続処理完了コールバック */
    private FinishCallback<Void> connectionFinishCallback;

    /** メッセージ送信処理完了コールバック */
    private FinishCallback<String> sendMsgFinishCallback;

    /** Slackイベントリスナー */
    public interface SlackEventListener {

        /**
         * 接続イベント
         */
        void OnConnect();

        /**
         * 切断イベント
         */
        void OnConnectLost();

        /**
         * メッセージを受信したイベント.
         * @param info メッセージ
         */
        void OnReceiveSlackMessage(HistoryInfo info);
    }

    /** Slackイベントリスナー */
    private ArrayList<SlackEventListener> slackEventListeners = new ArrayList<>();

    /** Botの情報 */
    public class BotInfo {
        public String id;
        public String name;
        public String teamName;
        public String teamDomain;
        @Override
        public String toString() {
            return "BotInfo = {id: "+ id + ", name:" + name + ", teamName:" + teamName + "}";
        }
    }

    /** Botの情報 */
    private BotInfo botInfo = new BotInfo();


    //endregion
    //---------------------------------------------------------------------------------------
    //region Init

    /**
     * 初期化。シングルトンのためにprivate.
     */
    private SlackManager() {
    }

    /** Botの情報を取得 */
    public BotInfo getBotInfo() {
        return botInfo;
    }

    /**
     * イベントリスナーを設定します.
     * @param listener リスナー
     */
    public void addSlackEventListener(SlackEventListener listener) {
        slackEventListeners.add(listener);
    }

    /**
     * イベントリスナーを解除します.
     * @param listener リスナー
     */
    public void removeSlackEventListener(SlackEventListener listener) {
        slackEventListeners.remove(listener);
    }

    /**
     * SlackBotのAPITokenを設定.
     * @param apiToken APIToken
     */
    public void setApiToken(final String apiToken, boolean needsConnect, final FinishCallback<Void> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*setApiToken");
        if (apiToken == null) {
            if (callback != null) {
                callback.onFinish(null, new SlackAPITokenValueException());
            }
            return;
        }
        // 不正文字列を入力できないようにencodeしておく
        String newToken = null;
        try {
            newToken = URLEncoder.encode(apiToken,"utf-8");
        } catch (UnsupportedEncodingException e) {
            if (callback != null) {
                callback.onFinish(null, new SlackAPITokenValueException());
            }
            return;
        }
        // トークンが変わらない場合は何もしない
        if (newToken.equals(token)) {
            if (callback != null) {
                callback.onFinish(null, null);
            }
            return;
        }
        token = newToken;
        // 接続
        if (needsConnect) {
            if (connectState > CONNECT_STATE_DISCONNECTING) {
                // すでに接続中なら切断後に再接続
                disconnect(new FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void v,Exception error) {
                        connect(new FinishCallback<Void>() {
                            @Override
                            public void onFinish(Void aVoid, Exception error) {
                                if (callback != null) {
                                    callback.onFinish(null, error);
                                }
                            }
                        });
                    }
                });
            } else {
                connect(callback);
            }
        } else {
            if (callback != null) {
                callback.onFinish(null, null);
            }
        }
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region Connection

    /**
     * 接続中かを返します.
     * @return 接続中ならtrue
     */
    public boolean isConnected() {
        return connectState == CONNECT_STATE_CONNECTED;
    }

    /**
     * 切断中かを返します.
     * @return 切断中ならtrue
     */
    public boolean isDisconnecting() {
        return (connectState > CONNECT_STATE_DISCONNECTING);
    }

    /**
     * 接続.
     */
    public void connect() {
        connect(null);
    }

    /**
     * 接続.
     * @param callback 接続完了コールバック
     */
    public void connect(FinishCallback<Void> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*connect");
        if (token == null) {
            if (callback != null) {
                callback.onFinish(null, new SlackAPITokenValueException());
            }
            return;
        }
        // 接続済み
        if (connectState > CONNECT_STATE_DISCONNECTING) {
            if (callback != null) {
                callback.onFinish(null, null);
            }
            return;
        }
        connectState = CONNECT_STATE_CONNECTING;
        if (callback != null) {
            connectionFinishCallback = callback;
        }

        // 接続処理
        new GetTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam("rtm.start", "&simple_latest=True&no_unreads=True") {
            @Override
            public void callBack(JSONObject json) {
                // rtm.startに失敗
                if (json == null) {
                    connectState = CONNECT_STATE_DISCONNECTED;
                    callConnectionFinishCallback(new SlackConnectionException(), null);
                    return;
                }
                // WebSocketに接続
                if (BuildConfig.DEBUG) Log.d(TAG, json.toString());
                // 接続
                String jsonUrl;
                try {
                    if (json.has("error")) {
                        connectState = CONNECT_STATE_DISCONNECTED;
                        String err = json.getString("error");
                        if (err.equals("invalid_auth") || err.equals("not_authed")) {
                            // Slack認証エラー
                            callConnectionFinishCallback(new SlackAuthException(), null);
                        } else {
                            // Slackサーバーエラー
                            callConnectionFinishCallback(new SlackConnectionException(), null);
                        }
                        return;
                    }
                    jsonUrl = json.getString("url");
                    if (BuildConfig.DEBUG) Log.d(TAG, "url:"+jsonUrl);
                    JSONObject selfJson = json.getJSONObject("self");
                    botInfo.id = selfJson.getString("id");
                    botInfo.name = selfJson.getString("name");
                    JSONObject teamJson = json.getJSONObject("team");
                    botInfo.teamName = teamJson.getString("name");
                    botInfo.teamDomain = teamJson.getString("domain");
                    if (BuildConfig.DEBUG) Log.d(TAG, "bot:" + botInfo.toString());
                    connectWebSocket(URI.create(jsonUrl));
                } catch (JSONException e) {
                    Log.e(TAG, "error", e);
                    connectState = CONNECT_STATE_DISCONNECTED;
                    callConnectionFinishCallback(e, null);
                }
            }
        });
    }

    /**
     * 切断.
     */
    @SuppressWarnings("unused")
    public void disconnect() {
        disconnect(null);
    }

    /**
     * 切断.
     * @param callback 切断完了コールバック
     */
    public void disconnect(FinishCallback<Void> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*disconnect");
        if (webSocket == null || connectState < CONNECT_STATE_CONNECTING) {
            if (callback != null) {
                callback.onFinish(null, null);
            }
            return;
        }
        connectState = CONNECT_STATE_DISCONNECTING;
        connectionFinishCallback = callback;
        webSocket.disconnect();
        webSocket = null;
    }

    //endregion
    //---------------------------------------------------------------------------------------
    //region SendMessage

    /**
     * Slackにメッセージ送信.
     * @param msg メッセージ
     * @param channel チャンネル
     * @param callback 完了コールバック
     */
    public void sendMessage(String msg, String channel, final FinishCallback<String> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*sendMessage:" + msg);
        if (connectState != CONNECT_STATE_CONNECTED) {
            callSendMsgFinishCallback(null, new SlackConnectionException(), null);
            return;
        }
        if (msg == null || channel == null) {
            callSendMsgFinishCallback(null, new InvalidParameterException(), null);
            return;
        }
        msg = escape(msg);
        channel = escape(channel);
        sendMsgFinishCallback = callback;
        String data = "{\"type\": \"message\", \"channel\": \"" + channel + "\", \"text\": \"" + msg + "\"}";
        webSocket.send(data);
    }

    /**
     * Slackにメッセージ送信.
     * @param msg メッセージ
     * @param channel チャンネル
     */
    public void sendMessage(String msg, String channel) {
        sendMessage(msg, channel, null);
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region etc.

    /**
     * 文字列をエスケープ処理する
     * @param str 文字列
     * @return 処理後の文字列
     */
    private String escape(String str) {
        String ret = str.replace("\\", "\\\\");
        return ret.replace("\"", "\\\"");
    }

    /**
     * 接続完了コールバックを呼ぶ
     * @param e 例外
     */
    private void callConnectionFinishCallback(final Exception e, Handler handler) {
        if (connectState == CONNECT_STATE_DISCONNECTED && handler != null) {
            for (final SlackEventListener listener: slackEventListeners){
                handler.post(new Runnable() {
                    public void run() {
                        listener.OnConnectLost();
                    }
                });
            }
        }
        if (connectionFinishCallback != null) {
            final FinishCallback callback = connectionFinishCallback;
            connectionFinishCallback = null;
            if (handler == null) {
                callback.onFinish(null, e);
            } else {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onFinish(null, e);
                    }
                });
            }
        }
    }

    /**
     * 送信完了コールバックを呼ぶ
     * @param text 送信text
     * @param e 例外
     */
    private void callSendMsgFinishCallback(String text, final Exception e, Handler handler) {
        if (sendMsgFinishCallback != null) {
            final FinishCallback callback = sendMsgFinishCallback;
            sendMsgFinishCallback = null;
            if (handler == null) {
                callback.onFinish(null, e);
            } else {
                handler.post(new Runnable() {
                    public void run() {
                        callback.onFinish(null, e);
                    }
                });
            }
        }
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region WebSocket

    /**
     * KeepAlive処理
     * @param handler Handler
     */
    private void keepAlive(final Handler handler) {
        // keepalive
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connectState == CONNECT_STATE_CONNECTED) {
                    webSocket.ping("ping");
                    handler.postDelayed(this, KEEPALIVE_SPAN);
                }
            }
        }, KEEPALIVE_SPAN);
    }

    /**
     * WebSocket接続
     * @param uri 接続先
     */
    private void connectWebSocket(final URI uri) {
        final Handler handler = new Handler();
        if (webSocket != null) {
            webSocket.disconnect();
        }
        webSocket = new WebSocketClient(uri, new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                if (BuildConfig.DEBUG) Log.d(TAG, "Connected!");
                connectState = CONNECT_STATE_CONNECTED;
                callConnectionFinishCallback(null, handler);
                for (final SlackEventListener listener: slackEventListeners){
                    handler.post(new Runnable() {
                        public void run() {
                            listener.OnConnect();
                        }
                    });
                }
                // KeepAlive
                keepAlive(handler);
            }

            @Override
            public void onMessage(String message) {
                if (BuildConfig.DEBUG) Log.d(TAG, String.format("Got string message! %s", message));
                try {
                    JSONObject json = new JSONObject(message);
                    if (json.has("type")) {
                        String type = json.getString("type");
                        switch (type) {
                            // メッセージ受信時
                            case "message":
                                if (!json.has("subtype")) {
                                    // subtypeが無いものが純粋なメッセージ
                                    final HistoryInfo info = new HistoryInfo();
                                    info.text = json.getString("text");
                                    info.channel = json.getString("channel");
                                    info.user = json.getString("user");
                                    info.ts = json.getDouble("ts");
                                    for (final SlackEventListener listener: slackEventListeners){
                                        handler.post(new Runnable() {
                                            public void run() {
                                                listener.OnReceiveSlackMessage(info);
                                            }
                                        });
                                    }
                                } else if (json.getString("subtype").equals("file_share")) {
                                    // ファイルアップロード
                                    final HistoryInfo info = new HistoryInfo();
                                    info.channel = json.getString("channel");
                                    info.user = json.getString("user");
                                    info.ts = json.getDouble("ts");
                                    if(json.has("file")) {
                                        JSONObject file = json.getJSONObject("file");
                                        info.mimetype = file.getString("mimetype");
                                        info.file = file.getString("url_private");
                                        if(file.has("initial_comment")) {
                                            JSONObject comment = file.getJSONObject("initial_comment");
                                            info.text = comment.getString("comment");
                                        }
                                        if (file.has("thumb_360")) {
                                            info.thumb = file.getString("thumb_360");
                                        }
                                        if (file.has("thumb_360_h")) {
                                            info.thumbHeight = file.getInt("thumb_360_h");
                                        }
                                        if (file.has("thumb_360_w")) {
                                            info.thumbWidth = file.getInt("thumb_360_w");
                                        }
                                    }
                                    for (final SlackEventListener listener: slackEventListeners){
                                        handler.post(new Runnable() {
                                            public void run() {
                                                listener.OnReceiveSlackMessage(info);
                                            }
                                        });
                                    }
                                }
                                break;
                            // エラー時
                            case "error":
                                callSendMsgFinishCallback(null, new SlackUnknownException(), handler);
                                break;
                            default:
                        }
                    }
                    if (json.has("ok")) {
                        if (json.getBoolean("ok")) {
                            callSendMsgFinishCallback(json.getString("text"), null, handler);
                        } else {
                            callSendMsgFinishCallback(null, new SlackUnknownException(), handler);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onMessage(byte[] data) {
            }

            @Override
            public void onDisconnect(int code, String reason) {
                if (BuildConfig.DEBUG) Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
                if (connectState == CONNECT_STATE_CONNECTED) {
                    // 接続中に急に切れたら再接続を試みる
                    connectState = CONNECT_STATE_CONNECTING;
                    retry(0, handler, new FinishCallback<Boolean>() {
                        @Override
                        public void onFinish(Boolean flg, Exception error) {
                            if (!flg) {
                                // 切断処理
                                connectState = CONNECT_STATE_DISCONNECTED;
                                callConnectionFinishCallback(null, handler);
                            }
                        }
                    });
                } else {
                    // 切断処理
                    connectState = CONNECT_STATE_DISCONNECTED;
                    callConnectionFinishCallback(null, handler);
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error!", error);
                if (connectState == CONNECT_STATE_CONNECTED) {
                    // 接続中に急に切れたら再接続を試みる
                    retry(0, handler, new FinishCallback<Boolean>() {
                        @Override
                        public void onFinish(Boolean flg, Exception error) {
                            if (!flg) {
                                // 切断処理
                                connectState = CONNECT_STATE_DISCONNECTED;
                                callConnectionFinishCallback(null, handler);
                            }
                        }
                    });
                } else {
                    callConnectionFinishCallback(error, handler);
                    callSendMsgFinishCallback(null, error, handler);
                }
            }
        }, null);
        webSocket.connect();
    }

    /**
     * 再接続処理
     * @param handler Handler
     * @return 再接続した場合はtrue
     */
    private boolean retry(int retryCount, final Handler handler, final FinishCallback<Boolean> callback) {
        int delay = 0;
        switch (retryCount++) {
            case 0:
                delay = 1000; // 1s
                break;
            case 1:
                delay = 5000; // 5s
                break;
            case 2:
                delay = 30000; // 30s
                break;
            case 3:
                delay = 60000; // 1m
                break;
            case 4:
                delay = 3 * 60000; // 3m
                break;
            default:
                // callback
                callback.onFinish(false, null);
                return false;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "retry:" + delay);
        final int _retryCount = retryCount;
        handler.postDelayed(new Runnable() {
            public void run() {
                // 再接続処理
                new GetTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam("rtm.start", "&simple_latest=True&no_unreads=True") {
                    @Override
                    public void callBack(JSONObject json) {
                        // rtm.startに失敗
                        if (json == null) {
                            retry(_retryCount, handler, callback);
                            return;
                        }
                        // WebSocketに接続
                        if (BuildConfig.DEBUG) Log.d(TAG, json.toString());
                        // 接続
                        String jsonUrl;
                        try {
                            if (json.has("error")) {
                                retry(_retryCount, handler, callback);
                                return;
                            }
                            jsonUrl = json.getString("url");
                            if (BuildConfig.DEBUG) Log.d(TAG, "url:"+jsonUrl);
                            JSONObject selfJson = json.getJSONObject("self");
                            botInfo.id = selfJson.getString("id");
                            botInfo.name = selfJson.getString("name");
                            JSONObject teamJson = json.getJSONObject("team");
                            botInfo.teamName = teamJson.getString("name");
                            botInfo.teamDomain = teamJson.getString("domain");
                            if (BuildConfig.DEBUG) Log.d(TAG, "bot:" + botInfo.toString());
                            connectWebSocket(URI.create(jsonUrl));
                            // callback
                            callback.onFinish(true, null);
                        } catch (JSONException e) {
                            Log.e(TAG, "error", e);
                            retry(_retryCount, handler, callback);
                        }
                    }
                });
            }
        }, delay);
        return true;
    }

    //endregion
    //---------------------------------------------------------------------------------------
    //region History

    /**
     * Channelの履歴を取得
     * @param callback 取得コールバック
     */
    public void getHistory(String channel, Double latest, final FinishCallback<ArrayList<HistoryInfo>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*getHistory:" + channel);
        String params = "&channel=" + channel;
        if (latest != null) {
            params += "&latest=" + String.format(Locale.ENGLISH, "%.2f", latest);
        }
        if (channel.startsWith("D")) {
            getHistoryData("im.history", params, callback);
        } else {
            getHistoryData("channels.history", params, callback);
        }
    }

    /**
     * 受け渡し情報
     */
    public static class HistoryInfo {
        public String channel;
        public String user;
        public Double ts;
        public String text;
        public String name;
        public String icon;
        public String file;
        public String mimetype;
        public String thumb;
        public int thumbHeight = 0;
        public int thumbWidth = 0;
    }

    /**
     * 履歴取得
     * @param target ターゲットAPI
     * @param params パラメータ
     * @param callback 取得コールバック
     */
    private void getHistoryData(String target, String params, final FinishCallback<ArrayList<HistoryInfo>> callback) {
        if (connectState != CONNECT_STATE_CONNECTED) {
            if (callback != null) {
                callback.onFinish(null, new SlackConnectionException());
            }
            return;
        }
        new GetTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam(target, params) {
            @Override
            public void callBack(JSONObject json) {
                if (BuildConfig.DEBUG) Log.d(TAG, json.toString());

                try {
                    if (json.has("error")) {
                        connectState = CONNECT_STATE_DISCONNECTED;
                        String err = json.getString("error");
                        if (err.equals("invalid_auth") || err.equals("not_authed")) {
                            // Slack認証エラー
                            callConnectionFinishCallback(new SlackAuthException(), null);
                        } else {
                            // Slackサーバーエラー
                            callConnectionFinishCallback(new SlackConnectionException(), null);
                        }
                        return;
                    }
                    JSONArray jsonArray = json.getJSONArray("messages");
                    int length = jsonArray.length();
                    ArrayList<HistoryInfo> array = new ArrayList<>(length);
                    for (int i = 0; i < length; i++) {
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        HistoryInfo info = new HistoryInfo();
                        String type = obj.getString("type");
                        if (BuildConfig.DEBUG) Log.d(TAG, "obj:" + obj.toString());
                        if (!"message".equals(type)) {
                            continue;
                        }
                        if (obj.has("user")) {
                            info.user = obj.getString("user");
                        }
                        if (obj.has("text")) {
                            info.text = obj.getString("text");
                        }
                        if (obj.has("ts")) {
                            info.ts = obj.getDouble("ts");
                        }
                        if (obj.has("file")) {
                            JSONObject file = obj.getJSONObject("file");
                            if (file.has("thumb_360")) {
                                info.thumb = file.getString("thumb_360");
                            }
                            if (file.has("thumb_360_h")) {
                                info.thumbHeight = file.getInt("thumb_360_h");
                            }
                            if (file.has("thumb_360_w")) {
                                info.thumbWidth = file.getInt("thumb_360_w");
                            }
                            if (file.has("initial_comment")) {
                                JSONObject comment = file.getJSONObject("initial_comment");
                                if (comment.has("comment")) {
                                    info.text = comment.getString("comment");
                                }
                            } else {
                                if (info.thumb != null) {
                                    info.text = null;
                                }
                            }
                        }
                        array.add(info);
                    }
                    if (callback != null) {
                        callback.onFinish(array, null);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "error", e);
                    if (callback != null) {
                        callback.onFinish(null, e);
                    }
                }
            }
        });
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region Channel/User/IM List

    /**
     * 受け渡し情報
     */
    public class ListInfo {
        public String id;
        public String name;
        public String icon;
    }


    /**
     * Channel一覧とDM一覧を合成したものを取得
     * @param callback 取得コールバック
     */
    public void getAllChannelList(final FinishCallback<List<ListInfo>> callback) {
        getAllChannelList(callback, null);
    }

    /**
     * Channel一覧とDM一覧を合成したものを取得
     * @param callback 取得コールバック
     * @param handler Callbackを返すスレッド
     */
    public void getAllChannelList(final FinishCallback<List<ListInfo>> callback, final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                final CountDownLatch latch = new CountDownLatch(3);
                final HashMap<String, ArrayList<ListInfo>> resMap = new HashMap<>();
                final Exception[] err = new Exception[1];

                // Channelリスト取得
                getChannelList(new FinishCallback<ArrayList<ListInfo>>() {
                    @Override
                    public void onFinish(ArrayList<ListInfo> listInfos, Exception error) {
                        if (error == null) {
                            resMap.put("channel", listInfos);
                        } else {
                            err[0] = error;
                            Log.e("slack", "err", error);
                        }
                        latch.countDown();
                    }
                });

                // IMリスト取得
                getIMList(new FinishCallback<ArrayList<ListInfo>>() {
                    @Override
                    public void onFinish(ArrayList<ListInfo> listInfos, Exception error) {
                        if (error == null) {
                            resMap.put("im", listInfos);
                        } else {
                            err[0] = error;
                            Log.e("slack", "err", error);
                        }
                        latch.countDown();
                    }
                });

                // ユーザーリスト取得
                getUserList(new FinishCallback<ArrayList<ListInfo>>() {
                    @Override
                    public void onFinish(ArrayList<ListInfo> listInfos, Exception error) {
                        if (error == null) {
                            resMap.put("user", listInfos);
                        } else {
                            err[0] = error;
                            Log.e("slack", "err", error);
                        }
                        latch.countDown();
                    }
                });

                // 処理終了を待つ
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 各情報を詰め替え
                ArrayList<ListInfo> channels = resMap.get("channel");
                ArrayList<ListInfo> ims = resMap.get("im");
                ArrayList<ListInfo> users = resMap.get("user");
                if (channels != null && ims != null && users != null) {
                    final List<ListInfo> resList = new ArrayList<>();
                    // Channel
                    resList.addAll(channels);
                    // UserをHashMapへ
                    HashMap<String, ListInfo> userMap = new HashMap<>();
                    for (ListInfo info : users) {
                        userMap.put(info.id, info);
                    }
                    // IM
                    for (ListInfo info : ims) {
                        // UserIDからUserNameを取得
                        ListInfo user = userMap.get(info.name);
                        if (user != null) {
                            info.name = user.name;
                            info.icon = user.icon;
                        }
                        resList.add(info);
                    }

                    if (callback != null) {
                        if (handler != null) {
                            handler.post(new Runnable() {
                                public void run() {
                                    callback.onFinish(resList, null);
                                }
                            });
                        } else {
                            callback.onFinish(resList, null);
                        }
                    }
                } else {
                    if (callback != null) {
                        if (handler != null) {
                            handler.post(new Runnable() {
                                public void run() {
                                    callback.onFinish(null, err[0]);
                                }
                            });
                        } else {
                            callback.onFinish(null, err[0]);
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * Channel一覧を取得
     * @param callback 取得コールバック
     */
    public void getChannelList(final FinishCallback<ArrayList<ListInfo>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*getChannelList");
        getList("channels.list", "&exclude_archived=1", "channels", callback);
    }

    /**
     * IM一覧を取得
     * @param callback 取得コールバック
     */
    public void getIMList(final FinishCallback<ArrayList<ListInfo>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*getIMList");
        getList("im.list", "", "ims",callback);
    }

    /**
     * ユーザー一覧を取得
     * @param callback 取得コールバック
     */
    public void getUserList(final FinishCallback<ArrayList<ListInfo>> callback) {
        if (BuildConfig.DEBUG) Log.d(TAG, "*getUserList");
        getList("users.list", "", "members", callback);
    }

    /**
     * 一覧取得ベース
     * @param target ターゲットAPI
     * @param params パラメータ
     * @param listname リスト名
     * @param callback 取得コールバック
     */
    private void getList(String target, String params, final String listname, final FinishCallback<ArrayList<ListInfo>> callback) {
        if (connectState != CONNECT_STATE_CONNECTED) {
            if (callback != null) {
                callback.onFinish(null, new SlackConnectionException());
            }
            return;
        }
        new GetTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam(target, params) {
            @Override
            public void callBack(JSONObject json) {
                if (BuildConfig.DEBUG && json != null) Log.d(TAG, json.toString());

                if (json == null) {
                    if (callback != null) {
                        // Slackサーバーエラー
                        callback.onFinish(null, new SlackConnectionException());
                    }
                    return;
                }

                try {
                    if (json.has("error") && callback != null) {
                        connectState = CONNECT_STATE_DISCONNECTED;
                        String err = json.getString("error");
                        if (err.equals("invalid_auth") || err.equals("not_authed")) {
                            // Slack認証エラー
                            callback.onFinish(null, new SlackAuthException());
                        } else {
                            // Slackサーバーエラー
                            callback.onFinish(null, new SlackConnectionException());
                        }
                        return;
                    }
                    JSONArray jsonArray = json.getJSONArray(listname);
                    int length = jsonArray.length();
                    ArrayList<ListInfo> array = new ArrayList<>(length);
                    for (int i = 0; i < length; i++) {
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        ListInfo info = new ListInfo();
                        if (obj.has("id")) {
                            info.id = obj.getString("id");
                        }
                        if (obj.has("name")) {
                            info.name = obj.getString("name");
                        }
                        if (obj.has("user")) {
                            info.name = obj.getString("user");
                        }
                        /* RealNameいる？
                        if (obj.has("real_name")) {
                            String realName = obj.getString("real_name");
                            if (!realName.isEmpty()) {
                                info.name = realName;
                            }
                        }
                        */
                        if (obj.has("profile")) {
                            JSONObject prof = obj.getJSONObject("profile");
                            info.icon = prof.getString("image_192");
                        }
                        array.add(info);
                    }
                    if (callback != null) {
                        callback.onFinish(array, null);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "error", e);
                    if (callback != null) {
                        callback.onFinish(null, e);
                    }
                }
            }
        });
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region GetTask

    /**
     * Get用Task用パラメータ
     */
    private class TaskParam {
        protected String target;
        protected String params;
        protected URL resource;
        protected String origin;
        public void callBack(JSONObject json) {}
        public TaskParam(String target, String params) {
            this.target = target;
            this.params = params;
        }
        public TaskParam(String target, String params, URL resource, String origin) {
            this.target = target;
            this.params = params;
            this.resource = resource;
            this.origin = origin;
        }
    }

    /**
     * Get用Task
     */
    private class GetTask extends AsyncTask<TaskParam, Void, JSONObject> {

        /** パラメータ */
        TaskParam param = null;

        /**
         * Background処理.
         *
         * @param params Params
         */
        @Override
        protected JSONObject doInBackground(TaskParam... params) {

            HttpURLConnection con = null;
            URL url;
            InputStream stream = null;
            JSONObject json = null;
            param = params[0];

            try {
                // 接続
                url = new URL(BASE_URL + param.target + "?token=" + token + param.params);
                con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setInstanceFollowRedirects(false);
                con.connect();
                stream = con.getInputStream();
                if (BuildConfig.DEBUG) Log.d(TAG, "url:"+url.toString());

                // レスポンス取得
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                // Json
                json = new JSONObject(responseStrBuilder.toString());

            } catch (IOException | JSONException e) {
                Log.e(TAG, "error", e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stream close error", e);
                    }
                }
                if (con != null) {
                    con.disconnect();
                }
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            param.callBack(json);
        }
    }


    //endregion
    //---------------------------------------------------------------------------------------
    //region FileUpload

    /**
     * Slackにファイルをアップロード.
     * @param msg コメント
     * @param channel チャンネル
     * @param url リソースURL
     * @param callback 終了コールバック
     */
    public void uploadFile(String msg, String channel, URL url, String orign, final FinishCallback<JSONObject> callback) {
        if (url == null || channel == null) {
            if (callback != null) {
                callback.onFinish(null, new InvalidParameterException());
            }
            return;
        }
        new UploadFileTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TaskParam(channel, msg, url, orign) {
            @Override
            public void callBack(JSONObject json) {
                if (callback != null) {
                    SlackManagerException exception = null;
                    if (json == null) {
                        exception = new SlackConnectionException();
                    } else {
                        try {
                            if (!json.getBoolean("ok")) {
                                exception = new SlackConnectionException();
                            }
                        } catch (JSONException e) {
                            exception = new SlackConnectionException();
                        }
                    }
                    callback.onFinish(json, exception);
                }
            }
        });
    }

    /**
     * Slackにファイルをアップロード.
     * @param msg コメント
     * @param channel チャンネル
     * @param url リソースURL
     */
    public void uploadFile(String msg, String channel, URL url, String orign) {
        uploadFile(msg, channel, url, orign, null);
    }

    /**
     * ファイルアップロード用Task
     */
    private class UploadFileTask extends AsyncTask<TaskParam, Void, JSONObject> {

        /** パラメータ */
        TaskParam param = null;

        /**
         * Background処理.
         *
         * @param params Params
         */
        @Override
        protected JSONObject doInBackground(TaskParam... params) {

            HttpURLConnection con1 = null;
            HttpURLConnection con2 = null;
            BufferedInputStream stream1 = null;
            DataOutputStream stream2 = null;
            BufferedReader streamReader = null;
            JSONObject json = null;
            param = params[0];

            try {
                // リソース取得
                URL url = param.resource;
                con1 = (HttpURLConnection)url.openConnection();
                con1.setInstanceFollowRedirects(true);
                con1.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, param.origin);

                con1.connect();
                stream1 = new BufferedInputStream(con1.getInputStream());

                // Slack接続
                url = new URL(BASE_URL + "files.upload");
                con2 = (HttpURLConnection)url.openConnection();
                con2.setRequestMethod("POST");
                con2.setInstanceFollowRedirects(false);
                con2.setDoOutput(true);
                con2.setRequestProperty("Accept-Charset", "UTF-8");
                con2.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                BufferedOutputStream bo = new BufferedOutputStream(con2.getOutputStream());
                stream2 = new DataOutputStream(bo);

                // パラメータ設定
                addDisposition(stream2, "token", token);
                addDisposition(stream2, "channels", param.target);
                if (param.params != null) {
                    addDisposition(stream2, "initial_comment", param.params);
                }
                addData(stream2, stream1, "file", param.resource.getFile());
                addEnd(stream2);
                stream2.flush();

                // 接続
                con2.connect();

                // レスポンス取得
                streamReader = new BufferedReader(new InputStreamReader(con2.getInputStream(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                // Json
                json = new JSONObject(responseStrBuilder.toString());
                if (BuildConfig.DEBUG) Log.d(TAG, responseStrBuilder.toString());


            } catch (IOException | JSONException e) {
                Log.e(TAG, "error", e);
            } finally {
                if (streamReader != null) {
                    try {
                        streamReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stream close error", e);
                    }
                }
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stream close error", e);
                    }
                }
                if (stream1 != null) {
                    try {
                        stream1.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stream close error", e);
                    }
                }
                if (con2 != null) {
                    con2.disconnect();
                }
                if (con1 != null) {
                    con1.disconnect();
                }
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            param.callBack(json);
        }

        /** 区切り文字 */
        private static final String BOUNDARY = "==================================";

        /**
         * パラメータ追加
         * @param os OutputStream
         * @param name 名前
         * @param value 値
         * @throws IOException 例外
         */
        private void addDisposition(DataOutputStream os, String name, String value) throws IOException {
            name = escape(name);
            os.writeBytes("--" + BOUNDARY + "\r\n");
            os.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
            byte[] bytes = value.getBytes();
            for (int i=0;i<bytes.length;i++){
                os.writeByte(bytes[i]);
            }
            os.writeBytes("\r\n");
        }

        /**
         * バイナリデータを追加
         * @param os OutputStream
         * @param is InputStream
         * @param name 名前
         * @param filename ファイル名
         * @throws IOException 例外
         */
        private void  addData(DataOutputStream os, InputStream is, String name, String filename) throws IOException {
            filename = escape(filename);
            os.writeBytes("--" + BOUNDARY + "\r\n");
            os.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
            os.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
            // リソースを書き出し
            int c;
            while ((c = is.read()) >= 0) {
                os.write(c);
            }
            os.writeBytes("\n");
        }

        /**
         * 終了文字を追加
         * @param os OutputStream
         * @throws IOException 例外
         */
        private void addEnd(DataOutputStream os) throws IOException {
            os.writeBytes("--" + BOUNDARY + "--\r\n");
        }
    }
    //endregion
    //---------------------------------------------------------------------------------------
}
