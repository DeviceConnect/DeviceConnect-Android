/*
 HvcCommManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import omron.HVC.HVC;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.HvcDebugUtils;
import org.deviceconnect.android.deviceplugin.hvc.HvcDeviceApplication;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectBodyRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectEvent;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectFaceRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectHandRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * HVC Communication Manager.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcCommManager {

    /**
     * log tag.
     */
    private static final String TAG = HvcCommManager.class.getSimpleName();

    /**
     * comm status.
     */
    private enum CommStatus {
        /**
         * HVC disconnect.
         */
        DISCONNECT,
        /**
         * HVC connect, wait request.
         */
        WAITREQUEST,
        /**
         * HVC connect, wait response.
         */
        WAITRESPONSE,
    };

    /**
     * Context.
     */
    private Context mContext;

    /**
     * ServiceId.
     */
    private String mServiceId;

    // /**
    // * Device search thread.
    // */
    // private HvcDeviceSearchThread mDeviceSearchThread;

    /**
     * Detect thread..
     */
    private HvcDetectThread mDetectThread;

    // /**
    // * Device search process result.
    // */
    // public enum DeviceSearchResult {
    // /**
    // * Success.
    // */
    // RESULT_SUCCESS,
    // /**
    // * ERROR Thread alived.
    // */
    // RESULT_ERR_THREAD_ALIVE,
    //
    // };

    /**
     * Constructor.
     * 
     * @param context context
     * @param serviceId serviceId
     */
    public HvcCommManager(final Context context, final String serviceId) {
        mContext = context;
        mServiceId = serviceId;
    }

    /**
     * get serviceId.
     * 
     * @return serviceId.
     */
    public String getServiceId() {
        return mServiceId;
    }

    //
    // Register / Unregister Event.
    //

    /**
     * event array.
     */
    private List<HumanDetectEvent> mEventArray = new ArrayList<HumanDetectEvent>();

    /**
     * process flag.
     */
    private static final int PROCESS_START_THERAD_BITFLAG = 0x0001;
    /**
     * process flag.
     */
    private static final int PROCESS_COMM_CONNECT_BITFLAG = 0x0002;
    /**
     * process flag.
     */
    private static final int PROCESS_POST_SET_PARAM_BITFLAG = 0x0004;
    /**
     * process flag.
     */
    private static final int PROCESS_POST_DETECT_REQUEST_BITFLAG = 0x0008;
    /**
     * process flag.
     */
    private static final int PROCESS_SEND_SUCCESS_RESPONSE_BITFLAG = 0x8000;

    /**
     * register detect event.
     * 
     * @param detectKind detectKind
     * @param requestParams request parameters.
     * @param response response
     * @param sessionKey sessionKey
     */
    public void registerDetectEvent(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response, final String sessionKey) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "registerDetectEvent() detectKind:" + detectKind.toString() + " sessionKey:" + sessionKey);
        }

        // get bluetooth device by serviceId.
        BluetoothDevice device = HvcCommManager.searchDevices(mServiceId);
        if (device == null) {
            // bluetooth device not found.
            MessageUtils.setNotFoundServiceError(response);
            mContext.sendBroadcast(response);
            return;
        }

        // store event register info.
        HumanDetectEvent event = new HumanDetectEvent(detectKind, sessionKey, requestParams, response);
        mEventArray.add(event);

        // set process flag.
        int processFlag = 0;
        if (mDetectThread == null || mDetectThread.isAlive()) {
            processFlag |= PROCESS_START_THERAD_BITFLAG;
        }
        if (getCommStatus() == CommStatus.DISCONNECT) {
            processFlag |= PROCESS_COMM_CONNECT_BITFLAG;
        }
        processFlag |= PROCESS_POST_SET_PARAM_BITFLAG;
        processFlag |= PROCESS_POST_DETECT_REQUEST_BITFLAG;
        processFlag |= PROCESS_SEND_SUCCESS_RESPONSE_BITFLAG;

        /* start comm process.(Recursive) */
        mProcessFlag = processFlag;
        commProc(response, detectKind, requestParams, sessionKey, device, null);
    }

    /**
     * unregister detect event.
     * 
     * @param detectKind detectKind
     * @param sessionKey sessionKey
     */
    public void unregisterDetectEvent(final HumanDetectKind detectKind, final String sessionKey) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "unregisterDetectEvent() detectKind:" + detectKind.toString() + " sessionKey:" + sessionKey);
        }

        int count = mEventArray.size();
        for (int index = (count - 1); index >= 0; index--) {
            HumanDetectEvent event = mEventArray.get(index);
            if (detectKind == event.getKind() && sessionKey.equals(event.getSessionKey())) {
                mEventArray.remove(index);
            }
        }
    }

    /**
     * detect process flag.
     */
    private int mProcessFlag = 0;

    /**
     * comm process(Recursive).<br>
     * 
     * - The process is recursive run off the flag when finished. if zero, send
     * success response.<br>
     * - If error, send error response and no call this function.<br>
     * 
     * @param response response
     * @param detectKind detectKind
     * @param requestParams request parameter.
     * @param sessionKey sessionKey(null: GET,PUT API / not null: EVENT API)
     * @param bluetoothDevice bluetoothDevice
     * @param hvcRes HVC response
     */
    public void commProc(final Intent response, final HumanDetectKind detectKind,
            final HumanDetectRequestParams requestParams, final String sessionKey,
            final BluetoothDevice bluetoothDevice, final HVC_RES hvcRes) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "commProc() - mProcessFlag:0x" + String.format("%04x", mProcessFlag));
        }

        if (mProcessFlag == 0) {
            // all process finished.
            return;
        } else if ((mProcessFlag & PROCESS_START_THERAD_BITFLAG) != 0) {

            // reset bit.
            mProcessFlag ^= PROCESS_START_THERAD_BITFLAG;

            // not busy.
            if (getCommStatus() != CommStatus.WAITREQUEST) {
                
                // start thread
                CommDetectionResult result = startDetectThread(mContext, bluetoothDevice, new HvcDetectListener() {
    
                    @Override
                    public void onConnected() {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onConnected()");
                        }
                        
                        // Remove cache.
                        mCacheHvcPrm = null;
                        
                        commProc(response, detectKind, requestParams, sessionKey, bluetoothDevice, null);
                    }
    
                    @Override
                    public void onPostSetParam(final HVC_PRM hvcPrm) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onPostSetParam()");
                        }
                        
                        // Replace cache.
                        mCacheHvcPrm = hvcPrm;
                        
                        commProc(response, detectKind, requestParams, sessionKey, bluetoothDevice, null);
                    };
    
                    @Override
                    public void onDetectFinished(final HVC_RES result) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onDetectFinished()");
                        }
    
                        // send response.
                        setDetectResultResponse(response, requestParams, result, detectKind);
                        if (sessionKey == null) {
                            // get,post API response(success).
                            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
HvcDebugUtils.intentDump("AAA", response, "send intent");
                            mContext.sendBroadcast(response);
                        } else {
                            // event API response.
                            for (HumanDetectEvent humanDetectEvent : mEventArray) {
                                String attribute = HvcConvertUtils.convertToEventAttribute(humanDetectEvent.getKind());
                                if (humanDetectEvent.getRequestParams().getEvent().getInterval() == requestParams
                                        .getEvent().getInterval()) {
                                    List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                                            HumanDetectProfile.PROFILE_NAME, null, attribute);
                                    for (Event event : events) {
                                        Intent intent = EventManager.createEventMessage(event);
                                        setDetectResultResponse(intent, requestParams, result, detectKind);
                                        mContext.sendBroadcast(intent);
                                        if (BuildConfig.DEBUG) {
                                            Log.d(TAG, "commProc() - send event.");
                                        }
                                    }
                                }
                            }
                        }
    
                        commProc(response, detectKind, requestParams, sessionKey, bluetoothDevice, result);
                    }
    
                    @Override
                    public void onSetParamError(final int status) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onSetParamError()");
                        }
                        // set parameter error.
                        MessageUtils.setTimeoutError(response, "set parameter error. status:" + status);
                        mContext.sendBroadcast(response);
                    }
    
                    @Override
                    public void onRequestDetectError(final int status) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onRequestDetectError()");
                        }
                        // request detect error.
                        MessageUtils.setTimeoutError(response, "request detect error. status:" + status);
                        mContext.sendBroadcast(response);
                    }
    
                    @Override
                    public void onDisconnected() {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onDisconnected()");
                        }
                        if (getCommStatus() == CommStatus.WAITRESPONSE) {
                            // disconnect error (waitng response).
                            MessageUtils.setIllegalDeviceStateError(response, "device disconnected.");
                            mContext.sendBroadcast(response);
                            return;
                        } else {
                            // no problem.
                        }
                    }
    
                    @Override
                    public void onDetectError(final int status) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onDetectError() status:" + status);
                        }
                        // request detect error.
                        MessageUtils.setIllegalDeviceStateError(response, "request detect error. status:" + status);
                        mContext.sendBroadcast(response);
                    }
    
                    @Override
                    public void onConnectError(final int status) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "commProc() - onConnectError() status:" + status);
                        }
                        MessageUtils.setIllegalDeviceStateError(response, "error. status:" + status);
                        mContext.sendBroadcast(response);
                    }
                });
                if (result == CommDetectionResult.RESULT_SUCCESS
                ||  result == CommDetectionResult.RESULT_ERR_THREAD_ALIVE) {
                    
                    // next command
                    commProc(response, detectKind, requestParams, sessionKey, bluetoothDevice, null);
                    
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "commProc() - startDetectThread() result:" + result.ordinal());
                    }
                    // response(error) unknown value.
                    MessageUtils
                            .setIllegalServerStateError(response, "detection result unknown value. " + result.ordinal());
                    mContext.sendBroadcast(response);
                    return;
                }
            } else {
                // busy
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "commProc() - startDetectThread() result:RESULT_ERR_THREAD_ALIVE");
                }
                // TODO: sleepできないのでコメントアウトしている
                // // busy
                // final int CONNECT_RETRY_COUNT = 3;
                // final int CONNECT_RETRY_INTERVAL = 1000;
                // for (int retryIndex = 0; retryIndex < CONNECT_RETRY_COUNT;
                // retryIndex ++) {
                // sleep(CONNECT_RETRY_INTERVAL); // TODO: sleepできないので確認する
                // CommStatus commStatus = getCommStatus();
                // if (commStatus == CommStatus.WAITREQUEST) {
                // // set bit.(Recursive)
                // mProcessFlag |= PROCESS_START_THERAD_BITFLAG;
                // // comm process.(Recursive)
                // commProc(response, useFunc, prm, sessionKey, bluetoothDevice,
                // null);
                // return;
                // }
                // }
                // busy error.
                MessageUtils.setTimeoutError(response, "device busy.");
                mContext.sendBroadcast(response);
                return;
            }
            
        } else if ((mProcessFlag & PROCESS_COMM_CONNECT_BITFLAG) != 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "commProc() - comm connect");
            }

            // reset bit.
            mProcessFlag ^= PROCESS_COMM_CONNECT_BITFLAG;

            // connect HVC.
            mDetectThread.connectProc();

        } else if ((mProcessFlag & PROCESS_POST_SET_PARAM_BITFLAG) != 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "commProc() - post set param");
            }

            // reset bit.
            mProcessFlag ^= PROCESS_POST_SET_PARAM_BITFLAG;

            // post parameter(if success, call onPost , if error, call
            // mListener.onSetParamError(result)).
            int useFunc = (new HvcDetectRequestParams(requestParams)).getUseFunc();
            HVC_PRM hvcPrm = (new HvcDetectRequestParams(requestParams)).getHvcParams();
            mDetectThread.postSetParameter(useFunc, hvcPrm);

        } else if ((mProcessFlag & PROCESS_POST_DETECT_REQUEST_BITFLAG) != 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "commProc() - post detect request");
            }

            // reset bit.
            mProcessFlag ^= PROCESS_POST_DETECT_REQUEST_BITFLAG;

            // post detect(if success, call onPost , if error, call
            // mListener.onSetParamError(result)).
            mDetectThread.postDetect();

        } else if ((mProcessFlag & PROCESS_SEND_SUCCESS_RESPONSE_BITFLAG) != 0) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "commProc() - send success response.");
            }

            // reset bit.
            mProcessFlag ^= PROCESS_SEND_SUCCESS_RESPONSE_BITFLAG;

            // response(success)
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnDetection event");
            mContext.sendBroadcast(response);

        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "commProc() - no process. mProcessFlag:" + mProcessFlag);
            }
        }

    }

    // /**
    // * check register.
    // * @param detectKind detectKind
    // * @param sessionKey sessionKey
    // * @return if register, true. not register false.
    // */
    // public boolean checkRegisterDetectEvent(final HumanDetectKind detectKind,
    // final String sessionKey) {
    //
    // for (HumanDetectEvent event : mEventArray) {
    // if (detectKind == event.getKind() &&
    // sessionKey.equals(event.getSessionKey())) {
    // return true;
    // }
    // }
    // return false;
    // }

    // /**
    // * check event empty.
    // * @return true: empty, false: not empty.
    // */
    // public boolean isEmptyEvent() {
    // if (mEventArray.size() <= 0) {
    // return true;
    // }
    // return false;
    // }

    // //
    // // device comm process.
    // //
    //
    // /**
    // * Start device search thread.
    // * @param context Context
    // * @param listener callback listener.
    // * @return result
    // */
    // public DeviceSearchResult startDeviceSearchThread(final Context context,
    // final HvcDeviceSearchListener listener) {
    // if (mDeviceSearchThread == null || !mDeviceSearchThread.isAlive()) {
    // mDeviceSearchThread = new HvcDeviceSearchThread(context, listener);
    // mDeviceSearchThread.start();
    // return DeviceSearchResult.RESULT_SUCCESS;
    // } else {
    // return DeviceSearchResult.RESULT_ERR_THREAD_ALIVE;
    // }
    // }

    /**
     * Detection process result.
     */
    public enum CommDetectionResult {
        /**
         * Success.
         */
        RESULT_SUCCESS,
        /**
         * ERROR serviceId not found.
         */
        RESULT_ERR_SERVICEID_NOT_FOUND,
        /**
         * ERROR Thread alived.
         */
        RESULT_ERR_THREAD_ALIVE,
    };

    /**
     * Start detect thread.
     * 
     * @param context Context
     * @param device device
     * @param listener listener
     * @return result
     */
    public CommDetectionResult startDetectThread(final Context context, final BluetoothDevice device,
            final HvcDetectListener listener) {
        if (mDetectThread == null || !mDetectThread.isAlive()) {
            mDetectThread = new HvcDetectThread(context, device, listener);
            mDetectThread.start();
            return CommDetectionResult.RESULT_SUCCESS;
        } else {
            return CommDetectionResult.RESULT_ERR_THREAD_ALIVE;
        }
    }

    //
    // store bluetooth devices.
    //

    /**
     * BluetoothDevices(found by service discovery).
     */
    private static List<BluetoothDevice> sDevices = new ArrayList<BluetoothDevice>();

    /**
     * Store BluetoothDevices(found by service discovery).
     * 
     * @param devices BluetoothDevices
     */
    public static void storeDevices(final List<BluetoothDevice> devices) {
        synchronized (sDevices) {
            sDevices = devices;
        }
    }

    /**
     * Search BluetoothDevice.
     * 
     * @param serviceId serviceId
     * @return not null: found BluetoothDevice / null:not found.
     */
    public static BluetoothDevice searchDevices(final String serviceId) {

        synchronized (sDevices) {
            if (sDevices != null) {
                for (BluetoothDevice device : sDevices) {
                    if (serviceId.equals(getServiceId(device.getAddress()))) {
                        return device;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Get serviceId from bluetoothAddress.
     * 
     * @param address bluetoothAddress
     * @return serviceId
     */
    public static String getServiceId(final String address) {
        String serviceId = address.replace(":", "").toLowerCase(Locale.ENGLISH);
        return serviceId;
    }

    //
    // get data by event registers.
    //

    /**
     * get use func by event registers.
     * 
     * @return useFunc useFunc
     */
    private int getUseFuncByEventRegisters() {
        int useFunc = 0;
        for (HumanDetectEvent event : mEventArray) {
            HumanDetectKind detectKind = event.getKind();
            HumanDetectRequestParams params = event.getRequestParams();

            HumanDetectBodyRequestParams body = params.getBody();
            HumanDetectHandRequestParams hand = params.getHand();
            HumanDetectFaceRequestParams face = params.getFace();

            List<String> bodyOptions = null;
            List<String> handOptions = null;
            List<String> faceOptions = null;
            if (body != null) {
                bodyOptions = params.getBody().getOptions();
            }
            if (hand != null) {
                handOptions = params.getHand().getOptions();
            }
            if (face != null) {
                faceOptions = params.getFace().getOptions();
            }

            List<String> options = new ArrayList<String>();
            if (bodyOptions != null) {
                options.addAll(bodyOptions);
            }
            if (handOptions != null) {
                options.addAll(handOptions);
            }
            if (faceOptions != null) {
                options.addAll(faceOptions);
            }

            useFunc |= HvcConvertUtils.convertUseFunc(detectKind, options);
        }
        return useFunc;
    }

    // /**
    // * request detect to all services.
    // */
    // private void requestDetectAllServices() {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "requestDetectAllServices()");
    // }
    //
    // for (HvcCommManager commManager : mHvcCommManagerArray) {
    //
    // // get bluetooth device by serviceId.
    // final String serviceId = commManager.getServiceId();
    // BluetoothDevice device = HvcCommManager.searchDevices(serviceId);
    // if (device == null) {
    // // serviceId not found. (erase? disconnect?)
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "requestDetectAllServices() serviceId not found. serviceId:" +
    // serviceId);
    // }
    // continue;
    // }
    //
    // // get register detect event kinds.
    // final int useFunc = commManager.getUseFuncByEventRegisters();
    //
    // // get request params(use default value).
    // final HumanDetectRequestParams params =
    // HvcDetectRequestParams.getDefaultRequestParameter();
    //
    // // start detection
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG,
    // "requestDetectAllServices() commManager.startDetectThread() useFunc:" +
    // useFunc);
    // }
    // commManager.startDetectThread(this, device, useFunc, params, new
    // HvcDetectListener() {
    // @Override
    // public void onRequestDetectError(final int status) {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "onRequestDetectError. status:" + status);
    // }
    // }
    //
    // @Override
    // public void onDetectFinished(final HVC_RES result) {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "onDetectFinished() body:" + result.body.size() + " hand:" +
    // result.hand.size()
    // + " face:" + result.face.size());
    // }
    // if (result.body.size() > 0) {
    // sendEvent(serviceId, HumanDetectKind.BODY, params, result);
    // }
    // if (result.hand.size() > 0) {
    // sendEvent(serviceId, HumanDetectKind.HAND, params, result);
    // }
    // if (result.face.size() > 0) {
    // sendEvent(serviceId, HumanDetectKind.FACE, params, result);
    // }
    // }
    //
    // @Override
    // public void onDisconnected() {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "onDetectFaceDisconnected(). serviceId:" + serviceId);
    // }
    // }
    //
    // @Override
    // public void onDetectError(final int status) {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "onDetectError(). serviceId:" + serviceId + " status:" +
    // status);
    // }
    // }
    //
    // @Override
    // public void onConnectError(final int status) {
    // if (BuildConfig.DEBUG) {
    // Log.d(TAG, "onConnectError(). serviceId:" + serviceId + " status:" +
    // status);
    // }
    // }
    // });
    // }
    // }

    /**
     * send event.
     * 
     * @param serviceId serviceId
     * @param detectKind detectKind
     * @param params params
     * @param result result
     */
    private void sendEvent(final String serviceId, final HumanDetectKind detectKind,
            final HumanDetectRequestParams params, final HVC_RES result) {
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

        List<Event> events = EventManager.INSTANCE.getEventList(serviceId, HumanDetectProfile.PROFILE_NAME, null,
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
            mContext.sendBroadcast(intent);
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
    public static void setDetectResultResponse(final Intent response, final HumanDetectRequestParams requestParams,
            final HVC_RES result, final HumanDetectKind detectKind) {

        // body detects response.
        if (detectKind == HumanDetectKind.BODY && result.body.size() > 0) {
            setBodyDetectResultResponse(response, new HvcDetectRequestParams(requestParams), result);
        }

        // hand detects response.
        if (detectKind == HumanDetectKind.HAND && result.hand.size() > 0) {
            setHandDetectResultResponse(response, new HvcDetectRequestParams(requestParams), result);
        }

        // face detects response.
        if (detectKind == HumanDetectKind.FACE && result.face.size() > 0) {
            setFaceDetectResultResponse(response, new HvcDetectRequestParams(requestParams), result);
        }
    }

    /**
     * set body detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    public static void setBodyDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

        List<Bundle> bodyDetects = new LinkedList<Bundle>();
        for (omron.HVC.HVC_RES.DetectionResult r : result.body) {

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

    /**
     * set hand detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    public static void setHandDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

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

    /**
     * set face detect result response.
     * 
     * @param response response
     * @param requestParams request
     * @param result result
     */
    public static void setFaceDetectResultResponse(final Intent response, final HvcDetectRequestParams requestParams,
            final HVC_RES result) {

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
                    double normalizeExpressionScore = HvcConvertUtils.convertToNormalizeExpressionScore(r.exp.score);
                    HumanDetectRequestParams humanDetectRequestParams = requestParams.getHumanDetectRequestParams();
                    if (normalizeExpressionScore >= humanDetectRequestParams.getFace().getExpressionThreshold()) {
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

    /**
     * get event interval by detectKind and sessionKey.
     * 
     * @param detectKind detect kind
     * @param sessionKey session key
     * @return not null: event interval / null: event not fuond
     */
    public Long getEventInterval(final HumanDetectKind detectKind, final String sessionKey) {
        for (HumanDetectEvent event : mEventArray) {
            if (event.getSessionKey().equals(sessionKey) && event.getKind() == detectKind) {
                return event.getRequestParams().getEvent().getInterval();
            }
        }
        return null;
    }

    /**
     * get event count.
     * 
     * @return event count.
     */
    public int getEventCount() {
        return mEventArray.size();
    }

    /**
     * comm manager destroy process.
     */
    public void destroy() {

        // if (mDeviceSearchThread.isAlive()) {
        // mDeviceSearchThread.halt();
        // }
        if (mDetectThread.isAlive()) {
            mDetectThread.halt();
        }
    }

    /**
     * check exist event by interval.
     * 
     * @param interval interval.
     * @return true: exist / false not exist
     */
    public boolean checkExistEventByInterval(final long interval) {
        for (HumanDetectEvent event : mEventArray) {
            if (event.getRequestParams().getEvent().getInterval() == interval) {
                return true;
            }
        }
        return false;
    }

    /**
     * on event process, if match interval.
     * 
     * @param context context
     * @param interval interval
     */
    public void onEventProc(final Context context, final long interval) {
        for (HumanDetectEvent event : mEventArray) {
            if (event.getRequestParams().getEvent().getInterval() == interval) {

                // comm start process.

            }
        }
    }

    // /**
    // * start comm.
    // *
    // * @param interval if interval not zero, event API. if interval zero,
    // * get/post API.
    // */
    // private boolean startComm(final Context context, final Intent response,
    // final long interval) {
    //
    //
    //
    // CommStatus commStatus = getCommStatus();
    // if (commStatus == CommStatus.DISCONNECT) {
    // boolean result = startCommDuringDisconnect(context, response, interval);
    // return result;
    // }
    //
    // return true;
    // }

    // private boolean startCommDuringDisconnect(final Context context, final
    // Intent response, final long interval) {
    //
    // // get parameter from match interval events.
    // HumanDetectRequestParams requestParams = new HumanDetectRequestParams();
    // for (HumanDetectEvent event : mEventArray) {
    // HumanDetectRequestParams requestParam = event.getRequestParams();
    // if (interval == requestParam.getEvent().getInterval()) {
    // HumanDetectKind detectKind = event.getKind();
    // if (detectKind == HumanDetectKind.BODY) {
    // requestParams.setBody((HumanDetectBodyRequestParams)
    // requestParam.getBody());
    // }
    // if (detectKind == HumanDetectKind.HAND) {
    // requestParams.setHand((HumanDetectHandRequestParams)
    // requestParam.getHand());
    // }
    // if (detectKind == HumanDetectKind.FACE) {
    // requestParams.setFace((HumanDetectFaceRequestParams)
    // requestParam.getFace());
    // }
    // }
    // }
    //
    // // get useFunc
    // int useFunc = getUseFuncByRequestParams(requestParams);
    // final HumanDetectRequestParams requestParamsFinal = requestParams;
    // final int useFuncFinal = useFunc;
    //
    // // start thread
    // DetectionResult result = startDetectThread(context, device, new
    // HvcDetectListener() {
    //
    // @Override
    // public void onConnected() {
    //
    // }
    //
    // @Override
    // public void onRequestDetectError(int status) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void onDetectFinished(HVC_RES result) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void onDisconnected() {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void onDetectError(int status) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void onConnectError(int status) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void onSetParamError(int status) {
    // // TODO Auto-generated method stub
    //
    // }
    // });
    //
    // if (result == DetectionResult.RESULT_ERR_SERVICEID_NOT_FOUND) {
    //
    // } else if (result == DetectionResult.RESULT_ERR_THREAD_ALIVE) {
    //
    // } else if (result != DetectionResult.RESULT_SUCCESS) {
    //
    // }
    //
    // return true;
    // }

    /**
     * get HVC comm status.
     * 
     * @return HVC comm status
     */
    private CommStatus getCommStatus() {
        if (mDetectThread == null) {
            return CommStatus.DISCONNECT;
        }
        int hvcStatus = mDetectThread.getHvcCommStatus();
        if (hvcStatus == HVC.HVC_NORMAL) {
            return CommStatus.WAITRESPONSE;
        } else if (hvcStatus == HVC.HVC_ERROR_NODEVICES) {
            return CommStatus.DISCONNECT;
        } else if (hvcStatus == HVC.HVC_ERROR_DISCONNECTED) {
            return CommStatus.DISCONNECT;
        } else if (hvcStatus == HVC.HVC_ERROR_BUSY) {
            return CommStatus.WAITRESPONSE;
        } else {
            return CommStatus.DISCONNECT;
        }
    }

    /**
     * timeout judge process.
     */
    public void onTimeoutJudgeProc() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onTimeoutJudgeProc()");
            
            // TODO: 実装する。
        }
    }

    // /**
    // * get useFunc
    // * @param requestParams request parameters.
    // * @return useFunc
    // */
    // private int getUseFuncByRequestParams(final HvcDetectRequestParams
    // requestParams) {
    // int useFunc = 0;
    // if (requestParams.getBody() != null) {
    // useFunc |= HvcConvertUtils.convertUseFunc(HumanDetectKind.BODY,
    // requestParams.getBody().getRequestParams()
    // .getOptions());
    // }
    // if (requestParams.getHand() != null) {
    // useFunc |= HvcConvertUtils.convertUseFunc(HumanDetectKind.HAND,
    // requestParams.getHand().getRequestParams()
    // .getOptions());
    // }
    // if (requestParams.getFace() != null) {
    // useFunc |= HvcConvertUtils.convertUseFunc(HumanDetectKind.FACE,
    // requestParams.getFace().getRequestParams()
    // .getOptions());
    // }
    // return useFunc;
    // }

    /**
     * get detection process.
     * 
     * @param detectKind detectKind
     * @param requestParams requestParams
     * @param response response
     */
    public void doGetDetectionProc(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "doGetDetectionProc() detectKind:" + detectKind.toString());
        }

        // get bluetooth device by serviceId.
        BluetoothDevice device = HvcCommManager.searchDevices(mServiceId);
        if (device == null) {
            // bluetooth device not found.
            MessageUtils.setNotFoundServiceError(response);
            mContext.sendBroadcast(response);
            return;
        }

        // set process flag.
        int processFlag = 0;
        if (mDetectThread == null || mDetectThread.isAlive()) {
            processFlag |= PROCESS_START_THERAD_BITFLAG;
        }
        if (getCommStatus() == CommStatus.DISCONNECT) {
            processFlag |= PROCESS_COMM_CONNECT_BITFLAG;
        }
        if (!compareHvcCacheParameter(detectKind, requestParams)) {
            processFlag |= PROCESS_POST_SET_PARAM_BITFLAG;
        }
        processFlag |= PROCESS_POST_DETECT_REQUEST_BITFLAG;
        processFlag |= PROCESS_SEND_SUCCESS_RESPONSE_BITFLAG;

        /* start comm process.(Recursive) */
        mProcessFlag = processFlag;
        commProc(response, detectKind, requestParams, null, device, null);
    }
    
    /**
     * HVC_PRM cache.
     */
    private HVC_PRM mCacheHvcPrm = null;
    
    /**
     * Compare cache HVC parameters.
     * @param detectKind detectKind
     * @param requestParams request parameters.
     * @return true: equal / false: not equal
     */
    private boolean compareHvcCacheParameter(final HumanDetectKind detectKind,
            final HumanDetectRequestParams requestParams) {
        
        HVC_PRM hvcPrm = (new HvcDetectRequestParams(requestParams)).getHvcParams();
        if (mCacheHvcPrm != null) {
            if (detectKind == HumanDetectKind.BODY) {
                if (mCacheHvcPrm.body.equals(hvcPrm.body)) {
                    return true;
                }
            } else if (detectKind == HumanDetectKind.HAND) {
                if (mCacheHvcPrm.hand.equals(hvcPrm.hand)) {
                    return true;
                }
            } else if (detectKind == HumanDetectKind.FACE) {
                if (mCacheHvcPrm.face.equals(hvcPrm.face)) {
                    return true;
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "compareHvcCacheParameter() - invalid value. detectKind:" + detectKind.ordinal());
                }
            }
        }
        return false;
    }
}
