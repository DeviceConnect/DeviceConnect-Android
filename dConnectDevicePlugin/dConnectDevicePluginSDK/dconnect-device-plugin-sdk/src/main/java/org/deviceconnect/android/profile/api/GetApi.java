package org.deviceconnect.android.profile.api;

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
