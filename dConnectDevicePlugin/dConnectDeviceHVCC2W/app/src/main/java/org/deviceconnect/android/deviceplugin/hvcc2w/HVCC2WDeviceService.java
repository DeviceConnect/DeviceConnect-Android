/*
 HVCC2WDeviceService
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvcc2w;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCManager;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.HVCStorage;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvcc2w.profile.HVCC2WServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hvcc2w.profile.HVCC2WSystemProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.HumanDetectionProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jp.co.omron.hvcw.OkaoResult;
import jp.co.omron.hvcw.ResultAge;
import jp.co.omron.hvcw.ResultBlink;
import jp.co.omron.hvcw.ResultBodies;
import jp.co.omron.hvcw.ResultDetection;
import jp.co.omron.hvcw.ResultDirection;
import jp.co.omron.hvcw.ResultExpression;
import jp.co.omron.hvcw.ResultFace;
import jp.co.omron.hvcw.ResultFaces;
import jp.co.omron.hvcw.ResultGaze;
import jp.co.omron.hvcw.ResultGender;
import jp.co.omron.hvcw.ResultHands;

/**
 * HVC-C2W Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class HVCC2WDeviceService extends DConnectMessageService
        implements HVCCameraInfo.OnBodyEventListener, HVCCameraInfo.OnHandEventListener,
        HVCCameraInfo.OnFaceEventListener {

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("hvcc2w.dplugin");

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());
        HVCManager.INSTANCE.init(this);
        HVCStorage.INSTANCE.init(this);

        addProfile(new HVCC2WServiceDiscoveryProfile(getServiceProvider()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        return new HVCC2WSystemProfile();
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
        HVCManager.INSTANCE.setCamera(serviceId, new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
                try {
                    Double threshold = HumanDetectionProfile.getThreshold(request);
                    Double min = HumanDetectionProfile.getMinWidth(request) != null
                            ? HumanDetectionProfile.getMinWidth(request)
                            : HumanDetectionProfile.getMinHeight(request);
                    Double max = HumanDetectionProfile.getMaxWidth(request) != null
                            ? HumanDetectionProfile.getMaxWidth(request)
                            : HumanDetectionProfile.getMaxHeight(request);

                    HVCCameraInfo camera = HVCManager.INSTANCE.getHVCDevices().get(serviceId);
                    camera.setThresholds(request);
                    Long interval = HumanDetectionProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                            HVCManager.PARAM_INTERVAL_MAX);
                    if (interval == null) {
                        interval = HVCManager.PARAM_INTERVAL_MIN;
                    }
                    List<String> options = HumanDetectionProfile.getOptions(request);
                    EventError error = EventManager.INSTANCE.addEvent(request);

                    if (error == EventError.NONE) {
                        switch (kind) {
                            case BODY:
                                HVCManager.INSTANCE.setThreshold(threshold, null, null, null, null);
                                HVCManager.INSTANCE.setMinMaxSize(min, max, null, null, null, null, null, null);
                                HVCManager.INSTANCE.addBodyDetectEventListener(serviceId, HVCC2WDeviceService.this);
                                break;
                            case HAND:
                                HVCManager.INSTANCE.setThreshold(null, threshold, null, null, null);
                                HVCManager.INSTANCE.setMinMaxSize(null, null, min, max, null, null, null, null);
                                HVCManager.INSTANCE.addHandDetectEventListener(serviceId, HVCC2WDeviceService.this);
                                break;
                            case FACE:
                                HVCManager.INSTANCE.setThreshold(null, null, null, threshold, null);
                                HVCManager.INSTANCE.setMinMaxSize(null, null, null, null, null, null, min, max);
                                HVCManager.INSTANCE.addFaceDetectEventListener(serviceId, HVCC2WDeviceService.this, options);
                                break;
                            case HUMAN:
                                HVCManager.INSTANCE.addBodyDetectEventListener(serviceId, HVCC2WDeviceService.this);
                                HVCManager.INSTANCE.addHandDetectEventListener(serviceId, HVCC2WDeviceService.this);
                                HVCManager.INSTANCE.addFaceDetectEventListener(serviceId, HVCC2WDeviceService.this, options);
                                break;
                            default:
                        }
                        HVCManager.INSTANCE.startEventTimer(kind, interval);
                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response, "Can not register event.");
                    }
                } catch (IllegalStateException e) {
                    // BUG: detectKind unknown value.
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (NumberFormatException e) {
                    // invalid request parameter error
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (IllegalArgumentException e) {
                    // invalid request parameter error
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                }
                sendResponse(response);
            }
        });
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
        HVCManager.INSTANCE.setCamera(serviceId, new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
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
                        case HUMAN:
                            HVCManager.INSTANCE.removeBodyDetectEventListener(serviceId);
                            HVCManager.INSTANCE.removeHandDetectEventListener(serviceId);
                            HVCManager.INSTANCE.removeFaceDetectEventListener(serviceId);
                            break;
                        default:
                    }
                    HVCManager.INSTANCE.stopEventTimer(kind);
                    DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "Can not unregister event.");
                }
                sendResponse(response);
            }
        });
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
        HVCManager.INSTANCE.setCamera(serviceId, new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
                try {
                    Double threshold = HumanDetectionProfile.getThreshold(request);
                    Double min = HumanDetectionProfile.getMinWidth(request) != null
                            ? HumanDetectionProfile.getMinWidth(request)
                            : HumanDetectionProfile.getMinHeight(request);
                    Double max = HumanDetectionProfile.getMaxWidth(request) != null
                            ? HumanDetectionProfile.getMaxWidth(request)
                            : HumanDetectionProfile.getMaxHeight(request);
                    HVCCameraInfo camera = HVCManager.INSTANCE.getHVCDevices().get(serviceId);
                    camera.setThresholds(request);

                    HumanDetectionProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                            HVCManager.PARAM_INTERVAL_MAX);
                    OkaoResult result;
                    switch (kind) {
                        case BODY:
                            HVCManager.INSTANCE.setThreshold(threshold, null, null, null, null);
                            HVCManager.INSTANCE.setMinMaxSize(min, max, null, null, null, null, null, null);
                            result = HVCManager.INSTANCE.execute();
                            makeBodyDetectResultResponse(response, result);
                            break;
                        case HAND:
                            HVCManager.INSTANCE.setThreshold(null, threshold, null, null, null);
                            HVCManager.INSTANCE.setMinMaxSize(null, null, min, max, null, null, null, null);
                            result = HVCManager.INSTANCE.execute();
                            makeHandDetectResultResponse(response, result);
                            break;
                        case FACE:
                            HVCManager.INSTANCE.setThreshold(null, null, null, threshold, null);
                            HVCManager.INSTANCE.setMinMaxSize(null, null, null, null, null, null, min, max);
                            result = HVCManager.INSTANCE.execute();
                            makeFaceDetectResultResponse(response, result, options, camera.getThresholds());
                            break;
                        case HUMAN:
                            result = HVCManager.INSTANCE.execute();
                            makeHumanDetectResultResponse(response, result);
                            break;
                        default:
                    }
                    DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);

                } catch (IllegalStateException e) {
                    // BUG: detectKind unknown value.
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (NumberFormatException e) {
                    // invalid request parameter error
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                } catch (IllegalArgumentException e) {
                    // invalid request parameter error
                    MessageUtils.setInvalidRequestParameterError(response, e.getMessage());
                }
                sendResponse(response);
            }
        });
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
                Log.d("ABC", "<EVENT> send event. attribute:" + HumanDetectionProfile.ATTRIBUTE_ON_BODY_DETECTION);
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
                Log.d("ABC", "<EVENT> send event. attribute:" + HumanDetectionProfile.ATTRIBUTE_ON_FACE_DETECTION);
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
                Log.d("ABC", "<EVENT> send event. attribute:" + HumanDetectionProfile.ATTRIBUTE_ON_HAND_DETECTION);
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
                Log.d("ABC", "<EVENT> send event. attribute:onDetection");
            }
        }
    }

    /**
     * Make Body Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeBodyDetectResultResponse(final Intent response, final OkaoResult result) {

        List<Bundle> bodyDetects = new LinkedList<>();
        ResultBodies r = result.getResultBodies();
        ResultDetection[] bodies = r.getResultDetection();
        int count = result.getResultBodies().getCount();
        for (int i = 0; i < count; i++) {
            ResultDetection detection = bodies[i];
            Bundle bodyDetect = new Bundle();
            HumanDetectionProfile.setParamX(bodyDetect,
                    (double) detection.getCenter().getX() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectionProfile.setParamY(bodyDetect,
                    (double) detection.getCenter().getY() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamWidth(bodyDetect,
                    (double) detection.getSize() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectionProfile.setParamHeight(bodyDetect,
                    (double) detection.getSize() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamConfidence(bodyDetect,
                    (double) detection.getConfidence() / (double) HVCManager.HVC_C2W_MAX_THRESHOLD);

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
        ResultHands hands = result.getResultHands();
        ResultDetection[] h = hands.getResultDetection();
        int count = result.getResultHands().getCount();

        for (int i = 0; i < count; i++) {
            Bundle handDetect = new Bundle();
            HumanDetectionProfile.setParamX(handDetect,
                    (double) h[i].getCenter().getX() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectionProfile.setParamY(handDetect,
                    (double) h[i].getCenter().getY() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamWidth(handDetect,
                    (double) h[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectionProfile.setParamHeight(handDetect,
                    (double) h[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamConfidence(handDetect,
                    (double) h[i].getConfidence() / (double) HVCManager.HVC_C2W_MAX_THRESHOLD);

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
        ResultFaces results = result.getResultFaces();
        ResultFace[] f = results.getResultFace();
        List<Bundle> faceDetects = new LinkedList<>();
        int count = result.getResultFaces().getCount();

        for (int i = 0; i < count; i++) {
            Bundle faceDetect = new Bundle();
            HumanDetectionProfile.setParamX(faceDetect,
                    (double) f[i].getCenter().getX() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectionProfile.setParamY(faceDetect,
                    (double) f[i].getCenter().getY() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamWidth(faceDetect,
                    (double) f[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectionProfile.setParamHeight(faceDetect,
                    (double) f[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectionProfile.setParamConfidence(faceDetect,
                    (double) f[i].getConfidence() / HVCManager.HVC_C2W_MAX_CONFIDENCE);
            ResultDirection faceDirection = f[i].getDirection();
            if (faceDirection != null && existOption(HVCManager.PARAM_OPTIONS_FACE_DIRECTION, options)
                    && (thresholds.get(HVCCameraInfo.ThresholdKind.FACEDIRECTION.ordinal(), 0.0) == null
                    || thresholds.get(HVCCameraInfo.ThresholdKind.FACEDIRECTION.ordinal(), 0.0) != null
                    && isExceedThresholds(thresholds.get(HVCCameraInfo.ThresholdKind.FACEDIRECTION.ordinal(), 0.0),
                    (double)  faceDirection.getConfidence()))) {
                // face direction.
                Bundle faceDirectionResult = new Bundle();
                HumanDetectionProfile.setParamYaw(faceDirectionResult, faceDirection.getLR());
                HumanDetectionProfile.setParamPitch(faceDirectionResult, faceDirection.getUD());
                HumanDetectionProfile.setParamRoll(faceDirectionResult, faceDirection.getRoll());
                HumanDetectionProfile.setParamConfidence(faceDirectionResult,
                        (double) faceDirection.getConfidence() / (double) HVCManager.HVC_C2W_MAX_CONFIDENCE);
                // Unsuppoted Face Direction's Confidence
                HumanDetectionProfile.setParamFaceDirectionResults(faceDetect, faceDirectionResult);
            }
            ResultAge age = f[i].getAge();
            if (age != null && existOption(HVCManager.PARAM_OPTIONS_AGE, options)
                    && (thresholds.get(HVCCameraInfo.ThresholdKind.AGE.ordinal(), 0.0) == null
                    || thresholds.get(HVCCameraInfo.ThresholdKind.AGE.ordinal(), 0.0) != null
                    && isExceedThresholds(thresholds.get(HVCCameraInfo.ThresholdKind.AGE.ordinal(), 0.0),
                    (double) age.getConfidence()))) {
                // age.
                Bundle ageResult = new Bundle();
                HumanDetectionProfile.setParamAge(ageResult, age.getAge());
                HumanDetectionProfile.setParamConfidence(ageResult,
                        (double) age.getConfidence() / (double) HVCManager.HVC_C2W_MAX_CONFIDENCE);
                HumanDetectionProfile.setParamAgeResults(faceDetect, ageResult);
            }
            ResultGender gender = f[i].getGender();
            if (gender != null && existOption(HVCManager.PARAM_OPTIONS_GENDER, options)
                    && (thresholds.get(HVCCameraInfo.ThresholdKind.GENDER.ordinal(), 0.0) == null
                    || thresholds.get(HVCCameraInfo.ThresholdKind.GENDER.ordinal(), 0.0) != null
                    && isExceedThresholds(thresholds.get(HVCCameraInfo.ThresholdKind.GENDER.ordinal(), 0.0),
                    (double) gender.getConfidence()))) {
                // gender.
                Bundle genderResult = new Bundle();
                HumanDetectionProfile.setParamGender(genderResult,
                        (gender.getGender() == HVCManager.HVC_GEN_MALE ? HumanDetectionProfile.VALUE_GENDER_MALE
                                : HumanDetectionProfile.VALUE_GENDER_FEMALE));
                HumanDetectionProfile.setParamConfidence(genderResult,
                        (double) gender.getConfidence() / HVCManager.HVC_C2W_MAX_CONFIDENCE);
                HumanDetectionProfile.setParamGenderResults(faceDetect, genderResult);
            }
            ResultGaze gaze = f[i].getGaze();
            if (gaze != null && existOption(HVCManager.PARAM_OPTIONS_GAZE, options)) {
                // gaze.
                Bundle gazeResult = new Bundle();
                HumanDetectionProfile.setParamGazeLR(gazeResult, gaze.getLR());
                HumanDetectionProfile.setParamGazeUD(gazeResult, gaze.getUD());
                // Unsuppoted Face Direction's Confidence
                HumanDetectionProfile.setParamGazeResults(faceDetect, gazeResult);
            }
            ResultBlink blink = f[i].getBlink();
            if (blink != null && existOption(HVCManager.PARAM_OPTIONS_BLINK, options)) {
                // blink.
                Bundle blinkResult = new Bundle();
                HumanDetectionProfile.setParamLeftEye(blinkResult,
                        (double) blink.getLeftEye() / (double) HVCManager.HVC_C2W_MAX_BLINK);
                HumanDetectionProfile.setParamRightEye(blinkResult,
                        (double) blink.getRightEye() / (double) HVCManager.HVC_C2W_MAX_BLINK);
                // Unsuppoted Face Direction's Confidence
                HumanDetectionProfile.setParamBlinkResults(faceDetect, blinkResult);
            }
            ResultExpression expression = f[i].getExpression();
            if (expression != null && existOption(HVCManager.PARAM_OPTIONS_EXPRESSION, options)) {
                int score = -1;
                int index = -1;
                int[] scores = expression.getScore();
                for (int j = 0; j < scores.length;j++) {
                    int s = scores[j];
                    if (s > score) {
                        score = s;
                        index = j;
                    }
                }

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
     * Make Human Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeHumanDetectResultResponse(final Intent response, final OkaoResult result) {

        Bundle humanDetect = new Bundle();
        int count = result.getResultBodies().getCount() + result.getResultFaces().getCount() + result.getResultHands().getCount();


        if (count > 0) {
            humanDetect.putBoolean("exist", true);
        } else {
            humanDetect.putBoolean("exist", false);
        }
        response.putExtra("humanDetect", humanDetect);
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
        return threshold >= 0 && threshold <= (confidence / HVCManager.HVC_C2W_MAX_CONFIDENCE);
    }
}