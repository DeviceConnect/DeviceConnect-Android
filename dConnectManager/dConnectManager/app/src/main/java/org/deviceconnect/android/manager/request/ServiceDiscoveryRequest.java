/*
 NetworkServiceDiscoveryRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.request;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.DConnectMessageService;
import org.deviceconnect.android.manager.plugin.DevicePlugin;
import org.deviceconnect.android.manager.plugin.MessagingException;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Service Discovery用のリクエストクラス.
 * <p>
 * 他のリクエストと異なる点として、複数のレスポンスを受け取る事が挙げられる.
 * 結果として、レスポンスタイムアウトの判断基準が普通のリクエストではレスポンスを1つ受け取ったかどうか
 * になり、他方Network Service Discovery用リクエストでは登録されているデバイスプラグイン
 * の数だけレスポンスを受け取ったかどうかになっている.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ServiceDiscoveryRequest extends DConnectRequest {

    /**
     * タイムアウト時間を定義. (8秒)
     */
    public static final int TIMEOUT = 8000;

    /** プラグイン側のService Discoveryのプロファイル名: {@value}. */
    private static final String PROFILE_NETWORK_SERVICE_DISCOVERY = "networkServiceDiscovery";

    /** プラグイン側のService Discoveryのプロファイル名: {@value}. */
    private static final String ATTRIBUTE_GET_NETWORK_SERVICES = "getNetworkServices";

    /** リクエストコードを格納する配列. */
    private SparseArray<DevicePlugin> mRequestCodeArray = new SparseArray<>();

    /** 発見したサービスを一時的に格納しておくリスト. */
    private final List<Bundle> mServices = new ArrayList<>();

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private CountDownLatch mCountDownLatch;

    @Override
    public synchronized boolean hasRequestCode(final int requestCode) {
        return false;
    }

    @Override
    public void run() {
        if (mRequest == null) {
            throw new RuntimeException("mRequest is null.");
        }

        if (mPluginMgr == null) {
            throw new RuntimeException("mDevicePluginManager is null.");
        }

        final List<DevicePlugin> plugins = mPluginMgr.getEnabledDevicePlugins();
        mCountDownLatch = new CountDownLatch(plugins.size());

        for (int i = 0; i < plugins.size(); i++) {
            DiscoveryRequestForPlugin request = new DiscoveryRequestForPlugin();
            request.setContext(mContext);
            request.setRequest(mRequest);
            request.setDevicePluginManager(mPluginMgr);
            request.setDestination(plugins.get(i));
            request.setTimeout(TIMEOUT);
            ((DConnectMessageService) mContext).addRequest(request);
        }

        if (mCountDownLatch.getCount() > 0) {
            try {
                mCountDownLatch.await(mTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                mLogger.warning("Exception occurred in wait.");
            }
        }

        // レスポンスの無かったプラグインのログを出力
        if (BuildConfig.DEBUG) {
            outputNotRespondedPlugins(mRequestCodeArray);
        }

        // パラメータを設定する
        mResponse = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        mResponse.putExtra(IntentDConnectMessage.EXTRA_RESULT,
                IntentDConnectMessage.RESULT_OK);
        mResponse.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES,
                mServices.toArray(new Bundle[mServices.size()]));

        // レスポンスを返却する
        sendResponse(mResponse);
    }

    private void outputNotRespondedPlugins(final SparseArray<DevicePlugin> notRespondedPlugins) {
        if (notRespondedPlugins.size() > 0) {
            String notRespondedLog = "Not responded plug-in(s) for service discovery: \n";
            for (int index = 0; index < notRespondedPlugins.size(); index++) {
                DevicePlugin plugin = notRespondedPlugins.valueAt(index);
                if (plugin != null) {
                    notRespondedLog += " - " + plugin.getDeviceName() + "\n";
                }
            }
            mLogger.warning(notRespondedLog);
        } else {
            if (BuildConfig.DEBUG) {
                mLogger.info("All plug-in(s) responded for service discovery.");
            }
        }
    }

    private class DiscoveryRequestForPlugin extends DConnectPluginRequest {

        @Override
        public void run() {
            // リクエストコード発行
            mRequestCode = UUID.randomUUID().hashCode();
            mRequestCodeArray.put(mRequestCode, mDevicePlugin);

            // 送信用のIntentを作成
            final Intent request = createRequestMessage(mRequest, null);

            // GotAPI-5 I/F仕様のパスに変換
            request.putExtra(DConnectMessage.EXTRA_PROFILE, PROFILE_NETWORK_SERVICE_DISCOVERY);
            request.putExtra(DConnectMessage.EXTRA_INTERFACE, (String) null);
            request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ATTRIBUTE_GET_NETWORK_SERVICES);
            request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, mRequestCode);
            request.setComponent(mDevicePlugin.getComponentName());
            sendRequest(request);
        }

        @Override
        protected void onResponseReceived(final Intent request, final Intent response) {
            // エラーが返ってきた場合には、サービスには登録しない。
            int result = response.getIntExtra(IntentDConnectMessage.EXTRA_RESULT, -1);
            if (result == IntentDConnectMessage.RESULT_OK) {
                // 送られてきたサービスIDにデバイスプラグインのIDを付加して保存
                Parcelable[] services = response.getParcelableArrayExtra(
                        ServiceDiscoveryProfileConstants.PARAM_SERVICES);
                if (services != null) {
                    for (Parcelable p : services) {
                        Bundle b = (Bundle) p;
                        String id = b.getString(ServiceDiscoveryProfile.PARAM_ID);
                        b.putString(ServiceDiscoveryProfile.PARAM_ID, mPluginMgr.appendServiceId(mDevicePlugin, id));
                        mServices.add(b);
                    }
                }
            }

            // レスポンス個数を追加
            mCountDownLatch.countDown();
            mRequestCodeArray.remove(mRequestCode);
        }

        @Override
        protected void onMessagingError(final MessagingException e) {
            mCountDownLatch.countDown();
            mRequestCodeArray.remove(mRequestCode);
        }
    }
}
