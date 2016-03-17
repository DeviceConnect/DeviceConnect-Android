/*
 LinkingManagerFactory.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;

public class LinkingManagerFactory {

    public static LinkingManager createManager(Context context) {
        return new LinkingManagerImpl(context);
    }

}
