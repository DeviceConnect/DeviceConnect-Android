/*
 HvcCommManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.comm;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectEvent;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectEventUtils;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.response.HvcResponseUtils;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import omron.HVC.HVC;
import omron.HVC.HVCBleCallback;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;

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
     * Debug.
     */
    private static final Boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Context.
     */
    private DConnectMessageService mContext;

    /**
     * ServiceId.
     */
    private String mServiceId;
    
    /**
     * Bluetooth device data.
     */
    private BluetoothDevice mBluetoothDevice;

    /**
     * HVC BLE class.
     */
    private HVC_BLE mHvcBle = new HVC_BLE();
    /**
     * HVC parameter class.
     */
    private HVC_PRM mHvcPrm = null;
    /**
     * HVC response class.
     */
    private HVC_RES mHvcRes = new HVC_RES();
    /**
     * Cache parameter.
     */
    private HVC_PRM mCacheHvcPrm = null;
    /**
     * Request parameters.
     */
    private HumanDetectRequestParams mRequestParams = null;
    /**
     * last process time(System.currentTimeMillis()).
     */
    private long mLastAccessTime;
    
    /**
     * HVC detect listener.
     */
    private HvcDetectListener mListener;
    
    
    /**
     * Constructor.
     * 
     * @param context context
     * @param serviceId serviceId
     * @param bluetoothDevice bluetoothDevice
     */
    public HvcCommManager(final DConnectMessageService context, final String serviceId, final BluetoothDevice bluetoothDevice) {
        mContext = context;
        mServiceId = serviceId;
        mBluetoothDevice = bluetoothDevice;
    }

    /**
     * get serviceId.
     * 
     * @return serviceId.
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * get bluetooth device data.
     * @return bluetooth device data.
     */
    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    //
    // Register / Unregister Event.
    //

    /**
     * event array.
     */
    private final List<HumanDetectEvent> mEventArray = new ArrayList<>();


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
        if (DEBUG) {
            Log.d(TAG, "registerDetectEvent() detectKind:" + detectKind.toString() + " sessionKey:" + sessionKey);
        }
        
        // check already event register info registered.
        if (HumanDetectEventUtils.search(mEventArray, detectKind, sessionKey) != null) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            mContext.sendResponse(response);
            return;
        }

        // store event register info.
        HumanDetectEvent event = new HumanDetectEvent(detectKind, sessionKey, requestParams, response);
        mEventArray.add(event);
        
        // response(success)
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnDetection event");
        mContext.sendResponse(response);
    }

    /**
     * unregister detect event.
     * 
     * @param detectKind detectKind
     * @param sessionKey sessionKey
     */
    public void unregisterDetectEvent(final HumanDetectKind detectKind, final String sessionKey) {
        if (DEBUG) {
            Log.d(TAG, "unregisterDetectEvent() detectKind:" + detectKind.toString() + " sessionKey:" + sessionKey);
        }

        // remove event register info.
        HumanDetectEventUtils.remove(mEventArray, detectKind, sessionKey);
    }

    /**
     * Get serviceId from bluetoothAddress.
     * 
     * @param address bluetoothAddress
     * @return serviceId
     */
    public static String getServiceId(final String address) {
        return address.replace(":", "").toLowerCase(Locale.ENGLISH);
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
     * get detection process.
     * @param detectKind detectKind
     * @param requestParams requestParams
     * @param response response
     */
    public void doGetDetectionProc(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response) {
        
        // check comm busy.
        if (checkCommBusy()) {
            if (DEBUG) {
                Log.d(TAG, "doGetDetectionProc() - BUG: Supposed to have been checked in HvcDeviceService.");
            }
            MessageUtils.setIllegalDeviceStateError(response, "device busy.");
            mContext.sendResponse(response);
            return;
        }
        
        // create lister.
        HvcDetectListener listener = new HvcDetectListener() {
            
            @Override
            public void onDetectFinished(final HVC_PRM hvcPrm, final HVC_RES hvcRes) {
                if (DEBUG) {
                    Log.d(TAG, "<GET> detect finished. body:" + hvcRes.body.size() + " hand:" + hvcRes.hand.size()
                            + " face:" + hvcRes.face.size());
                    HvcResponseUtils.debugLogHvcRes(hvcRes, TAG);
                }
                // send response.
                HvcResponseUtils.setDetectResultResponse(response, requestParams, hvcRes, detectKind);
                if (checkDetectResult(detectKind, response)) {
                    response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response, "No data to be sent.");
                }
                mContext.sendResponse(response);
            }
            
            @Override
            public void onSetParamError(final int status) {
                if (DEBUG) {
                    Log.d(TAG, "<GET> set parameter error. status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "set parameter error. status:" + status);
                mContext.sendResponse(response);
            }
            
            @Override
            public void onRequestDetectError(final int status) {
                if (DEBUG) {
                    Log.d(TAG, "<GET> request detect error. status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "request detect error. status:" + status);
                mContext.sendResponse(response);
            }
            
            @Override
            public void onDetectError(final int status) {
                if (DEBUG) {
                    Log.d(TAG, "<GET> detect error.  status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "detect error.  status:" + status);
                mContext.sendResponse(response);
            }
        };
        
        // detect request.
        commRequestProc(requestParams, listener);
    }
    
    /**
     * check comm busy.
     * @return true: busy / false: not busy.
     */
    public boolean checkCommBusy() {

        return mHvcBle.getStatus() == HVC_BLE.STATE_BUSY;
    }
    
    /**
     * check whether this manager has a device.
     * @return true: this manager has a device / false: this manager has no device.
     */
    public boolean checkConnect() {
        int commStatus = mHvcBle.getCommStatus();
        return commStatus != HVC.HVC_ERROR_NODEVICES;
    }

    /**
     * check device connect.
     * @return true: connected / false: disconnected.
     */
    public boolean isConnected() {
        return mHvcBle.getStatus() == HVC_BLE.STATE_CONNECTED;
    }

    /**
     * on event process (send event, only events that interval matches.).
     * 
     * @param interval interval. match interval 
     */
    public void onEventProc(final long interval) {
        
        // comm busy.
        if (checkCommBusy()) {
            if (DEBUG) {
                Log.d(TAG, "onEventProc() - skip event process.(busy)");
            }
            return;
        }
        
        // get request parameter by events.
        final HumanDetectRequestParams requestParams = getRequestParamsByEvents(interval);
        
        // create lister.
        HvcDetectListener listener = new HvcDetectListener() {
            
            @Override
            public void onDetectFinished(final HVC_PRM hvcPrm, final HVC_RES hvcRes) {
                if (DEBUG) {
                    Log.d(TAG, "<EVENT> detect finished. body:" + hvcRes.body.size() + " hand:" + hvcRes.hand.size()
                            + " face:" + hvcRes.face.size());
                }

                // send event.
                for (HumanDetectEvent humanDetectEvent : mEventArray) {
                    String attribute = HvcConvertUtils.convertToEventAttribute(humanDetectEvent.getKind());
                    HumanDetectKind detectKind = humanDetectEvent.getKind();
                    if (humanDetectEvent.getRequestParams().getEvent().getInterval() == requestParams
                            .getEvent().getInterval()) {
                        List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                                HumanDetectProfile.PROFILE_NAME, null, attribute);
                        for (Event event : events) {
                            Intent intent = EventManager.createEventMessage(event);
                            HvcResponseUtils.setDetectResultResponse(intent, requestParams, hvcRes, detectKind);
                            if (!checkDetectResult(detectKind, intent)) {
                                continue;
                            }
                            mContext.sendResponse(intent);
                            if (DEBUG) {
                                Log.d(TAG, "<EVENT> send event. attribute:" + attribute);
                            }
                        }
                    }
                }
            }
            
            @Override
            public void onSetParamError(final int status) {
                if (DEBUG) {
                    Log.d(TAG, "<EVENT> set parameter error. status:" + status);
                }
            }
            
            @Override
            public void onRequestDetectError(final int status) {
                if (DEBUG) {
                    Log.d(TAG, "<EVENT> request detect error. status:" + status);
                }
            }
            
            @Override
            public void onDetectError(final int status) {
                if (DEBUG) {
                    Log.d(TAG, "<EVENT> detect error.  status:" + status);
                }
            }
        };
        
        // request.
        commRequestProc(requestParams, listener);
    }

    /**
     * Checks whether the specified intent has a necessary bundle or not.
     *
     * @param detectKind a kind of detection
     * @param intent an instance of {@link Intent}
     * @return <code>true</code> if the specified intent has a necessary bundle, otherwise <code>false</code>
     */
    private boolean checkDetectResult(final HumanDetectKind detectKind, final Intent intent) {
        switch (detectKind) {
        case BODY:
            return intent.hasExtra(HumanDetectProfile.PARAM_BODYDETECTS);
        case HAND:
            return intent.hasExtra(HumanDetectProfile.PARAM_HANDDETECTS);
        case FACE:
            return intent.hasExtra(HumanDetectProfile.PARAM_FACEDETECTS);
        default:
            return false;
        }
    }

    /**
     * timeout judge process.
     */
    public void onTimeoutJudgeProc() {
        
        // BLE connect timeout judge.
        if (checkConnect()
        &&  (System.currentTimeMillis() - mLastAccessTime) > HvcConstants.HVC_CONNECT_TIMEOUT_TIME) {
            if (DEBUG) {
                Log.d(TAG,
                    "disconnect(BLE connect timeout). Not been accessed more than "
                    + (HvcConstants.HVC_CONNECT_TIMEOUT_TIME / 1000) + " seconds");
            }
            mHvcBle.disconnect();
        }
        
    }

    /**
     * get request parameter by events.
     * @param interval interval[msec]
     * @return request parameter by events.
     */
    private HumanDetectRequestParams getRequestParamsByEvents(final long interval) {
        HumanDetectRequestParams requestParams = new HumanDetectRequestParams();
        requestParams.setEvent(HvcDetectRequestParams.getDefaultEventRequestParameter());
        
        for (HumanDetectEvent event : mEventArray) {
            if (event.getRequestParams().getEvent().getInterval() == interval) {
                if (event.getKind() == HumanDetectKind.BODY) {
                    requestParams.setBody(event.getRequestParams().getBody());
                } else if (event.getKind() == HumanDetectKind.HAND) {
                    requestParams.setHand(event.getRequestParams().getHand());
                } else if (event.getKind() == HumanDetectKind.FACE) {
                    requestParams.setFace(event.getRequestParams().getFace());
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "invalid event.getKind()" + event.getKind().ordinal());
                    }
                }
            }
        }
        
        return requestParams;
    }
    
    /**
     * HVC comm request process.
     * @param requestParams request parameters
     * @param listener callback listener
     */
    public void commRequestProc(final HumanDetectRequestParams requestParams, final HvcDetectListener listener) {
        
        if (checkCommBusy()) {
            if (DEBUG) {
                Log.d(TAG, "commRequestProc() - BUG: Supposed to have been checked in HvcDeviceService.");
            }
            return;
        }
        
        mRequestParams = requestParams;
        mHvcPrm = new HvcDetectRequestParams(requestParams).getHvcParams();
        mListener = listener;
        
        // Replace last access time.
        mLastAccessTime = System.currentTimeMillis();
        if (DEBUG) {
            Log.d(TAG, "commRequestProc() - mLastAccessTime:" + mLastAccessTime);
        }
        
        // BLE initialize (GATT)
        mHvcBle.setCallBack(new HVCBleCallback() {
            @Override
            public void onConnected() {
                super.onConnected();
                if (DEBUG) {
                    Log.d(TAG, "commRequestProc() - onConnected()");
                }
                
                // send parameter(if cache hit, no send)
                sendParameterProc();
            }
            
            @Override
            public void onDisconnected() {
                if (DEBUG) {
                    Log.d(TAG, "commRequestProc() - onDisconnected()");
                }
                super.onDisconnected();
            }
            
            @Override
            public void onPostSetParam(final int nRet, final byte outStatus) {
                super.onPostSetParam(nRet, outStatus);
                if (DEBUG) {
                    Log.d(TAG, "commRequestProc() - onPostSetParam()");
                }
                
                // replace cache.
                mCacheHvcPrm = mHvcPrm;
                
                // send detect request.
                sendDetectRequestProc();
            }
            
            @Override
            public void onPostExecute(final int nRet, final byte outStatus) {
                if (DEBUG) {
                    Log.d(TAG, "commRequestProc() - onPostExecute() nRet:" + nRet + " outStatus:" + outStatus);
                }
                if (nRet != HVC.HVC_NORMAL || outStatus != 0) {
                    // Error processing
                    mListener.onDetectError(nRet);
                } else {
                    mListener.onDetectFinished(mHvcPrm, mHvcRes);
                }
            }
        });
        
        // connect
        int commStatus = mHvcBle.getCommStatus();
        if (commStatus == HVC.HVC_ERROR_NODEVICES || commStatus == HVC.HVC_ERROR_DISCONNECTED) {
            
            // clear cache.
            mCacheHvcPrm = null;
            
            // connect
            mHvcBle.connect(mContext, mBluetoothDevice);
        } else {
            // already connect
            sendParameterProc();
        }
    }
    
    /**
     * send parameter(if cache hit, no send).
     */
    private void sendParameterProc() {
        // send parameter.
        if (mCacheHvcPrm == null || !mCacheHvcPrm.equals(mHvcPrm)) {
            
            // no hit cache, send parameter.
            if (DEBUG) {
                Log.d(TAG, "mHvcBle.setParam()");
            }
            int result = mHvcBle.setParam(mHvcPrm);
            if (result != HVC.HVC_NORMAL) {
                mListener.onSetParamError(result);
            }
            
        } else {
            // cache hit (no send parameter, next step)
            sendDetectRequestProc();
        }
    }
    
    /**
     * send detect request.
     */
    private void sendDetectRequestProc() {
        // send detect request.
        int useFunc = (new HvcDetectRequestParams(mRequestParams)).getUseFunc();
        if (DEBUG) {
            Log.d(TAG, "mHvcBle.execute() useFunc:" + useFunc);
        }
        int result = mHvcBle.execute(useFunc, mHvcRes);
        if (result != HVC.HVC_NORMAL) {
            mListener.onRequestDetectError(result);
        }
    }
}
