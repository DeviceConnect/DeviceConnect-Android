/*
WearUtil.java
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import org.deviceconnect.profile.CanvasProfileConstants.Mode;


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
     * Android Wearに渡す描画モードに変換する.
     * @param mode モード
     * @return 変換後のモード
     */
    public static int convertMode(final Mode mode) {
        int mm = WearConst.MODE_NORMAL;
        if (Mode.SCALES.equals(mode)) {
            mm = WearConst.MODE_SCALES;
        } else if (Mode.FILLS.equals(mode)) {
            mm = WearConst.MODE_FILLS;
        } else {
            mm = WearConst.MODE_NORMAL;
        }
        return mm;
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
