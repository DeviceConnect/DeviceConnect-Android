/*
 PostApi.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.api;

/**
 * Device Connect APIクラス (POSTメソッド).
 * @author NTT DOCOMO, INC.
 */
public abstract class PostApi extends DConnectApi {

    @Override
    public Method getMethod() {
        return Method.POST;
    }

}
