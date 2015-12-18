/*
WearCanvasProfile.java
Copyright (c) 2015 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.wear.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnDataItemResultListener;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnMessageResultListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * Android Wear用のCanvasプロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class WearCanvasProfile extends CanvasProfile {

    /**
     * Android wearは100KB以上の画像は送信できない.
     */
    private static final int LIMIT_DATA_SIZE = 1024 * 1024;

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response, 
            final String serviceId, final String mimeType, final byte[] data,
            final double x, final double y, final String mode) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "data is not empty");
            return true;
        }
        if (data.length > LIMIT_DATA_SIZE) {
            MessageUtils.setInvalidRequestParameterError(response, "data size more than 1MB");
            return true;
        }
        Mode m = Mode.getInstance(mode);
        if ((mode != null && mode.length() > 0) && m == null) {
            MessageUtils.setInvalidRequestParameterError(response, "mode is invalid");
            return true;
        }

        Bitmap bitmap = getBitmap(data);
        if (bitmap == null) {
            MessageUtils.setInvalidRequestParameterError(response, "format invalid");
            return true;
        }

        int mm = WearUtils.convertMode(m);
        getManager().sendImageData(bitmap, (int) x, (int) y, mm, new OnDataItemResultListener() {
            @Override
            public void onResult(final DataItemResult result) {
                if (result.getStatus().isSuccess()) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response);
                }
                sendResponse(response);
            }
            @Override
            public void onError() {
                MessageUtils.setIllegalDeviceStateError(response);
                sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeleteDrawImage(final Intent request, final Intent response,
            final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        } else if (!WearUtils.checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        } else {
            String nodeId = WearUtils.getNodeId(serviceId);
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_CANCAS_DELETE_IMAGE,
                    "", new OnMessageResultListener() {
                @Override
                public void onResult(final SendMessageResult result) {
                    if (result.getStatus().isSuccess()) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response);
                    }
                    sendResponse(response);
                }
                @Override
                public void onError() {
                    MessageUtils.setIllegalDeviceStateError(response);
                    sendResponse(response);
                }
            });
            return false;
        }
    }

    /**
     * データを画像に変換します.
     * @param data 画像データ
     * @return Bitmap
     */
    private Bitmap getBitmap(final byte[] data) {
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
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
