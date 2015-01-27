/*
 CanvasDrawObjectInterface.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.canvas;

import android.content.Intent;
import android.graphics.Bitmap;

/**
 * Canvas Draw Object Common Interface.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface CanvasDrawObjectInterface {
    
    /**
     * Intent Extra key. identifying value of data stored in the intent .
     */
    final String EXTRA_DATAKIND = "datakind";
    
    /**
     * get data from intent.
     * @param intent Intent
     */
    void getValueFromIntent(Intent intent);
    
    /**
     * set data to intent.
     * @param intent Intent
     */
    void setValueToIntent(Intent intent);
    
    /**
     * draw shape to bitmap.
     * @param viewBitmap
     */
    void draw(Bitmap viewBitmap);
}
