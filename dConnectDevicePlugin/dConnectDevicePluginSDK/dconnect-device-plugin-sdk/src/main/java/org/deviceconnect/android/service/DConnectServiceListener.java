package org.deviceconnect.android.service;

/**
 * サービスの状態通知を行うリスナー.
 *
 * @author NTT DOCOMO, INC.
 */
public interface DConnectServiceListener {

    /**
     * 新しいサービスが追加された時に呼び出されます.
     * @param service 追加されたサービス
     */
    void onServiceAdded(DConnectService service);

    /**
     * サービスが削除された時に呼び出されます.
     * @param service 削除されたサービス
     */
    void onServiceRemoved(DConnectService service);

    /**
     * サービスの状態に変更があった時に呼び出されます.
     * @param service 状態が変わったサービス
     */
    void onStatusChange(DConnectService service);
}
