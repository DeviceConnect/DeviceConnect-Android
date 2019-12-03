/*
 HVCC2WDeviceService
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import org.deviceconnect.android.deviceplugin.hvcp.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.OkaoResult;
import org.deviceconnect.android.deviceplugin.hvcp.profile.HVCPSystemProfile;
import org.deviceconnect.android.deviceplugin.hvcp.service.HVCPService;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.HumanDetectionProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.HumanDetectionProfileConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * HVC-P Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCPDeviceService extends DConnectMessageService
        implements HVCCameraInfo.OnBodyEventListener, HVCCameraInfo.OnHandEventListener,
        HVCCameraInfo.OnFaceEventListener, HVCManager.ConnectionListener {

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("hvcp.dplugin");

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if ((device.getVendorId() == 1027 && device.getProductId() == 24577)
                        || (device.getVendorId() == 1118 && device.getProductId() == 688)) {
                        HVCManager.INSTANCE.addUSBDevice(device);
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    HVCManager.INSTANCE.removeUSBDevice(device);
                }

            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        HVCManager.INSTANCE.init(this);
        HVCManager.INSTANCE.addConnectionListener(this);

        final IntentFilter filter = new IntentFilter(HVCManager.INSTANCE.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); // MODIFIED
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        HVCManager.INSTANCE.removeConnectionListener(this);

        HVCManager.INSTANCE.destroyFilter(this);
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            if (EventManager.INSTANCE.removeEvents(sessionKey)) {
                String[] param = sessionKey.split(".", -1);
                if (param[1] != null) { /* param[1] : pluginID (serviceId) */
                    HVCManager.INSTANCE.removeBodyDetectEventListener(param[1]);
                    HVCManager.INSTANCE.removeHandDetectEventListener(param[1]);
                    HVCManager.INSTANCE.removeFaceDetectEventListener(param[1]);
                    HVCManager.INSTANCE.removeFaceRecognizeEventListener(param[1]);
                }
            }
        } else {
            EventManager.INSTANCE.removeAll();
            HVCManager.INSTANCE.removeAllEventListener();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /* 全イベント削除. */
        EventManager.INSTANCE.removeAll();
        HVCManager.INSTANCE.removeAllEventListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HVCPSystemProfile();
    }


    /**
     * Register Human Detect Event.
     * @param request Request
     * @param response Response
     * @param serviceId ServiceID
     * @param kind Detect type
     */
    public void registerHumanDetectEvent(final Intent request, final Intent response, final String serviceId,
                                          final HumanDetectKind kind) {
        try {
            final Double threshold = HumanDetectionProfile.getThreshold(request);
            final Double min = HumanDetectionProfile.getMinWidth(request) != null
                    ? HumanDetectionProfile.getMinWidth(request)
                    : HumanDetectionProfile.getMinHeight(request);
            final Double max = HumanDetectionProfile.getMaxWidth(request) != null
                    ? HumanDetectionProfile.getMaxWidth(request)
                    : HumanDetectionProfile.getMaxHeight(request);

            HVCCameraInfo camera = HVCManager.INSTANCE.getHVCDevices().get(serviceId);
            camera.setThresholds(request);

            final Long[] inter = new Long[1];
            inter[0] = HumanDetectionProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                    HVCManager.PARAM_INTERVAL_MAX);

            if (inter[0] == null) {
                inter[0] = HVCManager.PARAM_INTERVAL_MIN;
            }
            final List<String> options = HumanDetectionProfile.getOptions(request);
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                switch (kind) {
                    case BODY:
                        HVCManager.INSTANCE.setThreshold(threshold, null, null, serviceId, (resultCode) -> {
                            HVCManager.INSTANCE.setMinMaxSize(min, max, null, null, null, null, serviceId, (resultCodes) -> {
                                HVCManager.INSTANCE.addBodyDetectEventListener(serviceId, HVCPDeviceService.this, inter[0]);
                                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            });
                        });
                        break;
                    case HAND:
                        HVCManager.INSTANCE.setThreshold(null, threshold, null, serviceId, (resultCode) -> {
                            HVCManager.INSTANCE.setMinMaxSize(null, null, min, max, null, null, serviceId, (resultCodes) -> {
                                HVCManager.INSTANCE.addHandDetectEventListener(serviceId, HVCPDeviceService.this, inter[0]);
                                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);

                                sendResponse(response);
                            });
                        });
                        break;
                    case FACE:
                        HVCManager.INSTANCE.setThreshold(null, null, threshold, serviceId, (resultCode) -> {
                            HVCManager.INSTANCE.setMinMaxSize(null, null, null, null, min, max, serviceId, (resultCodes) -> {
                                HVCManager.INSTANCE.addFaceDetectEventListener(serviceId, HVCPDeviceService.this, options, inter[0]);
                                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            });
                        });
                        break;
                    case HUMAN:
                        HVCManager.INSTANCE.addBodyDetectEventListener(serviceId, HVCPDeviceService.this, inter[0]);
                        HVCManager.INSTANCE.addHandDetectEventListener(serviceId, HVCPDeviceService.this, inter[0]);
                        HVCManager.INSTANCE.addFaceDetectEventListener(serviceId, HVCPDeviceService.this, options, inter[0]);
                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                        break;
                    default:
                        MessageUtils.setInvalidRequestParameterError(response);
                        sendResponse(response);

                }
            } else {
                MessageUtils.setIllegalDeviceStateError(response, "Can not register event.");
                sendResponse(response);
            }
        } catch (IllegalStateException e) {
            // BUG: detectKind unknown value.
            MessageUtils.setUnknownError(response, e.getMessage());
            sendResponse(response);
        } catch (NumberFormatException e) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
            sendResponse(response);
        } catch (IllegalArgumentException e) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
            sendResponse(response);
        }
    }

    /**
     * Unregister Human Detect Event.
     * @param request Request
     * @param response Response
     * @param serviceId Service ID
     * @param kind Detect type
     */
    public void unregisterHumanDetectionProfileEvent(final Intent request, final Intent response, final String serviceId,
                                                   final HumanDetectKind kind) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            switch (kind) {
                case BODY:
                    HVCManager.INSTANCE.removeBodyDetectEventListener(serviceId);
                    break;
                case HAND:
                    HVCManager.INSTANCE.removeHandDetectEventListener(serviceId);
                    break;
                case FACE:
                    HVCManager.INSTANCE.removeFaceDetectEventListener(serviceId);
                    break;
                case HUMAN:
                    HVCManager.INSTANCE.removeBodyDetectEventListener(serviceId);
                    HVCManager.INSTANCE.removeHandDetectEventListener(serviceId);
                    HVCManager.INSTANCE.removeFaceDetectEventListener(serviceId);
                    break;
                default:
                    MessageUtils.setInvalidRequestParameterError(response);
            }
            DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setIllegalDeviceStateError(response, "Can not unregister event.");
        }
        sendResponse(response);
    }

    /**
     * Get Human Detect Result.
     * @param request Request
     * @param response Response
     * @param serviceId Service ID
     * @param kind Detect type
     * @param options FaceDetectOptions
     */
    public void doGetHumanDetectionProfile(final Intent request, final Intent response, final String serviceId,
                                        final HumanDetectKind kind, final List<String> options) {
        try {
            final Double threshold = HumanDetectionProfile.getThreshold(request);
            final Double min = HumanDetectionProfile.getMinWidth(request) != null
                    ? HumanDetectionProfile.getMinWidth(request)
                    : HumanDetectionProfile.getMinHeight(request);
            final Double max = HumanDetectionProfile.getMaxWidth(request) != null
                    ? HumanDetectionProfile.getMaxWidth(request)
                    : HumanDetectionProfile.getMaxHeight(request);
            final HVCCameraInfo camera = HVCManager.INSTANCE.getHVCDevices().get(serviceId);
            camera.setThresholds(request);

            HumanDetectionProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                    HVCManager.PARAM_INTERVAL_MAX);


            switch (kind) {
                case BODY:
                    HVCManager.INSTANCE.setThreshold(threshold, null, null, serviceId, (resultCode) -> {
                        HVCManager.INSTANCE.setMinMaxSize(min, max, null, null, null, null, serviceId, (resultCodes) -> {
                            HVCManager.INSTANCE.execute(serviceId, kind, (forServiceId, result) -> {
                                makeBodyDetectResultResponse(response, result);
                                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            });
                        });
                    });
                    break;
                case HAND:
                    HVCManager.INSTANCE.setThreshold(null, threshold, null, serviceId, (resultCode) -> {
                        HVCManager.INSTANCE.setMinMaxSize(null, null, min, max, null, null, serviceId, (resultCodes) -> {
                            HVCManager.INSTANCE.execute(serviceId, kind, (forServiceId, result) -> {
                                makeHandDetectResultResponse(response, result);
                                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            });
                        });
                    });
                    break;
                case FACE:
                    HVCManager.INSTANCE.setThreshold(null, null, threshold, serviceId, (resultCode) -> {
                        HVCManager.INSTANCE.setMinMaxSize(null, null, null, null, min, max, serviceId, (resultCodes) -> {
                            HVCManager.INSTANCE.execute(serviceId, kind, (forServiceId, result) -> {
                                makeFaceDetectResultResponse(response, result, options, camera.getThresholds());
                                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);
                            });
                        });
                    });
                    break;
                case HUMAN:
                    HVCManager.INSTANCE.execute(serviceId, kind, (forServiceId, result) -> {
                        makeHumanDetectResultResponse(response, result);
                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                    });
                    break;
                default:
                    MessageUtils.setInvalidRequestParameterError(response);
                    sendResponse(response);
            }

        } catch (IllegalStateException e) {
            // BUG: detectKind unknown value.
            MessageUtils.setUnknownError(response, e.getMessage());
            sendResponse(response);
        } catch (NumberFormatException e) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
            sendResponse(response);
        } catch (IllegalArgumentException e) {
            // invalid request parameter error
            MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
            sendResponse(response);
        }
    }

    @Override
    public void onNotifyForBodyDetectResult(String serviceId, OkaoResult result) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectionProfile.PROFILE_NAME, null, HumanDetectionProfile.ATTRIBUTE_ON_BODY_DETECTION);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeBodyDetectResultResponse(intent, result);
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                mLogger.info("<EVENT> send event. attribute:" + HumanDetectionProfile.ATTRIBUTE_ON_BODY_DETECTION);
            }
        }
        onNotifyForDetectResult(serviceId, result);
    }

    @Override
    public void onNotifyForFaceDetectResult(String serviceId, OkaoResult result) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectionProfile.PROFILE_NAME, null, HumanDetectionProfile.ATTRIBUTE_ON_FACE_DETECTION);
        HVCCameraInfo camera = HVCManager.INSTANCE.getHVCDevices().get(serviceId);

        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeFaceDetectResultResponse(intent, result, camera.getOptions(), camera.getThresholds());
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                mLogger.info("<EVENT> send event. attribute:" + HumanDetectionProfile.ATTRIBUTE_ON_FACE_DETECTION);
            }
        }
        onNotifyForDetectResult(serviceId, result);
    }



    @Override
    public void onNotifyForHandDetectResult(String serviceId, OkaoResult result) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectionProfile.PROFILE_NAME, null, HumanDetectionProfile.ATTRIBUTE_ON_HAND_DETECTION);

        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeHandDetectResultResponse(intent, result);
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                mLogger.info("<EVENT> send event. attribute:" + HumanDetectionProfile.ATTRIBUTE_ON_HAND_DETECTION);
            }
        }
        onNotifyForDetectResult(serviceId, result);
    }
    /**
     * Notify Default HumanDetection Result.
     * @param serviceId ServiceId
     * @param result Okao Result
     */
    private void onNotifyForDetectResult(String serviceId, OkaoResult result) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectionProfile.PROFILE_NAME, null, "onDetection");
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeHumanDetectResultResponse(intent, result);
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                mLogger.info("<EVENT> send event. attribute:onDetection");
            }
        }
    }
    @Override
    public void onConnected(final HVCCameraInfo camera) {
        DConnectService service = getServiceProvider().getService(camera.getID());
        if (service == null) {
            service = new HVCPService(camera);
            getServiceProvider().addService(service);
        }
        service.setOnline(true);
    }

    @Override
    public void onDisconnected(final HVCCameraInfo camera) {
        DConnectService service = getServiceProvider().getService(camera.getID());
        if (service != null) {
            service.setOnline(false);
        }
    }
    /**
     * Make Human Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeHumanDetectResultResponse(final Intent response, final OkaoResult result) {

        Bundle humanDetect = new Bundle();
        int count = result.getNumberOfBody() + result.getNumberOfFace() + result.getNumberOfHand();


        if (count > 0) {
            humanDetect.putBoolean("exist", true);
        } else {
            humanDetect.putBoolean("exist", false);
        }
        response.putExtra("humanDetect", humanDetect);
    }
    /**
     * Make Body Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeBodyDetectResultResponse(final Intent response, final OkaoResult result) {

        List<Bundle> bodyDetects = new LinkedList<>();
        int count = result.getNumberOfBody();
        for (int i = 0; i < count; i++) {
            Bundle bodyDetect = new Bundle();
            HumanDetectionProfile.setParamX(bodyDetect,
                    (double) result.getBodyX()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectionProfile.setParamY(bodyDetect,
                    (double) result.getBodyY()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamWidth(bodyDetect,
                    (double) result.getBodySize()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectionProfile.setParamHeight(bodyDetect,
                    (double) result.getBodySize()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamConfidence(bodyDetect,
                    (double) result.getBodyDetectConfidence()[i] / (double) HVCManager.HVC_P_MAX_THRESHOLD);

            bodyDetects.add(bodyDetect);
        }
        if (bodyDetects.size() > 0) {
            HumanDetectionProfile.setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
        }
    }

    /**
     * Make Hand Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeHandDetectResultResponse(final Intent response, final OkaoResult result) {

        List<Bundle> handDetects = new LinkedList<>();
        int count = result.getNumberOfHand();

        for (int i = 0; i < count; i++) {
            Bundle handDetect = new Bundle();
            HumanDetectionProfile.setParamX(handDetect,
                    (double) result.getHandX()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectionProfile.setParamY(handDetect,
                    (double) result.getHandY()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamWidth(handDetect,
                    (double) result.getHandSize()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectionProfile.setParamHeight(handDetect,
                    (double) result.getHandSize()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamConfidence(handDetect,
                    (double) result.getHandDetectConfidence()[i] / (double) HVCManager.HVC_P_MAX_THRESHOLD);

            handDetects.add(handDetect);
        }
        if (handDetects.size() > 0) {
            HumanDetectionProfile.setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
        }
    }

    /**
     * Make Face Detect Result Respnse.
     * @param response response
     * @param result result
     * @param options Options
     * @param thresholds confidence's thresholds
     */
    private void makeFaceDetectResultResponse(final Intent response, final OkaoResult result, final List<String> options,
                                              final SparseArray<Double> thresholds) {
        List<Bundle> faceDetects = new LinkedList<>();
        int count = result.getNumberOfFace();

        for (int i = 0; i < count; i++) {
            Bundle faceDetect = new Bundle();
            HumanDetectionProfile.setParamX(faceDetect,
                    (double) result.getFaceX()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectionProfile.setParamY(faceDetect,
                    (double) result.getFaceY()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamWidth(faceDetect,
                    (double) result.getFaceSize()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectionProfile.setParamHeight(faceDetect,
                    (double) result.getFaceSize()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamConfidence(faceDetect,
                    (double) result.getFaceDetectConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
            if (existOption(HVCManager.PARAM_OPTIONS_FACE_DIRECTION, options)
                    && (thresholds.get(HVCCameraInfo.ThresholdKind.FACEDIRECTION.ordinal(), 0.0) == null
                    || thresholds.get(HVCCameraInfo.ThresholdKind.FACEDIRECTION.ordinal(), 0.0) != null
                    && isExceedThresholds(thresholds.get(HVCCameraInfo.ThresholdKind.FACEDIRECTION.ordinal(), 0.0),
                    (double)  result.getFaceDirectionConfidence()[i]))) {
                // face direction.
                Bundle faceDirectionResult = new Bundle();
                HumanDetectionProfile.setParamYaw(faceDirectionResult, result.getFaceDirectionLR()[i]);
                HumanDetectionProfile.setParamPitch(faceDirectionResult, result.getFaceDirectionUD()[i]);
                HumanDetectionProfile.setParamRoll(faceDirectionResult, result.getFaceDirectionSlope()[i]);
                HumanDetectionProfile.setParamConfidence(faceDirectionResult,
                            (double) result.getFaceDirectionConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);

                HumanDetectionProfile.setParamFaceDirectionResults(faceDetect, faceDirectionResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_AGE, options)
                    && (thresholds.get(HVCCameraInfo.ThresholdKind.AGE.ordinal(), 0.0) == null
                    || thresholds.get(HVCCameraInfo.ThresholdKind.AGE.ordinal(), 0.0) != null
                    && isExceedThresholds(thresholds.get(HVCCameraInfo.ThresholdKind.AGE.ordinal(), 0.0),
                    (double) result.getAgeConfidence()[i]))) {
                // age.
                Bundle ageResult = new Bundle();
                HumanDetectionProfile.setParamAge(ageResult, (int) result.getAge()[i]);
                HumanDetectionProfile.setParamConfidence(ageResult,
                        (double) result.getAgeConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
                HumanDetectionProfile.setParamAgeResults(faceDetect, ageResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_GENDER, options)
                    && (thresholds.get(HVCCameraInfo.ThresholdKind.GENDER.ordinal(), 0.0) == null
                    || thresholds.get(HVCCameraInfo.ThresholdKind.GENDER.ordinal(), 0.0) != null
                    && isExceedThresholds(thresholds.get(HVCCameraInfo.ThresholdKind.GENDER.ordinal(), 0.0),
                    (double) result.getGenderConfidence()[i] ))) {

                // gender.
                Bundle genderResult = new Bundle();
                HumanDetectionProfile.setParamGender(genderResult,
                        (result.getGender()[i] == HVCManager.HVC_GEN_MALE ? HumanDetectionProfile.VALUE_GENDER_MALE
                                : HumanDetectionProfile.VALUE_GENDER_FEMALE));
                HumanDetectionProfile.setParamConfidence(genderResult,
                        (double) result.getGenderConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
                HumanDetectionProfile.setParamGenderResults(faceDetect, genderResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_GAZE, options)) {
                // gaze.
                Bundle gazeResult = new Bundle();
                HumanDetectionProfile.setParamGazeLR(gazeResult, result.getGazeLR()[i]);
                HumanDetectionProfile.setParamGazeUD(gazeResult, result.getGazeUD()[i]);
                // Unsuppoted Face Direction's Confidence
                HumanDetectionProfile.setParamGazeResults(faceDetect, gazeResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_BLINK, options)) {
                // blink.
                Bundle blinkResult = new Bundle();
                HumanDetectionProfile.setParamLeftEye(blinkResult,
                        (double) result.getBlinkLeft()[i] / (double) HVCManager.HVC_P_MAX_BLINK);
                HumanDetectionProfile.setParamRightEye(blinkResult,
                        (double) result.getBlinkRight()[i] / (double) HVCManager.HVC_P_MAX_BLINK);
                // Unsuppoted Face Direction's Confidence
                HumanDetectionProfile.setParamBlinkResults(faceDetect, blinkResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_EXPRESSION, options)) {
                long score = -1;
                int index = -1;
                long[] scores = new long[]{result.getExpressionUnknown()[i],
                                            result.getExpressionSmile()[i],
                                            result.getExpressionSurprise()[i],
                                            result.getExpressionMad()[i],
                                            result.getExpressionSad()[i]};
                for (int j = 0; j < scores.length;j++) {
                    long s = scores[j];
                    if (s > score) {
                        score = s;
                        index = j;
                    }
                }
                // expression.
                double confidence = (double) score / HVCManager.EXPRESSION_SCORE_MAX;
                if (thresholds.get(HVCCameraInfo.ThresholdKind.EXPRESSION.ordinal(), 0.0) == null
                        || (thresholds.get(HVCCameraInfo.ThresholdKind.EXPRESSION.ordinal(), 0.0) != null
                        && thresholds.get(HVCCameraInfo.ThresholdKind.EXPRESSION.ordinal(), 0.0) <= confidence)) {
                    // expression.
                    Bundle expressionResult = new Bundle();
                    HumanDetectionProfile.setParamExpression(expressionResult,
                            HVCManager.convertToNormalizeExpression(index));

                    HumanDetectionProfile.setParamConfidence(expressionResult,
                            confidence);

                    HumanDetectionProfile.setParamExpressionResults(faceDetect, expressionResult);
                }
           }

            faceDetects.add(faceDetect);
        }
        if (faceDetects.size() > 0) {
            HumanDetectionProfile.setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
        }
    }

    /**
     * Exist Option.
     * @param option option
     * @param options options
     * @return true:exist, false:no exist
     */
    public boolean existOption(final String option, final List<String> options) {
        if (options == null) {
            return false;
        }
        for (String o: options) {
            if (o.equals(option)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Is Exceed threshold.
     * @param threshold confidence's threshold
     * @param confidence confidence
     * @return true:exceed threshold
     */
    private boolean isExceedThresholds(final double threshold, final double confidence) {
        return threshold >= 0 && threshold <= (confidence / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
    }
}