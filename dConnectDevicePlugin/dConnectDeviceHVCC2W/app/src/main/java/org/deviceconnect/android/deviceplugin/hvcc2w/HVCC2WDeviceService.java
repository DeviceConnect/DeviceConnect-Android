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
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.LinkedList;
import java.util.List;

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
        // TODO: Managerアンインストール検知時の処理要追加。
    }

    @Override
    protected void onManagerTerminated() {
        // TODO: Manager正常終了通知受信時の処理要追加。
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // TODO: ManagerのEvent送信経路切断通知受信時の処理要追加。
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
        // TODO: Device Plug-inへのReset要求受信時の処理要追加。
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
                    Double threshold = HumanDetectProfile.getThreshold(request);
                    Double min = HumanDetectProfile.getMinWidth(request) != null
                            ? HumanDetectProfile.getMinWidth(request)
                            : HumanDetectProfile.getMinHeight(request);
                    Double max = HumanDetectProfile.getMaxWidth(request) != null
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
                    Long interval = HumanDetectProfile.getInterval(request, HVCManager.PARAM_INTERVAL_MIN,
                            HVCManager.PARAM_INTERVAL_MAX);
                    if (interval == null) {
                        interval = new Long(HVCManager.PARAM_INTERVAL_MIN);
                    }
                    List<String> options = HumanDetectProfile.getOptions(request);
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
                            default:
                        }
                        HVCManager.INSTANCE.startEventTimer(interval);
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
    public void unregisterHumanDetectProfileEvent(final Intent request, final Intent response, final String serviceId,
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
                        default:
                    }
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
    public void doGetHumanDetectProfile(final Intent request, final Intent response, final String serviceId,
                                        final HumanDetectKind kind, final List<String> options) {
        HVCManager.INSTANCE.setCamera(serviceId, new HVCManager.ResponseListener() {
            @Override
            public void onReceived(String json) {
                try {
                    Double threshold = HumanDetectProfile.getThreshold(request);
                    Double min = HumanDetectProfile.getMinWidth(request) != null
                            ? HumanDetectProfile.getMinWidth(request)
                            : HumanDetectProfile.getMinHeight(request);
                    Double max = HumanDetectProfile.getMaxWidth(request) != null
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
                            makeFaceDetectResultResponse(response, result, options);
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


    /**
     * Make Body Detect Response.
     * @param response response
     * @param result Okao Result
     */
    private void makeBodyDetectResultResponse(final Intent response, final OkaoResult result) {

        List<Bundle> bodyDetects = new LinkedList<Bundle>();
        ResultBodies r = result.getResultBodies();
        ResultDetection[] bodies = r.getResultDetection();
        int count = result.getResultBodies().getCount();
        for (int i = 0; i < count; i++) {
            ResultDetection detection = bodies[i];
            Bundle bodyDetect = new Bundle();
            HumanDetectProfile.setParamX(bodyDetect,
                    (double) detection.getCenter().getX() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectProfile.setParamY(bodyDetect,
                    (double) detection.getCenter().getY() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectProfile.setParamWidth(bodyDetect,
                    (double) detection.getSize() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectProfile.setParamHeight(bodyDetect,
                    (double) detection.getSize() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectProfile.setParamConfidence(bodyDetect,
                    (double) detection.getConfidence() / (double) HVCManager.HVC_C2W_MAX_THRESHOLD);

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

        List<Bundle> handDetects = new LinkedList<Bundle>();
        ResultHands hands = result.getResultHands();
        ResultDetection[] h = hands.getResultDetection();
        int count = result.getResultHands().getCount();

        for (int i = 0; i < count; i++) {
            Bundle handDetect = new Bundle();
            HumanDetectProfile.setParamX(handDetect,
                    (double) h[i].getCenter().getX() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectProfile.setParamY(handDetect,
                    (double) h[i].getCenter().getY() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectProfile.setParamWidth(handDetect,
                    (double) h[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectProfile.setParamHeight(handDetect,
                    (double) h[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectProfile.setParamConfidence(handDetect,
                    (double) h[i].getConfidence() / (double) HVCManager.HVC_C2W_MAX_THRESHOLD);

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
        ResultFaces results = result.getResultFaces();
        ResultFace[] f = results.getResultFace();
        List<Bundle> faceDetects = new LinkedList<Bundle>();
        int count = result.getResultFaces().getCount();

        for (int i = 0; i < count; i++) {
            Bundle faceDetect = new Bundle();
            HumanDetectProfile.setParamX(faceDetect,
                    (double) f[i].getCenter().getX() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectProfile.setParamY(faceDetect,
                    (double) f[i].getCenter().getY() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectProfile.setParamWidth(faceDetect,
                    (double) f[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_WIDTH);
            HumanDetectProfile.setParamHeight(faceDetect,
                    (double) f[i].getSize() / (double) HVCManager.HVC_C2W_CAMERA_HEIGHT);
            HumanDetectProfile.setParamConfidence(faceDetect,
                    (double) f[i].getConfidence() / (double) HVCManager.HVC_C2W_MAX_CONFIDENCE);
            ResultDirection faceDirection = f[i].getDirection();
            if (faceDirection != null && existOption(HVCManager.PARAM_OPTIONS_FACE_DIRECTION, options)) {
                // face direction.
                Bundle faceDirectionResult = new Bundle();
                HumanDetectProfile.setParamYaw(faceDirectionResult, faceDirection.getLR());
                HumanDetectProfile.setParamPitch(faceDirectionResult, faceDirection.getUD());
                HumanDetectProfile.setParamRoll(faceDirectionResult, faceDirection.getRoll());
                // Unsuppoted Face Direction's Confidence
                HumanDetectProfile.setParamFaceDirectionResults(faceDetect, faceDirectionResult);
            }
            ResultAge age = f[i].getAge();
            if (age != null && existOption(HVCManager.PARAM_OPTIONS_AGE, options)) {
                // age.
                Bundle ageResult = new Bundle();
                HumanDetectProfile.setParamAge(ageResult, age.getAge());
                HumanDetectProfile.setParamConfidence(ageResult,
                        (double) age.getConfidence() / (double) HVCManager.HVC_C2W_MAX_CONFIDENCE);
                HumanDetectProfile.setParamAgeResults(faceDetect, ageResult);
            }
            ResultGender gender = f[i].getGender();
            if (gender != null && existOption(HVCManager.PARAM_OPTIONS_GENDER, options)) {
                // gender.
                Bundle genderResult = new Bundle();
                HumanDetectProfile.setParamGender(genderResult,
                        (gender.getGender() == HVCManager.HVC_GEN_MALE ? HumanDetectProfile.VALUE_GENDER_MALE
                                : HumanDetectProfile.VALUE_GENDER_FEMALE));
                HumanDetectProfile.setParamConfidence(genderResult,
                        (double) gender.getConfidence() / (double) HVCManager.HVC_C2W_MAX_CONFIDENCE);
                HumanDetectProfile.setParamGenderResults(faceDetect, genderResult);
            }
            ResultGaze gaze = f[i].getGaze();
            if (gaze != null && existOption(HVCManager.PARAM_OPTIONS_GAZE, options)) {
                // gaze.
                Bundle gazeResult = new Bundle();
                HumanDetectProfile.setParamGazeLR(gazeResult, gaze.getLR());
                HumanDetectProfile.setParamGazeUD(gazeResult, gaze.getUD());
                // Unsuppoted Face Direction's Confidence
                HumanDetectProfile.setParamGazeResults(faceDetect, gazeResult);
            }
            ResultBlink blink = f[i].getBlink();
            if (blink != null && existOption(HVCManager.PARAM_OPTIONS_BLINK, options)) {
                // blink.
                Bundle blinkResult = new Bundle();
                HumanDetectProfile.setParamLeftEye(blinkResult,
                        (double) blink.getLeftEye() / (double) HVCManager.HVC_C2W_MAX_BLINK);
                HumanDetectProfile.setParamRightEye(blinkResult,
                        (double) blink.getRightEye() / (double) HVCManager.HVC_C2W_MAX_BLINK);
                // Unsuppoted Face Direction's Confidence
                HumanDetectProfile.setParamBlinkResults(faceDetect, blinkResult);
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
                // expression.
                Bundle expressionResult = new Bundle();
                HumanDetectProfile.setParamExpression(expressionResult,
                        HVCManager.INSTANCE.convertToNormalizeExpression(index));
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