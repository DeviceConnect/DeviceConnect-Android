/*
 DConnectSDKFactory.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Context;

public final class DConnectSDKFactory {

    public static final int TYPE_HTTP = 1;
    public static final int TYPE_INTENT= 2;

    private DConnectSDKFactory() {
    }

    public static DConnectSDK create(final Context context, final int type) {
        DConnectSDK sdk;
        switch (type) {
            case TYPE_HTTP:
                sdk = new HttpDConnectSDK();
                break;
            case TYPE_INTENT:
                sdk =  new IntentDConnectSDK(context);
                break;
            default:
                throw new IllegalArgumentException("type is invalid.");
        }
        sdk.setOrigin(context.getPackageName());
        return sdk;
    }
}
