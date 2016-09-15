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
import com.google.android.gms.wearable.MessageApi;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.deviceplugin.wear.WearManager.OnDataItemResultListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Android Wear用のCanvasプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class WearCanvasProfile extends CanvasProfile {

    /**
     * Android wearは1MB以上の画像は送信できない.
     */
    private static final int LIMIT_DATA_SIZE = 1024 * 1024;

    private ExecutorService mImageService = Executors.newSingleThreadExecutor();

    public WearCanvasProfile() {
        addApi(mPostDrawImage);
        addApi(mDeleteDrawImage);
    }

    private DConnectApi mPostDrawImage = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final byte[] data = getData(request);
            final double x = getX(request);
            final double y = getY(request);
            final String mode = getMode(request);
            if (data == null) {
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        String uri = getURI(request);
                        byte[] result = getData(uri);
                        if (result == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "could not get image from uri.");
                            sendResponse(response);
                            return;
                        }
                        if (drawImage(response, result, x, y, mode)) {
                            sendResponse(response);
                        }
                    }
                });
                return false;
            } else {
                return drawImage(response, data, x, y, mode);
            }
        }
    };

    private boolean drawImage(final Intent response, byte[] data, double x, double y, String mode) {
        if (data.length > LIMIT_DATA_SIZE) {
            MessageUtils.setInvalidRequestParameterError(response, "data size more than 1MB");
            return true;
        }
        Mode m = Mode.getInstance(mode);
        if ((mode != null && mode.length() > 0) && m == null) {
            MessageUtils.setInvalidRequestParameterError(response, "mode is invalid");
            return true;
        }

        //for check binary
        Bitmap bitmap = getBitmap(data);
        if (bitmap == null) {
            MessageUtils.setInvalidRequestParameterError(response, "format invalid");
            return true;
        }
        int mm = WearUtils.convertMode(m);

        //Adjust image format and compress
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, o);
        data = o.toByteArray();

        getManager().sendImageData(data, (int) x, (int) y, mm, new OnDataItemResultListener() {
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

    private final DConnectApi mDeleteDrawImage = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String nodeId = WearUtils.getNodeId(getServiceID(request));
            getManager().sendMessageToWear(nodeId, WearConst.DEVICE_TO_WEAR_CANCAS_DELETE_IMAGE,
                "", new WearManager.OnMessageResultListener() {
                    @Override
                    public void onResult(final MessageApi.SendMessageResult result) {
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
    };

    /**
     * データを画像に変換します.
     *
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
     *
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }
}
