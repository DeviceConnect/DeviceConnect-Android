/*
 ConfirmAuthRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 承認確認画面表示リクエスト.<br>
 * - リクエストを保存しておき、承認／拒否のボタンがタップされた後の処理を行うまで、このクラスにパラメータを保存しておく。
 * @author NTT DOCOMO, INC.
 */
class ConfirmAuthRequest {

    /** スレッドID. */
    private long mThreadId;
    
    /** 承認確認画面表示パラメータ. */
    private ConfirmAuthParams mConfirmAuthParams;

    /** アクセストークン発行リスナー. */
    private PublishAccessTokenListener mPublishAccessTokenListener;
    
    /** リクエスト時間. */
    private long mRequestTime;
    
    /** 表示スコープ名配列. */
    private String[] mDisplayScopes;

    /** タイムアウト監視. */
    private Timer mTimeoutTimer;

    /** レスポンスの有無. */
    private boolean mDoneResponse;

    private boolean mIsAutoFlag = false;

    /**
     * コンストラクタ.
     * 
     * @param threadId スレッドID
     * @param confirmAuthParams パラメータ
     * @param publishAccessTokenListener アクセストークン発行リスナー
     * @param requestTime 承認確認画面表示要求した日時
     * @param displayScopes 表示用スコープ名配列
     */
    private ConfirmAuthRequest(final long threadId, final ConfirmAuthParams confirmAuthParams,
            final PublishAccessTokenListener publishAccessTokenListener, final long requestTime,
            final String[] displayScopes, boolean isAutoFlag) {
        mThreadId = threadId;
        mConfirmAuthParams = confirmAuthParams;
        mPublishAccessTokenListener = publishAccessTokenListener;
        mRequestTime = requestTime;
        mDisplayScopes = displayScopes;
        mIsAutoFlag = isAutoFlag;
    }

    /**
     * コンストラクタ.
     * 
     * @param threadId スレッドID
     * @param confirmAuthParams パラメータ
     * @param publishAccessTokenListener アクセストークン発行リスナー
     * @param displayScopes 表示用スコープ名配列
     */
    ConfirmAuthRequest(final long threadId, final ConfirmAuthParams confirmAuthParams,
            final PublishAccessTokenListener publishAccessTokenListener,
            final String[] displayScopes, boolean isAutoFlag) {
        this(threadId, confirmAuthParams, publishAccessTokenListener, System.currentTimeMillis(), displayScopes, isAutoFlag);
    }

    /**
     * スレッドID取得.
     * @return スレッドID
     */
    long getThreadId() {
        return mThreadId;
    }
    
    /**
     * 承認確認画面表示パラメータを取得.
     * 
     * @return 承認確認画面表示パラメータ
     */
    ConfirmAuthParams getConfirmAuthParams() {
        return mConfirmAuthParams;
    }

    /**
     * アクセストークン発行リスナー取得.
     * @return アクセストークン発行リスナー
     */
    PublishAccessTokenListener getPublishAccessTokenListener() {
        return mPublishAccessTokenListener;
    }

    public boolean isAutoFlag() {
        return mIsAutoFlag;
    }

    /**
     * リクエスト時間を取得.
     * 
     * @return リクエスト時間
     */
    long getRequestTime() {
        return mRequestTime;
    }
    
    /**
     * 表示用スコープ名配列を取得.
     * @return 表示用スコープ名配列
     */
    String[] getDisplayScopes() {
        return mDisplayScopes;
    }

    /**
     * レスポンスを受け取っているか確認を行う.
     * @return レスポンスを受け取っている場合はtrue、それ以外はfalse
     */
    synchronized boolean isDoneResponse() {
        return mDoneResponse;
    }

    /**
     * レスポンスの受領設定を行う.
     * @param doneResponse 受領した場合はtrue、それ以外はfalse
     */
    synchronized void setDoneResponse(final boolean doneResponse) {
        mDoneResponse = doneResponse;
    }

    /**
     * リクエストが実行するまでのタイムアウトを開始する.
     * @param callback タイムアウトを通知するコールバック
     */
    void startTimer(final OnTimeoutCallback callback) {
        if (mTimeoutTimer == null) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onTimeout();
                    }
                }
            };
            mTimeoutTimer = new Timer(true);
            mTimeoutTimer.schedule(timerTask, 10 * 1000);
        }
    }

    /**
     * タイムアウト用のタイマーを停止する.
     */
    void stopTimer() {
        if (mTimeoutTimer != null) {
            mTimeoutTimer.cancel();
            mTimeoutTimer = null;
        }
    }

    /**
     * タイムアウトを通知するコールバッグ.
     */
    interface OnTimeoutCallback {
        /**
         * タイムアウトが発生した時に通知を行うメソッド.
         */
        void onTimeout();
    }
}
