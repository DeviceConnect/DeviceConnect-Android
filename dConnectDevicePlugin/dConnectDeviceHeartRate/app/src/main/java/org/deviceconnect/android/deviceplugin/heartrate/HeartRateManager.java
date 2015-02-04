/*
 HeartRateManager
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate;

import android.content.Context;

/**
 * @author NTT DOCOMO, INC.
 */
public class HeartRateManager {
    /**
     * application context.
     */
    private Context mContext;

    /**
     * Constructor.
     * @param context application context
     */
    public HeartRateManager(Context context) {
        mContext = context;
    }
}
