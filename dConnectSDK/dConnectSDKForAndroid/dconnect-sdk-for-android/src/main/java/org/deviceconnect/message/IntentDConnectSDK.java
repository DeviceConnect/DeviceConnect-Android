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

import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class IntentDConnectSDK extends DConnectSDK {
    /**
     * レスポンスを格納するマップ.
     */
    private static Map<Integer, Intent> sResponseMap = new ConcurrentHashMap<>();

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
     * コンストラクタ.
     * @param context コンテキスト
     */
    IntentDConnectSDK(final Context context) {
        mContext = context;
    }

    public void setManagerPackageName(String managerPackageName) {
        mManagerPackageName = managerPackageName;
    }

    public void setManagerClassName(String managerClassName) {
        mManagerClassName = managerClassName;
    }

    @Override
    public void connectWebSocket(OnWebSocketListener listener) {

    }

    @Override
    public void disconnectWebSocket() {

    }

    @Override
    public void addEventListener(String uri, OnEventListener listener) {

    }

    @Override
    public void removeEventListener(String uri) {

    }

    @Override
    protected DConnectResponseMessage sendRequest(final Method method, final String uri, final Map<String, String> headers, final byte[] body) {
        final int requestCode = UUID.randomUUID().hashCode();

        Uri u = Uri.parse(uri);
        String[] paths = parsePath(u);
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
        if (u.getQueryParameterNames() != null) {
            for (String key : u.getQueryParameterNames()) {
                request.putExtra(key, u.getQueryParameter(key));
            }
        }
        if (getOrigin() != null) {
            request.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());
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
            return createTimeout();
        }
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        if (sResponseMap.get(requestCode) == null) {
            throw new IOException("response timeout");
        }

        return sResponseMap.remove(requestCode);
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
        }
    }
}
