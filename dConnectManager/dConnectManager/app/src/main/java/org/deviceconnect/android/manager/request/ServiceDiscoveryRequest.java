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

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public synchronized void setResponse(final Intent response) {
        // リクエストコードを取得
        int requestCode = response.getIntExtra(
                IntentDConnectMessage.EXTRA_REQUEST_CODE, -1);
        if (requestCode == -1) {
            mLogger.warning("Illegal requestCode. requestCode=" + requestCode);
            return;
        }

        // エラーが返ってきた場合には、サービスには登録しない。
        int result = response.getIntExtra(IntentDConnectMessage.EXTRA_RESULT, -1);
        if (result == IntentDConnectMessage.RESULT_OK) {
            // 送られてきたサービスIDにデバイスプラグインのIDを付加して保存
            Parcelable[] services = response.getParcelableArrayExtra(
                    ServiceDiscoveryProfileConstants.PARAM_SERVICES);
            if (services != null) {
                DevicePlugin plugin = mRequestCodeArray.get(requestCode);
                for (Parcelable p : services) {
                    Bundle b = (Bundle) p;
                    String id = b.getString(ServiceDiscoveryProfile.PARAM_ID);
                    b.putString(ServiceDiscoveryProfile.PARAM_ID, mPluginMgr.appendServiceId(plugin, id));
                    mServices.add(b);
                }
                mRequestCodeArray.remove(requestCode);
            }
        }

        // レスポンス個数を追加
        mCountDownLatch.countDown();
    }

    @Override
    public synchronized boolean hasRequestCode(final int requestCode) {
        return mRequestCodeArray.get(requestCode) != null;
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
            final int index = i;
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // 送信用のIntentを作成
                    final Intent request = createRequestMessage(mRequest, null);

                    // プラグイン側のI/Fに変換
                    request.putExtra(DConnectMessage.EXTRA_PROFILE, PROFILE_NETWORK_SERVICE_DISCOVERY);
                    request.putExtra(DConnectMessage.EXTRA_INTERFACE, (String) null);
                    request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ATTRIBUTE_GET_NETWORK_SERVICES);

                    DevicePlugin plugin = plugins.get(index);

                    // リクエストコード発行
                    int requestCode = UUID.randomUUID().hashCode();
                    mRequestCodeArray.put(requestCode, plugin);

                    request.setComponent(plugin.getComponentName());
                    request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, requestCode);
                    try {
                        plugin.send(request);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        mCountDownLatch.countDown();
                    }
                }
            });
        }

        if (mCountDownLatch.getCount() > 0) {
            try {
                mCountDownLatch.await(mTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                mLogger.warning("Exception occurred in wait.");
            }
        }

        // TODO: 応答のなかったプラグインだけを再起動する
//        if (mResponseCount < plugins.size()) {
//            restartDevicePlugins();
//        }

        // パラメータを設定する
        mResponse = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
        mResponse.putExtra(IntentDConnectMessage.EXTRA_RESULT,
                IntentDConnectMessage.RESULT_OK);
        mResponse.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES,
                mServices.toArray(new Bundle[mServices.size()]));

        // レスポンスを返却する
        sendResponse(mResponse);
    }
    
    /**
     * Restart all device plugins that response did not come back.
     */
    private void restartDevicePlugins() {
        for (int i = 0; i < mRequestCodeArray.size(); i++) {
            DevicePlugin plugin = mRequestCodeArray.valueAt(i);
            if (plugin.getStartServiceClassName() != null) {
                Intent service = new Intent();
                service.setClassName(plugin.getPackageName(), 
                        plugin.getStartServiceClassName());
                getContext().startService(service);
            }
        }
    }
}
