/*
 DConnectServiceProvider.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.service;

import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.spec.DConnectServiceSpec;

import java.util.List;

/**
 * Device Connect APIサービス管理インターフェース.
 * @author NTT DOCOMO, INC.
 */
public interface DConnectServiceProvider {

    /**
     * 指定されたサービスが登録されているかどうかを取得する.
     * @param serviceId サービスID
     * @return 管理されている場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    boolean hasService(String serviceId);

    /**
     * 登録されているサービスを取得する.
     * <p>
     * サービスIDがnullとなるDConnectServiceは存在し得ないため、<code>serviceId</code>に
     * <code>null</code>が指定された場合は常に<code>null</code>を返す.
     * </p>
     * @param serviceId サービスID
     * @return DConnectServiceのインスタンス. 登録されていない場合は<code>null</code>
     */
    DConnectService getService(String serviceId);

    /**
     * 登録されているサービスのリストを取得する.
     * @return 登録されているサービスのリスト
     */
    List<DConnectService> getServiceList();

    /**
     * サービスを追加する.
     *
     * <p>
     * 同一のサービス ID が追加された場合は上書きする.
     * </p>
     *
     * <p>
     * サービスを追加する時に、{@link DevicePluginContext}や{@link DConnectServiceSpec}などの
     * 設定を DConnectService に対して行います。<br>
     * 既に設定されている場合には、既存の設定を優先します。
     * </p>
     *
     * @param service 追加するDConnectServiceのインスタンス
     */
    void addService(DConnectService service);

    /**
     * サービスを削除する.
     * @param service 削除するDConnectServiceのインスタンス
     * @return 削除対象が存在した場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    boolean removeService(DConnectService service);

    /**
     * サービスを削除する.
     * <p>
     * サービスIDがnullとなるDConnectServiceは存在し得ないため、<code>serviceId</code>に
     * <code>null</code>が指定された場合は常に<code>null</code>を返す.
     * </p>
     * @param serviceId サービスID.
     * @return 削除されたDConnectServiceのインスタンス. 削除対象が存在しなかった場合は<code>null</code>
     */
    DConnectService removeService(String serviceId);

    /**
     * すべてのサービスを削除する.
     */
    void removeAllServices();

    /**
     * サービスの追加または削除イベントを受信するためのリスナーを追加する.
     * @param listener リスナー
     */
    void addServiceListener(DConnectServiceListener listener);

    /**
     * サービスの追加または削除イベントを受信するためのリスナーを削除する.
     * @param listener リスナー
     */
    void removeServiceListener(DConnectServiceListener listener);
}
