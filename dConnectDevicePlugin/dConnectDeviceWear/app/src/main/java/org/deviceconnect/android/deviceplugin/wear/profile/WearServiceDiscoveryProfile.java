/*
 WearServiceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnNodeResultListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * NetworkServiceDiscoveryプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider プロファイルプロバイダ
     */
    public WearServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        getManager().getNodes(new OnNodeResultListener() {
            @Override
            public void onResult(final GetConnectedNodesResult result) {
                List<Bundle> services = new ArrayList<Bundle>();
                for (Node node : result.getNodes()) {
                    services.add(convertNodeToService(node.getId()));
                }
                setResult(response, DConnectMessage.RESULT_OK);
                setServices(response, services);
                sendResponse(response);
            }
            @Override
            public void onError() {
                MessageUtils.setUnknownError(response);
                sendResponse(response);
            }
        });
        return false;
    }

    /**
     * nodeIdをサービス (Bundle)に変換する.
     * @param nodeId Wear ノードID
     * @return サービス(Bundle)
     */
    private Bundle convertNodeToService(final String nodeId) {
        String[] serviceId = nodeId.split("-");
        Bundle service = new Bundle();
        setId(service, WearUtils.createServiceId(nodeId));
        setName(service, WearConst.DEVICE_NAME + "(" + serviceId[0] + ")");
        setType(service, NetworkType.BLE);
        setOnline(service, true);
        setConfig(service, "");
        setScopes(service, getProfileProvider());
        return service;
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
