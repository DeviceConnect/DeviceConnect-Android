/*
 HvcCommManager.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;

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
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.HumanDetectProfile;
import org.deviceconnect.message.DConnectMessage;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
     * Context.
     */
    private Context mContext;

    /**
     * ServiceId.
     */
    private String mServiceId;
    
    /**
     * Bluetooth device data.
     */
    private BluetoothDevice mBluetoothDevice;

    // /**
    // * Device search thread.
    // */
    // private HvcDeviceSearchThread mDeviceSearchThread;

    /**
     * Detect thread..
     */
    private HvcDetectThread mDetectThread;
    
    
    
    /**
     * Constructor.
     * 
     * @param context context
     * @param serviceId serviceId
     * @param bluetoothDevice bluetoothDevice
     */
    public HvcCommManager(final Context context, final String serviceId, final BluetoothDevice bluetoothDevice) {
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
    private List<HumanDetectEvent> mEventArray = new ArrayList<HumanDetectEvent>();


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
        
        // check already event register info registered.
        if (HumanDetectEventUtils.search(mEventArray, detectKind, sessionKey) != null) {
            MessageUtils.setInvalidRequestParameterError(response, "already event registered.");
            mContext.sendBroadcast(response);
            return;
        }

        // store event register info.
        HumanDetectEvent event = new HumanDetectEvent(detectKind, sessionKey, requestParams, response);
        mEventArray.add(event);
        
        // response(success)
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(DConnectMessage.EXTRA_VALUE, "Register OnDetection event");
        mContext.sendBroadcast(response);
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

        // remove event register info.
        HumanDetectEventUtils.remove(mEventArray, detectKind, sessionKey);
    }
    


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
     * Get serviceId from bluetoothAddress.
     * 
     * @param address bluetoothAddress
     * @return serviceId
     */
    public static String getServiceId(final String address) {
        String serviceId = address.replace(":", "").toLowerCase(Locale.ENGLISH);
        return serviceId;
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
        if (mDetectThread != null && mDetectThread.isAlive()) {
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
     * get detection process.
     * @param detectKind detectKind
     * @param requestParams requestParams
     * @param response response
     */
    public void doGetDetectionProc(final HumanDetectKind detectKind, final HumanDetectRequestParams requestParams,
            final Intent response) {
        
        // check comm busy.
        if (checkCommBusy()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doGetDetectionProc() - skip event process.(busy)");
            }
            return;
        }
        
        // create lister.
        HvcDetectListener listener = new HvcDetectListener() {
            
            @Override
            public void onDetectFinished(final HVC_PRM hvcPrm, final HVC_RES hvcRes) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<GET> detect finished. body:" + hvcRes.body.size() + " hand:" + hvcRes.hand.size()
                            + " face:" + hvcRes.face.size());
                    HvcResponseUtils.debugLogHvcRes(hvcRes, TAG);
                }
                // send response.
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                HvcResponseUtils.setDetectResultResponse(response, requestParams, hvcRes, detectKind);
                mContext.sendBroadcast(response);
            }
            
            @Override
            public void onConnected() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<GET> connected.");
                }
            }
            
            @Override
            public void onSetParamError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<GET> set parameter error. status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "set parameter error. status:" + status);
            }
            
            @Override
            public void onPostSetParam(final HVC_PRM hvcPrm) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<GET> success post set parameter.");
                }
            }
            
            @Override
            public void onDisconnected() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<GET> disconnected.");
                }
            }
            
            @Override
            public void onRequestDetectError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<GET> request detect error. status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "request detect error. status:" + status);
            }
            
            @Override
            public void onDetectError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> detect error.  status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "detect error.  status:" + status);
            }
            
            @Override
            public void onConnectError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> connect error.  status:" + status);
                }
                MessageUtils.setIllegalDeviceStateError(response, "connect error.  status:" + status);
            }
        };
        
        // start comm process and request.
        if (mDetectThread == null || !mDetectThread.isAlive()) {
            mDetectThread = new HvcDetectThread(mContext, mBluetoothDevice);
        }
        mDetectThread.request(requestParams, listener);
    }
    
    /**
     * check comm busy.
     * @return true: busy / false: not busy.
     */
    public boolean checkCommBusy() {
         if (mDetectThread != null && mDetectThread.checkBusy()) {
             return true;
         }
        return false;
    }
    
    /**
     * check device connect.
     * @return true: busy / false: not busy.
     */
    public boolean checkConnect() {
         if (mDetectThread != null && mDetectThread.checkConnect()) {
             return true;
         }
        return false;
    }

    /**
     * on event process (send event, only events that interval matches.).
     * 
     * @param interval interval. match interval 
     */
    public void onEventProc(final long interval) {
        
        // comm busy.
        if (mDetectThread != null && mDetectThread.checkBusy()) {
            if (BuildConfig.DEBUG) {
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
                if (BuildConfig.DEBUG) {
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
                            mContext.sendBroadcast(intent);
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "<EVENT> send event. attribute:" + attribute);
                            }
                        }
                    }
                }
            }
            
            @Override
            public void onConnected() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> connected.");
                }
            }
            
            @Override
            public void onSetParamError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> set parameter error. status:" + status);
                }
            }
            
            @Override
            public void onPostSetParam(final HVC_PRM hvcPrm) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> success post set parameter.");
                }
            }
            
            @Override
            public void onDisconnected() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> disconnected.");
                }
            }
            
            @Override
            public void onRequestDetectError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> request detect error. status:" + status);
                }
            }
            
            @Override
            public void onDetectError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> detect error.  status:" + status);
                }
            }
            
            @Override
            public void onConnectError(final int status) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "<EVENT> connect error.  status:" + status);
                }
            }
        };
        
        // start comm process and request.
        if (mDetectThread == null || !mDetectThread.isAlive()) {
            mDetectThread = new HvcDetectThread(mContext, mBluetoothDevice);
        }
        mDetectThread.request(requestParams, listener);
    }

    /**
     * timeout judge process.
     */
    public void onTimeoutJudgeProc() {
        
        // BLE connect timeout judge.
        if (mDetectThread != null
        &&  mDetectThread.isAlive()
        &&  (System.currentTimeMillis() - mDetectThread.getLastAccessTime()) > HvcConstants.HVC_CONNECT_TIMEOUT_TIME) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                    "disconnect(BLE connect timeout). Not been accessed more than "
                    + (HvcConstants.HVC_CONNECT_TIMEOUT_TIME / 1000) + " seconds");
            }
            mDetectThread.disconnect();
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
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "invalid event.getKind()" + event.getKind().ordinal());
                    }
                }
            }
        }
        
        return requestParams;
    }
}
