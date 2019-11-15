package org.deviceconnect.android.deviceplugin.wear;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.deviceconnect.android.deviceplugin.wear.profile.WearConst;

import org.deviceconnect.android.logger.AndroidHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Android Wearを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearManager {

    private final Logger mLogger = Logger.getLogger("dconnect.wear");


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
            = new HashMap<>();

    /**
     * ノード検知リスナー一覧.
     */
    private final List<NodeEventListener> mNodeEventListeners = new ArrayList<>();

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
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(mLogger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }


    /**
     * このクラスを初期化する.
     */
    public void init() {
        setCapabilityListener();
        setMessageListener();
        getNodes((results) -> {
            if (results == null) {
                return;
            }
            synchronized (mNodeCache) {
                for (Node node : results) {
                    if (!mNodeCache.containsKey(node.getId())) {
                        mNodeCache.put(node.getId(), node);
                        mLogger.info("getNodes: name = " + node.getDisplayName()
                                + ", id = " + node.getId());
                        notifyOnNodeConnected(node);
                    }
                }
            }
        });
    }

    /**
     * 接続しているAndroidWearにIDを送る.
     */
    public void sendWearData() {
        new Thread(() -> {
            getNodes((results) -> {
                if (results == null) {
                    return;
                }
                synchronized (mNodeCache) {
                    for (Node node : results) {
                        if (!mNodeCache.containsKey(node.getId())) {
                            mNodeCache.put(node.getId(), node);
                            mLogger.info("getNode: name = " + node.getDisplayName()
                                    + ", id = " + node.getId());
                            notifyOnNodeConnected(node);

                        }
                    }
                }
            });
            for (String key : mNodeCache.keySet()) {
                Node node = mNodeCache.get(key);
                sendMessageToWear(node.getId(), WearConst.DEVICE_TO_WEAR_SET_ID, node.getId(), null);
                mLogger.info("sendMessage: name = " + node.getDisplayName()
                        + ", id = " + node.getId());
            }
    }).start();
    }
    /**
     * 後始末処理を行う.
     */
    public void destroy() {
        mExecutorService.shutdown();
        mNodeEventListeners.clear();
        mOnMessageEventListeners.clear();
        mNodeCache.clear();
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

    /**
     * Wearとの接続状況を検知するリスナー.
     */
    private void setCapabilityListener() {
        Wearable.getCapabilityClient(mContext).addListener((capabilityInfo) -> {
            for (Node node : capabilityInfo.getNodes()) {
                if (node.isNearby()) {
                    mLogger.info("isNearby=true: name = " + node.getDisplayName()
                            + ", id = " + node.getId());
                    mNodeCache.put(node.getId(), node);
                    notifyOnNodeConnected(node);
                } else {
                    mLogger.info("onPeerDisconnected: name = " + node.getDisplayName()
                            + ", id = " + node.getId());
                    mNodeCache.remove(node.getId());
                    notifyOnNodeDisconnected(node);
                }
            }
        }, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
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
        Wearable.getMessageClient(mContext).addListener((messageEvent) -> {
            final String data = new String(messageEvent.getData());
            final String path = messageEvent.getPath();
            final String nodeId = messageEvent.getSourceNodeId();
            OnMessageEventListener listener = mOnMessageEventListeners.get(path);
            if (listener != null) {
                listener.onEvent(nodeId, data);
            }
        });
    }

    /**
     * Wear nodeを取得.
     *
     * @param listener Wear node取得を通知するリスナー
     */
    public void getNodes(final OnNodeResultListener listener) {
        sendMessageToWear(() -> {
            Task<List<Node>> nodeListTask = Wearable.getNodeClient(mContext).getConnectedNodes();
            List<Node> nodes = null;
            try {
                 nodes = Tasks.await(nodeListTask);
            } catch (ExecutionException exception) {
                mLogger.warning("Task failed: " + exception);
            } catch (InterruptedException exception) {
                mLogger.warning("Interrupt occurred: " + exception);
            }
            if (listener != null) {
                listener.onResult(nodes);
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
        getNodes((results) -> {
            for (Node node : results) {
                if (node.getId().contains(dest)) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(mContext).sendMessage(node.getId(), action, message.getBytes());
                    sendMessageTask.addOnSuccessListener((integer) -> {
                        if (listener != null) {
                            listener.onResult();
                        }
                    });

                    sendMessageTask.addOnFailureListener((e) -> {
                        if (listener != null) {
                            listener.onError();
                        }
                    });
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
        PutDataMapRequest dataMap = PutDataMapRequest.create(WearConst.PATH_CANVAS + "/" + nodeId + "/" + requestId);
        dataMap.getDataMap().putAsset(WearConst.PARAM_BITMAP, asset);
        dataMap.getDataMap().putInt(WearConst.PARAM_X, x);
        dataMap.getDataMap().putInt(WearConst.PARAM_Y, y);
        dataMap.getDataMap().putInt(WearConst.PARAM_MODE, mode);
        dataMap.getDataMap().putLong(WearConst.TIMESTAMP,
                System.currentTimeMillis());
        return dataMap.asPutDataRequest();
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
        sendMessageToWear(() -> {
            final PutDataRequest request = createPutDataRequest(nodeId, requestId, data, x, y, mode);
            if (request == null) {
                if (listener != null) {
                    listener.onError();
                }
            } else {
                request.setUrgent();
                Task<DataItem> dataItemTask = Wearable.getDataClient(mContext).putDataItem(request);
                dataItemTask.addOnSuccessListener((dataItem) -> {
                    if (listener != null) {
                        listener.onResult(dataItem);
                    }
                });
                dataItemTask.addOnFailureListener((e) -> {
                    if (listener != null) {
                        listener.onError();
                    }
                });
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

    public void getLocalNodeId(final String serviceId, final OnLocalNodeListener listener) {
        getNodes((results) -> {
             for (Node node : results) {
                 if (node.getId().equals(serviceId)) {
                     if (listener != null) {
                         listener.onResult(node);
                     }
                     return;
                 }
             }
             if (listener != null) {
                 listener.onError();
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
         * @param results 検索結果
         */
        void onResult(List<Node> results);

    }

    public interface OnLocalNodeListener {

        void onResult(Node localNode);

        void onError();
    }

    /**
     * メッセージ送信の結果を通知するリスナー.
     */
    public interface OnMessageResultListener {
        /**
         * メッセージ送信結果を通知する.
         *
         */
        void onResult();

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
        void onResult(DataItem result);

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
