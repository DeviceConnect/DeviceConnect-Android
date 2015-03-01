/*
 HvcDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import omron.HVC.HVC;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManager;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcCommManagerUtils;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcConvertUtils;
import org.deviceconnect.android.deviceplugin.hvc.comm.HvcDetectListener;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcHumanDetectProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcSystemProfile;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * HVC Device Service.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceService extends DConnectMessageService {
    
    /**
     * log tag.
     */
    private static final String TAG = HvcDeviceApplication.class.getSimpleName();
    
    /**
     * HVC comm managers(1serviceId,1record).
     */
    private List<HvcCommManager> mHvcCommManagerArray = new ArrayList<HvcCommManager>();
    
    /**
     * timer interval time[msec].
     */
    private static final long TIMER_INTERVAL = 1 * 60 * 1000;
    
    /**
     * timer running flag.
     */
    private boolean mIsTimerRunning = false;
    
    /**
     * timer.
     */
    private Timer mTimer;
    
    
    @Override
    public void onCreate() {

        super.onCreate();

        // EventManagerの初期化
        EventManager.INSTANCE.setController(new MemoryCacheController());

        // add supported profiles
        addProfile(new HvcHumanDetectProfile());
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
//        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                    HvcConnectProfile.PROFILE_NAME,
//                    List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
//                    null,
//                    HvcConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
//
//            for (int i = 0; i < events.size(); i++) {
//                Event event = events.get(i);
//                Intent mIntent = EventManager.createEventMessage(event);
//                HvcConnectProfile.setAttribute(mIntent, HvcConnectProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
//                Bundle bluetoothConnecting = new Bundle();
//                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                HvcConnectProfile.setEnable(bluetoothConnecting, mBluetoothAdapter.isEnabled());
//                HvcConnectProfile.setConnectStatus(mIntent, bluetoothConnecting);
//                getContext().sendBroadcast(mIntent);
//            }
//            return START_STICKY;
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HvcSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new HvcServiceDiscoveryProfile();
    }
    
    
    
    // 
    // register detection event.
    // 
    
    /**
     * Human Detect Profile register body detection event.<br>
     * 
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    public void registerBodyDetectionEvent(final Intent response,
            final String serviceId, final String sessionKey) {
        registerDetectionEvent(HumanDetectKind.BODY, response, serviceId, sessionKey);
    }
    
    /**
     * Human Detect Profile register hand detection event.<br>
     * 
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    public void registerHandDetectionEvent(final Intent response,
            final String serviceId, final String sessionKey) {
        registerDetectionEvent(HumanDetectKind.HAND, response, serviceId, sessionKey);
    }
    
    /**
     * Human Detect Profile register face detection event.<br>
     * 
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    public void registerFaceDetectEvent(final Intent response,
            final String serviceId, final String sessionKey) {
        registerDetectionEvent(HumanDetectKind.FACE, response, serviceId, sessionKey);
    }
    
    /**
     * Human Detect Profile register detection event.<br>
     * 
     * @param detectKind detectKind
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    private void registerDetectionEvent(final HumanDetectKind detectKind, final Intent response,
            final String serviceId, final String sessionKey) {
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "registerDetectionEvent(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId
                    + " sessionKey:" + sessionKey);
        }
        
        // search CommManager by serviceId.
        HvcCommManager commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        if (commManager == null) {
            // if null, add CommManager.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "registerDetectionEvent(). add HvcCommManager");
            }
            commManager = new HvcCommManager(serviceId);
            mHvcCommManagerArray.add(commManager);
        }
        
        // register
        if (!commManager.checkRegisterDetectEvent(detectKind, sessionKey)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "registerDetectionEvent(). add registerDetectEvent");
            }
            commManager.registerDetectEvent(detectKind, sessionKey);
        }
        
        // if timer not start , start timer.
        if (!mIsTimerRunning) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "registerDetectionEvent(). start timer");
            }
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    requestDetectAllServices();
                }
            }, 0, TIMER_INTERVAL);
            mIsTimerRunning = true;
        }
        
        // response.
        response.putExtra(DConnectMessage.EXTRA_RESULT,
                DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE,
                "Register OnFaceDetection event");
        sendBroadcast(response);
    }
    
    // 
    // unregister detection event.
    // 
    
    /**
     * Human Detect Profile unregister body detection event.<br>
     * 
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    public void unregisterBodyDetectionEvent(final Intent response,
            final String serviceId, final String sessionKey) {
        unregisterDetectionEvent(HumanDetectKind.BODY, response, serviceId, sessionKey);
    }
    
    /**
     * Human Detect Profile unregister hand detection event.<br>
     * 
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    public void unregisterHandDetectionEvent(final Intent response,
            final String serviceId, final String sessionKey) {
        unregisterDetectionEvent(HumanDetectKind.HAND, response, serviceId, sessionKey);
    }
    
    /**
     * Human Detect Profile unregister face detection event.<br>
     * 
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    public void unregisterFaceDetectionEvent(final Intent response,
            final String serviceId, final String sessionKey) {
        unregisterDetectionEvent(HumanDetectKind.FACE, response, serviceId, sessionKey);
    }

    /**
     * Human Detect Profile unregister face detection event.<br>
     * 
     * @param detectKind detectKind
     * @param response response
     * @param serviceId serviceId
     * @param sessionKey sessionKey
     */
    private void unregisterDetectionEvent(final HumanDetectKind detectKind, final Intent response,
            final String serviceId, final String sessionKey) {
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "unregisterDetectionEvent(). detectKind:" + detectKind.toString() + " serviceId:" + serviceId
                    + " sessionKey:" + sessionKey);
        }
        
        // search CommManager by serviceId.
        HvcCommManager commManager = HvcCommManagerUtils.search(mHvcCommManagerArray, serviceId);
        if (commManager == null) {
            // no register.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "unregisterDetectionEvent(). no register.");
            }
            return;
        }
        
        // unregister
        commManager.unregisterDetectEvent(detectKind, sessionKey);
        
        // if no register, stop timer.
        if (commManager.isEmptyEvent()) {
            mTimer.cancel();
            mTimer = null;
            mIsTimerRunning = false;
        }
        
        // set response.
        response.putExtra(DConnectMessage.EXTRA_RESULT,
                DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE,
                "unregister Detection event");
        sendBroadcast(response);
    }
    
    /**
     * request detect to all services.
     */
    private void requestDetectAllServices() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "requestDetectAllServices()");
        }
        
        for (HvcCommManager commManager : mHvcCommManagerArray) {
            
            // get bluetooth device by serviceId.
            final String serviceId = commManager.getServiceId();
            BluetoothDevice device = HvcCommManager.searchDevices(serviceId);
            if (device == null) {
                // serviceId not found. (erase? disconnect?)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "requestDetectAllServices() serviceId not found. serviceId:" + serviceId);
                }
                continue;
            }
            
            // get register detect event kinds.
            final int useFunc = commManager.getUseFuncByEventRegisters();
            
            // get request params(use default value).
            final HvcDetectRequestParams params = new HvcDetectRequestParams();
            
            // start detection
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "requestDetectAllServices() commManager.startDetectThread() useFunc:" + useFunc);
            }
            commManager.startDetectThread(this, device, useFunc, params, new HvcDetectListener() {
                @Override
                public void onRequestDetectError(final int status) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onRequestDetectError. status:" + status);
                    }
                }
                
                @Override
                public void onDetectFinished(final HVC_RES result) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onDetectFinished() body:" + result.body.size() + " hand:" + result.hand.size()
                                + " face:" + result.face.size());
                    }
                    if (result.body.size() > 0) {
                        sendEvent(serviceId, HumanDetectKind.BODY, params, result);
                    }
                    if (result.hand.size() > 0) {
                        sendEvent(serviceId, HumanDetectKind.HAND, params, result);
                    }
                    if (result.face.size() > 0) {
                        sendEvent(serviceId, HumanDetectKind.FACE, params, result);
                    }
                }
                
                @Override
                public void onDetectFaceDisconnected() {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onDetectFaceDisconnected(). serviceId:" + serviceId);
                    }
                }
                
                @Override
                public void onDetectError(final int status) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onDetectError(). serviceId:" + serviceId + " status:" + status);
                    }
                }
                
                @Override
                public void onConnectError(final int status) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onConnectError(). serviceId:" + serviceId + " status:" + status);
                    }
                }
            });
        }
    }
    
    /**
     * send event.
     * @param serviceId serviceId
     * @param detectKind detectKind
     * @param params params
     * @param result result
     */
    private void sendEvent(final String serviceId, final HumanDetectKind detectKind,
            final HvcDetectRequestParams params, final HVC_RES result) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sendEvent()");
        }
        
        String attribute = HvcConvertUtils.convertToEventAttribute(detectKind);
        if (attribute == null) {
            // BUG: not exist event attribute.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "sendEvent() not exist event attribute. detectKind:" + detectKind.toString());
            }
            return;
        }
        
        List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                HumanDetectProfile.PROFILE_NAME, null,
                attribute);
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                        "sendEvent() sendBroadcast - serviceId:" + event.getServiceId() + " attribute:"
                                + event.getAttribute() + "sessionKey:" + event.getSessionKey());
            }
            Intent intent = EventManager.createEventMessage(event);
            // set response
            setDetectResultResponse(intent, params, result, detectKind);
            getContext().sendBroadcast(intent);
        }
    }
    
    /**
     * set response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     * @param detectKind detectKind
     */
    public static void setDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result, final HumanDetectKind detectKind) {

        // body detects response.
        if (detectKind == HumanDetectKind.BODY && result.body.size() > 0) {

            List<Bundle> bodyDetects = new LinkedList<Bundle>();
            for (DetectionResult r : result.body) {

                // threshold check
                if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                    Bundle bodyDetect = new Bundle();
                    HumanDetectProfile.setParamX(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    HumanDetectProfile.setParamY(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    HumanDetectProfile.setParamWidth(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    HumanDetectProfile.setParamHeight(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    HumanDetectProfile.setParamConfidence(bodyDetect,
                            HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));
                    
                    bodyDetects.add(bodyDetect);
                }
            }
            if (bodyDetects.size() > 0) {
                HumanDetectProfile.setBodyDetects(response, bodyDetects.toArray(new Bundle[bodyDetects.size()]));
            }
        }

        // hand detects response.
        if (detectKind == HumanDetectKind.HAND && result.hand.size() > 0) {

            List<Bundle> handDetects = new LinkedList<Bundle>();
            for (DetectionResult r : result.hand) {

                // threshold check
                if (r.confidence >= requestParams.getHand().getHvcThreshold()) {
                    Bundle handDetect = new Bundle();
                    HumanDetectProfile.setParamX(handDetect,
                            HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    HumanDetectProfile.setParamY(handDetect,
                            HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    HumanDetectProfile.setParamWidth(handDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    HumanDetectProfile.setParamHeight(handDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    HumanDetectProfile.setParamConfidence(handDetect,
                            HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));
                    
                    handDetects.add(handDetect);
                }
            }
            if (handDetects.size() > 0) {
                HumanDetectProfile.setHandDetects(response, handDetects.toArray(new Bundle[handDetects.size()]));
            }
        }

        // face detects response.
        if (detectKind == HumanDetectKind.FACE && result.face.size() > 0) {

            List<Bundle> faceDetects = new LinkedList<Bundle>();
            for (FaceResult r : result.face) {

                // threshold check
                if (r.confidence >= requestParams.getFace().getHvcThreshold()) {
                    Bundle faceDetect = new Bundle();
                    HumanDetectProfile.setParamX(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.posX, HvcConstants.HVC_C_CAMERA_WIDTH));
                    HumanDetectProfile.setParamY(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.posY, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    HumanDetectProfile.setParamWidth(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_WIDTH));
                    HumanDetectProfile.setParamHeight(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.size, HvcConstants.HVC_C_CAMERA_HEIGHT));
                    HumanDetectProfile.setParamConfidence(faceDetect,
                            HvcConvertUtils.convertToNormalize(r.confidence, HvcConstants.CONFIDENCE_MAX));

                    // face direction.
                    if ((result.executedFunc & HVC.HVC_ACTIV_FACE_DIRECTION) != 0) {

                        // threshold check
                        if (r.dir.confidence >= requestParams.getFace().getHvcFaceDirectionThreshold()) {
                            Bundle faceDirectionResult = new Bundle();
                            HumanDetectProfile.setParamYaw(faceDirectionResult, r.dir.yaw);
                            HumanDetectProfile.setParamPitch(faceDirectionResult, r.dir.pitch);
                            HumanDetectProfile.setParamRoll(faceDirectionResult, r.dir.roll);
                            HumanDetectProfile.setParamConfidence(faceDirectionResult,
                                    HvcConvertUtils.convertToNormalizeConfidence(r.dir.confidence));
                            HumanDetectProfile.setParamFaceDirectionResult(faceDetect, faceDirectionResult);
                        }
                    }
                    // age.
                    if ((result.executedFunc & HVC.HVC_ACTIV_AGE_ESTIMATION) != 0) {

                        // threshold check
                        if (r.age.confidence >= requestParams.getFace().getHvcAgeThreshold()) {
                            Bundle ageResult = new Bundle();
                            HumanDetectProfile.setParamAge(ageResult, r.age.age);
                            HumanDetectProfile.setParamConfidence(ageResult,
                                    HvcConvertUtils.convertToNormalizeConfidence(r.age.confidence));
                            HumanDetectProfile.setParamAgeResult(faceDetect, ageResult);
                        }
                    }
                    // gender.
                    if ((result.executedFunc & HVC.HVC_ACTIV_GENDER_ESTIMATION) != 0) {

                        // threshold check
                        if (r.gen.confidence >= requestParams.getFace().getHvcGenderThreshold()) {
                            Bundle genderResult = new Bundle();
                            HumanDetectProfile.setParamGender(genderResult,
                                    (r.gen.gender == HVC.HVC_GEN_MALE ? HumanDetectProfile.VALUE_GENDER_MALE
                                            : HumanDetectProfile.VALUE_GENDER_FEMALE));
                            HumanDetectProfile.setParamConfidence(genderResult,
                                    HvcConvertUtils.convertToNormalizeConfidence(r.gen.confidence));
                            HumanDetectProfile.setParamGenderResult(faceDetect, genderResult);
                        }
                    }
                    // gaze.
                    if ((result.executedFunc & HVC.HVC_ACTIV_GAZE_ESTIMATION) != 0) {
                        Bundle gazeResult = new Bundle();
                        HumanDetectProfile.setParamGazeLR(gazeResult, r.gaze.gazeLR);
                        HumanDetectProfile.setParamGazeUD(gazeResult, r.gaze.gazeUD);
                        HumanDetectProfile.setParamConfidence(gazeResult,
                                HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                        HumanDetectProfile.setParamGazeResult(faceDetect, gazeResult);
                    }
                    // blink.
                    if ((result.executedFunc & HVC.HVC_ACTIV_BLINK_ESTIMATION) != 0) {
                        Bundle blinkResult = new Bundle();
                        HumanDetectProfile.setParamLeftEye(blinkResult,
                                HvcConvertUtils.convertToNormalize(r.blink.ratioL, HvcConstants.BLINK_MAX));
                        HumanDetectProfile.setParamRightEye(blinkResult,
                                HvcConvertUtils.convertToNormalize(r.blink.ratioR, HvcConstants.BLINK_MAX));
                        HumanDetectProfile.setParamConfidence(blinkResult,
                                HvcConvertUtils.convertToNormalizeConfidence(HvcConstants.CONFIDENCE_MAX));
                        HumanDetectProfile.setParamBlinkResult(faceDetect, blinkResult);
                    }
                    // expression.
                    if ((result.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0) {

                        // threshold check
                        double normalizeExpressionScore = HvcConvertUtils
                                .convertToNormalizeExpressionScore(r.exp.score);
                        if (normalizeExpressionScore >= requestParams.getFace().getExpressionThreshold()) {
                            Bundle expressionResult = new Bundle();
                            HumanDetectProfile.setParamExpression(expressionResult,
                                    HvcConvertUtils.convertToNormalizeExpression(r.exp.expression));
                            HumanDetectProfile.setParamConfidence(expressionResult, normalizeExpressionScore);
                            HumanDetectProfile.setParamExpressionResult(faceDetect, expressionResult);
                        }
                    }
                    
                    faceDetects.add(faceDetect);
                }
            }
            if (faceDetects.size() > 0) {
                HumanDetectProfile.setFaceDetects(response, faceDetects.toArray(new Bundle[faceDetects.size()]));
            }
        }
    }
}
