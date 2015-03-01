/*
WearUtil.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

/**
 * Wear Utils.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class WearUtils {
    /**
     * Constructor.
     */
    private WearUtils() {
    }

    /**
     * サービスIDを確認する.
     * @param serviceId サービスID
     * @return 問題ない場合にはtrue、それ以外はfalse
     */
    public static boolean checkServiceId(final String serviceId) {
        if (serviceId == null) {
            return false;
        }
        return serviceId.startsWith(WearConst.SERVICE_ID);
    }

    /**
     * nodeIdからサービスIDを作成する.
     * @param nodeId ノードID
     * @return サービスID
     */
    public static String createServiceId(final String nodeId) {
        String[] id = nodeId.split("-");
        return WearConst.SERVICE_ID + "-" + id[0];
    }

    /**
     * Get node form Service ID.
     * 
     * @param serviceId Service ID.
     * @return nodeId Internal management Node ID.
     */
    public static String getNodeId(final String serviceId) {
        String[] mServiceIdArray = serviceId.split("-");
        return mServiceIdArray[1];
    }
}
