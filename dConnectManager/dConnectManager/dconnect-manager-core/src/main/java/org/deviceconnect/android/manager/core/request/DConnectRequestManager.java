/*
 DConnectRequestManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.request;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.manager.core.BuildConfig;
import org.deviceconnect.android.manager.core.DConnectInterface;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * dConnect Managerで処理されるリクエストを管理するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectRequestManager {
    /**
     * エラーコードを定義する.
     */
    private static final int ERROR_CODE = Integer.MIN_VALUE;

    /**
     * 最大スレッド数を定義する.
     */
    private static final int MAX_THREAD_SIZE = 32;

    /**
     * リクエスト一覧.
     */
    private final List<DConnectRequest> mRequestList = new ArrayList<>();

    /**
     * リクエストを実行するためのスレッドを管理するExecutor.
     */
    private ExecutorService mExecutor;

    /**
     * コンテキスト.
     */
    private Context mContext;
    private DConnectInterface mInterface;

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public DConnectRequestManager(final Context context) {
        mContext = context;
    }
    public void setDConnectInterface(final DConnectInterface i) {
        mInterface = i;
    }

    /**
     * リクエスト管理を開始します.
     */
    public synchronized void start() {
        if (mExecutor != null) {
            return;
        }
        mExecutor = Executors.newFixedThreadPool(MAX_THREAD_SIZE);
    }

    /**
     * リクエスト管理を終了する.
     */
    public synchronized void stop() {
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }

        synchronized (mRequestList) {
            mRequestList.clear();
        }
    }

    /**
     * 実行するリクエストを追加する.
     *
     * @param request 追加するリクエスト
     */
    public synchronized void addRequest(final DConnectRequest request) {
        if (mExecutor.isShutdown()) {
            return;
        }

        synchronized (mRequestList) {
            request.setRequestManager(this);
            request.setDConnectInterface(mInterface);
            mRequestList.add(request);
        }

        mExecutor.execute(() -> {
            try {
                request.run();
            } catch (Throwable e) {
                request.sendRuntimeException(e.getMessage());
                if (BuildConfig.DEBUG) {
                    Log.e("dConnectManager", "runtime", e);
                }
            } finally {
                synchronized (mRequestList) {
                    mRequestList.remove(request);
                }
            }
        });
    }

    /**
     * レスポンスを受け取り、リクエストコードが一致するリクエストに設定します.
     *
     * @param response レスポンス
     */
    public void setResponse(final Intent response) {
        int code = response.getIntExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, ERROR_CODE);
        synchronized (mRequestList) {
            for (DConnectRequest request : mRequestList) {
                if (request.hasRequestCode(code)) {
                    request.setResponse(response);
                    return;
                }
            }
        }
    }
}
