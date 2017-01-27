/*
 IntentDConnectSDK.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.deviceconnect.message.entity.BinaryEntity;
import org.deviceconnect.message.entity.Entity;
import org.deviceconnect.message.entity.FileEntity;
import org.deviceconnect.message.entity.MultipartEntity;
import org.deviceconnect.message.entity.StringEntity;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intentを使用してDevice Connect Managerと通信を行うSDKクラス.
 * @author NTT DOCOMO, INC.
 */
class IntentDConnectSDK extends DConnectSDK {
    /**
     * レスポンスを格納するマップ.
     */
    private static Map<Integer, Intent> sResponseMap = new ConcurrentHashMap<>();

    /**
     * イベントを配送するSDKを登録するリスト.
     */
    private static final List<IntentDConnectSDK> sEventList = Collections.synchronizedList(new ArrayList<IntentDConnectSDK>());

    /**
     * リスナーを登録するマップ.
     */
    private Map<String, HttpDConnectSDK.OnEventListener> mListenerMap = new HashMap<>();

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * タイムアウト.
     */
    private int mSoTimeout = 30 * 1000;

    /**
     * Device Connect Managerのパッケージ名.
     * TODO: 別のManagerへ送りたいときにどうするべきか検討
     */
    private String mManagerPackageName = "org.deviceconnect.android.manager";

    /**
     * Device Connect Managerのクラス名.
     */
    private String mManagerClassName = "org.deviceconnect.android.manager.DConnectBroadcastReceiver";

    /**
     * イベントを通知するリスナー.
     */
    private OnWebSocketListener mOnWebSocketListener;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    IntentDConnectSDK(final Context context) {
        mContext = context;
    }

    /**
     * 接続先のDevice Connect Managerのパッケージ名を設定する.
     * @param managerPackageName パッケージ名
     */
    public void setManagerPackageName(final String managerPackageName) {
        mManagerPackageName = managerPackageName;
    }

    /**
     * 接続先のDevcie Connect Managerのクラス名を設定する.
     * @param managerClassName クラス名
     */
    public void setManagerClassName(final String managerClassName) {
        mManagerClassName = managerClassName;
    }

