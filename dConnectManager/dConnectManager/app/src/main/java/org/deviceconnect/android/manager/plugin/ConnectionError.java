/*
 ConnectionError.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


/**
 * プラグインとの接続について発生するエラー.
 *
 * ここで定義されたエラーが発生した場合、{@link Connection}クラスの管理する接続状態は、
 * {@link ConnectionState#SUSPENDED} へ遷移する.
 *
 * @author NTT DOCOMO, INC.
 */
public enum ConnectionError {

    /**
     * 接続不可.
     *
     * 接続を試みたが、プラグインによって許可されなかった.
     */
    NOT_PERMITTED,

    /**
     * 応答なし.
     *
     * 接続を試みたが、制限時間内にプラグインによって応答が返されなかった.
     */
    NOT_RESPONDED,

    /**
     * 強制終了.
     *
     * 接続に成功したが、その後、プラグインまたはAndroid OSによって接続を強制終了された.
     */
    TERMINATED,

    /**
     * キャンセル.
     *
     * 接続処理が完了する前にインスタンスが破棄されたため、接続処理を中断した.
     */
    CANCELED,

    /**
     * 内部的エラーによる異常終了.
     *
     * 接続処理が完了する前に内部的なエラーが発生したため、接続処理を中断した.
     */
    INTERNAL_ERROR

}
