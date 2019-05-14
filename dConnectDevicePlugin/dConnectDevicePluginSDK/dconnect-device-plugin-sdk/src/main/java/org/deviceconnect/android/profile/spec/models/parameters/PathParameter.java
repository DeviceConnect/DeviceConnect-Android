/*
 PathParameter.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models.parameters;

import org.deviceconnect.android.profile.spec.models.In;

/**
 * API 操作で使用されるパラメータ情報.
 *
 * @author NTT DOCOMO, INC.
 */
public class PathParameter extends AbstractParameter {
    /**
     * コンストラクタ.
     */
    public PathParameter() {
        setIn(In.PATH);
    }
}
