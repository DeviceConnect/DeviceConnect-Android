/*
 DConnectDataSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


/**
 * Device Connect API上で入力または出力されるデータの仕様定義.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectDataSpec implements DConnectSpecConstants {

    final DataType mDataType;

    /**
     * コンストラクタ.
     *
     * @param type データの種類
     */
    protected DConnectDataSpec(final DataType type) {
        mDataType = type;
    }

    /**
     * データの種類を取得する.
     *
     * @return データの種類
     */
    public DataType getDataType() {
        return mDataType;
    }

    /**
     * 入力されたパラメータ値が仕様に反していないことを確認する.
     *
     * @param param 入力されたパラメータ値
     * @return 仕様に反していない場合は<code>true</code>. そうでない場合は<code>false</code>
     */
    public abstract boolean validate(final Object param);

}
