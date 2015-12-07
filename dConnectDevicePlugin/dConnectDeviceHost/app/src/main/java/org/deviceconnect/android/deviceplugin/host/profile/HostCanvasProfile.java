/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import org.deviceconnect.android.deviceplugin.host.activity.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Canvas Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;
        if (ATTRIBUTE_DRAW_IMAGE.equals(attribute)) {
            String serviceId = getServiceID(request);
            String mimeType = getMIMEType(request);
            String uri = request.getStringExtra(CanvasProfile.PARAM_URI);

            if (mimeType != null && !checkMimeTypeFormat(mimeType)) {
                MessageUtils.setInvalidRequestParameterError(response, "mimeType format is incorrect.");
                return result;
            }
            if (!checkXFormat(request)) {
                MessageUtils.setInvalidRequestParameterError(response, "x is different type.");
                return result;
            }
            if (!checkYFormat(request)) {
                MessageUtils.setInvalidRequestParameterError(response, "y is different type.");
                return result;
            }

            double x = getX(request);
            double y = getY(request);

            String mode = getMode(request);
            result = onPostDrawImageForHost(request, response, serviceId, mimeType, uri, x, y, mode);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

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
     * @param request request intent
     * @param response response
     * @param serviceId serviceId
     * @param mimeType mime type
     * @param uri uri of image
     * @param x x coordinate for drawing
     * @param y y coordinate for drawing
     * @param mode mode of rendering
     * @return true if send response immediately, false otherwise
     */
    private boolean onPostDrawImageForHost(final Intent request, final Intent response,
            final String serviceId, final String mimeType, final String uri, 
            final double x, final double y, final String mode) {
        if (uri == null) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "data is not specied to update a file.");
            return true;
        }

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
        if (enumMode == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        String type = CanvasDrawUtils.getMimeType(uri);
        if (type != null
                && type.indexOf("image") == -1) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "Data format is invalid.");
            return true;
        }
        if (!CanvasDrawUtils.checkBitmap(getContext(), uri)) {
            MessageUtils.setInvalidRequestParameterError(response,
                    "The width and height of image must be less than 2048px.");
            return true;
        }

        CanvasDrawImageObject drawObj = new CanvasDrawImageObject(uri, enumMode, x, y);

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
        return true;
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