    @Override
    public void connectWebSocket(final OnWebSocketListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null.");
        } else {
            mOnWebSocketListener = listener;
            sEventList.add(IntentDConnectSDK.this);
            listener.onOpen();
        }
    }

    @Override
    public void disconnectWebSocket() {
        if (mOnWebSocketListener != null) {
            mOnWebSocketListener.onClose();
            mOnWebSocketListener = null;
        }
        sEventList.remove(IntentDConnectSDK.this);
    }

    @Override
    public boolean isConnectedWebSocket() {
        return !sEventList.isEmpty();
    }

    @Override
    public void addEventListener(final Uri uri, final OnEventListener listener) {

        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        if (listener == null) {
            throw new NullPointerException("listener is null.");
        }

        put(uri, null, new OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
                if (response.getResult() == DConnectMessage.RESULT_OK) {
                    mListenerMap.put(convertUriToPath(uri), listener);
                }
                listener.onResponse(response);
            }
        });
    }

    @Override
    public void removeEventListener(final Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null.");
        }

        delete(uri, new OnResponseListener() {
            @Override
            public void onResponse(final DConnectResponseMessage response) {
            }
        });
        mListenerMap.remove(convertUriToPath(uri));
    }

    @Override
    protected DConnectResponseMessage sendRequest(final Method method, final Uri uri,
                                                  final Map<String, String> headers, final Entity body) {
        final int requestCode = UUID.randomUUID().hashCode();

        String[] paths = parsePath(uri);
        String api;
        String profile;
        String interfaces = null;
        String attribute = null;

        if (paths.length == 2) {
            api = paths[0];
            profile = paths[1];
        } else if (paths.length == 3) {
            api = paths[0];
            profile = paths[1];
            attribute = paths[2];
        } else if (paths.length == 4) {
            api = paths[0];
            profile = paths[1];
            interfaces = paths[2];
            attribute = paths[3];
        } else {
            throw new IllegalArgumentException("uri is invalid.");
        }

        Intent request = new Intent();
        request.setAction(convertMethod(method));
        request.setClassName(mManagerPackageName, mManagerClassName);
        request.putExtra(IntentDConnectMessage.EXTRA_API, api);
        request.putExtra(IntentDConnectMessage.EXTRA_PROFILE, profile);
        if (interfaces != null) {
            request.putExtra(IntentDConnectMessage.EXTRA_INTERFACE, interfaces);
        }
        if (attribute != null) {
            request.putExtra(IntentDConnectMessage.EXTRA_ATTRIBUTE, attribute);
        }
        if (uri.getQueryParameterNames() != null) {
            for (String key : uri.getQueryParameterNames()) {
                request.putExtra(key, uri.getQueryParameter(key));
            }
        }
        if (getOrigin() != null) {
            request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());
        }
        if (body != null && body instanceof MultipartEntity) {
            for (Map.Entry<String, Entity> data : (((MultipartEntity) body).getContent()).entrySet()) {
                String key = data.getKey();
                Entity val = data.getValue();
                if (val instanceof StringEntity) {
                    request.putExtra(key, ((StringEntity) val).getContent());
                } else if (val instanceof BinaryEntity) {
                    request.putExtra(key, ((BinaryEntity) val).getContent());
                } else if (val instanceof FileEntity) {
                    request.putExtra(key, ((FileEntity) val).getContent());
                }
            }
        }

        request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
        request.putExtra(IntentDConnectMessage.EXTRA_RECEIVER,
                new ComponentName(mContext, DConnectMessageReceiver.class));

        mContext.sendBroadcast(request);

        try {
            return new DConnectResponseMessage(waitForResponse(requestCode));
        } catch (JSONException e) {
            return createErrorMessage(DConnectMessage.ErrorCode.UNKNOWN.getCode(), e.getMessage());
        } catch (IOException e) {
            return createTimeoutResponse();
        }
    }

    /**
     * URIからパスを抽出する.
     * @param uri パスを抽出するURI
     * @return パス
     */
    private String convertUriToPath(final Uri uri) {
        return uri.getPath().toLowerCase();
    }

    /**
     * URLパスを「/」で分割した配列を作成します.
     * <p>
     * 分割できない場合には、0の配列を返却します。
     * </p>
     *
     * @param uri uri
     * @return パスの配列
     */
    private String[] parsePath(final Uri uri) {
        String path = uri.getPath();
        if (path == null || !path.contains("/")) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }

    /**
     * HttpメソッドをIntentのAction名に変換する.
     * @param method Httpメソッド
     * @return Action名
     */
    private String convertMethod(final Method method) {
        switch (method) {
            case GET:
                return IntentDConnectMessage.ACTION_GET;
            case PUT:
                return IntentDConnectMessage.ACTION_PUT;
            case POST:
                return IntentDConnectMessage.ACTION_POST;
            case DELETE:
                return IntentDConnectMessage.ACTION_DELETE;
            default:
                throw new IllegalArgumentException("Unknown method.");
        }
    }

    /**
     * レスポンスが返ってくるまで待ちます.
     * <p>
     * ただし、タイムアウトなどを起こした場合にはnullが返却される。
     * </p>
     *
     * @param requestCode リクエストコード
     * @return レスポンス用のIntent
     */
    private Intent waitForResponse(final int requestCode) throws IOException {
        long parseStart = System.currentTimeMillis();

        while (sResponseMap.get(requestCode) == null && (mSoTimeout == 0
                || mSoTimeout > System.currentTimeMillis() - parseStart)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        if (sResponseMap.get(requestCode) == null) {
            throw new IOException("response timeout");
        }

        return sResponseMap.remove(requestCode);
    }

    private void onReceivedEvent(final Intent intent) {
        try {
            DConnectSDK.OnEventListener l = mListenerMap.get(createPath(intent));
            if (l != null) {
                l.onMessage(new DConnectEventMessage(intent));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String createPath(final Intent intent) {
        String profile = intent.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String interfaces = intent.getStringExtra(DConnectMessage.EXTRA_INTERFACE);
        String attribute = intent.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
        String uri = "/gotapi";
        if (profile != null) {
            uri += "/";
            uri += profile;
        }
        if (interfaces != null) {
            uri += "/";
            uri += interfaces;
        }
        if (attribute != null) {
            uri += "/";
            uri += attribute;
        }
        return uri.toLowerCase();
    }

    /**
     * レスポンスを追加する.
     *
     * @param intent レスポンスインテント
     */
    static void onReceivedResponse(final Intent intent) {
        String action = intent.getAction();
        if (IntentDConnectMessage.ACTION_RESPONSE.equals(action)) {
            int requestCode = intent.getIntExtra(DConnectMessage.EXTRA_REQUEST_CODE, 0);
            if (requestCode != 0) {
                sResponseMap.put(requestCode, intent);
            }
        } else if (IntentDConnectMessage.ACTION_EVENT.equals(action)) {
            synchronized (sEventList) {
                for (IntentDConnectSDK sdk : sEventList) {
                    sdk.onReceivedEvent(intent);
                }
            }
        }
    }
}
