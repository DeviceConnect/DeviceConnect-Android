package org.deviceconnect.android.profile.api;

/**
 * Device Connect APIクラス (PUTメソッド).
 * @author NTT DOCOMO, INC.
 */
public abstract class PutApi extends DConnectApi {

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

}
