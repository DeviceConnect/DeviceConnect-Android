/*
 DConnectPluginRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.request;


import android.content.Intent;

import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.MessagingException;

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

    /**
     * ロックオブジェクト.
     */
    private final Object mLockObj = new Object();

    /**
     * リクエストコード.
     */
    protected int mRequestCode;

    /**
     * 通信履歴を保存するかどうかのフラグ.
     */
    private boolean mIsReportedRoundTripFrag = true;

    @Override
    public boolean hasRequestCode(final int requestCode) {
        return mRequestCode == requestCode;
    }

    /**
     * 通信履歴を保存するかどうかのフラグを設定する.
     *
     * @param isReported 通信履歴を保存する場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public void setReportedRoundTrip(final boolean isReported) {
        mIsReportedRoundTripFrag = isReported;
    }

    /**
     * リクエスト先のデバイスプラグインを設定する.
     *
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
     *
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

        long startDateTime = System.currentTimeMillis();
        try {
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
            long endDateTime = System.currentTimeMillis();
            if (forwarded) {
                reportHistory(request, responded, startDateTime, endDateTime);
            }
        }
    }

    /**
     * プラグインに対する履歴を報告します.
     *
     * @param request リクエスト
     * @param responded レスポンスの有無
     * @param start リクエスト送信時間
     * @param end レスポンス受信
     */
    private void reportHistory(final Intent request, final boolean responded, final long start, final long end) {
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
     * <p>
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

    /**
     * エラーを受け取ります.
     * @param e エラーの原因
     */
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

    /**
     * プラグインへのリクエストがタイムアウトになったことを受け取ります.
     */
    protected void onResponseTimeout() {
        sendTimeoutError();
    }

    /**
     * プラグインからのレスポンスを受け取ったことを通知します.
     * <p>
     * NOTE: 必要な場合のみ、子クラスで拡張すること。
     * </p>
     * @param request リクエスト
     * @param response レスポンス
     */
    protected void onResponseReceived(final Intent request, final Intent response) {
    }
}
