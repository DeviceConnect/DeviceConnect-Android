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

import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;

import org.deviceconnect.android.deviceplugin.wear.WearDeviceService;
import org.deviceconnect.android.deviceplugin.wear.WearManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

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

    private final Logger mLogger = Logger.getLogger("dconnect.wear");

    private ExecutorService mImageService = Executors.newSingleThreadExecutor();

    private static final Map<String, DrawImageRequest> mRequestMap = new HashMap<>();

    private final WearManager mWearManager;

    public WearCanvasProfile(final WearManager mgr) {
        mWearManager = mgr;
        mgr.addMessageEventListener(WearConst.WEAR_TO_DEVICE_CANVAS_RESULT,
            this::onCanvasResponse);
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
            final String nodeId = WearUtils.getNodeId(getServiceID(request));
            final byte[] data = getData(request);
            final double x = getX(request);
            final double y = getY(request);
            final String mode = getMode(request);
            String mimeType = getMIMEType(request);
            if (mimeType != null && !mimeType.contains("image")) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Unsupported mimeType: " + mimeType);
                return true;
            }

            if (data == null) {
                mImageService.execute(() -> {
                    String uri = getURI(request);
                    byte[] result = getData(uri);
                    if (result == null) {
                        MessageUtils.setInvalidRequestParameterError(response, "could not get image from uri.");
                        sendResponse(response);
                        return;
                    }
                    drawImage(response, nodeId, result, x, y, mode);
                });
                return false;
            } else {
                drawImage(response, nodeId, data, x, y, mode);
                return false;
            }
        }
    };

    private void drawImage(final Intent response, final String nodeId,
                              final byte[] data, final double x, final double y, final String mode) {
        mWearManager.getLocalNodeId(nodeId, new WearManager.OnLocalNodeListener() {

            @Override
            public void onResult(final Node localNode) {
                final String localNodeId = localNode.getId();
                if (data.length > LIMIT_DATA_SIZE) {
                    MessageUtils.setInvalidRequestParameterError(response, "data size more than 1MB");
                    sendResponse(response);
                    return;
                }
                Mode m = Mode.getInstance(mode);
                if ((mode != null && mode.length() > 0) && m == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "mode is invalid");
                    sendResponse(response);
                    return;
                }

                //for check binary
                Bitmap bitmap;
                try {
                    bitmap = getBitmap(data);
                } catch (OutOfMemoryError e) {
                    MessageUtils.setInvalidRequestParameterError(response, "Too large bitmap for host device.");
                    sendResponse(response);
                    return;
                }
                if (bitmap == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "format invalid");
                    sendResponse(response);
                    return;
                }
                int mm = WearUtils.convertMode(m);

                //Adjust image format and compress
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, o);
                final byte[] bitmapData = o.toByteArray();

                final String requestId = UUID.randomUUID().toString();
                final DrawImageRequest wearRequest = createCanvasRequest(localNodeId, requestId);
                getManager().sendImageData(localNodeId, requestId, bitmapData, (int) x, (int) y, mm, new WearManager.OnDataItemResultListener() {
                    @Override
                    public void onResult(final DataItem result) {
                        new Thread(() -> {
                            if (result != null) {
                                try {
                                    DrawImageResponse wearResponse = wearRequest.await();
                                    if (wearResponse.isSuccess()) {
                                        setResult(response, DConnectMessage.RESULT_OK);
                                    } else {
                                        int errorCode = wearResponse.getErrorCode();
                                        String errorMessage = wearResponse.getErrorMessage();
                                        MessageUtils.setError(response, errorCode, errorMessage);
                                    }
                                } catch (Exception e) {
                                    MessageUtils.setUnknownError(response, e.getLocalizedMessage());
                                }
                            } else {
                                MessageUtils.setIllegalDeviceStateError(response);
                            }
                            sendResponse(response);
                        }).start();
                    }

                    @Override
                    public void onError() {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendResponse(response);
                    }
                });
            }

            @Override
            public void onError() {
                MessageUtils.setUnknownError(response, "Failed to get Local Node ID.");
            }
        });
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
                    public void onResult() {
                        setResult(response, DConnectMessage.RESULT_OK);
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
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * Android Wear管理クラスを取得する.
     *
     * @return WearManager管理クラス
     */
    private WearManager getManager() {
        return ((WearDeviceService) getContext()).getManager();
    }

    private DrawImageRequest createCanvasRequest(final String nodeId, final String requestId) {
        DrawImageRequest request = new DrawImageRequest(nodeId);
        mRequestMap.put(requestId, request);
        return request;
    }

    private void onCanvasResponse(final String nodeId, final String message) {
        String[] items = message.split(",");
        String requestId = items[0];
        String result = items[1];
        DrawImageRequest request = mRequestMap.get(requestId);
        if (request == null) {
            mLogger.warning("onCanvasImageResponse: request is not found: nodeId = " + nodeId);
            return;
        }
        if (!request.getNodeId().equals(nodeId)) {
            mLogger.warning("onCanvasImageResponse: nodeId are not matched for request: requestId = " + requestId);
            return;
        }
        request.receive(result);
        mRequestMap.remove(requestId);
    }

    private static class DrawImageRequest {

        private CountDownLatch mLock = new CountDownLatch(1);

        private DrawImageResponse mResponse;

        private final String mNodeId;

        public DrawImageRequest(final String nodeId) {
            mNodeId = nodeId;
        }

        public DrawImageResponse await() throws InterruptedException, ResponseTimeoutException {
            mLock.await(30, TimeUnit.SECONDS);
            if (!hasResponse()) {
                throw new ResponseTimeoutException();
            }
            return mResponse;
        }

        public void receive(final String message) {
            mResponse = new DrawImageResponse(message);
            mLock.countDown();
        }

        public boolean hasResponse() {
            return mResponse != null;
        }

        public String getNodeId() {
            return mNodeId;
        }
    }

    private static class DrawImageResponse {

        private final String mResult;
        private final int mErrorCode;
        private final String mErrorMessage;

        public DrawImageResponse(final String message) {
            mResult = message;
            if (WearConst.RESULT_ERROR_TOO_LARGE_BITMAP.equals(message)) {
                mErrorCode = DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode();
                mErrorMessage = "Too large bitmap for watch.";
            } else if (WearConst.RESULT_ERROR_CONNECTION_FAILURE.equals(message)) {
                mErrorCode = DConnectMessage.ErrorCode.ILLEGAL_DEVICE_STATE.getCode();
                mErrorMessage = "Connection failure.";
            } else if (WearConst.RESULT_ERROR_NOT_SUPPORTED_FORMAT.equals(message)) {
                mErrorCode = DConnectMessage.ErrorCode.INVALID_REQUEST_PARAMETER.getCode();
                mErrorMessage = "Not supported format.";
            } else {
                mErrorCode = 0;
                mErrorMessage = "";
            }
        }

        public boolean isSuccess() {
            return WearConst.RESULT_SUCCESS.equals(mResult);
        }

        public int getErrorCode() {
            return mErrorCode;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }
    }

    private static class ResponseTimeoutException extends TimeoutException {
    }
}
