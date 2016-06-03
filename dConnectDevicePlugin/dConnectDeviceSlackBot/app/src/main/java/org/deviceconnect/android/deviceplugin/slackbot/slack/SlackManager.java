package org.deviceconnect.android.deviceplugin.slackbot.slack;

import android.os.AsyncTask;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Slackの制御を管理するクラス.
 */
public class SlackManager {

    /** シングルトンなManagerのインスタンス */
    public static final SlackManager INSTANCE = new SlackManager();

    /** TAG. */
    private final static String TAG = "SlackManager";

    /** 接続状態フラグ */
    private int connectFlg = 0; // 0:None 1:Disconnected 2:Disconnecting 3:Connecting 4:Connected

    /** WebSocket */
    private WebSocketClient webSocket;

    /** SlackBotのApiToken */
    private String token = null;

    /**
     * WebSocket接続
     * @param uri 接続先
     */
    private void connectWebSocket(URI uri) {
        webSocket = new WebSocketClient(uri, new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "Connected!");
                connectFlg = 4;
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, String.format("Got string message! %s", message));
            }

            @Override
            public void onMessage(byte[] data) {
            }

            @Override
            public void onDisconnect(int code, String reason) {
                Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
                connectFlg = 1;
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error!", error);
            }
        }, null);
        webSocket.connect();
    }

    /**
     * Slack送信用Task
     */
    class ConnectTask extends AsyncTask<Void, Void, String> {

        /**
         * Background処理.
         *
         * @param params Params
         * @return エラーメッセージ
         */
        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection con = null;
            URL url;
            InputStream stream = null;
            try {
                // 接続
                url = new URL("https://slack.com/api/rtm.start?token=" + token + "&simple_latest=True&no_unreads=True");
                con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setInstanceFollowRedirects(false);
                con.connect();
                stream = con.getInputStream();
                Log.d(TAG, "token:"+token);

                // レスポンス取得
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                // 接続先取得
                JSONObject json = new JSONObject(responseStrBuilder.toString());
                String jsonUrl = json.getString("url");
                Log.d(TAG, "url:"+jsonUrl);

                // 接続
                connectWebSocket(URI.create(jsonUrl));

            } catch (IOException e) {
                Log.e(TAG, "error", e);
            } catch (JSONException e) {
                Log.e(TAG, "error", e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stream close error", e);
                        e.printStackTrace();
                    }
                }
                if (con != null) {
                    con.disconnect();
                }
            }
            return null;
        }

        /**
         * Background処理終了時.
         *
         * @param s エラーメッセージ
         */
        @Override
        protected void onPostExecute(String s) {
//            if (callback != null) {
//                callback.onFinished(s);
//            }
        }
    }


    /**
     * 初期化。シングルトンのためにprivate.
     */
    private SlackManager() {
    }

    /**
     * SlackBotのAPITokenを設定.
     * @param apiToken APIToken
     */
    public void setApiToken(final String apiToken) {
        // 不正文字列を入力できないようにencodeしておく
        try {
            token = URLEncoder.encode(apiToken,"utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "error", e);
            return;
        }
        // 接続中なら再接続
        if (connectFlg > 2) {
            disconnect();
            connect();
        }
    }

    /**
     * 接続.
     */
    public void connect() {
        // TODO: エラーを返す
        if (token == null) {
            return;
        }
        if (connectFlg > 2) return;
        connectFlg = 3;
        ConnectTask task = new ConnectTask();
        task.execute();
    }

    /**
     * 切断.
     */
    public void disconnect() {
        if (connectFlg < 3) return;
        connectFlg = 2;
        webSocket.disconnect();
    }

    /**
     * Slackにメッセージ送信.
     * @param msg メッセージ
     */
    public void sendMessage(String msg, String channel) {
        if (connectFlg != 4) return;
        String data = "{\"id\": 1, \"type\": \"message\", \"channel\": \"" + channel + "\", \"text\": \"" + msg + "\"}";
        webSocket.send(data);
    }
}
