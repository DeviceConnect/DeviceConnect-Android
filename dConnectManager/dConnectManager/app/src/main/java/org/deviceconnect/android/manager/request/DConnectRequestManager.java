/*
 DConnectRequestManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * dConnect Managerで処理されるリクエストを管理するクラス.
 * @author NTT DOCOMO, INC.
 */
public class DConnectRequestManager {
    /** エラーコードを定義する. */
    private static final int ERROR_CODE = Integer.MIN_VALUE;

    /** 最大スレッド数を定義する. */
    private static final int MAX_THREAD_SIZE = 4;

    /** リクエストを実行するためのスレッドを管理するExecutor. */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_THREAD_SIZE);

    /** シングルスレッドでリクエストを実行するためのスレッドを管理するExecutor. */
    private final ExecutorService mSingleExecutor = Executors.newSingleThreadExecutor();

    /** リクエスト一覧. */
    private final List<DConnectRequest> mRequestList = Collections.synchronizedList(new ArrayList<DConnectRequest>());

    /**
     * リクエスト管理を終了する.
     */
    public synchronized void shutdown() {
        mExecutor.shutdown();
        mSingleExecutor.shutdown();
    }

    /**
     * 実行するリクエストを追加する.
     * @param request 追加するリクエスト
     */
    public synchronized void addRequest(final DConnectRequest request) {
        if (mExecutor.isShutdown()) {
            return;
        }
        request.setRequestMgr(this);
        mRequestList.add(request);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    request.run();
                } catch (Throwable e) {
                    request.sendRuntimeException(e.getMessage());
                    if (BuildConfig.DEBUG) {
                        Log.e("dConnectManager", "runtime", e);
                    }
                } finally {
                    mRequestList.remove(request);
                }
            }
        });
    }

    /**
     * シングルスレッドで実行するリクエストを追加する.
     * @param request 追加するリクエスト
     */
    public synchronized void addRequestOnSingleThread(final DConnectRequest request) {
        if (mSingleExecutor.isShutdown()) {
            return;
        }
        request.setRequestMgr(this);
        mRequestList.add(request);
        mSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    request.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                    request.sendRuntimeException(e.getMessage());
                    if (BuildConfig.DEBUG) {
                        Log.e("dConnectManager", "runtime", e);
                    }
                } finally {
                    mRequestList.remove(request);
                }
            }
        });
    }

    /**
     * レスポンスを受け付ける.
     * @param response レスポンス
     */
    public void setResponse(final Intent response) {
        int code = response.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, ERROR_CODE);
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
