package org.deviceconnect.android.deviceplugin.wear;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deviceconnect.android.deviceplugin.wear.profile.WearConst;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Android Wearを管理するクラス.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearManager implements ConnectionCallbacks, OnConnectionFailedListener {
    /**
     * PNGのクオリティを定義する.
     */
    private static final int PNG_QUALTY = 100;

    /**
     * Google Play Service.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * スレッド管理用クラス.
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * イベントリスナー一覧.
     */
    private Map<String, OnMessageEventListener> mListeners
            = new HashMap<String, OnMessageEventListener>();

    /**
     * コンストラクタ.
     * @param context このクラスが属するコンテキスト
     */
    public WearManager(final Context context) {
        mContext = context;
        init();
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
    }


    @Override
    public void onConnected(final Bundle bundle) {
        setMessageListener();
    }

    @Override
    public void onConnectionSuspended(final int state) {
    }

    /**
     * このクラスを初期化する.
     */
    public void init() {
        if (mGoogleApiClient != null) {
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * 後始末処理を行う.
     */
    public void destory() {
        mExecutorService.shutdown();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Android Wearとの接続状態を取得する.
     * @return true接続中、それ以外はfalse
     */
    public boolean isConnected() {
        if (mGoogleApiClient != null) {
            return mGoogleApiClient.isConnected();
        }
        return false;
    }
    /**
     * イベントリスナーを追加する.
     * @param path パス
     * @param listener リスナー
     */
    public void addMessageEventListener(final String path, final OnMessageEventListener listener) {
        mListeners.put(path, listener);
    }

    /**
     * Android Wearのリスナーを設定する.
     */
    private void setMessageListener() {
        Wearable.MessageApi.addListener(mGoogleApiClient, new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(final MessageEvent messageEvent) {
                final String data = new String(messageEvent.getData());
                final String path = messageEvent.getPath();
                final String nodeId = messageEvent.getSourceNodeId();
                OnMessageEventListener listener = mListeners.get(path);
                if (listener != null) {
                    listener.onEvent(nodeId, data);
                }
            }
        });
    }

    /**
     * Wear nodeを取得.
     * @param listener Wear node取得を通知するリスナー
     */
    public void getNodes(final OnNodeResultListener listener) {
        sendMessageToWear(new Runnable() {
            public void run() {
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi
                        .getConnectedNodes(mGoogleApiClient).await();
                if (listener != null) {
                    listener.onResult(result);
                }
            }
        });
    }

    /**
     * メッセージをWearに送信する.
     * @param dest 送信先のWearのnodeId
     * @param action メッセージのアクション
     * @param message メッセージ
     * @param listener メッセージを送信した結果を通知するリスナー
     */
    public void sendMessageToWear(final String dest, final String action, final String message,
            final OnMessageResultListener listener) {
        sendMessageToWear(new Runnable() {
            @Override
            public void run() {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, dest, action, message.getBytes()).await();
                if (result != null) {
                    if (listener != null) {
                        listener.onResult(result);
                    }
                } else {
                    if (listener != null) {
                        listener.onError();
                    }
                }
            }
        });
    }

    /**
     * Wearにメッセージを送ります.
     * @param run 送るメッセージを実行するrunnable
     */
    private void sendMessageToWear(final Runnable run) {
        mExecutorService.execute(run);
    }

    /**
     * Nodeの検索結果を通知するリスナー.
     */
    public interface OnNodeResultListener {
        /**
         * 結果を通知する.
         * @param result 検索結果
         */
        void onResult(NodeApi.GetConnectedNodesResult result);
        /**
         * エラーが発生したことを通知する.
         */
        void onError();
    }

    /**
     * メッセージ送信の結果を通知するリスナー.
     */
    public interface OnMessageResultListener {
        /**
         * メッセージ送信結果を通知する.
         * @param result 結果
         */
        void onResult(MessageApi.SendMessageResult result);
        /**
         * エラーが発生したことを通知する.
         */
        void onError();
    }

    /**
     * データ送信の結果を通知するリスナー.
     */
    public interface OnDataItemResultListener {
        /**
         * データ送信の結果を通知する.
         * @param result 結果
         */
        void onResult(DataApi.DataItemResult result);
        /**
         * エラーが発生したことを通知する.
         */
        void onError();
    }

    /**
     * イベント受信を通知するリスナー.
     */
    public interface OnMessageEventListener {
        /**
         * 受信したイベントを通知する.
         * @param nodeId ノートID
         * @param message イベントメッセージ
         */
        void onEvent(String nodeId, String message);
    }
}
