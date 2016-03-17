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

    @Override
    protected boolean onDeleteDrawImage(final Intent request, final Intent response,
                                        final String serviceId) {
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

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

    /**
     * Execute a request.
     *
     * @param request   request intent
     * @param response  response
     * @param serviceId serviceId
     * @param mimeType  mime type
     * @param uri       uri of image
     * @param x         x coordinate for drawing
     * @param y         y coordinate for drawing
     * @param mode      mode of rendering
     * @return true if send response immediately, false otherwise
     */
    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response,
                                      final String serviceId, final String mimeType, byte[] data, final String uri,
                                      final double x, final double y, final String mode) {

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        final CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
        if (enumMode == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        if (mimeType != null && !mimeType.contains("image")) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Data format is invalid.");
            return true;
        }
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
        ActivityManager activitMgr = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String className = activitMgr.getRunningTasks(1).get(0).topActivity.getClassName();
        return className;
    }
}
