/*
 ConnectionFactory.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;


/**
 * {@link Connection}オブジェクトを生成するためのファクトリー.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ConnectionFactory {

    /**
     * 指定されたプラグインに対応する{@link Connection}オブジェクトを生成する.
     *
     * @param plugin 接続先のプラグイン
     * @return {@link Connection}オブジェクト
     */
    Connection createConnectionForPlugin(final DevicePlugin plugin);

}
