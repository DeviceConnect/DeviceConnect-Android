package org.deviceconnect.android.profile.api;


/**
 * Device Connect APIクラス (DELETEメソッド).
 * @author NTT DOCOMO, INC.
 */
public abstract class DeleteApi extends DConnectApi {

    @Override
    public Method getMethod() {
        return Method.DELETE;
    }
    
}
