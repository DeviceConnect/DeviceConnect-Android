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
