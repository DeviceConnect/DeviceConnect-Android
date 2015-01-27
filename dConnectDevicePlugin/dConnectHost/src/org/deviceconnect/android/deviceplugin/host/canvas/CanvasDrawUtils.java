/*
 CanvasDrawUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.canvas;

import android.content.Intent;

/**
 * Canvas Draw Utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasDrawUtils {

    /**
     * get canvas draw object from intent.
     * @param intent intent
     * @return canvas draw object. if null, there is no draw objects that datakind matches.
     */
    public static CanvasDrawObjectInterface getCanvasDrawObjectFromIntent(Intent intent) {
        
        String dataKind = intent.getStringExtra(CanvasDrawObjectInterface.EXTRA_DATAKIND);
        
        /* CanvasDrawImageObject? */
        if (dataKind.equals(CanvasDrawImageObject.DATAKIND)) {
            CanvasDrawImageObject parameter = new CanvasDrawImageObject();
            parameter.getValueFromIntent(intent);
            return parameter;
        }
        
        return null;
    }
}
