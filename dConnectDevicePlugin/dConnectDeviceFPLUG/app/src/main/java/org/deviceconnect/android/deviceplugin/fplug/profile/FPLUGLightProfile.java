/*
 FPLUGLightProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.fplug.FPLUGApplication;
import org.deviceconnect.android.deviceplugin.fplug.FPLUGDeviceService;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGRequestCallback;
import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGResponse;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.LightProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGLightProfile extends LightProfile {

    private Map<String, FlashingExecutor> mFlashingMap = new HashMap<String, FlashingExecutor>();

    private final DConnectApi mGetLightApi = new GetApi() {

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);

            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            List<FPLUGController> fplugs = app.getConnectedController();
            FPLUGController fplug = app.getConnectedController(serviceId);
            if (fplugs == null) {
                MessageUtils.setNotFoundServiceError(response, "Not found fplug: " + serviceId);
                return true;
            }

            Bundle lightParam = new Bundle();
            setLightId(lightParam, fplug.getAddress());
            setName(lightParam, "F-PLUG LED");
            setConfig(lightParam, "");
            setOn(lightParam, false);//f-plug's status can not be take. So always OFF.
            List<Bundle> lightParams = new ArrayList<>();
            lightParams.add(lightParam);
            setLights(response, lightParams);

            sendResultOK(response);
            return true;
        }

    };

    private final DConnectApi mPostLightApi = new PostApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            String lightId = getLightId(request);
            long[] flashing = getFlashing(request);

            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            FPLUGController controller;
            if (lightId == null) {
                controller = app.getConnectedController(serviceId);
            } else if (lightId.length() != 0) {
                controller = app.getConnectedController(lightId);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }
            if (controller == null) {
                MessageUtils.setInvalidRequestParameterError(response, "Not found fplug: " + lightId);
                return true;
            }
            if (flashing != null) {
                flashing(controller, flashing);//do not check result of flashing
                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            } else {
                controller.requestLEDControl(true, new FPLUGRequestCallback() {
                    @Override
                    public void onSuccess(final FPLUGResponse fResponse) {
                        sendResultOK(response);
                    }

                    @Override
                    public void onError(final String message) {
                        sendResultError(response);
                    }

                    @Override
                    public void onTimeout() {
                        sendResultTimeout(response);
                    }
                });
                return false;
            }
        }
    };

    private final DConnectApi mDeleteLightApi = new DeleteApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            String lightId = getLightId(request);

            FPLUGApplication app = ((FPLUGApplication) getContext().getApplicationContext());
            FPLUGController controller;
            if (lightId == null) {
                controller = app.getConnectedController(serviceId);
            } else if (lightId.length() != 0) {
                controller = app.getConnectedController(lightId);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "lightId is not specified.");
                return true;
            }
            if (controller == null) {
                MessageUtils.setInvalidRequestParameterError(response, "Not found fplug: " + lightId);
                return true;
            }
            controller.requestLEDControl(false, new FPLUGRequestCallback() {
                @Override
                public void onSuccess(final FPLUGResponse fResponse) {
                    sendResultOK(response);
                }

                @Override
                public void onError(final String message) {
                    sendResultError(response);
                }

                @Override
                public void onTimeout() {
                    sendResultTimeout(response);
                }
            });
            return false;
        }
    };

    public FPLUGLightProfile() {
        addApi(mGetLightApi);
        addApi(mPostLightApi);
        addApi(mDeleteLightApi);
    }

    private void flashing(final FPLUGController controller, long[] flashing) {
        FlashingExecutor exe = mFlashingMap.get(controller.getAddress());
        if (exe == null) {
            exe = new FlashingExecutor();
            mFlashingMap.put(controller.getAddress(), exe);
        }
        exe.setLightControllable(new FlashingExecutor.LightControllable() {
            @Override
            public void changeLight(boolean isOn, final FlashingExecutor.CompleteListener listener) {
                controller.requestLEDControl(isOn, new FPLUGRequestCallback() {
                    @Override
                    public void onSuccess(FPLUGResponse response) {
                        listener.onComplete();
                    }

                    @Override
                    public void onError(String message) {
                        listener.onComplete();
                    }

                    @Override
                    public void onTimeout() {
                        listener.onComplete();
                    }
                });
            }
        });
        exe.start(flashing);
    }

    private void sendResultOK(final Intent response) {
        setResult(response, DConnectMessage.RESULT_OK);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultError(final Intent response) {
        MessageUtils.setUnknownError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

    private void sendResultTimeout(final Intent response) {
        MessageUtils.setTimeoutError(response);
        ((FPLUGDeviceService) getContext()).sendResponse(response);
    }

}
