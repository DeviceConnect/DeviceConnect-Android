/*
WearCanvasProfile.java
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Android Wear用のCanvasプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearCanvasProfile extends CanvasProfile {

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response, 
            final String serviceId, final String mimeType, final byte[] data,
            final double x, final double y, final String mode) {
//        return super.onPostDrawImage(request, response, serviceId, mimeType, data, x, y, mode);
        
        
//        if (uri == null) {
//            MessageUtils.setInvalidRequestParameterError(response, "data is not specied to update a file.");
//            return true;
//        }

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

//        CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
//        if (enumMode == null) {
//            MessageUtils.setInvalidRequestParameterError(response);
//            return true;
//        }

//        if (!CanvasDrawUtils.checkBitmap(getContext(), uri)) {
//            MessageUtils.setInvalidRequestParameterError(response, "Data format is invalid.");
//            return true;
//        }
//
//        CanvasDrawImageObject drawObj = new CanvasDrawImageObject(uri, enumMode, x, y);

//        Intent intent = new Intent();
//        intent.setClass(getContext(), CanvasProfileActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        drawObj.setValueToIntent(intent);
//        getContext().startActivity(intent);

        new Thread(new Runnable() {
            public void run() {
                Log.e("ABC", "AAAAAAAAAAAAAAA");
                Bitmap bitmap = getBitmap(data);
                if (bitmap != null) {
                    Log.e("ABC", "AAAAAAAAAAAAAAA22");
                    if (getManager().sendImage(bitmap)) {
                        Log.e("ABC", "AAAAAAAAAAAAAAA33");
                        setResult(response, DConnectMessage.RESULT_OK);
                    }
                }
                
                getContext().sendBroadcast(response);
            }
        }).start();
        return false;
    }
    
    private Bitmap getBitmap(byte[] buf) {
        if (buf == null) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            return BitmapFactory.decodeByteArray(buf, 0, buf.length, options);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * Android Wear管理クラスを取得する.
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
