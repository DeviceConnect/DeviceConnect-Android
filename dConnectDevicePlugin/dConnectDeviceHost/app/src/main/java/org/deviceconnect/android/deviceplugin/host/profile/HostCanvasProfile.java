/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.host.activity.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Canvas Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {

    private ExecutorService mImageService = Executors.newSingleThreadExecutor();

    private final DConnectApi mDrawImageApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String mode = getMode(request);
            String mimeType = getMIMEType(request);
            final CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
            if (enumMode == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }

            if (mimeType != null && !mimeType.contains("image")) {
                MessageUtils.setInvalidRequestParameterError(response,
                    "Unsupported mimeType: " + mimeType);
                return true;
            }

            final byte[] data = getData(request);
            final String uri = getURI(request);
            final double x = getX(request);
            final double y = getY(request);
            if (data == null) {
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        byte[] result = getData(uri);
                        if (result == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "could not get image from uri.");
                            sendResponse(response);
                            return;
                        }
                        drawImage(response, result, enumMode, x, y);
                        sendResponse(response);
                    }
                });
                return false;
            } else {
                drawImage(response, data, enumMode, x, y);
                return true;
            }
        }
    };

    private final DConnectApi mDeleteImageApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String className = getClassnameOfTopActivity();
            if (CanvasProfileActivity.class.getName().equals(className)) {
                Intent intent = new Intent(CanvasDrawImageObject.ACTION_DELETE_CANVAS);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setIllegalDeviceStateError(response, "canvas not display");
            }

            return true;
        }
    };

    /**
     * コンストラクタ.
     */
    public HostCanvasProfile() {
        addApi(mDrawImageApi);
        addApi(mDeleteImageApi);
    }

    private void drawImage(Intent response, byte[] data, CanvasDrawImageObject.Mode enumMode, double x, double y) {
        if (!CanvasDrawUtils.checkBitmap(data)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "The width and height of image must be less than 2048px.");
            return;
        }

        CanvasDrawImageObject drawObj = new CanvasDrawImageObject(data, enumMode, x, y);

        String className = getClassnameOfTopActivity();
        if (CanvasProfileActivity.class.getName().equals(className)) {
            Intent intent = new Intent(CanvasDrawImageObject.ACTION_DRAW_CANVAS);
            drawObj.setValueToIntent(intent);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setClass(getContext(), CanvasProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            drawObj.setValueToIntent(intent);
            getContext().startActivity(intent);
        }

        setResult(response, DConnectMessage.RESULT_OK);
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     *
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager activityMgr = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        return activityMgr.getRunningTasks(1).get(0).topActivity.getClassName();
    }
}
