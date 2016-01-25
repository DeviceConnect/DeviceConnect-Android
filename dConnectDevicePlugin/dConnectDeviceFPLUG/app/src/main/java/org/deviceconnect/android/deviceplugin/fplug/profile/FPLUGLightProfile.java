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
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Light Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGLightProfile extends LightProfile {

    private ScheduledExecutorService mFlashingService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mLatestScheduledFuture;
    private Queue<Long> mFlashingQueue = new ConcurrentLinkedQueue<>();

    @Override
    protected boolean onGetLight(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

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

    @Override
    protected boolean onPostLight(final Intent request, final Intent response, final String serviceId,
                                  final String lightId, final Integer color, final Double brightness,
                                  final long[] flashing) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

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

    @Override
    protected boolean onDeleteLight(final Intent request, final Intent response, final String serviceId,
                                    final String lightId) {
        if (serviceId == null || serviceId.length() == 0) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

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

    private void flashing(final FPLUGController controller, long[] flashing) {
        if (mLatestScheduledFuture != null && !mLatestScheduledFuture.isCancelled()) {
            mLatestScheduledFuture.cancel(false);
        }
        mFlashingQueue.clear();
        for (long value : flashing) {
            mFlashingQueue.add(value);
        }
        mLatestScheduledFuture = mFlashingService.schedule(new Runnable() {
            boolean isOn = true;

            @Override
            public void run() {
                controller.requestLEDControl(isOn, new FPLUGRequestCallback() {
                    @Override
                    public void onSuccess(FPLUGResponse response) {
                        next();
                    }

                    @Override
                    public void onError(String message) {
                        next();
                    }

                    @Override
                    public void onTimeout() {
                        next();
                    }
                });
            }

            private void next() {
                Long interval = mFlashingQueue.poll();
                if (interval != null) {
                    mLatestScheduledFuture = mFlashingService.schedule(this, interval, TimeUnit.MILLISECONDS);
                    isOn = !isOn;
                }
            }
        }, mFlashingQueue.poll(), TimeUnit.MILLISECONDS);
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
