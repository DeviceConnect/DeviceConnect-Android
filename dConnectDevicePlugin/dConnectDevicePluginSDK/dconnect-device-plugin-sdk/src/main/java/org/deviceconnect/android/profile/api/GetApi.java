/*
 GetApi.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.api;

import org.deviceconnect.android.profile.spec.models.Method;

/**
 * Device Connect APIクラス (GETメソッド).
 * @author NTT DOCOMO, INC.
 */
public abstract class GetApi extends DConnectApi {

    @Override
    public Method getMethod() {
        return Method.GET;
    }

}
