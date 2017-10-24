/*
 DConnectPluginRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;


import android.content.Intent;

import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.MessagingException;

/**
 * プラグインへ送信するリクエスト.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectPluginRequest extends DConnectRequest {

    /**
     * リクエスト先のプラグイン.
     */
    DevicePlugin mDevicePlugin;

    /** ロックオブジェクト. */
    private final Object mLockObj = new Object();

    /** リクエストコード. */
    protected int mRequestCode;

    /** リクエスト開始時刻. */
    private long mStartDateTime;

    /** リクエスト完了時刻. */
    private long mEndDateTime;

    /** 通信履歴を保存するかどうかのフラグ. */
    private boolean mIsReportedRoundTripFrag = true;

    @Override
    public boolean hasRequestCode(final int requestCode) {
        return mRequestCode == requestCode;
    }

    /**
     * 通信履歴を保存するかどうかのフラグを設定する.
     * @param isReported 通信履歴を保存する場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public void setReportedRoundTrip(final boolean isReported) {
        mIsReportedRoundTripFrag = isReported;
    }

    /**
     * リクエスト先のデバイスプラグインを設定する.
     * @param plugin デバイスプラグイン
     */
    public void setDestination(final DevicePlugin plugin) {
        mDevicePlugin = plugin;
    }

    /**
     * プラグインにリクエストを送信します.
     * <p>
     * 送信に失敗した場合には、この中で、レスポンスを返却します。
     * </p>
     * @param request 送信するリクエスト
     * @return 送信に成功した場合は<code>true</code>
     */
    private boolean forwardRequest(final Intent request) {
        if (mDevicePlugin == null) {
            throw new IllegalStateException("destination is not set.");
        }
        try {
            mDevicePlugin.send(request);
            return true;
        } catch (MessagingException e) {
            onMessagingError(e);
            return false;
        }
    }

    /**
     * 指定したリクエストを送信した後、プラグインからのレスポンスを待機する.
     *
     * @param request プラグインへのリクエスト
     * @return レスポンスの受信に成功した場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    boolean sendRequest(final Intent request) {
        boolean forwarded = false;
        boolean responded = false;
        try {
            mStartDateTime = getCurrentDateTime();
            forwarded = forwardRequest(request);
            if (!forwarded) {
                return false;
            }
            if (mResponse == null) {
                waitForResponse();
            }

            final Intent response = mResponse;
            responded = response != null;
            if (responded) {
                onResponseReceived(request, response);
                return true;
            } else {
                onResponseTimeout();
                return false;
            }
        } finally {
            mEndDateTime = getCurrentDateTime();
            if (forwarded) {
                reportHistory(request, responded);
            }
        }
    }

    private void reportHistory(final Intent request, final boolean responded) {
        long start = mStartDateTime;
        long end = mEndDateTime;
        if (responded) {
            if (mIsReportedRoundTripFrag) {
                mDevicePlugin.reportRoundTrip(request, start, end);
            }
        } else {
            mDevicePlugin.reportResponseTimeout(request, start);
        }
    }

    /**
     * 各デバイスからのレスポンスを待つ.
     *
     * この関数から返答があるのは以下の条件になる。
     * <ul>
     * <li>デバイスプラグインからレスポンスがあった場合
     * <li>指定された時間無いにレスポンスが返ってこない場合
     * </ul>
     */
    private void waitForResponse() {
        synchronized (mLockObj) {
            try {
                mLockObj.wait(mTimeout);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    @Override
    public void setResponse(final Intent response) {
        super.setResponse(response);
        synchronized (mLockObj) {
            mLockObj.notifyAll();
        }
    }

    private long getCurrentDateTime() {
        return System.currentTimeMillis();
    }

    protected void onMessagingError(final MessagingException e) {
        switch (e.getReason()) {
            case NOT_ENABLED:
                sendPluginDisabledError();
                break;
            case CONNECTION_SUSPENDED:
                sendPluginSuspendedError();
                break;
            default: // NOT_CONNECTED
                sendIllegalServerStateError("Failed to send a message to the plugin: " + mDevicePlugin.getPackageName());
                break;
        }
    }

    protected void onResponseTimeout() {
        sendTimeoutError();
    }

    protected void onResponseReceived(final Intent request, final Intent response) {
        // NOTE: 必要な場合のみ、子クラスで拡張.
    }
}
