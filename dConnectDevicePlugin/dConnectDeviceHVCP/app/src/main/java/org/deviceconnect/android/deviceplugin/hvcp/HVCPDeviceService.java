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
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

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
                if (param[1] != null) { /** param[1] : pluginID (serviceId) */
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
        /** 全イベント削除. */
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
            final Double threshold = HumanDetectProfile.getThreshold(request);
            final Double min = HumanDetectProfile.getMinWidth(request) != null
                    ? HumanDetectProfile.getMinWidth(request)
                    : HumanDetectProfile.getMinHeight(request);
            final Double max = HumanDetectProfile.getMaxWidth(request) != null
                    ? HumanDetectProfile.getMaxWidth(request)
                    : HumanDetectProfile.getMaxHeight(request);

            HumanDetectProfile.getEyeThreshold(request);
            HumanDetectProfile.getNoseThreshold(request);
            HumanDetectProfile.getMouthThreshold(request);
            HumanDetectProfile.getBlinkThreshold(request);
            HumanDetectProfile.getAgeThreshold(request);
            HumanDetectProfile.getGenderThreshold(request);
            HumanDetectProfile.getFaceDirectionThreshold(request);
            HumanDetectProfile.getGazeThreshold(request);
            HumanDetectProfile.getExpressionThreshold(request);
            final Long[] inter = new Long[1];
            inter[0] = HumanDetectProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                    HVCManager.PARAM_INTERVAL_MAX);

            if (inter[0] == null) {
                inter[0] = HVCManager.PARAM_INTERVAL_MIN;
            }
            final List<String> options = HumanDetectProfile.getOptions(request);
            EventError error = EventManager.INSTANCE.addEvent(request);

            if (error == EventError.NONE) {
                switch (kind) {
                    case BODY:
                        HVCManager.INSTANCE.setThreshold(threshold, null, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                            @Override
                            public void onResponse(int resultCode) {
                                HVCManager.INSTANCE.setMinMaxSize(min, max, null, null, null, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                                    @Override
                                    public void onResponse(int resultCode) {
                                        HVCManager.INSTANCE.addBodyDetectEventListener(serviceId, HVCPDeviceService.this, inter[0]);
                                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                        sendResponse(response);
                                    }
                                });
                            }
                        });
                        break;
                    case HAND:
                        HVCManager.INSTANCE.setThreshold(null, threshold, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                            @Override
                            public void onResponse(int resultCode) {
                                HVCManager.INSTANCE.setMinMaxSize(null, null, min, max, null, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                                    @Override
                                    public void onResponse(int resultCode) {
                                        HVCManager.INSTANCE.addHandDetectEventListener(serviceId, HVCPDeviceService.this, inter[0]);
                                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);

                                        sendResponse(response);
                                    }
                                });
                            }
                        });
                        break;
                    case FACE:
                        HVCManager.INSTANCE.setThreshold(null, null, threshold, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                            @Override
                            public void onResponse(int resultCode) {
                                HVCManager.INSTANCE.setMinMaxSize(null, null, null, null, min, max, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                                    @Override
                                    public void onResponse(int resultCode) {
                                        HVCManager.INSTANCE.addFaceDetectEventListener(serviceId, HVCPDeviceService.this, options, inter[0]);
                                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);

                                        sendResponse(response);
                                    }
                                });
                            }
                        });
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
    public void unregisterHumanDetectProfileEvent(final Intent request, final Intent response, final String serviceId,
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
                case RECOGNIZE:
                    HVCManager.INSTANCE.removeFaceRecognizeEventListener(serviceId);
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
    public void doGetHumanDetectProfile(final Intent request, final Intent response, final String serviceId,
                                        final HumanDetectKind kind, final List<String> options) {
        try {
            final Double threshold = HumanDetectProfile.getThreshold(request);
            final Double min = HumanDetectProfile.getMinWidth(request) != null
                    ? HumanDetectProfile.getMinWidth(request)
                    : HumanDetectProfile.getMinHeight(request);
            final Double max = HumanDetectProfile.getMaxWidth(request) != null
                    ? HumanDetectProfile.getMaxWidth(request)
                    : HumanDetectProfile.getMaxHeight(request);

            HumanDetectProfile.getEyeThreshold(request);
            HumanDetectProfile.getNoseThreshold(request);
            HumanDetectProfile.getMouthThreshold(request);
            HumanDetectProfile.getBlinkThreshold(request);
            HumanDetectProfile.getAgeThreshold(request);
            HumanDetectProfile.getGenderThreshold(request);
            HumanDetectProfile.getFaceDirectionThreshold(request);
            HumanDetectProfile.getGazeThreshold(request);
            HumanDetectProfile.getExpressionThreshold(request);

            HumanDetectProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                    HVCManager.PARAM_INTERVAL_MAX);


            switch (kind) {
                case BODY:
                    HVCManager.INSTANCE.setThreshold(threshold, null, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                        @Override
                        public void onResponse(int resultCode) {
                            HVCManager.INSTANCE.setMinMaxSize(min, max, null, null, null, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                                @Override
                                public void onResponse(int resultCode) {
                                    HVCManager.INSTANCE.execute(serviceId, kind, new HVCCameraInfo.OneShotOkaoResultResoponseListener() {
                                        @Override
                                        public void onResponse(String serviceId, OkaoResult result) {
                                            makeBodyDetectResultResponse(response, result);
                                            DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                            sendResponse(response);
                                        }
                                    });

                                }
                            });
                        }
                    });
                    break;
                case HAND:
                    HVCManager.INSTANCE.setThreshold(null, threshold, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                        @Override
                        public void onResponse(int resultCode) {
                            HVCManager.INSTANCE.setMinMaxSize(null, null, min, max, null, null, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                                @Override
                                public void onResponse(int resultCode) {
                                    HVCManager.INSTANCE.execute(serviceId, kind, new HVCCameraInfo.OneShotOkaoResultResoponseListener() {
                                        @Override
                                        public void onResponse(String serviceId, OkaoResult result) {
                                            makeHandDetectResultResponse(response, result);
                                            DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                            sendResponse(response);
                                        }
                                    });

                                }
                            });
                        }
                    });
                    break;
                case FACE:
                    HVCManager.INSTANCE.setThreshold(null, null, threshold, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                        @Override
                        public void onResponse(int resultCode) {
                            HVCManager.INSTANCE.setMinMaxSize(null, null, null, null, min, max, serviceId, new HVCCameraInfo.OneShotSetParameterResoponseListener() {
                                @Override
                                public void onResponse(int resultCode) {
                                    HVCManager.INSTANCE.execute(serviceId, kind, new HVCCameraInfo.OneShotOkaoResultResoponseListener() {
                                        @Override
                                        public void onResponse(String serviceId, OkaoResult result) {
                                            makeFaceDetectResultResponse(response, result, options);
                                            DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                            sendResponse(response);
                                        }
                                    });

                                }
                            });
                        }
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
                HumanDetectProfile.PROFILE_NAME, null, HumanDetectProfile.ATTRIBUTE_ON_BODY_DETECTION);
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeBodyDetectResultResponse(intent, result);
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                Log.d("ABC", "<EVENT> send event. attribute:" + HumanDetectProfile.ATTRIBUTE_ON_BODY_DETECTION);
            }
        }
    }

    @Override
    public void onNotifyForFaceDetectResult(String serviceId, OkaoResult result) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectProfile.PROFILE_NAME, null, HumanDetectProfile.ATTRIBUTE_ON_FACE_DETECTION);
        HVCCameraInfo camera = HVCManager.INSTANCE.getHVCDevices().get(serviceId);

        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeFaceDetectResultResponse(intent, result, camera.getOptions());
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                Log.d("ABC", "<EVENT> send event. attribute:" + HumanDetectProfile.ATTRIBUTE_ON_FACE_DETECTION);
            }
        }
    }



    @Override
    public void onNotifyForHandDetectResult(String serviceId, OkaoResult result) {
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectProfile.PROFILE_NAME, null, HumanDetectProfile.ATTRIBUTE_ON_HAND_DETECTION);

        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            makeHandDetectResultResponse(intent, result);
            sendEvent(intent, event.getAccessToken());
            if (BuildConfig.DEBUG) {
                Log.d("ABC", "<EVENT> send event. attribute:" + HumanDetectProfile.ATTRIBUTE_ON_HAND_DETECTION);
            }
        }
    }

    @Override
    public void onConnected(final HVCCameraInfo camera) {
        DConnectService service = getServiceProvider().getService(camera.getID());

        Log.d("TEST", "init");
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
     * Make Body Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeBodyDetectResultResponse(final Intent response, final OkaoResult result) {

        List<Bundle> bodyDetects = new LinkedList<>();
        int count = result.getNumberOfBody();
        for (int i = 0; i < count; i++) {
            Bundle bodyDetect = new Bundle();
            HumanDetectProfile.setParamX(bodyDetect,
                    (double) result.getBodyX()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectProfile.setParamY(bodyDetect,
                    (double) result.getBodyY()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectProfile.setParamWidth(bodyDetect,
                    (double) result.getBodySize()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectProfile.setParamHeight(bodyDetect,
                    (double) result.getBodySize()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectProfile.setParamConfidence(bodyDetect,
                    (double) result.getBodyDetectConfidence()[i] / (double) HVCManager.HVC_P_MAX_THRESHOLD);

            bodyDetects.add(bodyDetect);
        }
        if (bodyDetects.size() > 0) {
            HumanDetectProfile.setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
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
            HumanDetectProfile.setParamX(handDetect,
                    (double) result.getHandX()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectProfile.setParamY(handDetect,
                    (double) result.getHandY()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectProfile.setParamWidth(handDetect,
                    (double) result.getHandSize()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectProfile.setParamHeight(handDetect,
                    (double) result.getHandSize()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectProfile.setParamConfidence(handDetect,
                    (double) result.getHandDetectConfidence()[i] / (double) HVCManager.HVC_P_MAX_THRESHOLD);

            handDetects.add(handDetect);
        }
        if (handDetects.size() > 0) {
            HumanDetectProfile.setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
        }
    }

    /**
     * Make Face Detect Result Respnse.
     * @param response response
     * @param result result
     * @param options Options
     */
    private void makeFaceDetectResultResponse(final Intent response, final OkaoResult result, final List<String> options) {
        List<Bundle> faceDetects = new LinkedList<>();
        int count = result.getNumberOfFace();

        for (int i = 0; i < count; i++) {
            Bundle faceDetect = new Bundle();
            HumanDetectProfile.setParamX(faceDetect,
                    (double) result.getFaceX()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectProfile.setParamY(faceDetect,
                    (double) result.getFaceY()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectProfile.setParamWidth(faceDetect,
                    (double) result.getFaceSize()[i] / (double) HVCManager.HVC_P_CAMERA_WIDTH);
            HumanDetectProfile.setParamHeight(faceDetect,
                    (double) result.getFaceSize()[i] / (double) HVCManager.HVC_P_CAMERA_HEIGHT);
            HumanDetectProfile.setParamConfidence(faceDetect,
                    (double) result.getFaceDetectConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
            if (existOption(HVCManager.PARAM_OPTIONS_FACE_DIRECTION, options)) {
                // face direction.
                Bundle faceDirectionResult = new Bundle();
                HumanDetectProfile.setParamYaw(faceDirectionResult, result.getFaceDirectionLR()[i]);
                HumanDetectProfile.setParamPitch(faceDirectionResult, result.getFaceDirectionUD()[i]);
                HumanDetectProfile.setParamRoll(faceDirectionResult, result.getFaceDirectionSlope()[i]);
                HumanDetectProfile.setParamConfidence(faceDirectionResult,
                            (double) result.getFaceDirectionConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);

                HumanDetectProfile.setParamFaceDirectionResults(faceDetect, faceDirectionResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_AGE, options)) {
                // age.
                Bundle ageResult = new Bundle();
                HumanDetectProfile.setParamAge(ageResult, (int) result.getAge()[i]);
                HumanDetectProfile.setParamConfidence(ageResult,
                        (double) result.getAgeConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
                HumanDetectProfile.setParamAgeResults(faceDetect, ageResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_GENDER, options)) {
                // gender.
                Bundle genderResult = new Bundle();
                HumanDetectProfile.setParamGender(genderResult,
                        (result.getGender()[i] == HVCManager.HVC_GEN_MALE ? HumanDetectProfile.VALUE_GENDER_MALE
                                : HumanDetectProfile.VALUE_GENDER_FEMALE));
                HumanDetectProfile.setParamConfidence(genderResult,
                        (double) result.getGenderConfidence()[i] / (double) HVCManager.HVC_P_MAX_CONFIDENCE);
                HumanDetectProfile.setParamGenderResults(faceDetect, genderResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_GAZE, options)) {
                // gaze.
                Bundle gazeResult = new Bundle();
                HumanDetectProfile.setParamGazeLR(gazeResult, result.getGazeLR()[i]);
                HumanDetectProfile.setParamGazeUD(gazeResult, result.getGazeUD()[i]);
                // Unsuppoted Face Direction's Confidence
                HumanDetectProfile.setParamGazeResults(faceDetect, gazeResult);
            }
            if (existOption(HVCManager.PARAM_OPTIONS_BLINK, options)) {
                // blink.
                Bundle blinkResult = new Bundle();
                HumanDetectProfile.setParamLeftEye(blinkResult,
                        (double) result.getBlinkLeft()[i] / (double) HVCManager.HVC_P_MAX_BLINK);
                HumanDetectProfile.setParamRightEye(blinkResult,
                        (double) result.getBlinkRight()[i] / (double) HVCManager.HVC_P_MAX_BLINK);
                // Unsuppoted Face Direction's Confidence
                HumanDetectProfile.setParamBlinkResults(faceDetect, blinkResult);
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
                Bundle expressionResult = new Bundle();
                HumanDetectProfile.setParamExpression(expressionResult,
                        HVCManager.convertToNormalizeExpression(index));
                HumanDetectProfile.setParamConfidence(expressionResult,
                        (double) score / (double) HVCManager.EXPRESSION_SCORE_MAX);

                HumanDetectProfile.setParamExpressionResults(faceDetect, expressionResult);
           }

            faceDetects.add(faceDetect);
        }
        if (faceDetects.size() > 0) {
            HumanDetectProfile.setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
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

}