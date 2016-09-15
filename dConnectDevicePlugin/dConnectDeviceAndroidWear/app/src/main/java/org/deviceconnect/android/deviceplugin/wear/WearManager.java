package org.deviceconnect.android.deviceplugin.wear;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.deviceconnect.android.deviceplugin.wear.profile.WearConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Android Wearを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearManager implements ConnectionCallbacks, OnConnectionFailedListener {

    private final Logger mLogger = Logger.getLogger("dconnect.wear");

    /**
     * Google Play Service.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * コンテキスト.
     */
    private final Context mContext;

    /**
     * スレッド管理用クラス.
     */
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * メッセージイベントリスナー一覧.
     */
    private final Map<String, OnMessageEventListener> mOnMessageEventListeners
            = new HashMap<String, OnMessageEventListener>();

    /**
     * ノード検知リスナー一覧.
     */
    private final List<NodeEventListener> mNodeEventListeners = new ArrayList<NodeEventListener>();

    /**
     * ノード情報のキャッシュ.
     */
    private final Map<String, Node> mNodeCache = new HashMap<String, Node>();

    /**
     * コンストラクタ.
     *
     * @param context このクラスが属するコンテキスト
     */
    public WearManager(final Context context) {
        mContext = context;
    }

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
    }


    @Override
    public void onConnected(final Bundle bundle) {
        setNodeListener();
        setMessageListener();
        getNodes(new OnNodeResultListener() {
            @Override
            public void onResult(final NodeApi.GetConnectedNodesResult result) {
                List<Node> nodes = result.getNodes();
                if (nodes != null) {
                    synchronized (mNodeCache) {
                        for (Node node : nodes) {
                            if (!mNodeCache.containsKey(node.getId())) {
                                mNodeCache.put(node.getId(), node);
                                mLogger.info("getNodes: name = " + node.getDisplayName()
                                    + ", id = " + node.getId());
                                notifyOnNodeConnected(node);
                            }
                        }
                    }
                }
            }
        });
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
    public void destroy() {
        mExecutorService.shutdown();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        mNodeEventListeners.clear();
        mOnMessageEventListeners.clear();
        mNodeCache.clear();
    }

    /**
     * Android Wearとの接続状態を取得する.
     *
     * @return true接続中、それ以外はfalse
     */
    public boolean isConnected() {
        if (mGoogleApiClient != null) {
            return mGoogleApiClient.isConnected();
        }
        return false;
    }

    /**
     * メッセージイベントリスナーを追加する.
     *
     * @param path     パス
     * @param listener リスナー
     */
    public void addMessageEventListener(final String path, final OnMessageEventListener listener) {
        mOnMessageEventListeners.put(path, listener);
    }

    /**
     * ノード検知リスナーを追加する.
     */
    public void addNodeListener(final NodeEventListener listener) {
        synchronized (mNodeEventListeners) {
            mNodeEventListeners.add(listener);
        }
    }

    private void setNodeListener() {
        Wearable.NodeApi.addListener(mGoogleApiClient, new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(final Node node) {
                mLogger.info("onPeerConnected: name = " + node.getDisplayName()
                    + ", id = " + node.getId());
                mNodeCache.put(node.getId(), node);
                notifyOnNodeConnected(node);
            }

            @Override
            public void onPeerDisconnected(final Node node) {
                mLogger.info("onPeerDisconnected: name = " + node.getDisplayName()
                    + ", id = " + node.getId());
                mNodeCache.remove(node.getId());
                notifyOnNodeDisconnected(node);
            }
        });
    }

    private void notifyOnNodeConnected(final Node node) {
        synchronized (mNodeEventListeners) {
            for (NodeEventListener listener : mNodeEventListeners) {
                listener.onNodeConnected(node);
            }
        }
    }

    private void notifyOnNodeDisconnected(final Node node) {
        synchronized (mNodeEventListeners) {
            for (NodeEventListener listener : mNodeEventListeners) {
                listener.onNodeDisconnected(node);
            }
        }
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
                mLogger.info("onMessageReceived: path = " + path);
                OnMessageEventListener listener = mOnMessageEventListeners.get(path);
                if (listener != null) {
                    listener.onEvent(nodeId, data);
                }
            }
        });
    }

    /**
     * Wear nodeを取得.
     *
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
     *
     * @param dest     送信先のWearのnodeId
     * @param action   メッセージのアクション
     * @param message  メッセージ
     * @param listener メッセージを送信した結果を通知するリスナー
     */
    public void sendMessageToWear(final String dest, final String action, final String message,
                                  final OnMessageResultListener listener) {
        sendMessageToWear(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                MessageApi.SendMessageResult result = null;
                for (Node node : nodes.getNodes()) {
                    if (node.getId().indexOf(dest) != -1) {
                        result = Wearable.MessageApi.sendMessage(
                                mGoogleApiClient, node.getId(), action, message.getBytes()).await();
                    }
                }
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
     * PutDataRequestを作成する.
     *
     * @param nodeId   ノードID
     * @param requestId リクエストID
     * @param data requestに格納する画像
     * @param x    x座標
     * @param y    y座標
     * @param mode 描画モード
     * @return PutDataRequestのインスタンス
     */
    private PutDataRequest createPutDataRequest(final String nodeId, final String requestId, final byte[] data,
                                                final int x, final int y, final int mode) {
        Asset asset = Asset.createFromBytes(data);
        if (asset == null) {
            return null;
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create(WearConst.PATH_CANVAS + "/" + nodeId + "/" + requestId);
        dataMap.getDataMap().putAsset(WearConst.PARAM_BITMAP, asset);
        dataMap.getDataMap().putInt(WearConst.PARAM_X, x);
        dataMap.getDataMap().putInt(WearConst.PARAM_Y, y);
        dataMap.getDataMap().putInt(WearConst.PARAM_MODE, mode);
        dataMap.getDataMap().putLong(WearConst.TIMESTAMP,
                System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        return request;
    }

    /**
     * 画像データを送信する.
     *
     * @param nodeId   ノードID
     * @param requestId リクエストID
     * @param data     画像データ
     * @param x        x座標
     * @param y        y座標
     * @param mode     描画モード
     * @param listener 送信結果を通知するリスナー
     */
    public void sendImageData(final String nodeId, final String requestId,
                              final byte[] data, final int x, final int y,
                              final int mode, final OnDataItemResultListener listener) {
        // リクエストIDとともに画像送信
        sendMessageToWear(new Runnable() {
            @Override
            public void run() {
                final PutDataRequest request = createPutDataRequest(nodeId, requestId, data, x, y, mode);
                if (request == null) {
                    if (listener != null) {
                        listener.onError();
                    }
                } else {
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request);
                    DataApi.DataItemResult result = pendingResult.await();
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
            }
        });
    }

    /**
     * Wearにメッセージを送ります.
     *
     * @param run 送るメッセージを実行するrunnable
     */
    private void sendMessageToWear(final Runnable run) {
        mExecutorService.execute(run);
    }

    public void getLocalNodeId(final OnLocalNodeListener listener) {
        sendMessageToWear(new Runnable() {
            @Override
            public void run() {
                PendingResult<NodeApi.GetLocalNodeResult> pendingResult = Wearable.NodeApi.getLocalNode(mGoogleApiClient);
                NodeApi.GetLocalNodeResult result = pendingResult.await();
                if (result.getStatus().isSuccess()) {
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
     * ノード検知イベントリスナー.
     */
    public interface NodeEventListener {

        /**
         * ノードとの接続イベント.
         * @param node ノード
         */
        void onNodeConnected(Node node);

        /**
         * ノードとの接続の切断イベント.
         * @param node ノード
         */
        void onNodeDisconnected(Node node);

    }

    /**
     * Nodeの検索結果を通知するリスナー.
     */
    public interface OnNodeResultListener {
        /**
         * 結果を通知する.
         *
         * @param result 検索結果
         */
        void onResult(NodeApi.GetConnectedNodesResult result);

    }

    public interface OnLocalNodeListener {

        void onResult(NodeApi.GetLocalNodeResult localNode);

        void onError();
    }

    /**
     * メッセージ送信の結果を通知するリスナー.
     */
    public interface OnMessageResultListener {
        /**
         * メッセージ送信結果を通知する.
         *
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
         *
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
         *
         * @param nodeId  ノートID
         * @param message イベントメッセージ
         */
        void onEvent(String nodeId, String message);
    }
}
