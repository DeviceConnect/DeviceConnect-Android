/*
SonyCameraZoomProfile
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.sonycamera.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraDeviceService;
import org.deviceconnect.android.deviceplugin.sonycamera.SonyCameraManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

/**
 * Sony Camera 用 カメラプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraZoomProfile extends CameraProfile {

    private final DConnectApi mPutZoomApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ZOOM;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onPutActZoom(request, response);
        }
    };

    private final DConnectApi mGetZoomApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ZOOM;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return onGetZoomDiameter(request, response);
        }
    };

    public SonyCameraZoomProfile() {
        addApi(mPutZoomApi);
        addApi(mGetZoomApi);
    }

    /**
     * ズーム倍率を設定します.
     *
     * @param request リクエスト
     * @param response レスポンス
     * @return 即時レスポンスを返却する場合はtrue、それ以外はfalse
     */
    private boolean onPutActZoom(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);
        String direction = getDirection(request);
        String movement = getMovement(request);

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (direction == null || movement == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        SonyCameraManager manager = getSonyCameraManager();
        if (!manager.isConnectedService(serviceId)) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not ready.");
            return true;
        }

        if (!manager.isSupportedZoom()) {
            MessageUtils.setNotSupportAttributeError(response, "Sony's camera is not support setZoom.");
            return true;
        }

        if (!direction.equals("in")) {
            if (!direction.equals("out")) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }
        }

        if (!movement.equals("in-start")) {
            if (!movement.equals("in-stop")) {
                if (!movement.equals("1shot")) {
                    if (!movement.equals("max")) {
                        MessageUtils.setInvalidRequestParameterError(response);
                        return true;
                    }
                }
            }
        }

        manager.setZoom(direction, movement, new SonyCameraManager.OnSonyCameraListener() {
            @Override
            public void onSuccess() {
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }

            @Override
            public void onError() {
                MessageUtils.setUnknownError(response);
                sendResponse(response);
            }
        });

        return false;
    }

    /**
     * ズーム倍率を取得します.
     *
     * @param request request
     * @param response response
     * @return 即時レスポンスを返却する場合はtrue、それ以外はfalse
     */
    private boolean onGetZoomDiameter(final Intent request, final Intent response) {
        String serviceId = getServiceID(request);

        if (serviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            sendResponse(response);
            return true;
        }

        SonyCameraManager manager = getSonyCameraManager();
        if (!manager.isConnectedService(serviceId)) {
            MessageUtils.setIllegalDeviceStateError(response, "Sony's camera is not ready.");
            return true;
        }

        if (!manager.isSupportedZoom()) {
            MessageUtils.setNotSupportAttributeError(response, "Sony's camera is not support setZoom.");
            return true;
        }

        manager.getZoom(new SonyCameraManager.OnZoomListener() {
            @Override
            public void onZoom(final double zoom) {
                response.putExtra("zoomPosition", zoom);
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }

            @Override
            public void onError() {
                MessageUtils.setUnknownError(response);
                sendResponse(response);
            }
        });

        return false;
    }

    private SonyCameraManager getSonyCameraManager() {
        return ((SonyCameraDeviceService) getContext()).getSonyCameraManager();
    }
}
